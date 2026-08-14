package kim.biryeong.semiontd.tower.engineer;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.api.SemionTdApi;
import kim.biryeong.semiontd.api.area.AreaEffectOutcome;
import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.monster.DamageType;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.MonsterDataKey;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;
import kim.biryeong.semiontd.entity.visual.BlockDisplayVisual;
import kim.biryeong.semiontd.entity.visual.EntityVisual;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class EngineerTrapTower extends EntityBackedTower {
    private static final MonsterDataKey<Long> PISTON_IMMUNITY_UNTIL = MonsterDataKey.of(
            ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "engineer_piston_immunity_until"), Long.class
    );

    private final EngineerTowers.TrapKind kind;
    private final int tier;
    private boolean waveActive;
    private boolean armed = true;
    private int activeTicks;
    private int actionCooldown;
    private int fuseTicks = -1;
    private boolean tntUsed;
    private boolean poweredLastTick;
    private int activationPlateDistance;
    private EntityVisual placedVisual;
    private ElementHolder upperDoorHolder;

    public EngineerTrapTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
        this.kind = EngineerTowers.trapKind(type).orElseThrow();
        this.tier = EngineerTowers.trapTier(type);
    }

    @Override
    public boolean participatesInFinalDefense() {
        return false;
    }

    @Override
    public boolean targetableByMonsters() {
        return kind == EngineerTowers.TrapKind.DOOR && waveActive && activeTicks > 0 && health() > 0.0;
    }

    @Override
    public EntityVisual visual() {
        return placedVisual == null ? super.visual() : placedVisual;
    }

    @Override
    public void onPlaced(PlayerLane lane) {
        Direction facing = incomingDirection(lane);
        placedVisual = switch (kind) {
            case DISPENSER -> BlockDisplayVisual.builder(Blocks.DISPENSER.defaultBlockState()
                    .setValue(DispenserBlock.FACING, facing)).build();
            case PISTON -> BlockDisplayVisual.builder(Blocks.PISTON.defaultBlockState()
                    .setValue(PistonBaseBlock.FACING, facing)).build();
            default -> null;
        };
        super.onPlaced(lane);
    }

    @Override
    protected void configureEntityAfterSpawn(SemionTowerEntity entity, PlayerLane lane) {
        if (kind != EngineerTowers.TrapKind.DOOR) {
            return;
        }
        BlockDisplayElement upperDoor = new BlockDisplayElement(
                Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
        );
        upperDoor.setTranslation(new Vector3f(-0.5F, 1.0F, -0.5F));
        upperDoor.setShadowRadius(0.5F);
        upperDoor.setShadowStrength(1.0F);
        upperDoorHolder = new ElementHolder();
        upperDoorHolder.addElement(upperDoor);
        EntityAttachment.ofTicking(upperDoorHolder, entity);
    }

    @Override
    public void onRemoved(PlayerLane lane) {
        discardUpperDoorVisual();
        super.onRemoved(lane);
    }

    @Override
    public void onWaveStarted(PlayerLane lane, int currentRound) {
        waveActive = true;
        armed = true;
        activeTicks = 0;
        actionCooldown = 0;
        fuseTicks = -1;
        tntUsed = false;
        poweredLastTick = false;
        activationPlateDistance = 0;
        updateActiveName(source(lane), false);
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        disable(lane);
        armed = true;
        tntUsed = false;
        super.resetForRound(lane);
    }

    @Override
    public void moveToFinalDefense(PlayerLane lane, GridPosition position) {
        disable(lane);
    }

    @Override
    public void tick(PlayerLane lane) {
        super.tick(lane);
        if (!waveActive || health() <= 0.0 || lane == null || lane.arenaWorld() == null) {
            return;
        }
        SemionTowerEntity source = source(lane);
        if (source == null) {
            return;
        }
        boolean physicalPower = lane.arenaWorld().hasNeighborSignal(signalPosition());
        OptionalInt plateDistance = physicalPower ? recentPlateDistance(lane) : OptionalInt.empty();
        boolean powered = plateDistance.isPresent();
        if (!powered) {
            armed = true;
        }
        if (powered && !poweredLastTick) {
            activationPlateDistance = plateDistance.getAsInt();
            if (activeTicks > 0) {
                activeTicks = EngineerBalance.activeTicks();
                armed = false;
                updateActiveName(source, true);
                showActivationVfx(source);
            } else if (armed) {
                activate(lane, source);
            }
        }
        poweredLastTick = powered;

        if (fuseTicks >= 0) {
            showTntFuseVfx(source);
            if (--fuseTicks <= 0) {
                explodeTnt(lane, source);
                fuseTicks = -1;
            }
        }
        if (activeTicks <= 0) {
            return;
        }
        if (activeTicks % 10 == 0) {
            showActivationVfx(source);
        }
        activeTicks--;
        switch (kind) {
            case DOOR -> tickDoor(lane, source);
            case DISPENSER -> tickDispenser(lane, source);
            case SLIME -> tickSlime(source);
            case TNT, PISTON -> { }
        }
        if (activeTicks <= 0) {
            if (kind == EngineerTowers.TrapKind.DOOR) {
                clearDoorTargets(lane, source);
            }
            updateActiveName(source, false);
        }
    }

    @Override
    public List<String> runtimeDetailLines() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("<gold>전력</gold> <white>" + (activeTicks > 0 ? "작동 중" : "대기") + "</white>");
        lines.add("<yellow>작동 잔여시간</yellow> <white>" + String.format(java.util.Locale.ROOT, "%.1f초", activeTicks / 20.0) + "</white>");
        lines.add("<red>재무장</red> <white>" + (armed ? "완료" : "신호 해제 필요") + "</white>");
        if (kind == EngineerTowers.TrapKind.TNT) {
            lines.add("<red>라운드 폭발</red> <white>" + (tntUsed ? "사용함" : "준비됨") + "</white>");
        }
        if (kind == EngineerTowers.TrapKind.DISPENSER) {
            lines.add("<aqua>발판 회로 거리</aqua> <white>" + activationPlateDistance + "칸</white>");
            lines.add("<gold>거리 피해 보너스</gold> <green>+"
                    + Math.round((EngineerBalance.dispenserDamageMultiplier(activationPlateDistance) - 1.0) * 100.0)
                    + "%</green>");
        }
        return List.copyOf(lines);
    }

    public int activeTicksRemaining() {
        return activeTicks;
    }

    public boolean armed() {
        return armed;
    }

    private void activate(PlayerLane lane, SemionTowerEntity source) {
        armed = false;
        activeTicks = EngineerBalance.activeTicks();
        actionCooldown = 0;
        updateActiveName(source, true);
        showActivationVfx(source);
        switch (kind) {
            case TNT -> {
                if (!tntUsed) {
                    tntUsed = true;
                    fuseTicks = EngineerBalance.tntFuseTicks();
                }
            }
            case PISTON -> firePiston(lane, source);
            case DOOR -> tickDoor(lane, source);
            case DISPENSER -> tickDispenser(lane, source);
            case SLIME -> tickSlime(source);
        }
    }

    private void tickDoor(PlayerLane lane, SemionTowerEntity source) {
        if (actionCooldown-- > 0) {
            return;
        }
        actionCooldown = Math.max(1, EngineerBalance.doorRetargetTicks()) - 1;
        double radius = ability("radius", doorRadius(tier));
        for (SemionMonsterEntity target : liveMonsters(lane)) {
            if (target.position().distanceToSqr(source.position()) <= radius * radius) {
                target.setTarget(source);
            }
        }
    }

    private void tickDispenser(PlayerLane lane, SemionTowerEntity source) {
        if (actionCooldown-- > 0) {
            return;
        }
        actionCooldown = Math.max(1, intAbility("intervalTicks", dispenserInterval(tier))) - 1;
        double range = ability("range", dispenserRange(tier));
        liveMonsters(lane).stream()
                .filter(target -> target.position().distanceToSqr(source.position()) <= range * range)
                .max(Comparator.comparingDouble(target -> target.runtimeMonster().laneProgress()))
                .ifPresent(target -> {
                    double damage = ability("damage", dispenserDamage(tier))
                            * EngineerBalance.dispenserDamageMultiplier(activationPlateDistance);
                    DamageResult result = damageTargetResult(source, target, damage, DamageType.PHYSICAL);
                    TowerVfxService.showSecondaryAttack(source, target);
                    if (result.killed()) {
                        onKill(source, target, damage);
                    }
                });
    }

    private void tickSlime(SemionTowerEntity source) {
        double radius = ability("radius", slimeRadius(tier));
        double slow = ability("slow", slimeSlow(tier));
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTower(
                AreaEffectIds.tower(this, "slime"), source, radius, AreaVfxSpec.none()
        );
        SemionTdApi.areaEffects().applyToMonsters(request, target -> {
            target.applyTimedEffect(TimedEffectType.MONSTER_MOVE_SPEED_REDUCTION, slow, 3);
            return AreaEffectOutcome.APPLIED;
        });
    }

    private void explodeTnt(PlayerLane lane, SemionTowerEntity source) {
        double radius = ability("radius", tntRadius(tier));
        int cap = intAbility("maxTargets", tntMaxTargets(tier));
        List<SemionMonsterEntity> selected = liveMonsters(lane).stream()
                .filter(target -> target.position().distanceToSqr(source.position()) <= radius * radius)
                .sorted(Comparator.comparingDouble(target -> target.position().distanceToSqr(source.position())))
                .limit(cap)
                .toList();
        Set<UUID> ids = selected.stream().map(SemionMonsterEntity::getUUID).collect(java.util.stream.Collectors.toUnmodifiableSet());
        MonsterAreaEffectRequest request = new MonsterAreaEffectRequest(
                AreaEffectIds.tower(this, "tnt"), source, source.position(), radius, Set.of(),
                target -> ids.contains(target.getUUID()), AreaVfxSpec.onTrigger(AreaVfxStyles.CORPSE_EXPLOSION)
        );
        TowerAreaDamage.apply(
                this, source, request, ignored -> ability("damage", tntDamage(tier)), true,
                (target, amount, killed) -> {}, DamageType.PHYSICAL
        );
    }

    private void firePiston(PlayerLane lane, SemionTowerEntity source) {
        double radius = ability("radius", pistonRadius(tier));
        int cap = intAbility("maxTargets", tier);
        long now = lane.arenaWorld().getGameTime();
        Vec3 start = lane.laneLayout().positionAt(0.0);
        liveMonsters(lane).stream()
                .filter(target -> !target.runtimeMonster().inFinalDefenseCombat())
                .filter(target -> target.position().distanceToSqr(source.position()) <= radius * radius)
                .filter(target -> target.runtimeMonster().getData(PISTON_IMMUNITY_UNTIL).orElse(0L) <= now)
                .sorted(Comparator.comparingDouble(target -> target.position().distanceToSqr(source.position())))
                .limit(cap)
                .forEach(target -> {
                    Monster monster = target.runtimeMonster();
                    monster.setData(PISTON_IMMUNITY_UNTIL, now + EngineerBalance.pistonImmunityTicks());
                    monster.syncLaneProgress(0.0);
                    target.teleportTo(start.x, start.y, start.z);
                    target.getNavigation().stop();
                    TowerVfxService.showSecondaryAttack(source, target);
                });
    }

    private void clearDoorTargets(PlayerLane lane, SemionTowerEntity source) {
        for (SemionMonsterEntity monster : liveMonsters(lane)) {
            if (monster.getTarget() == source) {
                monster.setTarget(null);
            }
        }
    }

    private void disable(PlayerLane lane) {
        SemionTowerEntity source = source(lane);
        if (source != null && kind == EngineerTowers.TrapKind.DOOR) {
            clearDoorTargets(lane, source);
        }
        waveActive = false;
        activeTicks = 0;
        actionCooldown = 0;
        fuseTicks = -1;
        poweredLastTick = false;
        activationPlateDistance = 0;
        updateActiveName(source, false);
    }

    private void showActivationVfx(SemionTowerEntity source) {
        TowerVfxService.showAreaEffect(
                source,
                AreaEffectIds.tower(this, "powered"),
                AreaVfxStyles.PULSE,
                source.position().add(0.0, 0.15, 0.0),
                0.85,
                List.of(),
                0,
                0,
                0
        );
        if (source.level() instanceof ServerLevel level) {
            DustParticleOptions power = new DustParticleOptions(0x39E7FF, 0.72F);
            Vec3 center = source.position().add(0.0, 0.18, 0.0);
            for (int index = 0; index < 18; index++) {
                double angle = Math.PI * 2.0 * index / 18.0;
                level.sendParticles(
                        power,
                        center.x + Math.cos(angle) * 0.85,
                        center.y,
                        center.z + Math.sin(angle) * 0.85,
                        1, 0.0, 0.0, 0.0, 0.0
                );
            }
        }
    }

    private void showTntFuseVfx(SemionTowerEntity source) {
        if (kind != EngineerTowers.TrapKind.TNT || fuseTicks < 0 || fuseTicks % 2 != 0
                || !(source.level() instanceof ServerLevel level)) {
            return;
        }
        int total = Math.max(1, EngineerBalance.tntFuseTicks());
        double progress = 1.0 - Math.min(total, fuseTicks) / (double) total;
        int green = Math.max(20, (int) Math.round(190.0 * (1.0 - progress)));
        DustParticleOptions dust = new DustParticleOptions((255 << 16) | (green << 8) | 20, 0.85F);
        Vec3 center = source.position().add(0.0, 0.22, 0.0);
        double phase = progress * Math.PI * 8.0;
        int points = fuseTicks <= 6 ? 24 : 10;
        double arc = fuseTicks <= 6 ? Math.PI * 2.0 : Math.PI * 1.55;
        for (int index = 0; index < points; index++) {
            double angle = phase + arc * index / points;
            level.sendParticles(
                    dust,
                    center.x + Math.cos(angle) * 0.72,
                    center.y,
                    center.z + Math.sin(angle) * 0.72,
                    1, 0.0, 0.0, 0.0, 0.0
            );
        }
        double head = phase + arc;
        level.sendParticles(
                progress > 0.7 ? ParticleTypes.FLAME : ParticleTypes.SMALL_FLAME,
                center.x + Math.cos(head) * 0.72,
                center.y + 0.05,
                center.z + Math.sin(head) * 0.72,
                2, 0.03, 0.03, 0.03, 0.005
        );
    }

    private void updateActiveName(SemionTowerEntity source, boolean active) {
        if (source != null) {
            source.setCustomName(Component.literal((active ? "활성화된 " : "") + type().displayName()));
        }
    }

    private Direction incomingDirection(PlayerLane lane) {
        if (lane == null || lane.laneLayout() == null) {
            return Direction.NORTH;
        }
        Vec3 incoming = lane.laneLayout().positionAt(0.0);
        double x = incoming.x - (position().x() + 0.5);
        double z = incoming.z - (position().z() + 0.5);
        if (Math.abs(x) >= Math.abs(z)) {
            return x >= 0.0 ? Direction.EAST : Direction.WEST;
        }
        return z >= 0.0 ? Direction.SOUTH : Direction.NORTH;
    }

    private void discardUpperDoorVisual() {
        if (upperDoorHolder != null) {
            upperDoorHolder.destroy();
            upperDoorHolder = null;
        }
    }

    boolean hasUpperDoorVisual() {
        return upperDoorHolder != null;
    }

    private SemionTowerEntity source(PlayerLane lane) {
        if (lane == null || lane.arenaWorld() == null) {
            return null;
        }
        return entityId().stream()
                .mapToObj(lane.arenaWorld()::getEntity)
                .filter(SemionTowerEntity.class::isInstance)
                .map(SemionTowerEntity.class::cast)
                .filter(entity -> entity.isAlive() && !entity.isRemoved())
                .findFirst()
                .orElse(null);
    }

    private static List<SemionMonsterEntity> liveMonsters(PlayerLane lane) {
        if (lane == null || lane.arenaWorld() == null) {
            return List.of();
        }
        return lane.activeMonsters().stream()
                .filter(monster -> monster != null && monster.hasMinecraftEntity() && monster.health() > 0.0)
                .map(monster -> lane.arenaWorld().getEntity(monster.minecraftEntityId()))
                .filter(SemionMonsterEntity.class::isInstance)
                .map(SemionMonsterEntity.class::cast)
                .filter(entity -> entity.isAlive() && !entity.isRemoved() && entity.runtimeMonster() != null)
                .toList();
    }

    private BlockPos signalPosition() {
        return new BlockPos(originalPosition().x(), originalPosition().y() + 1, originalPosition().z());
    }

    private OptionalInt recentPlateDistance(PlayerLane lane) {
        long now = lane.arenaWorld().getGameTime();
        long oldestAccepted = now - EngineerBalance.activeTicks();
        Map<BlockPos, EngineerCircuitTower> circuits = new HashMap<>();
        for (var tower : lane.towers()) {
            if (tower instanceof EngineerCircuitTower circuit && ownerPlayer().equals(circuit.ownerPlayer())) {
                circuits.put(circuit.circuitPosition(), circuit);
            }
        }
        ArrayDeque<CircuitStep> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new java.util.HashSet<>();
        for (Direction direction : Direction.values()) {
            BlockPos adjacent = signalPosition().relative(direction);
            if (circuits.containsKey(adjacent) && visited.add(adjacent)) {
                pending.addLast(new CircuitStep(adjacent, 1));
            }
        }
        while (!pending.isEmpty()) {
            CircuitStep step = pending.removeFirst();
            EngineerCircuitTower circuit = circuits.get(step.position());
            if (circuit.plateKind() != null && circuit.lastPressedGameTime() >= oldestAccepted
                    && circuit.lastPressedGameTime() <= now) {
                return OptionalInt.of(step.distance());
            }
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = step.position().relative(direction);
                if (circuits.containsKey(adjacent) && visited.add(adjacent)) {
                    pending.addLast(new CircuitStep(adjacent, step.distance() + 1));
                }
            }
        }
        return OptionalInt.empty();
    }

    int activationPlateDistance() {
        return activationPlateDistance;
    }

    private record CircuitStep(BlockPos position, int distance) {
    }

    private double ability(String key, double fallback) {
        return TowerBalanceRuntime.ability(type().id(), key, fallback);
    }

    private int intAbility(String key, int fallback) {
        return TowerBalanceRuntime.abilityInt(type().id(), key, fallback);
    }

    public static double doorRadius(int tier) { return new double[]{6.0, 8.0, 10.0}[tier - 1]; }
    public static double tntDamage(int tier) { return new double[]{180.0, 360.0, 650.0}[tier - 1]; }
    public static double tntRadius(int tier) { return new double[]{2.5, 3.25, 4.0}[tier - 1]; }
    public static int tntMaxTargets(int tier) { return new int[]{6, 9, 12}[tier - 1]; }
    public static double dispenserDamage(int tier) { return new double[]{18.0, 30.0, 48.0}[tier - 1]; }
    public static int dispenserInterval(int tier) { return new int[]{12, 8, 5}[tier - 1]; }
    public static double dispenserRange(int tier) { return new double[]{7.0, 9.0, 11.0}[tier - 1]; }
    public static double pistonRadius(int tier) { return new double[]{2.5, 3.0, 3.5}[tier - 1]; }
    public static double slimeRadius(int tier) { return new double[]{2.5, 3.0, 3.5}[tier - 1]; }
    public static double slimeSlow(int tier) { return new double[]{0.55, 0.70, 0.85}[tier - 1]; }

    @Override
    protected boolean execute(PlayerLane lane) {
        return false;
    }
}
