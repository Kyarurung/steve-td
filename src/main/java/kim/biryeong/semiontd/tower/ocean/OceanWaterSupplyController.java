package kim.biryeong.semiontd.tower.ocean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.TowerDataKey;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

final class OceanWaterSupplyController {
    private static final double EPSILON = 1.0E-9;
    private static final TowerDataKey<UUID> SUPPLY_TARGET_ID = TowerDataKey.of(
            ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "ocean/water_supply_target"),
            UUID.class
    );
    private static final BlockState WATER_MARKER = Blocks.LIGHT.defaultBlockState()
            .setValue(LightBlock.LEVEL, 0)
            .setValue(LightBlock.WATERLOGGED, true);

    private final OceanConfig config;
    private final OceanResourceState state;
    private BlockPos placedWaterPos;
    private BlockState replacedState;

    OceanWaterSupplyController(OceanConfig config, OceanResourceState state) {
        this.config = config;
        this.state = state;
    }

    static BlockPos waterBlockPos(GridPosition position) {
        return new BlockPos(position.x(), position.y() + 1, position.z());
    }

    static BlockState waterMarker() {
        return WATER_MARKER;
    }

    static boolean canPlaceAt(PlayerLane lane, GridPosition position) {
        return lane != null && position != null && lane.arenaWorld().getBlockState(waterBlockPos(position)).isAir();
    }

    void onPlaced(OceanWaterTower tower, PlayerLane lane) {
        placeWater(tower, lane);
    }

    void onRemoved(PlayerLane lane) {
        restoreWater(lane);
    }

    void onStateChanged(OceanWaterTower tower, PlayerLane lane) {
        BlockPos desired = waterBlockPos(tower.originalPosition());
        if (tower.entityId().isPresent() && !desired.equals(placedWaterPos)) {
            restoreWater(lane);
            placeWater(tower, lane);
        }
    }

    void onWaveStarted(OceanWaterTower tower, PlayerLane lane) {
        state.startWave();
        OceanSupplySnapshot snapshot = OceanSupplySnapshot.capture(lane, config);
        state.captureSupplyTargets(snapshot.targetsFor(tower).stream()
                .map(OceanWaterSupplyController::supplyTargetId)
                .collect(Collectors.toUnmodifiableSet()));
        List<OceanTower> targets = supply(tower, snapshot, config.value(tower.type(), OceanAbilityKey.WAVE_START_WATER));
        if (!targets.isEmpty()) {
            showSupplyVfx(tower, lane, targets, true);
        }
    }

    boolean execute(OceanWaterTower tower, PlayerLane lane) {
        if (!state.waveActive()) {
            return false;
        }
        List<OceanTower> targets = supply(
                tower,
                OceanSupplySnapshot.capture(lane, config),
                config.value(tower.type(), OceanAbilityKey.WATER_PER_SUPPLY)
        );
        if (targets.isEmpty()) {
            return false;
        }
        showSupplyVfx(tower, lane, targets, false);
        return true;
    }

    private List<OceanTower> supply(OceanWaterTower tower, OceanSupplySnapshot snapshot, double amount) {
        if (tower.deployedAtFinalDefense() || snapshot.combatTowers().isEmpty()
                || amount <= 0.0 || state.supplyTargetIds().isEmpty()) {
            return List.of();
        }
        List<OceanTower> targets = snapshot.combatTowers().stream()
                .filter(target -> target.getData(SUPPLY_TARGET_ID).filter(state.supplyTargetIds()::contains).isPresent())
                .toList();
        ArrayList<OceanTower> suppliedTargets = new ArrayList<>();
        for (OceanTower target : targets) {
            double remainingCapacity = Math.max(
                    0.0,
                    config.global(OceanAbilityKey.WATER_SUPPLY_STOP_THRESHOLD) - target.water()
            );
            double supplied = Math.min(
                    remainingCapacity,
                    amount * supplyStackMultiplier(snapshot.sourceCountFor(target))
                            * OceanRules.supplyEfficiency(
                                    target.water(),
                                    config.global(OceanAbilityKey.WATER_SOFT_CAP),
                                    config.global(OceanAbilityKey.WATER_SUPPLY_STOP_THRESHOLD)
                            )
            );
            if (supplied > EPSILON) {
                target.addWater(supplied);
                suppliedTargets.add(target);
            }
        }
        return List.copyOf(suppliedTargets);
    }

    private double supplyStackMultiplier(int sourceCount) {
        return OceanRules.stackedSupplyMultiplier(
                Math.max(1, sourceCount),
                config.global(OceanAbilityKey.WATER_SUPPLY_STACK_DECAY)
        );
    }

    private static UUID supplyTargetId(OceanTower target) {
        return target.getData(SUPPLY_TARGET_ID).orElseGet(() -> {
            UUID id = UUID.randomUUID();
            target.setData(SUPPLY_TARGET_ID, id);
            return id;
        });
    }

    private void placeWater(OceanWaterTower tower, PlayerLane lane) {
        if (lane == null || placedWaterPos != null) {
            return;
        }
        BlockPos target = waterBlockPos(tower.originalPosition());
        BlockState current = lane.arenaWorld().getBlockState(target);
        if (!current.isAir()) {
            return;
        }
        replacedState = current;
        if (lane.arenaWorld().setBlock(target, WATER_MARKER, Block.UPDATE_CLIENTS)) {
            placedWaterPos = target;
        } else {
            replacedState = null;
        }
    }

    private void restoreWater(PlayerLane lane) {
        if (lane == null || placedWaterPos == null) {
            return;
        }
        if (lane.arenaWorld().getBlockState(placedWaterPos).equals(WATER_MARKER) && replacedState != null) {
            lane.arenaWorld().setBlock(placedWaterPos, replacedState, Block.UPDATE_CLIENTS);
        }
        placedWaterPos = null;
        replacedState = null;
    }

    private static void showSupplyVfx(
            OceanWaterTower tower,
            PlayerLane lane,
            List<OceanTower> targets,
            boolean burst
    ) {
        BlockPos center = waterBlockPos(tower.originalPosition());
        Vec3 source = new Vec3(center.getX() + 0.5, center.getY() + 1.03, center.getZ() + 0.5);
        OceanVfx.showWaterSourcePulse(lane.arenaWorld(), source, OceanTowers.tier(tower.type()), burst);
        OceanVfx.showWaterSupply(lane.arenaWorld(), source, targets, burst);
    }

}
