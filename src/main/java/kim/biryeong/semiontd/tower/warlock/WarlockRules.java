package kim.biryeong.semiontd.tower.warlock;

import kim.biryeong.semiontd.tower.LogarithmicScaling;

final class WarlockRules {
    private WarlockRules() {
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
        static final ScalingRule NONE = new ScalingRule(0.0, 0.0);

        double value(double rawBonus) {
            if (!enabled()) {
                return finiteNonNegative(rawBonus);
            }
            return LogarithmicScaling.logarithmicBonus(rawBonus, threshold, scale);
        }

        private boolean enabled() {
            return threshold > 0.0 && scale > 0.0;
        }

        private static double finiteNonNegative(double value) {
            return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
        }
    }

    record StackRule(int sacrificesPerStep, double bonusPerStep, double maximum) {
        static final StackRule NONE = new StackRule(1, 0.0, 0.0);

        double value(int sacrificeCount) {
            double stacked = (Math.max(0, sacrificeCount) / sacrificesPerStep) * bonusPerStep;
            return Math.min(maximum, stacked);
        }
    }

    record SplashRule(int sacrificesPerStep, double radiusPerStep, double maximumRadius, double damageRatio) {
        static final SplashRule NONE = new SplashRule(1, 0.0, 0.0, 0.0);

        double radius(int sacrificeCount) {
            return Math.min(maximumRadius, (Math.max(0, sacrificeCount) / sacrificesPerStep) * radiusPerStep);
        }
    }

    record DefenseRule(int sacrificesPerStep, double bonusPerStep, double maximum, boolean fixedAfterThreshold) {
        static final DefenseRule NONE = new DefenseRule(1, 0.0, 0.0, false);

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
        static final PassiveRule NONE = new PassiveRule(0.0, 0.0, 0.0, 0.0);

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
        static final AwakeningBonus NONE = new AwakeningBonus(0.0, 0.0, 1, 0.0, 0.0);
    }

    record CombatRule(int minimumIntervalTicks, int maximumIntervalReductionTicks, double meleeReductionPerSacrifice) {
        int meleeIntervalReduction(int roundSacrificeCount) {
            int reduction = (int) Math.floor(Math.max(0, roundSacrificeCount) * meleeReductionPerSacrifice);
            return Math.min(maximumIntervalReductionTicks, reduction);
        }
    }

    record AwakeningRule(double healthThreshold, AwakeningBonus bonus) {
        boolean canActivate(boolean unlocked, double currentHealthRatio, boolean lastSurvivingTower) {
            if (!Double.isFinite(currentHealthRatio) || !Double.isFinite(healthThreshold)) {
                return false;
            }
            return unlocked
                    && lastSurvivingTower
                    && currentHealthRatio > 0.0
                    && currentHealthRatio <= Math.max(0.0, healthThreshold);
        }
    }

    record DeathEffectRule(WarlockPath path, double radius, int durationTicks, double magnitude) {
    }
}
