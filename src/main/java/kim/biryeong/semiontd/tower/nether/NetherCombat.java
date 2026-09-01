package kim.biryeong.semiontd.tower.nether;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.api.area.AreaEffectResult;
import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.monster.DamageType;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.summon.SummonRole;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;
import net.minecraft.resources.ResourceLocation;

final class NetherCombat {
    private static final ResourceLocation LOW_HEALTH_DAMAGE_SOURCE = source("low_health_damage");
    private static final ResourceLocation LOW_HEALTH_DAMAGE_REDUCTION_SOURCE = source("low_health_damage_reduction");
    private static final ResourceLocation LOW_HEALTH_ATTACK_SPEED_SOURCE = source("low_health_attack_speed");
    private static final ResourceLocation ZOMBIE_ATTACK_SPEED_SOURCE = source("zombie_attack_speed");
    private static final ResourceLocation PIGLIN_KILL_DAMAGE_SOURCE = source("piglin_kill_damage");
    private static final ResourceLocation ZOMBIE_TRANSITION_DAMAGE_SOURCE = source("zombie_transition_damage");

    private final NetherConfig config;
    private final NetherCombatState state;

    NetherCombat(NetherConfig config, NetherCombatState state) {
        this.config = config;
        this.state = state;
    }

    void tick(NetherTower tower, SemionTowerEntity entity) {
        if (entity != null) {
            refreshDynamicTimedEffects(tower, entity);
        }
        state.tick();
    }

    double modifyAttackDamage(
            NetherTower tower,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        Monster monster = target == null ? null : target.runtimeMonster();
        double adjusted = damageAmount;
        if (NetherTowers.isStriderLine(tower.type()) && monster != null && monster.senderTeam().isPresent()) {
            adjusted *= 1.0 + value(tower, NetherAbilityKey.INCOME_DAMAGE_BONUS);
        }
        if (tower.isType(NetherTowers.T3_PIGLIN_BRUTE) && tower.isCritical() && isTankOrHighHealthTarget(tower, monster)) {
            adjusted *= 1.0 + value(tower, NetherAbilityKey.TANK_DAMAGE_BONUS);
        }
        if (NetherTowers.isSkeletonLine(tower.type())
                && targetHealthRatio(target) <= value(tower, NetherAbilityKey.LOW_TARGET_HEALTH_THRESHOLD)) {
            adjusted *= 1.0 + value(tower, NetherAbilityKey.LOW_TARGET_DAMAGE_BONUS);
        }
        if (tower.isType(NetherTowers.T3_WITHER)
                && monster != null
                && monster.maxHealth() >= value(tower, NetherAbilityKey.HIGH_HEALTH_THRESHOLD)) {
            adjusted *= 1.0 + value(tower, NetherAbilityKey.HIGH_HEALTH_DAMAGE_BONUS);
        }
        if (tower.isType(NetherTowers.T3_WITHER)
                && tower.state() == NetherTowerState.ZOMBIE
                && targetHealthRatio(target) <= value(tower, NetherAbilityKey.ZOMBIE_EXECUTE_THRESHOLD)) {
            adjusted *= 1.0 + value(tower, NetherAbilityKey.ZOMBIE_EXECUTE_DAMAGE_BONUS);
        }
        return adjusted;
    }

    Optional<SemionMonsterEntity> selectAttackTarget(
            NetherTower tower,
            List<SemionMonsterEntity> candidates
    ) {
        if (candidates == null || candidates.isEmpty() || !NetherTowers.isSkeletonLine(tower.type())) {
            return Optional.empty();
        }
        if (tower.isType(NetherTowers.T3_WITHER)) {
            Optional<SemionMonsterEntity> highHealthTarget = candidates.stream()
                    .filter(target -> {
                        Monster monster = target.runtimeMonster();
                        return monster != null
                                && monster.maxHealth() >= value(tower, NetherAbilityKey.HIGH_HEALTH_THRESHOLD);
                    })
                    .max(Comparator.comparingDouble(target -> {
                        Monster monster = target.runtimeMonster();
                        return monster == null ? target.getMaxHealth() : monster.maxHealth();
                    }));
            if (highHealthTarget.isPresent()) {
                return highHealthTarget;
            }
        }
        return candidates.stream().min(Comparator.comparingDouble(NetherCombat::targetHealthRatio));
    }

    void onAttack(
            NetherTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        boolean critical = tower.isCritical();
        state.recordAttack(critical);
        heal(towerEntity, damageAmount * lifeStealRatio(tower, target));

        if (NetherTowers.isStriderLine(tower.type()) && critical) {
            state.extendDecayReduction(config.ticks(tower.type(), NetherAbilityKey.DECAY_REDUCTION_TICKS));
        }
        if (NetherTowers.isHoglinLine(tower.type()) || NetherTowers.isBlazeLine(tower.type())) {
            splashAroundTarget(
                    tower,
                    towerEntity,
                    target,
                    damageAmount,
                    splashRadius(tower),
                    value(tower, NetherAbilityKey.SPLASH_DAMAGE_RATIO)
            );
        }
        if (NetherTowers.isBlazeLine(tower.type()) && critical && state.pulseReady()) {
            damageAroundTarget(
                    tower,
                    towerEntity,
                    target,
                    value(tower, NetherAbilityKey.PULSE_RADIUS),
                    scaledPulseDamage(tower, NetherAbilityKey.PULSE_DAMAGE_RATIO)
            );
            state.startPulseCooldown(config.ticks(tower.type(), NetherAbilityKey.PULSE_INTERVAL_TICKS));
        }
        int extraAttackEvery = config.integer(tower.type(), NetherAbilityKey.EXTRA_ATTACK_EVERY);
        if (NetherTowers.isBlazeLine(tower.type())
                && critical
                && state.extraAttackReady(extraAttackEvery)) {
            extraAttack(
                    tower,
                    towerEntity,
                    target,
                    damageAmount * value(tower, NetherAbilityKey.EXTRA_ATTACK_DAMAGE_RATIO)
            );
        }
        if (tower.isType(NetherTowers.T3_GHAST) && critical) {
            applyMonsterDamageTakenMark(
                    target,
                    sourceId(tower, "ghast_mark"),
                    value(tower, NetherAbilityKey.CRITICAL_MARK_DAMAGE_TAKEN_BONUS),
                    config.ticks(tower.type(), NetherAbilityKey.MARK_DURATION_TICKS)
            );
        }
        if (tower.isType(NetherTowers.T2_WITHER_SKELETON) || tower.isType(NetherTowers.T3_WITHER)) {
            int maxStacks = Math.max(1, config.integer(tower.type(), NetherAbilityKey.MAX_MARK_STACKS));
            double bonus = value(tower, NetherAbilityKey.MARK_DAMAGE_TAKEN_BONUS)
                    + (tower.state() == NetherTowerState.ZOMBIE
                    ? value(tower, NetherAbilityKey.ZOMBIE_MARK_DAMAGE_TAKEN_BONUS)
                    : 0.0);
            applyMonsterDamageTakenMark(
                    target,
                    sourceId(tower, "wither_skeleton_mark_" + state.nextMarkIndex(maxStacks)),
                    bonus,
                    config.ticks(tower.type(), NetherAbilityKey.MARK_DURATION_TICKS)
            );
        }
        if (tower.isType(NetherTowers.T3_WITHER) && critical) {
            splashAroundTarget(
                    tower,
                    towerEntity,
                    target,
                    damageAmount,
                    value(tower, NetherAbilityKey.CRITICAL_SPLASH_RADIUS),
                    value(tower, NetherAbilityKey.CRITICAL_SPLASH_DAMAGE_RATIO)
            );
            applyMonsterDamageTakenMark(
                    target,
                    sourceId(tower, "wither_mark"),
                    value(tower, NetherAbilityKey.CRITICAL_MARK_DAMAGE_TAKEN_BONUS),
                    config.ticks(tower.type(), NetherAbilityKey.MARK_DURATION_TICKS)
            );
        }
    }

    void onKill(
            NetherTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        if (towerEntity != null
                && NetherTowers.isStriderLine(tower.type())
                && value(tower, NetherAbilityKey.KILL_DAMAGE_BONUS) > 0.0) {
            towerEntity.applyTimedEffect(
                    TimedEffectType.TOWER_DAMAGE_BONUS,
                    PIGLIN_KILL_DAMAGE_SOURCE,
                    value(tower, NetherAbilityKey.KILL_DAMAGE_BONUS),
                    config.ticks(tower.type(), NetherAbilityKey.KILL_DAMAGE_BONUS_TICKS)
            );
        }
        if (NetherTowers.isSkeletonLine(tower.type()) && state.lastAttackWasCritical()) {
            heal(towerEntity, damageAmount * value(tower, NetherAbilityKey.CRITICAL_KILL_LIFE_STEAL_RATIO));
        }
    }

    void onZombieTransition(NetherTower tower, SemionTowerEntity entity) {
        if (entity != null && tower.isType(NetherTowers.T3_ZOMBIFIED_PIGLIN)) {
            entity.forceAttackReady();
        }
        if (entity != null && tower.isType(NetherTowers.T3_PIGLIN_BRUTE)) {
            entity.applyTimedEffect(
                    TimedEffectType.TOWER_DAMAGE_BONUS,
                    ZOMBIE_TRANSITION_DAMAGE_SOURCE,
                    value(tower, NetherAbilityKey.ZOMBIE_TRANSITION_DAMAGE_BONUS),
                    config.ticks(tower.type(), NetherAbilityKey.ZOMBIE_TRANSITION_DAMAGE_BONUS_TICKS)
            );
        }
        if (entity != null && NetherTowers.isBlazeLine(tower.type())) {
            damageAroundTower(
                    tower,
                    entity,
                    value(tower, NetherAbilityKey.ZOMBIE_TRANSITION_PULSE_RADIUS),
                    scaledPulseDamage(tower, NetherAbilityKey.ZOMBIE_TRANSITION_PULSE_DAMAGE_RATIO)
            );
        }
    }

    double lowHealthDamageBonus(NetherTower tower) {
        if (tower.healthRatio() > config.global(NetherAbilityKey.LOW_HEALTH_THRESHOLD)) {
            return 0.0;
        }
        return cappedMissingHealthBonus(
                tower.missingHealthRatio(),
                config.global(NetherAbilityKey.DAMAGE_PER_MISSING_HEALTH),
                config.global(NetherAbilityKey.LOW_HEALTH_DAMAGE_CAP)
        );
    }

    double lifeStealRatio(NetherTower tower, SemionMonsterEntity target) {
        double base = tower.state() == NetherTowerState.ZOMBIE
                ? config.valueOrGlobal(tower.type(), NetherAbilityKey.ZOMBIE_LIFE_STEAL)
                : config.valueOrGlobal(tower.type(), NetherAbilityKey.NETHER_LIFE_STEAL);
        double ratio = base + lowHealthLifeStealBonus(tower) + value(tower, NetherAbilityKey.LIFE_STEAL_BONUS);
        Monster monster = target == null ? null : target.runtimeMonster();
        if (tower.isType(NetherTowers.T3_PIGLIN_BRUTE)
                && tower.isCritical()
                && isTankOrHighHealthTarget(tower, monster)) {
            ratio += value(tower, NetherAbilityKey.TANK_LIFE_STEAL_BONUS);
        }
        return Math.max(0.0, ratio);
    }

    static double cappedMissingHealthBonus(double missingHealthRatio, double bonusPerMissingHealth, double cap) {
        return Math.min(cap, missingHealthRatio * bonusPerMissingHealth);
    }

    private void refreshDynamicTimedEffects(NetherTower tower, SemionTowerEntity entity) {
        int refreshTicks = Math.max(2, (int) Math.round(config.global(NetherAbilityKey.EFFECT_REFRESH_TICKS)));
        entity.refreshTimedEffect(
                TimedEffectType.TOWER_DAMAGE_BONUS,
                LOW_HEALTH_DAMAGE_SOURCE,
                lowHealthDamageBonus(tower),
                refreshTicks
        );
        entity.refreshTimedEffect(
                TimedEffectType.TOWER_DAMAGE_REDUCTION,
                LOW_HEALTH_DAMAGE_REDUCTION_SOURCE,
                criticalDamageReduction(tower),
                refreshTicks
        );
        entity.refreshTimedEffect(
                TimedEffectType.TOWER_ATTACK_SPEED_BONUS,
                LOW_HEALTH_ATTACK_SPEED_SOURCE,
                missingHealthAttackSpeedBonus(tower),
                refreshTicks
        );
        entity.refreshTimedEffect(
                TimedEffectType.TOWER_ATTACK_SPEED_BONUS,
                ZOMBIE_ATTACK_SPEED_SOURCE,
                zombieAttackSpeedBonus(tower),
                refreshTicks
        );
    }

    private double lowHealthLifeStealBonus(NetherTower tower) {
        if (tower.healthRatio() > config.global(NetherAbilityKey.LOW_HEALTH_THRESHOLD)) {
            return 0.0;
        }
        return cappedMissingHealthBonus(
                tower.missingHealthRatio(),
                config.global(NetherAbilityKey.LIFE_STEAL_PER_MISSING_HEALTH),
                config.global(NetherAbilityKey.LIFE_STEAL_BONUS_CAP)
        );
    }

    private double missingHealthAttackSpeedBonus(NetherTower tower) {
        double cap = value(tower, NetherAbilityKey.MISSING_HEALTH_ATTACK_SPEED_CAP);
        if (tower.state() == NetherTowerState.ZOMBIE
                && value(tower, NetherAbilityKey.ZOMBIE_MISSING_HEALTH_ATTACK_SPEED_CAP) > 0.0) {
            cap = value(tower, NetherAbilityKey.ZOMBIE_MISSING_HEALTH_ATTACK_SPEED_CAP);
        }
        return Math.max(0.0, tower.missingHealthRatio() * cap);
    }

    private double criticalDamageReduction(NetherTower tower) {
        return tower.isCritical() ? value(tower, NetherAbilityKey.CRITICAL_DAMAGE_REDUCTION) : 0.0;
    }

    private double zombieAttackSpeedBonus(NetherTower tower) {
        return tower.state() == NetherTowerState.ZOMBIE
                ? value(tower, NetherAbilityKey.ZOMBIE_ATTACK_SPEED_BONUS)
                : 0.0;
    }

    private boolean isTankOrHighHealthTarget(NetherTower tower, Monster monster) {
        return monster != null
                && (monster.summonRoles().contains(SummonRole.TANK)
                || monster.maxHealth() >= value(tower, NetherAbilityKey.HIGH_HEALTH_THRESHOLD));
    }

    private double scaledPulseDamage(NetherTower tower, NetherAbilityKey ratio) {
        return Math.max(0.0, tower.type().damage() * value(tower, ratio));
    }

    private double splashRadius(NetherTower tower) {
        double radius = value(tower, NetherAbilityKey.SPLASH_RADIUS);
        if (NetherTowers.isHoglinLine(tower.type()) && tower.state() == NetherTowerState.ZOMBIE) {
            radius += value(tower, NetherAbilityKey.ZOMBIE_SPLASH_RADIUS_BONUS);
        }
        if (tower.isType(NetherTowers.T3_GHAST)
                && tower.healthRatio() <= config.global(NetherAbilityKey.LOW_HEALTH_THRESHOLD)) {
            radius += value(tower, NetherAbilityKey.LOW_HEALTH_SPLASH_RADIUS_BONUS);
        }
        return radius;
    }

    private void splashAroundTarget(
            NetherTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount,
            double radius,
            double damageRatio
    ) {
        if (towerEntity == null || target == null || radius <= 0.0 || damageRatio <= 0.0) {
            return;
        }
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTarget(
                AreaEffectIds.tower(tower, "splash"),
                towerEntity,
                target,
                radius,
                NetherVfx.splash()
        );
        TowerAreaDamage.applyBasicAttackSplash(
                tower,
                towerEntity,
                request,
                monster -> damageAmount * damageRatio,
                true,
                (monster, splashDamage, killed) -> heal(towerEntity, splashDamage * lifeStealRatio(tower, monster))
        );
    }

    private void damageAroundTower(
            NetherTower tower,
            SemionTowerEntity towerEntity,
            double radius,
            double damageAmount
    ) {
        if (towerEntity == null || radius <= 0.0 || damageAmount <= 0.0) {
            return;
        }
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTower(
                AreaEffectIds.tower(tower, "pulse"),
                towerEntity,
                radius,
                NetherVfx.pulse()
        );
        TowerAreaDamage.apply(
                tower,
                towerEntity,
                request,
                monster -> damageAmount,
                true,
                (monster, appliedDamage, killed) -> heal(towerEntity, appliedDamage * lifeStealRatio(tower, monster)),
                DamageType.MAGIC
        );
    }

    private void damageAroundTarget(
            NetherTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double radius,
            double damageAmount
    ) {
        if (towerEntity == null || target == null || radius <= 0.0 || damageAmount <= 0.0) {
            return;
        }
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTarget(
                AreaEffectIds.tower(tower, "pulse"),
                towerEntity,
                target,
                radius,
                NetherVfx.pulse()
        ).including(target.getUUID());
        TowerAreaDamage.apply(
                tower,
                towerEntity,
                request,
                monster -> damageAmount,
                true,
                (monster, appliedDamage, killed) -> heal(towerEntity, appliedDamage * lifeStealRatio(tower, monster)),
                DamageType.MAGIC
        );
    }

    private void extraAttack(
            NetherTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity primaryTarget,
            double damageAmount
    ) {
        if (towerEntity == null || primaryTarget == null) {
            return;
        }
        double range = Math.max(0.0, value(tower, NetherAbilityKey.SECONDARY_RANGE));
        AreaEffectResult<SemionMonsterEntity> result = range <= 0.0
                ? AreaEffectResult.empty()
                : TowerAreaDamage.applyBasicAttack(
                        tower,
                        towerEntity,
                        MonsterAreaEffectRequest.aroundTower(
                                        AreaEffectIds.tower(tower, "extra_attack"),
                                        towerEntity,
                                        range,
                                        AreaVfxSpec.none()
                                )
                                .withFilter(monster -> monster != primaryTarget)
                                .nearestTargets(1),
                        monster -> damageAmount,
                        true,
                        (monster, dealtDamage, killed) -> heal(
                                towerEntity,
                                damageAmount * lifeStealRatio(tower, monster)
                        ),
                        DamageType.MAGIC
                );
        if (!result.hits().isEmpty()) {
            NetherVfx.secondaryAttack(towerEntity, result.hits().getFirst().target());
            return;
        }
        if (!primaryTarget.isAlive()) {
            return;
        }
        var primaryResult = tower.damageBasicAttackTargetResult(
                towerEntity,
                primaryTarget,
                damageAmount,
                DamageType.MAGIC
        );
        NetherVfx.secondaryAttack(towerEntity, primaryTarget);
        heal(towerEntity, damageAmount * lifeStealRatio(tower, primaryTarget));
        if (primaryResult.killed()) {
            tower.onKill(towerEntity, primaryTarget, damageAmount);
        }
    }

    private void applyMonsterDamageTakenMark(
            SemionMonsterEntity target,
            ResourceLocation sourceId,
            double magnitude,
            int durationTicks
    ) {
        if (target != null && sourceId != null && magnitude > 0.0 && durationTicks > 0) {
            target.applyTimedEffect(
                    TimedEffectType.MONSTER_TOWER_DAMAGE_TAKEN_BONUS,
                    sourceId,
                    magnitude,
                    durationTicks
            );
        }
    }

    private double value(NetherTower tower, NetherAbilityKey ability) {
        return config.value(tower.type(), ability);
    }

    private static double targetHealthRatio(SemionMonsterEntity target) {
        if (target == null || target.getMaxHealth() <= 0.0F) {
            return 1.0;
        }
        return target.getHealth() / target.getMaxHealth();
    }

    private static void heal(SemionTowerEntity towerEntity, double amount) {
        if (towerEntity != null && amount > 0.0) {
            towerEntity.healTarget(towerEntity, amount);
        }
    }

    private static ResourceLocation source(String path) {
        return ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "nether/" + path);
    }

    private static ResourceLocation sourceId(NetherTower tower, String suffix) {
        String path = "nether/" + tower.type().id() + "/" + tower.ownerPlayer() + "/" + tower.laneId()
                + "/" + tower.position().x() + "_" + tower.position().y() + "_" + tower.position().z() + "/" + suffix;
        return ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, path);
    }
}
