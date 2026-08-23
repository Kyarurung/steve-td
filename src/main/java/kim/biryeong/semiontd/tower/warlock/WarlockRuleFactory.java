package kim.biryeong.semiontd.tower.warlock;

import static kim.biryeong.semiontd.tower.warlock.WarlockAbilityKey.*;

import kim.biryeong.semiontd.tower.TowerType;

final class WarlockRuleFactory {
    private final WarlockConfigReader values;

    WarlockRuleFactory(WarlockConfigReader values) {
        this.values = values;
    }

    WarlockRules.PathRule path(WarlockPath path) {
        return switch (path) {
            case BASE -> basePath();
            case RANGED -> rangedPath();
            case MELEE -> meleePath();
        };
    }

    private WarlockRules.PathRule basePath() {
        return WarlockRules.PathRule.builder()
                .sacrifice(sacrifice(BASE_RADIUS))
                .absorption(new WarlockRules.AbsorptionRule(
                        0.0,
                        0.0,
                        values.nonNegative(BASE_PERMANENT_HEALTH),
                        values.nonNegative(BASE_PERMANENT_DAMAGE)
                ))
                .build();
    }

    private WarlockRules.PathRule rangedPath() {
        return WarlockRules.PathRule.builder()
                .sacrifice(sacrifice(SACRIFICE_RADIUS))
                .absorption(new WarlockRules.AbsorptionRule(
                        values.ratio(RANGED_THRESHOLD),
                        values.nonNegative(RANGED_ROUND_STAT),
                        values.nonNegative(RANGED_PERMANENT_HEALTH),
                        values.nonNegative(RANGED_PERMANENT_DAMAGE)
                ))
                .healthScaling(scaling(RANGED_HEALTH_THRESHOLD, RANGED_HEALTH_SCALE))
                .damageScaling(scaling(RANGED_DAMAGE_THRESHOLD, RANGED_DAMAGE_SCALE))
                .lifeSteal(stackRule(RANGED_LIFE_EVERY, RANGED_LIFE_STEP, RANGED_LIFE_CAP))
                .splash(splashRule(RANGED_SPLASH_EVERY, RANGED_SPLASH_STEP, RANGED_SPLASH_CAP, RANGED_SPLASH_DAMAGE))
                .defense(WarlockRules.DefenseRule.fixed(
                        values.requiredAfter(RANGED_DEFENSE_THRESHOLD),
                        values.ratio(RANGED_DEFENSE)
                ))
                .passive(passiveRule(
                        RANGED_PET_HEALTH,
                        RANGED_PET_HEALTH_CAP,
                        RANGED_PET_DAMAGE,
                        RANGED_PET_DAMAGE_CAP
                ))
                .incomeDebuffResistance(values.ratio(RANGED_INCOME_DEBUFF_RESISTANCE))
                .awakeningBonus(rangedAwakeningBonus())
                .build();
    }

    private WarlockRules.PathRule meleePath() {
        return WarlockRules.PathRule.builder()
                .sacrifice(sacrifice(SACRIFICE_RADIUS))
                .absorption(new WarlockRules.AbsorptionRule(
                        values.ratio(MELEE_THRESHOLD),
                        values.nonNegative(MELEE_ROUND_STAT),
                        values.nonNegative(MELEE_PERMANENT_HEALTH),
                        values.nonNegative(MELEE_PERMANENT_DAMAGE)
                ))
                .healthScaling(scaling(MELEE_HEALTH_THRESHOLD, MELEE_HEALTH_SCALE))
                .damageScaling(scaling(MELEE_DAMAGE_THRESHOLD, MELEE_DAMAGE_SCALE))
                .lifeSteal(new WarlockRules.StackRule(
                        1,
                        values.nonNegative(MELEE_LIFE_STEP),
                        values.nonNegative(MELEE_LIFE_CAP)
                ))
                .splash(splashRule(MELEE_SPLASH_STEP, MELEE_SPLASH_CAP, MELEE_SPLASH_DAMAGE))
                .defense(WarlockRules.DefenseRule.stacking(
                        values.positiveInteger(MELEE_DEFENSE_EVERY),
                        values.ratio(MELEE_DEFENSE_STEP),
                        values.ratio(MELEE_DEFENSE_CAP)
                ))
                .passive(passiveRule(
                        MELEE_PET_HEALTH,
                        MELEE_PET_HEALTH_CAP,
                        MELEE_PET_DAMAGE,
                        MELEE_PET_DAMAGE_CAP
                ))
                .incomeDebuffResistance(values.ratio(MELEE_INCOME_DEBUFF_RESISTANCE))
                .awakeningBonus(meleeAwakeningBonus())
                .build();
    }

    WarlockRules.CombatRule combat() {
        return new WarlockRules.CombatRule(
                values.positiveInteger(MIN_INTERVAL),
                values.nonNegativeInteger(SPEED_CAP),
                values.nonNegative(MELEE_SPEED_STEP)
        );
    }

    WarlockRules.AwakeningRule awakening(WarlockPath path) {
        return new WarlockRules.AwakeningRule(
                values.ratio(AWAKENING_THRESHOLD),
                path(path).awakeningBonus()
        );
    }

    int requiredAwakeningKills() {
        return values.nonNegativeInteger(AWAKENING_KILLS);
    }

    WarlockRules.DeathEffectRule deathEffect(TowerType type) {
        WarlockPath path = WarlockPath.fromTower(type);
        return new WarlockRules.DeathEffectRule(
                path,
                values.towerNonNegative(type, "deathEffectRadius"),
                values.towerPositiveTicks(type, "deathEffectDurationTicks"),
                path == WarlockPath.MELEE
                        ? values.towerRatio(type, "towerDamageTakenBonus")
                        : values.towerRatio(type, "attackSpeedReduction")
        );
    }

    private WarlockRules.SacrificeRule sacrifice(WarlockAbilityKey radius) {
        double configuredRadius = values.value(radius);
        return new WarlockRules.SacrificeRule(
                configuredRadius <= 0.0 ? Double.MAX_VALUE : configuredRadius,
                values.nonNegative(ABSORPTION_HEAL)
        );
    }

    private WarlockRules.ScalingRule scaling(WarlockAbilityKey threshold, WarlockAbilityKey scale) {
        return new WarlockRules.ScalingRule(values.value(threshold), values.value(scale));
    }

    private WarlockRules.StackRule stackRule(WarlockAbilityKey every, WarlockAbilityKey step, WarlockAbilityKey cap) {
        return new WarlockRules.StackRule(
                values.positiveInteger(every),
                values.nonNegative(step),
                values.nonNegative(cap)
        );
    }

    private WarlockRules.SplashRule splashRule(
            WarlockAbilityKey every,
            WarlockAbilityKey step,
            WarlockAbilityKey cap,
            WarlockAbilityKey damage
    ) {
        return new WarlockRules.SplashRule(
                values.positiveInteger(every),
                values.nonNegative(step),
                values.nonNegative(cap),
                values.nonNegative(damage)
        );
    }

    private WarlockRules.SplashRule splashRule(
            WarlockAbilityKey step,
            WarlockAbilityKey cap,
            WarlockAbilityKey damage
    ) {
        return new WarlockRules.SplashRule(
                1,
                values.nonNegative(step),
                values.nonNegative(cap),
                values.nonNegative(damage)
        );
    }

    private WarlockRules.AwakeningBonus rangedAwakeningBonus() {
        return new WarlockRules.AwakeningBonus(
                values.nonNegative(RANGED_AWAKENING_HEAL),
                values.nonNegative(RANGED_AWAKENING_REGENERATION),
                values.positiveInteger(RANGED_AWAKENING_REGENERATION_TICKS),
                0.0,
                0.0
        );
    }

    private WarlockRules.AwakeningBonus meleeAwakeningBonus() {
        return new WarlockRules.AwakeningBonus(
                values.nonNegative(MELEE_AWAKENING_HEAL),
                0.0,
                1,
                values.nonNegative(MELEE_AWAKENING_DAMAGE),
                values.nonNegative(MELEE_AWAKENING_MOVE_SPEED)
        );
    }

    private WarlockRules.PassiveRule passiveRule(
            WarlockAbilityKey healthStep,
            WarlockAbilityKey healthCap,
            WarlockAbilityKey damageStep,
            WarlockAbilityKey damageCap
    ) {
        return new WarlockRules.PassiveRule(
                values.nonNegative(healthStep),
                values.nonNegative(healthCap),
                values.nonNegative(damageStep),
                values.nonNegative(damageCap)
        );
    }
}
