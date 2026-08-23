package kim.biryeong.semiontd.tower.end;

import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;

final class EndCombat {
    private static final int REGENERATION_TICKS = 20;
    private static final int SPLASH_STEP_COUNT = 5;
    private final EndConfig config;
    private final EndTransferController transfers;

    EndCombat(EndConfig config, EndTransferController transfers) {
        this.config = config;
        this.transfers = transfers;
    }

    int attackInterval(TowerType type) {
        return reducedAttackInterval(type.attackIntervalTicks(), config.attackSpeed().minimumIntervalTicks());
    }

    int adjustAttackInterval(int baseIntervalTicks) {
        return reducedAttackInterval(baseIntervalTicks, config.attackSpeed().minimumIntervalTicks());
    }

    double attackRange(TowerType type, EndTowerState state) {
        return type.range() + attackRangeBonus() + dragonRangeBonus(state);
    }

    double modifyAttackDamage(TowerType type, double transferredDamageBonus, double damageAmount) {
        if (type.damage() <= 0.0) {return damageAmount;}
        return damageAmount * (1.0 + transferredDamageBonus / type.damage());
    }

    double dragonEvolutionHealth() {
        return config.dragon().evolutionHealth();
    }

    double phantomScale(double maxHealth) {
        EndConfig.PhantomScaleRule rule = config.phantomScale();
        double resolvedMaxHealth = Double.isFinite(maxHealth) ? Math.max(0.0, maxHealth) : 0.0;
        double growth = rule.healthInterval() > 0.0 ? resolvedMaxHealth / rule.healthInterval() * rule.step() : 0.0;
        return Math.min(rule.cap(), rule.base() + growth);
    }

    double lifeStealRatio() {
        return transfers.stacks().shulkerBonus(config.lifeSteal());
    }

    double maximumLifeSteal() {
        return config.lifeSteal().maximum();
    }

    double damageReduction() {
        return transfers.stacks().shulkerBonus(config.damageReduction());
    }

    double maximumDamageReduction() {
        return config.damageReduction().maximum();
    }

    double shulkerDamageReduction(TowerType type) {
        return config.towerDamageReduction(type);
    }

    double regenerationPerSecond() {
        return transfers.stacks().shulkerBonus(config.regeneration());
    }

    double maximumRegeneration() {
        return config.regeneration().maximum();
    }

    int regenerationTicks() {return REGENERATION_TICKS;}

    double splashRadius(EndTowerState state) {
        if (!state.hatched()) {return 0.0;}
        return splashRadiusForSteps(config.splash().unlockedSteps(transfers.stacks().endCrystalCount()));
    }

    double maximumSplashRadius() {return splashRadiusForSteps(SPLASH_STEP_COUNT);}

    double resolvedSplashDamage(double resolvedOutgoingDamage) {
        if (!Double.isFinite(resolvedOutgoingDamage) || resolvedOutgoingDamage <= 0.0) {return 0.0;}
        return resolvedOutgoingDamage * config.splash().damageRatio();
    }

    int maximumAttackIntervalReduction(TowerType type) {
        EndConfig.AttackSpeedRule attackSpeed = config.attackSpeed();
        int availableReduction = Math.max(0, type.attackIntervalTicks() - attackSpeed.minimumIntervalTicks());
        if (config.roundAttackSpeed().ticksPerStep() > 0) {return availableReduction;}
        return Math.min(availableReduction, attackSpeed.maximumReductionTicks());
    }

    double attackRangeBonus() {
        return transfers.stacks().endCrystalBonus(config.attackRange());
    }

    double maximumAttackRange(TowerType type, EndTowerState state) {
        return type.range() + config.attackRange().maximum() + dragonRangeBonus(state);
    }

    double finalDamageBonus(EndTowerState state) {
        return state == EndTowerState.DRAGON ? config.dragon().finalDamageBonus() : 0.0;
    }

    double dragonRangeBonus(EndTowerState state) {
        return state == EndTowerState.DRAGON ? config.dragon().rangeBonus() : 0.0;
    }

    void resolveAttack(EndTower tower, SemionTowerEntity towerEntity, SemionMonsterEntity target, double attemptedDamage, double resolvedOutgoingDamage, double dealtDamage) {
        applySplashDamage(tower, towerEntity, target, attemptedDamage, resolvedOutgoingDamage);
        heal(towerEntity, dealtDamage * lifeStealRatio());
    }

    private double splashRadiusForSteps(int unlockedSteps) {
        EndConfig.SplashRule splash = config.splash();
        return Math.min(splash.maximumRadius(), unlockedSteps * splash.radiusPerStep());
    }

    private void applySplashDamage(
            EndTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double attemptedDamage,
            double resolvedOutgoingDamage
    ) {
        double radius = splashRadius(tower.state());
        double splashDamage = resolvedSplashDamage(resolvedOutgoingDamage);
        double igniteAttackDamage = resolvedSplashDamage(attemptedDamage);
        if (radius <= 0.0 || splashDamage <= 0.0) {return;}
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTarget(
                AreaEffectIds.tower(tower, "splash"),
                towerEntity,
                target,
                radius,
                EndVfx.attack(tower.state(), true)
        );
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

    private int reducedAttackInterval(int baseIntervalTicks, int minimumInterval) {
        long reduction = transfers.stacks().attackIntervalReduction(
                config.attackSpeed(),
                config.roundAttackSpeed()
        );
        return (int) Math.max(minimumInterval, (long) baseIntervalTicks - reduction);
    }

    private static void heal(SemionTowerEntity towerEntity, double amount) {
        if (amount > 0.0) {towerEntity.healTarget(towerEntity, amount);}
    }

}
