package kim.biryeong.semiontd.tower.end;

import static kim.biryeong.semiontd.tower.end.EndConfig.Ability.*;

import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.tower.DamageScaling;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;

final class EndCombat {
    private static final int SPLASH_STEP_COUNT = 5;
    private final EndConfig config;
    private final EndTransferController transfers;

    EndCombat(EndConfig config, EndTransferController transfers) {
        this.config = config;
        this.transfers = transfers;
    }

    int attackInterval(TowerType type) {
        return reducedAttackInterval(type.attackIntervalTicks(), Math.max(1, config.integer(ATTACK_SPEED_MINIMUM_TICKS)));
    }

    int adjustAttackInterval(int baseIntervalTicks) {
        return reducedAttackInterval(baseIntervalTicks, Math.max(1, config.integer(ATTACK_SPEED_MINIMUM_TICKS)));
    }

    double attackRange(TowerType type, boolean dragon) {
        return type.range() + attackRangeBonus() + dragonRangeBonus(dragon);
    }

    double modifyAttackDamage(TowerType type, double transferredDamageBonus, double damageAmount) {
        if (type.damage() <= 0.0) {return damageAmount;}
        return damageAmount * (1.0 + effectiveDamageBonus(transferredDamageBonus) / type.damage());
    }

    double effectiveDamageBonus(double transferredDamageBonus) {
        return DamageScaling.logarithmicBonus(
                transferredDamageBonus,
                config.value(DAMAGE_SOFT_CAP)
        );
    }

    double finalDamageBonus(boolean dragon) {return dragon ? Math.max(0.0, config.value(DRAGON_FINAL_DAMAGE)) : 0.0;}
    double dragonRangeBonus(boolean dragon) {return dragon ? Math.max(0.0, config.value(DRAGON_RANGE_BONUS)) : 0.0;}
    double dragonEvolutionHealth() {return Math.max(0.0, config.value(DRAGON_EVOLUTION));}
    double maximumAttackRange(TowerType type, boolean dragon) {return type.range() + Math.max(0.0, config.value(ATTACK_RANGE_CAP)) + dragonRangeBonus(dragon);}
    double maximumRegeneration() {return Math.max(0.0, config.value(REGENERATION_CAP));}
    double maximumLifeSteal() {return Math.max(0.0, config.value(LIFE_STEAL_CAP));}
    double maximumDamageReduction() {return Math.max(0.0, config.value(DAMAGE_REDUCTION_CAP));}

    double splashRadius(boolean hatchedOrPreview) {
        int unlockedSteps = hatchedOrPreview ? 1 : 0;
        int stacks = transfers.endCrystalCount();
        if (stacks >= positiveThreshold(SPLASH_1)) {unlockedSteps++;}
        if (stacks >= positiveThreshold(SPLASH_2)) {unlockedSteps++;}
        if (stacks >= positiveThreshold(SPLASH_3)) {unlockedSteps++;}
        if (stacks >= positiveThreshold(SPLASH_4)) {unlockedSteps++;}
        return splashRadiusForSteps(unlockedSteps);
    }

    double maximumSplashRadius() {return splashRadiusForSteps(SPLASH_STEP_COUNT);}

    double resolvedSplashDamage(double resolvedOutgoingDamage) {
        if (!Double.isFinite(resolvedOutgoingDamage) || resolvedOutgoingDamage <= 0.0) {return 0.0;}
        return resolvedOutgoingDamage * Math.max(0.0, config.value(SPLASH_DAMAGE_RATIO));
    }

    void resolveAttack(
            EndTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double attemptedDamage,
            double resolvedOutgoingDamage,
            double dealtDamage
    ) {
        applySplashDamage(tower, towerEntity, target, attemptedDamage, resolvedOutgoingDamage);
        heal(towerEntity, dealtDamage * lifeStealRatio());
    }

    int maximumAttackIntervalReduction(TowerType type) {
        int minimumInterval = Math.max(1, config.integer(ATTACK_SPEED_MINIMUM_TICKS));
        int availableReduction = Math.max(0, type.attackIntervalTicks() - minimumInterval);
        if (config.integer(TRANSFER_ATTACK_SPEED_STEP) > 0) {return availableReduction;}
        return Math.min(availableReduction, Math.max(0, config.integer(ATTACK_SPEED_CAP)));
    }

    double attackRangeBonus() {
        return cappedStackBonus(transfers.endCrystalCount(), config.integer(ATTACK_RANGE_STACKS), config.value(ATTACK_RANGE_STEP), config.value(ATTACK_RANGE_CAP));
    }

    double lifeStealRatio() {
        return cappedStackBonus(transfers.shulkerCount(), config.integer(LIFE_STEAL_STACKS), config.value(LIFE_STEAL_STEP), config.value(LIFE_STEAL_CAP));
    }

    double regenerationPerSecond() {
        return cappedStackBonus(transfers.shulkerCount(), config.integer(REGENERATION_STACKS), config.value(REGENERATION_STEP), config.value(REGENERATION_CAP));
    }

    double damageReduction() {
        return cappedStackBonus(transfers.shulkerCount(), config.integer(DAMAGE_REDUCTION_STACKS), config.value(DAMAGE_REDUCTION_STEP), config.value(DAMAGE_REDUCTION_CAP));
    }

    double shulkerDamageReduction(TowerType type) {
        return Math.max(0.0, Math.min(1.0, config.towerDamageReduction(type)));
    }

    int regenerationTicks() {return Math.max(1, config.ticks(REGENERATION_TICKS));}

    double phantomScale(double maxHealth) {
        double baseScale = Math.max(0.0, config.value(PHANTOM_SCALE_BASE));
        double healthInterval = config.value(PHANTOM_SCALE_HEALTH);
        double scalePerInterval = Math.max(0.0, config.value(PHANTOM_SCALE_STEP));
        double scaleCap = Math.max(0.0, config.value(PHANTOM_SCALE_CAP));
        double resolvedMaxHealth = Double.isFinite(maxHealth) ? Math.max(0.0, maxHealth) : 0.0;
        double growth = healthInterval > 0.0 ? resolvedMaxHealth / healthInterval * scalePerInterval : 0.0;
        return Math.min(scaleCap, baseScale + growth);
    }

    private int attackIntervalReduction() {
        int every = Math.max(1, config.integer(ATTACK_SPEED_STACKS));
        long reduction = (transfers.endCrystalCount() / (long) every) * Math.max(0, config.integer(ATTACK_SPEED_STEP));
        return (int) Math.min(Math.max(0, config.integer(ATTACK_SPEED_CAP)), reduction);
    }

    private int roundAttackIntervalReduction() {
        int every = Math.max(1, config.integer(TRANSFER_ATTACK_SPEED_STACKS));
        long reduction = (transfers.roundCompletedCount() / (long) every) * Math.max(0, config.integer(TRANSFER_ATTACK_SPEED_STEP));
        return (int) Math.min(Integer.MAX_VALUE, reduction);
    }

    private int reducedAttackInterval(int baseIntervalTicks, int minimumInterval) {
        long reduction = (long) attackIntervalReduction() + roundAttackIntervalReduction();
        return (int) Math.max(minimumInterval, (long) baseIntervalTicks - reduction);
    }

    private int positiveThreshold(EndConfig.Ability ability) {return Math.max(1, config.integer(ability));}

    private double splashRadiusForSteps(int unlockedSteps) {
        return Math.min(Math.max(0.0, config.value(SPLASH_CAP)), unlockedSteps * Math.max(0.0, config.value(SPLASH_STEP)));
    }

    private void applySplashDamage(
            EndTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double attemptedDamage,
            double resolvedOutgoingDamage
    ) {
        double radius = splashRadius(tower.isHatched());
        double splashDamage = resolvedSplashDamage(resolvedOutgoingDamage);
        double igniteAttackDamage = resolvedSplashDamage(attemptedDamage);
        if (radius <= 0.0 || splashDamage <= 0.0) {return;}
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTarget(AreaEffectIds.tower(tower, "splash"), towerEntity, target, radius, EndVfx.attack(tower.isDragon()));
        TowerAreaDamage.applyResolved(
                tower,
                towerEntity,
                request,
                ignored -> splashDamage,
                true,
                (splashTarget, dealtSplashDamage, killed) -> {
                    heal(towerEntity, dealtSplashDamage * lifeStealRatio());
                    towerEntity.applyIgniteFromBasicAttack(
                            splashTarget,
                            igniteAttackDamage,
                            killed
                    );
                }
        );
    }

    private static void heal(SemionTowerEntity towerEntity, double amount) {
        if (amount > 0.0) {towerEntity.receiveHealing(amount);}
    }

    private static double cappedStackBonus(int stackCount, int everyValue, double perStep, double cap) {
        int every = Math.max(1, everyValue);
        int completedSteps = stackCount / every;
        double value = completedSteps * Math.max(0.0, perStep);
        return Math.min(Math.max(0.0, cap), value);
    }
}
