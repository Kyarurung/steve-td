package kim.biryeong.semiontd.tower.warlock;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

public final class WarlockConfig {
    public static final WarlockConfig RUNTIME = new WarlockConfig();

    private WarlockConfig() {
    }

    PathRule path(WarlockPath path) {
        return switch (path) {
            case BASE -> new PathRule(
                    sacrifice(Ability.BASE_RADIUS),
                    new AbsorptionRule(0.0, 0.0, nonNegative(value(Ability.BASE_PERMANENT_HEALTH)), nonNegative(value(Ability.BASE_PERMANENT_DAMAGE))),
                    ScalingRule.NONE,
                    ScalingRule.NONE,
                    StackRule.NONE,
                    SplashRule.NONE,
                    DefenseRule.NONE,
                    PassiveRule.NONE,
                    0.0,
                    AwakeningBonus.NONE
            );
            case RANGED -> new PathRule(
                    sacrifice(Ability.SACRIFICE_RADIUS),
                    new AbsorptionRule(
                            ratio(Ability.RANGED_THRESHOLD),
                            nonNegative(value(Ability.RANGED_ROUND_STAT)),
                            nonNegative(value(Ability.RANGED_PERMANENT_HEALTH)),
                            nonNegative(value(Ability.RANGED_PERMANENT_DAMAGE))
                    ),
                    scaling(Ability.RANGED_HEALTH_THRESHOLD, Ability.RANGED_HEALTH_SCALE),
                    scaling(Ability.RANGED_DAMAGE_THRESHOLD, Ability.RANGED_DAMAGE_SCALE),
                    stackRule(Ability.RANGED_LIFE_EVERY, Ability.RANGED_LIFE_STEP, Ability.RANGED_LIFE_CAP),
                    splashRule(Ability.RANGED_SPLASH_EVERY, Ability.RANGED_SPLASH_STEP, Ability.RANGED_SPLASH_CAP, Ability.RANGED_SPLASH_DAMAGE),
                    DefenseRule.fixed(requiredAfter(Ability.RANGED_DEFENSE_THRESHOLD), ratio(Ability.RANGED_DEFENSE)),
                    passiveRule(
                            Ability.RANGED_PET_HEALTH,
                            Ability.RANGED_PET_HEALTH_CAP,
                            Ability.RANGED_PET_DAMAGE,
                            Ability.RANGED_PET_DAMAGE_CAP
                    ),
                    ratio(Ability.RANGED_INCOME_DEBUFF_RESISTANCE),
                    new AwakeningBonus(
                            nonNegative(value(Ability.RANGED_AWAKENING_HEAL)),
                            nonNegative(value(Ability.RANGED_AWAKENING_REGENERATION)),
                            positiveInteger(Ability.RANGED_AWAKENING_REGENERATION_TICKS),
                            0.0,
                            0.0
                    )
            );
            case MELEE -> new PathRule(
                    sacrifice(Ability.SACRIFICE_RADIUS),
                    new AbsorptionRule(
                            ratio(Ability.MELEE_THRESHOLD),
                            nonNegative(value(Ability.MELEE_ROUND_STAT)),
                            nonNegative(value(Ability.MELEE_PERMANENT_HEALTH)),
                            nonNegative(value(Ability.MELEE_PERMANENT_DAMAGE))
                    ),
                    scaling(Ability.MELEE_HEALTH_THRESHOLD, Ability.MELEE_HEALTH_SCALE),
                    scaling(Ability.MELEE_DAMAGE_THRESHOLD, Ability.MELEE_DAMAGE_SCALE),
                    new StackRule(1, nonNegative(value(Ability.MELEE_LIFE_STEP)), nonNegative(value(Ability.MELEE_LIFE_CAP))),
                    splashRule(null, Ability.MELEE_SPLASH_STEP, Ability.MELEE_SPLASH_CAP, Ability.MELEE_SPLASH_DAMAGE),
                    DefenseRule.stacking(
                            positiveInteger(Ability.MELEE_DEFENSE_EVERY),
                            ratio(Ability.MELEE_DEFENSE_STEP),
                            ratio(Ability.MELEE_DEFENSE_CAP)
                    ),
                    passiveRule(
                            Ability.MELEE_PET_HEALTH,
                            Ability.MELEE_PET_HEALTH_CAP,
                            Ability.MELEE_PET_DAMAGE,
                            Ability.MELEE_PET_DAMAGE_CAP
                    ),
                    ratio(Ability.MELEE_INCOME_DEBUFF_RESISTANCE),
                    new AwakeningBonus(
                            nonNegative(value(Ability.MELEE_AWAKENING_HEAL)),
                            0.0,
                            1,
                            nonNegative(value(Ability.MELEE_AWAKENING_DAMAGE)),
                            nonNegative(value(Ability.MELEE_AWAKENING_MOVE_SPEED))
                    )
            );
        };
    }

    CombatRule combat() {
        return new CombatRule(
                positiveInteger(Ability.MIN_INTERVAL),
                nonNegativeInteger(Ability.SPEED_CAP),
                nonNegative(value(Ability.MELEE_SPEED_STEP))
        );
    }

    AwakeningRule awakening(WarlockPath path) {
        return new AwakeningRule(
                requiredAwakeningKills(),
                ratio(Ability.AWAKENING_THRESHOLD),
                path(path).awakeningBonus()
        );
    }

    public int requiredAwakeningKills() {
        return nonNegativeInteger(Ability.AWAKENING_KILLS);
    }

    DeathEffectRule deathEffect(TowerType type) {
        WarlockPath path = WarlockPath.fromTower(type);
        return new DeathEffectRule(
                path,
                nonNegative(TowerBalanceRuntime.ability(type.id(), "deathEffectRadius")),
                Math.max(1, TowerBalanceRuntime.abilityTicks(type.id(), "deathEffectDurationTicks")),
                path == WarlockPath.MELEE
                        ? ratio(type, "towerDamageTakenBonus")
                        : ratio(type, "attackSpeedReduction")
        );
    }

    private SacrificeRule sacrifice(Ability radius) {
        double configuredRadius = value(radius);
        return new SacrificeRule(
                configuredRadius <= 0.0 ? Double.MAX_VALUE : configuredRadius,
                nonNegative(value(Ability.ABSORPTION_HEAL))
        );
    }

    private ScalingRule scaling(Ability threshold, Ability scale) {
        return new ScalingRule(value(threshold), value(scale));
    }

    private StackRule stackRule(Ability every, Ability step, Ability cap) {
        return new StackRule(positiveInteger(every), nonNegative(value(step)), nonNegative(value(cap)));
    }

    private SplashRule splashRule(Ability every, Ability step, Ability cap, Ability damage) {
        return new SplashRule(
                every == null ? 1 : positiveInteger(every),
                nonNegative(value(step)),
                nonNegative(value(cap)),
                nonNegative(value(damage))
        );
    }

    private PassiveRule passiveRule(Ability healthStep, Ability healthCap, Ability damageStep, Ability damageCap) {
        return new PassiveRule(
                nonNegative(value(healthStep)),
                nonNegative(value(healthCap)),
                nonNegative(value(damageStep)),
                nonNegative(value(damageCap))
        );
    }

    private int requiredAfter(Ability threshold) {
        int configured = nonNegativeInteger(threshold);
        return configured == Integer.MAX_VALUE ? configured : configured + 1;
    }

    private double value(Ability ability) {
        return TowerBalanceRuntime.ability(ability.configId(), ability.key());
    }

    private int integer(Ability ability) {
        return TowerBalanceRuntime.abilityInt(ability.configId(), ability.key());
    }

    private double ratio(Ability ability) {
        return Math.clamp(value(ability), 0.0, 1.0);
    }

    private static double ratio(TowerType type, String key) {
        return Math.clamp(TowerBalanceRuntime.ability(type.id(), key), 0.0, 1.0);
    }

    private int positiveInteger(Ability ability) {
        return Math.max(1, integer(ability));
    }

    private int nonNegativeInteger(Ability ability) {
        return Math.max(0, integer(ability));
    }

    private static double nonNegative(double value) {
        return Math.max(0.0, value);
    }

    record PathRule(
            SacrificeRule sacrifice,
            AbsorptionRule absorption,
            ScalingRule healthScaling,
            ScalingRule damageScaling,
            StackRule lifeSteal,
            SplashRule splash,
            DefenseRule defense,
            PassiveRule passive,
            double incomeDebuffResistance,
            AwakeningBonus awakeningBonus
    ) {
    }

    record SacrificeRule(double radius, double completionHealing) {
    }

    record AbsorptionRule(
            double triggerHealthRatio,
            double roundStatRatio,
            double permanentHealthRatio,
            double permanentDamageRatio
    ) {
    }

    record ScalingRule(double threshold, double scale) {
        private static final ScalingRule NONE = new ScalingRule(0.0, 0.0);

        boolean enabled() {
            return threshold > 0.0 && scale > 0.0;
        }
    }

    record StackRule(int sacrificesPerStep, double bonusPerStep, double maximum) {
        private static final StackRule NONE = new StackRule(1, 0.0, 0.0);
    }

    record SplashRule(int sacrificesPerStep, double radiusPerStep, double maximumRadius, double damageRatio) {
        private static final SplashRule NONE = new SplashRule(1, 0.0, 0.0, 0.0);

        double radius(int sacrificeCount) {
            return Math.min(maximumRadius, (Math.max(0, sacrificeCount) / sacrificesPerStep) * radiusPerStep);
        }
    }

    record DefenseRule(int sacrificesPerStep, double bonusPerStep, double maximum, boolean fixedAfterThreshold) {
        private static final DefenseRule NONE = new DefenseRule(1, 0.0, 0.0, false);

        static DefenseRule fixed(int requiredSacrifices, double magnitude) {
            return new DefenseRule(requiredSacrifices, magnitude, magnitude, true);
        }

        static DefenseRule stacking(int sacrificesPerStep, double bonusPerStep, double maximum) {
            return new DefenseRule(sacrificesPerStep, bonusPerStep, maximum, false);
        }

        double value(int sacrificeCount) {
            if (fixedAfterThreshold) {
                return sacrificeCount >= sacrificesPerStep ? maximum : 0.0;
            }
            return Math.min(maximum, (Math.max(0, sacrificeCount) / sacrificesPerStep) * bonusPerStep);
        }
    }

    record PassiveRule(double healthPerTower, double maximumHealth, double damagePerTower, double maximumDamage) {
        private static final PassiveRule NONE = new PassiveRule(0.0, 0.0, 0.0, 0.0);

        double healthBonus(int towerCount) {
            return Math.min(maximumHealth, Math.max(0, towerCount) * healthPerTower);
        }

        double damageBonus(int towerCount) {
            return Math.min(maximumDamage, Math.max(0, towerCount) * damagePerTower);
        }
    }

    record AwakeningBonus(
            double healing,
            double regenerationPerSecond,
            int regenerationIntervalTicks,
            double attackDamage,
            double movementSpeed
    ) {
        private static final AwakeningBonus NONE = new AwakeningBonus(0.0, 0.0, 1, 0.0, 0.0);
    }

    record CombatRule(int minimumIntervalTicks, int maximumIntervalReductionTicks, double meleeReductionPerSacrifice) {
    }

    record AwakeningRule(int requiredKills, double healthThreshold, AwakeningBonus bonus) {
    }

    record DeathEffectRule(WarlockPath path, double radius, int durationTicks, double magnitude) {
    }

    public enum Ability {
        SACRIFICE_RADIUS(Scope.GLOBAL, "sacrificeRadius"), ABSORPTION_HEAL(Scope.GLOBAL, "absorptionHeal"),
        MIN_INTERVAL(Scope.GLOBAL, "minInterval"), SPEED_CAP(Scope.GLOBAL, "speedCap"),
        AWAKENING_KILLS(Scope.GLOBAL, "awakeningKills"), AWAKENING_THRESHOLD(Scope.GLOBAL, "awakeningThreshold"),
        BASE_RADIUS(Scope.BASE, "sacrificeRadius"), BASE_PERMANENT_HEALTH(Scope.BASE, "permanentHealth"), BASE_PERMANENT_DAMAGE(Scope.BASE, "permanentDamage"),
        RANGED_THRESHOLD(Scope.RANGED, "threshold"), RANGED_ROUND_STAT(Scope.RANGED, "roundStat"),
        RANGED_PERMANENT_HEALTH(Scope.RANGED, "permanentHealth"), RANGED_HEALTH_THRESHOLD(Scope.RANGED, "healthThreshold"),
        RANGED_HEALTH_SCALE(Scope.RANGED, "healthScale"), RANGED_PERMANENT_DAMAGE(Scope.RANGED, "permanentDamage"),
        RANGED_DAMAGE_THRESHOLD(Scope.RANGED, "damageThreshold"), RANGED_DAMAGE_SCALE(Scope.RANGED, "damageScale"),
        RANGED_LIFE_EVERY(Scope.RANGED, "lifeEvery"), RANGED_LIFE_STEP(Scope.RANGED, "lifeStep"), RANGED_LIFE_CAP(Scope.RANGED, "lifeCap"),
        RANGED_INCOME_DEBUFF_RESISTANCE(Scope.RANGED, "incomeDebuffResistance"),
        RANGED_SPLASH_EVERY(Scope.RANGED, "splashEvery"), RANGED_SPLASH_STEP(Scope.RANGED, "splashStep"),
        RANGED_SPLASH_CAP(Scope.RANGED, "splashCap"), RANGED_SPLASH_DAMAGE(Scope.RANGED, "splashDamage"),
        RANGED_DEFENSE_THRESHOLD(Scope.RANGED, "defenseThreshold"), RANGED_DEFENSE(Scope.RANGED, "defense"),
        RANGED_PET_HEALTH(Scope.RANGED, "petHealth"), RANGED_PET_HEALTH_CAP(Scope.RANGED, "petHealthCap"),
        RANGED_PET_DAMAGE(Scope.RANGED, "petDamage"), RANGED_PET_DAMAGE_CAP(Scope.RANGED, "petDamageCap"),
        RANGED_AWAKENING_HEAL(Scope.RANGED, "awakeningHeal"), RANGED_AWAKENING_REGENERATION(Scope.RANGED, "awakeningRegeneration"),
        RANGED_AWAKENING_REGENERATION_TICKS(Scope.RANGED, "awakeningRegenerationTicks"),
        MELEE_THRESHOLD(Scope.MELEE, "threshold"), MELEE_ROUND_STAT(Scope.MELEE, "roundStat"),
        MELEE_PERMANENT_HEALTH(Scope.MELEE, "permanentHealth"), MELEE_HEALTH_THRESHOLD(Scope.MELEE, "healthThreshold"),
        MELEE_HEALTH_SCALE(Scope.MELEE, "healthScale"), MELEE_PERMANENT_DAMAGE(Scope.MELEE, "permanentDamage"),
        MELEE_DAMAGE_THRESHOLD(Scope.MELEE, "damageThreshold"), MELEE_DAMAGE_SCALE(Scope.MELEE, "damageScale"),
        MELEE_LIFE_STEP(Scope.MELEE, "lifeStep"), MELEE_LIFE_CAP(Scope.MELEE, "lifeCap"),
        MELEE_INCOME_DEBUFF_RESISTANCE(Scope.MELEE, "incomeDebuffResistance"), MELEE_SPEED_STEP(Scope.MELEE, "speedStep"),
        MELEE_SPLASH_STEP(Scope.MELEE, "splashStep"), MELEE_SPLASH_CAP(Scope.MELEE, "splashCap"), MELEE_SPLASH_DAMAGE(Scope.MELEE, "splashDamage"),
        MELEE_DEFENSE_EVERY(Scope.MELEE, "defenseEvery"), MELEE_DEFENSE_STEP(Scope.MELEE, "defenseStep"), MELEE_DEFENSE_CAP(Scope.MELEE, "defenseCap"),
        MELEE_PET_HEALTH(Scope.MELEE, "petHealth"), MELEE_PET_HEALTH_CAP(Scope.MELEE, "petHealthCap"),
        MELEE_PET_DAMAGE(Scope.MELEE, "petDamage"), MELEE_PET_DAMAGE_CAP(Scope.MELEE, "petDamageCap"),
        MELEE_AWAKENING_HEAL(Scope.MELEE, "awakeningHeal"), MELEE_AWAKENING_DAMAGE(Scope.MELEE, "awakeningDamage"),
        MELEE_AWAKENING_MOVE_SPEED(Scope.MELEE, "awakeningMoveSpeed");

        private final Scope scope;
        private final String key;

        Ability(Scope scope, String key) {
            this.scope = scope;
            this.key = key;
        }

        String configId() {
            return switch (scope) {
                case GLOBAL -> WarlockTowers.CONFIG_ID;
                case BASE -> WarlockTowers.BASE_WARLOCK_TOWER.id();
                case RANGED -> WarlockTowers.RANGED_WARLOCK_TOWER.id();
                case MELEE -> WarlockTowers.MELEE_WARLOCK_TOWER.id();
            };
        }

        String key() {
            return key;
        }
    }

    private enum Scope {
        GLOBAL, BASE, RANGED, MELEE
    }
}
