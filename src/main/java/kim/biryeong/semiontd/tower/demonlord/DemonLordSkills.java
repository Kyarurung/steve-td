package kim.biryeong.semiontd.tower.demonlord;

import java.util.ArrayList;
import java.util.List;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.monster.DamageType;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The five demon lord skills.
 *
 * <p>Every skill reads its numbers from the altar's tower id, so a live config can retune any single
 * tier. Damage is always multiplied by {@link DemonLordState#damageMultiplier()} - levels are the
 * builder's only scaling, since none of its towers ever deal damage.
 */
public final class DemonLordSkills {
    /** 파티클을 바닥에 살짝 띄워 지면에 묻히지 않게 합니다. */
    private static final double GROUND_OFFSET = 0.15;

    /** 부채꼴을 면으로 보이게 하는 안쪽 호 위치입니다. */
    private static final double[] CONE_ARC_FRACTIONS = {0.45, 0.75, 1.0};

    private static final int DOME_RINGS = 4;

    /** 벽에 부딪혔을 때 벽면에서 떨어뜨려 놓을 거리. 블록 안에 끼는 걸 막습니다. */
    private static final double WALL_STANDOFF = 0.8;

    private DemonLordSkills() {
    }

    /**
     * @return cooldown ticks to refund, or 0. 파멸의 손아귀만 처치 시 일부를 돌려줍니다.
     */
    public static int cast(
            ServerPlayer player,
            PlayerLane lane,
            DemonLordState state,
            DemonLordSkill skill,
            TowerType altarType,
            long gameTime
    ) {
        switch (skill) {
            case WAVE_OF_MALICE -> castWaveOfMalice(player, lane, state, altarType);
            case DEMON_WINGS -> castDemonWings(player, lane, state, altarType);
            case SKY_BREAKER -> castSkyBreaker(player, lane, state, altarType);
            case ARCANE_BOMBARDMENT -> castArcaneBombardment(player, state, altarType, gameTime);
            case DEMON_BARRIER -> castDemonBarrier(player, state, altarType);
            case HELLFIRE_BRAND -> castHellfireBrand(player, state, altarType, gameTime);
            case SOUL_DRAIN -> castSoulDrain(player, lane, state, altarType);
            case ROAR_OF_DREAD -> castRoarOfDread(player, lane, state, altarType);
            case GRIP_OF_DOOM -> {
                return castGripOfDoom(player, lane, state, altarType);
            }
            case HELL_GUILLOTINE -> castHellGuillotine(player, lane, state, altarType);
            default -> {
            }
        }
        return 0;
    }

    /**
     * 유일한 단일 대상 기술. 정면에서 가장 가까운 적 하나만 잡습니다.
     *
     * <p>잃은 체력 비례 추가 피해가 붙어 이미 두들겨 맞은 대상을 끊어 내는 데 강하고, 처치에
     * 성공하면 쿨타임 일부를 돌려받아 연쇄로 이어 갈 수 있습니다.
     *
     * @return 처치 시 돌려줄 쿨타임 틱
     */
    private static int castGripOfDoom(ServerPlayer player, PlayerLane lane, DemonLordState state, TowerType altar) {
        double range = ability(altar, "range", 9.0);
        Vec3 origin = player.position();
        Vec3 look = horizontal(player.getLookAngle());

        SemionMonsterEntity target = null;
        double bestScore = Double.MAX_VALUE;
        for (SemionMonsterEntity candidate : monstersNear(lane, origin, range)) {
            Vec3 toTarget = horizontal(candidate.position().subtract(origin));
            // 정면 90도 안쪽만 후보로 봅니다. 뒤에 있는 적이 잡히면 조준이 안 됩니다.
            if (look.dot(toTarget) < Math.cos(Math.toRadians(45.0))) {
                continue;
            }
            double distance = candidate.position().distanceTo(origin);
            if (distance < bestScore) {
                bestScore = distance;
                target = candidate;
            }
        }
        if (target == null) {
            sound(player, SoundEvents.WITHER_SHOOT, 0.6f, 0.5f);
            return 0;
        }

        Monster runtime = target.runtimeMonster();
        if (runtime == null) {
            return 0;
        }
        Vec3 victimPosition = target.position();
        trail(player, player.getEyePosition(), victimPosition.add(0.0, 1.0, 0.0));

        double threshold = runtime.maxHealth() * ability(altar, "executeHealthRatio", 0.50);
        if (runtime.health() > threshold) {
            // 처형 조건 미달. 일반 피해만 넣고, 잃은 체력이 많을수록 아프게 해 임계값까지 밀어 줍니다.
            double missing = Math.max(0.0, runtime.maxHealth() - runtime.health());
            double damage = (ability(altar, "damage", 130.0) + missing * ability(altar, "missingHealthRatio", 0.10))
                    * state.damageMultiplier();
            DemonLordService.dealDamage(player, target, damage, DamageType.MAGIC);

            Vec3 pull = horizontal(origin.subtract(victimPosition)).scale(ability(altar, "pullStrength", 0.5));
            target.setDeltaMovement(pull.x, 0.2, pull.z);
            target.hurtMarked = true;
            sound(player, SoundEvents.WITHER_HURT, 1.0f, 0.6f);
            return 0;
        }

        // 처형. 남은 체력을 고정 피해로 그대로 날려 방어·저항과 무관하게 확실히 끊습니다.
        double victimHealth = runtime.health();
        DemonLordService.dealDamage(player, target, victimHealth, DamageType.TRUE);

        // 시체가 터집니다. 폭발 피해는 처형 시점 체력에 비례하므로 단단한 적일수록 크게 터집니다.
        double blast = victimHealth * ability(altar, "explosionHealthRatio", 1.0)
                + ability(altar, "areaDamage", 40.0) * state.damageMultiplier();
        double blastRadius = ability(altar, "explosionRadius", 4.0);
        for (SemionMonsterEntity nearby : monstersNear(lane, victimPosition, blastRadius)) {
            if (nearby == target) {
                continue;
            }
            DemonLordService.dealDamage(player, nearby, blast, DamageType.MAGIC);
        }

        if (player.level() instanceof ServerLevel level) {
            drawCircle(level, victimPosition, blastRadius, ParticleTypes.SOUL_FIRE_FLAME);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    victimPosition.x, victimPosition.y + 1.0, victimPosition.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        sound(player, SoundEvents.WITHER_DEATH, 1.0f, 0.7f);
        return (int) ability(altar, "killRefundTicks", 60.0);
    }

    /**
     * 바라보는 지점에 지속 장판을 깝니다.
     *
     * <p>발밑 고정이 아니라 시선으로 놓기 때문에, 몰려오는 길목에 미리 깔아 두거나 이미 뭉친
     * 무리 한가운데를 노릴 수 있습니다. 한 번에 하나만 유지되며 재시전하면 이전 장판을 덮어씁니다.
     */
    private static void castHellfireBrand(ServerPlayer player, DemonLordState state, TowerType altar, long gameTime) {
        int interval = (int) Math.max(1.0, ability(altar, "tickIntervalTicks", 20.0));
        int duration = (int) Math.max(1.0, ability(altar, "zoneDurationTicks", 100.0));
        Vec3 centre = lookTarget(player, ability(altar, "placementRange", 10.0));

        state.placeZone(new DemonLordState.HellfireZone(
                centre,
                ability(altar, "zoneRadius", 3.5),
                ability(altar, "damage", 18.0) * state.damageMultiplier(),
                ability(altar, "damageTakenBonus", 0.10),
                interval,
                gameTime + duration,
                gameTime + interval
        ));
        if (player.level() instanceof ServerLevel level) {
            drawCircle(level, centre, ability(altar, "zoneRadius", 3.5), ParticleTypes.SOUL_FIRE_FLAME);
            trail(player, player.getEyePosition(), centre);
        }
        sound(player, SoundEvents.FIRECHARGE_USE, 1.0f, 0.7f);
    }

    /**
     * 시선 지점으로 순간이동해 내리찍습니다.
     *
     * <p>피해는 <b>마왕 자신이</b> 잃은 체력에 비례해 커집니다. 대상이 아니라 시전자 기준인 것은
     * 의도적입니다 — 대상 기준 증폭은 이미 파멸의 손아귀가 맡고 있고, 이쪽은 몰렸을 때 판을
     * 뒤집는 역할이라 위험을 감수할수록 보상이 커야 합니다.
     *
     * <p>순간이동은 돌진과 같은 이유로 두 번 막습니다 — 레이캐스트로 벽을 넘지 않게 하고,
     * 자기 레인 밖으로 못 나가게 조입니다.
     */
    private static void castHellGuillotine(ServerPlayer player, PlayerLane lane, DemonLordState state, TowerType altar) {
        double radius = ability(altar, "radius", 4.0);
        Vec3 landing = DemonLordService.clampToLane(lane, lookTarget(player, ability(altar, "range", 10.0)));

        // 잃은 체력 비율 0(만피)~1(빈사). 빈사에서 missingHealthDamageBonus 만큼 증가합니다.
        double missingRatio = Math.max(0.0, Math.min(1.0, 1.0 - state.healthRatio()));
        double amplifier = 1.0 + missingRatio * ability(altar, "missingHealthDamageBonus", 1.0);
        double damage = ability(altar, "damage", 60.0) * state.damageMultiplier() * amplifier;

        player.teleportTo(landing.x, landing.y, landing.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();

        for (SemionMonsterEntity monster : monstersNear(lane, landing, radius)) {
            DemonLordService.dealDamage(player, monster, damage, DamageType.MAGIC);
            push(monster, horizontal(monster.position().subtract(landing)), 0.5, 0.3);
        }

        if (player.level() instanceof ServerLevel level) {
            drawCircle(level, landing, radius, ParticleTypes.CRIT);
            level.sendParticles(ParticleTypes.EXPLOSION,
                    landing.x, landing.y + 0.5, landing.z, 5, 0.6, 0.2, 0.6, 0.0);
        }
        sound(player, SoundEvents.ANVIL_LAND, 1.0f, 0.8f);
    }

    /** 시선이 닿는 지점. 블록에 막히면 그 자리, 아니면 최대 사거리 끝입니다. */
    private static Vec3 lookTarget(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 far = eye.add(player.getLookAngle().scale(range));
        HitResult clip = player.level().clip(new ClipContext(
                eye, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return clip.getType() == HitResult.Type.MISS ? far : clip.getLocation();
    }

    /** 전방 직선을 꿰뚫어 피해를 주고, 준 피해에 비례해 회복합니다. */
    private static void castSoulDrain(ServerPlayer player, PlayerLane lane, DemonLordState state, TowerType altar) {
        double range = ability(altar, "range", 7.0);
        double width = ability(altar, "width", 1.6);
        double damage = ability(altar, "damage", 35.0) * state.damageMultiplier();

        Vec3 start = player.position();
        Vec3 look = horizontal(player.getLookAngle());
        Vec3 end = start.add(look.scale(range));

        int hits = 0;
        for (SemionMonsterEntity monster : monstersNear(lane, start.lerp(end, 0.5), range)) {
            if (distanceToSegment(monster.position(), start, end) > width) {
                continue;
            }
            DemonLordService.dealDamage(player, monster, damage, DamageType.MAGIC);
            hits++;
        }
        if (hits > 0) {
            double drained = damage * hits * ability(altar, "lifeStealRatio", 0.25);
            state.heal(Math.min(drained, state.maxHealth() * ability(altar, "lifeStealCap", 0.12)));
        }

        if (player.level() instanceof ServerLevel level) {
            drawCorridor(level, start, end, width, ParticleTypes.SOUL);
            if (hits > 0) {
                level.sendParticles(ParticleTypes.HEART, start.x, start.y + 1.6, start.z, 4, 0.3, 0.3, 0.3, 0.0);
            }
        }
        sound(player, SoundEvents.SOUL_ESCAPE.value(), 1.0f, 0.6f);
    }

    /** 주위를 밀어내고 이동을 늦추며 공격을 막습니다. 포위를 푸는 용도입니다. */
    private static void castRoarOfDread(ServerPlayer player, PlayerLane lane, DemonLordState state, TowerType altar) {
        double radius = ability(altar, "radius", 5.0);
        double damage = ability(altar, "damage", 25.0) * state.damageMultiplier();
        double knockback = ability(altar, "knockback", 1.0);
        double slow = ability(altar, "moveSpeedReduction", 0.50);
        int duration = (int) Math.max(1.0, ability(altar, "dreadDurationTicks", 50.0));

        Vec3 origin = player.position();
        for (SemionMonsterEntity monster : monstersNear(lane, origin, radius)) {
            DemonLordService.dealDamage(player, monster, damage, DamageType.MAGIC);
            push(monster, horizontal(monster.position().subtract(origin)), knockback, 0.4);
            monster.applyTimedEffect(TimedEffectType.MONSTER_MOVE_SPEED_REDUCTION, slow, duration);
            // 공격 자체를 막습니다. 속도만 깎으면 붙어 있는 적은 계속 때립니다.
            monster.applyTimedEffect(TimedEffectType.MONSTER_ATTACK_SPEED_REDUCTION, 1.0, duration);
        }
        if (player.level() instanceof ServerLevel level) {
            drawCircle(level, origin, radius, ParticleTypes.SONIC_BOOM);
        }
        sound(player, SoundEvents.WARDEN_ROAR, 1.2f, 0.8f);
    }

    /** 점과 선분 사이의 수평 거리. 영혼 흡수의 직선 판정에 씁니다. */
    private static double distanceToSegment(Vec3 point, Vec3 from, Vec3 to) {
        Vec3 flatPoint = new Vec3(point.x, 0.0, point.z);
        Vec3 flatFrom = new Vec3(from.x, 0.0, from.z);
        Vec3 segment = new Vec3(to.x - from.x, 0.0, to.z - from.z);
        double lengthSqr = segment.lengthSqr();
        if (lengthSqr <= 1.0e-6) {
            return flatPoint.distanceTo(flatFrom);
        }
        double projection = Math.max(0.0, Math.min(1.0,
                flatPoint.subtract(flatFrom).dot(segment) / lengthSqr));
        return flatPoint.distanceTo(flatFrom.add(segment.scale(projection)));
    }

    /** 전방 부채꼴을 쓸어 피해를 주고 뒤로 밀어냅니다. */
    private static void castWaveOfMalice(ServerPlayer player, PlayerLane lane, DemonLordState state, TowerType altar) {
        double range = ability(altar, "range", 6.0);
        double halfAngleCos = Math.cos(Math.toRadians(ability(altar, "coneDegrees", 60.0) / 2.0));
        double damage = ability(altar, "damage", 45.0) * state.damageMultiplier();
        double knockback = ability(altar, "knockback", 0.8);

        Vec3 origin = player.position();
        Vec3 look = horizontal(player.getLookAngle());
        for (SemionMonsterEntity monster : monstersNear(lane, origin, range)) {
            Vec3 toMonster = horizontal(monster.position().subtract(origin));
            if (toMonster.lengthSqr() > 1.0e-4 && look.dot(toMonster.normalize()) < halfAngleCos) {
                continue;
            }
            DemonLordService.dealDamage(player, monster, damage, DamageType.MAGIC);
            push(monster, toMonster, knockback, 0.35);
        }
        if (player.level() instanceof ServerLevel level) {
            drawCone(level, origin, look, range, ability(altar, "coneDegrees", 60.0), ParticleTypes.SOUL_FIRE_FLAME);
        }
        sound(player, SoundEvents.WARDEN_SONIC_BOOM, 0.7f, 1.4f);
    }

    /** 도약하며 주위를 밀어내고 체력을 회복합니다. */
    private static void castDemonWings(ServerPlayer player, PlayerLane lane, DemonLordState state, TowerType altar) {
        double radius = ability(altar, "radius", 4.0);
        double damage = ability(altar, "damage", 30.0) * state.damageMultiplier();
        double knockback = ability(altar, "knockback", 0.7);
        double leapPower = ability(altar, "leapPower", 1.0);

        Vec3 origin = player.position();
        for (SemionMonsterEntity monster : monstersNear(lane, origin, radius)) {
            DemonLordService.dealDamage(player, monster, damage, DamageType.MAGIC);
            push(monster, horizontal(monster.position().subtract(origin)), knockback, 0.4);
        }
        state.heal(state.maxHealth() * ability(altar, "healRatio", 0.10));

        if (player.level() instanceof ServerLevel level) {
            drawCircle(level, origin, radius, ParticleTypes.SOUL_FIRE_FLAME);
            // 회복은 초록 하트로 따로 알립니다.
            level.sendParticles(ParticleTypes.HEART, origin.x, origin.y + 1.6, origin.z, 6, 0.4, 0.3, 0.4, 0.0);
        }

        Vec3 look = horizontal(player.getLookAngle());
        player.setDeltaMovement(look.x * leapPower, 0.62, look.z * leapPower);
        player.hurtMarked = true;
        player.resetFallDistance();

        sound(player, SoundEvents.ENDER_DRAGON_FLAP, 1.0f, 0.8f);
    }

    /**
     * 전방으로 돌진해 부딪힌 적을 띄우고 기절시킵니다.
     *
     * <p>돌진은 텔레포트로 처리합니다. 속도로 밀면 서버 틱 동안 충돌 판정이 새기 때문에, 경로를
     * 샘플링해 맞은 적을 모두 잡아낸 뒤 끝점으로 옮기는 쪽이 결과가 일정합니다.
     */
    private static void castSkyBreaker(ServerPlayer player, PlayerLane lane, DemonLordState state, TowerType altar) {
        double distance = ability(altar, "dashDistance", 8.0);
        double hitRadius = ability(altar, "hitRadius", 2.0);
        double damage = ability(altar, "damage", 90.0) * state.damageMultiplier();
        double lift = ability(altar, "liftPower", 0.8);
        int stunTicks = (int) Math.max(1.0, ability(altar, "stunTicks", 40.0));

        Vec3 start = player.position();
        Vec3 look = horizontal(player.getLookAngle());
        Vec3 end = resolveDashEnd(player, lane, start, look, distance);
        double travelled = start.distanceTo(end);

        List<SemionMonsterEntity> hit = new ArrayList<>();
        int samples = Math.max(2, (int) Math.ceil(Math.max(1.0, travelled)));
        for (int i = 0; i <= samples; i++) {
            Vec3 point = start.lerp(end, (double) i / samples);
            for (SemionMonsterEntity monster : monstersNear(lane, point, hitRadius)) {
                if (!hit.contains(monster)) {
                    hit.add(monster);
                }
            }
        }
        for (SemionMonsterEntity monster : hit) {
            DemonLordService.dealDamage(player, monster, damage, DamageType.PHYSICAL);
            monster.setDeltaMovement(monster.getDeltaMovement().x, lift, monster.getDeltaMovement().z);
            monster.hurtMarked = true;
            // 기절: 이동·공격 속도·공격력을 모두 100% 깎아 아무것도 못 하게 만듭니다.
            monster.applyTimedEffect(TimedEffectType.MONSTER_MOVE_SPEED_REDUCTION, 1.0, stunTicks);
            monster.applyTimedEffect(TimedEffectType.MONSTER_ATTACK_SPEED_REDUCTION, 1.0, stunTicks);
            monster.applyTimedEffect(TimedEffectType.MONSTER_ATTACK_DAMAGE_REDUCTION, 1.0, stunTicks);
        }

        if (player.level() instanceof ServerLevel level) {
            drawCorridor(level, start, end, hitRadius, ParticleTypes.CRIT);
        }
        player.teleportTo(end.x, end.y, end.z);
        player.resetFallDistance();
        effect(player, ParticleTypes.EXPLOSION, end, 4, 0.8);
        sound(player, SoundEvents.RAVAGER_ROAR, 1.0f, 0.9f);
    }

    /**
     * 돌진이 실제로 멈춰야 하는 지점을 구합니다.
     *
     * <p>텔레포트는 충돌을 무시하므로 그냥 목표 지점으로 옮기면 아레나 배리어를 뚫고 맵 밖으로
     * 나가 떨어집니다. 그래서 두 겹으로 막습니다 — 먼저 블록에 레이캐스트해 벽 앞에서 멈추고,
     * 그다음 자기 레인 영역으로 한 번 더 조입니다.
     */
    private static Vec3 resolveDashEnd(ServerPlayer player, PlayerLane lane, Vec3 start, Vec3 look, double distance) {
        Vec3 from = start.add(0.0, 0.6, 0.0);
        Vec3 to = from.add(look.scale(distance));
        HitResult clip = player.level().clip(new ClipContext(
                from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 stop = clip.getType() == HitResult.Type.MISS
                ? to
                : clip.getLocation().subtract(look.scale(WALL_STANDOFF));

        Vec3 end = new Vec3(stop.x, start.y, stop.z);
        // 벽이 코앞이면 뒤로 밀려 시작점보다 뒤에 설 수 있으므로 제자리로 되돌립니다.
        if (horizontal(end.subtract(start)).dot(look) <= 0.0) {
            end = start;
        }
        return DemonLordService.clampToLane(lane, end);
    }

    /**
     * 먼저 공중으로 솟아오르고, 정점에 도달하면 그때 포격합니다.
     *
     * <p>발사는 {@code castDelayTicks} 만큼 미뤄 예약해 두고 {@link #tickPending} 이 처리합니다.
     * 조준은 시전 시점이 아니라 <b>발사 시점의 시선</b>을 씁니다. 솟아오른 뒤 아래를 내려다보며
     * 조준하는 게 이 스킬의 그림이기 때문입니다.
     */
    private static void castArcaneBombardment(ServerPlayer player, DemonLordState state, TowerType altar, long gameTime) {
        player.setDeltaMovement(player.getDeltaMovement().x, ability(altar, "jumpPower", 0.9), player.getDeltaMovement().z);
        player.hurtMarked = true;
        player.resetFallDistance();

        int delay = (int) Math.max(1.0, ability(altar, "castDelayTicks", 10.0));
        state.queueBombardment(altar, gameTime + delay);

        effect(player, ParticleTypes.SOUL_FIRE_FLAME, player.position(), 25, 0.6);
        sound(player, SoundEvents.ENDER_DRAGON_FLAP, 0.9f, 1.4f);
    }

    /** Runs the delayed and lasting parts of skills. Called once per lane tick. */
    public static void tickPending(ServerPlayer player, PlayerLane lane, DemonLordState state, long gameTime) {
        tickHellfireZone(player, lane, state, gameTime);
        if (!state.bombardmentReady(gameTime)) {
            return;
        }
        TowerType altar = state.consumeBombardment();
        if (altar == null) {
            return;
        }
        double blastRadius = ability(altar, "blastRadius", 4.0);
        double damage = ability(altar, "damage", 70.0) * state.damageMultiplier();
        double range = ability(altar, "projectileRange", 18.0);

        // 실제 투사체 엔티티 대신 레이캐스트로 착탄 지점을 구합니다. 결과는 같고, 라운드마다
        // 수십 발이 날아다니는 엔티티를 만들지 않아도 됩니다.
        Vec3 eye = player.getEyePosition();
        Vec3 target = eye.add(player.getLookAngle().scale(range));
        HitResult clip = player.level().clip(new ClipContext(
                eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 impact = clip.getType() == HitResult.Type.MISS ? target : clip.getLocation();

        for (SemionMonsterEntity monster : monstersNear(lane, impact, blastRadius)) {
            DemonLordService.dealDamage(player, monster, damage, DamageType.MAGIC);
        }

        trail(player, eye, impact);
        if (player.level() instanceof ServerLevel level) {
            drawCircle(level, impact, blastRadius, ParticleTypes.FLAME);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, impact.x, impact.y, impact.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        sound(player, SoundEvents.GENERIC_EXPLODE.value(), 1.0f, 1.1f);
    }

    /**
     * Pulses the 지옥불 낙인 zone and draws its outline.
     *
     * <p>The zone lives on the player's state rather than as an entity, so it costs one bounds check
     * per monster per pulse and disappears on its own when the round ends.
     */
    private static void tickHellfireZone(ServerPlayer player, PlayerLane lane, DemonLordState state, long gameTime) {
        DemonLordState.HellfireZone zone = state.zone();
        if (zone == null) {
            return;
        }
        if (gameTime >= zone.expiryTick()) {
            state.clearZone();
            return;
        }
        // 테두리는 매 초 한 번만 그려 파티클 패킷을 아낍니다.
        if (gameTime % 20 == 0 && player.level() instanceof ServerLevel level) {
            drawCircle(level, zone.centre(), zone.radius(), ParticleTypes.SOUL_FIRE_FLAME);
        }
        if (gameTime < zone.nextPulseTick()) {
            return;
        }
        for (SemionMonsterEntity monster : monstersNear(lane, zone.centre(), zone.radius())) {
            DemonLordService.dealDamage(player, monster, zone.damage(), DamageType.MAGIC);
            monster.applyTimedEffect(
                    TimedEffectType.MONSTER_TOWER_DAMAGE_TAKEN_BONUS,
                    zone.damageTakenBonus(),
                    zone.tickIntervalTicks() * 2
            );
        }
        state.placeZone(new DemonLordState.HellfireZone(
                zone.centre(),
                zone.radius(),
                zone.damage(),
                zone.damageTakenBonus(),
                zone.tickIntervalTicks(),
                zone.expiryTick(),
                gameTime + zone.tickIntervalTicks()
        ));
    }

    /** Draws the shot so the blast does not appear out of nowhere. */
    private static void trail(ServerPlayer player, Vec3 from, Vec3 to) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        int steps = Math.max(4, (int) from.distanceTo(to));
        for (int i = 0; i <= steps; i++) {
            Vec3 point = from.lerp(to, (double) i / steps);
            level.sendParticles(ParticleTypes.SMALL_FLAME, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** 최대 체력 비례 방어막을 두릅니다. */
    private static void castDemonBarrier(ServerPlayer player, DemonLordState state, TowerType altar) {
        double shield = state.maxHealth() * ability(altar, "shieldRatio", 0.25);
        int duration = (int) Math.max(1.0, ability(altar, "shieldDurationTicks", 160.0));
        state.grantShield(shield, player.level().getGameTime() + duration);
        if (player.level() instanceof ServerLevel level) {
            drawDome(level, player.position(), 2.0, ParticleTypes.SCULK_SOUL);
        }
        sound(player, SoundEvents.TOTEM_USE, 0.8f, 1.2f);
    }

    // ------------------------------------------------------------- internals

    private static double ability(TowerType altar, String key, double fallback) {
        return TowerBalanceRuntime.ability(altar.id(), key, fallback);
    }

    /** Live monsters of this lane whose entity sits within {@code radius} of {@code center}. */
    private static List<SemionMonsterEntity> monstersNear(PlayerLane lane, Vec3 center, double radius) {
        List<SemionMonsterEntity> found = new ArrayList<>();
        double radiusSqr = radius * radius;
        for (Monster monster : List.copyOf(lane.activeMonsters())) {
            if (monster == null || !monster.isAlive() || !monster.hasMinecraftEntity()) {
                continue;
            }
            if (!(lane.arenaWorld().getEntity(monster.minecraftEntityId()) instanceof SemionMonsterEntity entity)
                    || entity.isRemoved()) {
                continue;
            }
            if (entity.position().distanceToSqr(center) <= radiusSqr) {
                found.add(entity);
            }
        }
        return found;
    }

    private static Vec3 horizontal(Vec3 vector) {
        Vec3 flat = new Vec3(vector.x, 0.0, vector.z);
        return flat.lengthSqr() < 1.0e-6 ? new Vec3(0.0, 0.0, 1.0) : flat.normalize();
    }

    private static void push(SemionMonsterEntity monster, Vec3 direction, double strength, double lift) {
        Vec3 away = horizontal(direction).scale(strength);
        monster.setDeltaMovement(away.x, lift, away.z);
        monster.hurtMarked = true;
    }

    private static void effect(ServerPlayer player, ParticleOptions particle,
            Vec3 at, int count, double spread) {
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(particle, at.x, at.y + 1.0, at.z, count, spread, spread, spread, 0.02);
        }
    }

    // ------------------------------------------------- 범위 표시용 파티클 도형
    //
    // 스킬이 실제로 판정하는 것과 같은 반경·각도로 그립니다. 눈에 보이는 모양과 판정이 어긋나면
    // 플레이어가 거리를 못 재므로, 수치는 항상 판정과 같은 ability 값에서 가져옵니다.

    /** 지면에 원 테두리를 그립니다. */
    private static void drawCircle(ServerLevel level, Vec3 centre, double radius, ParticleOptions particle) {
        int points = Math.max(16, (int) Math.round(radius * 12.0));
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2.0 * i / points;
            level.sendParticles(
                    particle,
                    centre.x + Math.cos(angle) * radius,
                    centre.y + GROUND_OFFSET,
                    centre.z + Math.sin(angle) * radius,
                    1, 0.0, 0.0, 0.0, 0.0
            );
        }
    }

    /**
     * 부채꼴을 그립니다. 바깥 호와 양쪽 변을 그리고, 안쪽에 호를 두 겹 더 넣어 면으로 읽히게 합니다.
     */
    private static void drawCone(
            ServerLevel level,
            Vec3 origin,
            Vec3 look,
            double range,
            double degrees,
            ParticleOptions particle
    ) {
        double half = Math.toRadians(degrees / 2.0);
        double base = Math.atan2(look.z, look.x);
        for (double fraction : CONE_ARC_FRACTIONS) {
            double radius = range * fraction;
            int points = Math.max(8, (int) Math.round(radius * degrees / 12.0));
            for (int i = 0; i <= points; i++) {
                double angle = base - half + (2.0 * half * i / points);
                level.sendParticles(
                        particle,
                        origin.x + Math.cos(angle) * radius,
                        origin.y + GROUND_OFFSET,
                        origin.z + Math.sin(angle) * radius,
                        1, 0.0, 0.0, 0.0, 0.0
                );
            }
        }
        for (int side = -1; side <= 1; side += 2) {
            double angle = base + side * half;
            int steps = Math.max(4, (int) Math.round(range * 2.0));
            for (int i = 1; i <= steps; i++) {
                double radius = range * i / steps;
                level.sendParticles(
                        particle,
                        origin.x + Math.cos(angle) * radius,
                        origin.y + GROUND_OFFSET,
                        origin.z + Math.sin(angle) * radius,
                        1, 0.0, 0.0, 0.0, 0.0
                );
            }
        }
    }

    /** 돌진 경로를 폭 {@code radius} 의 통로로 그립니다. */
    private static void drawCorridor(ServerLevel level, Vec3 from, Vec3 to, double radius, ParticleOptions particle) {
        Vec3 direction = horizontal(to.subtract(from));
        Vec3 side = new Vec3(-direction.z, 0.0, direction.x).scale(radius);
        int steps = Math.max(6, (int) Math.round(from.distanceTo(to) * 2.0));
        for (int i = 0; i <= steps; i++) {
            Vec3 point = from.lerp(to, (double) i / steps);
            for (int edge = -1; edge <= 1; edge += 2) {
                level.sendParticles(
                        particle,
                        point.x + side.x * edge,
                        point.y + GROUND_OFFSET,
                        point.z + side.z * edge,
                        1, 0.0, 0.0, 0.0, 0.0
                );
            }
        }
        drawCircle(level, to, radius, particle);
    }

    /** 플레이어를 감싸는 돔. 방어막이 켜졌다는 걸 한눈에 보여 줍니다. */
    private static void drawDome(ServerLevel level, Vec3 centre, double radius, ParticleOptions particle) {
        for (int ring = 0; ring < DOME_RINGS; ring++) {
            double height = radius * ring / DOME_RINGS;
            double ringRadius = Math.sqrt(Math.max(0.0, radius * radius - height * height));
            drawCircle(level, new Vec3(centre.x, centre.y + height, centre.z), ringRadius, particle);
        }
    }

    private static void sound(ServerPlayer player, net.minecraft.sounds.SoundEvent event, float volume, float pitch) {
        if (player.level() instanceof ServerLevel level) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), event, SoundSource.PLAYERS, volume, pitch);
        }
    }
}
