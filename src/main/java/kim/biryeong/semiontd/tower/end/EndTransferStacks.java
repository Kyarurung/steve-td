package kim.biryeong.semiontd.tower.end;

import kim.biryeong.semiontd.tower.TowerType;

record EndTransferStacks(int shulkerCount, int endCrystalCount, int roundCompletedCount) {
    static final EndTransferStacks EMPTY = new EndTransferStacks(0, 0, 0);

    EndTransferStacks {
        shulkerCount = Math.max(0, shulkerCount);
        endCrystalCount = Math.max(0, endCrystalCount);
        roundCompletedCount = Math.max(0, roundCompletedCount);
    }

    EndTransferStacks recordCompletion(TowerType sourceType) {
        int tier = EndTowers.transferTier(sourceType);
        return EndTowers.isShulkerLine(sourceType)
                ? new EndTransferStacks(
                        saturatedAdd(shulkerCount, tier),
                        endCrystalCount,
                        saturatedAdd(roundCompletedCount, 1)
                )
                : new EndTransferStacks(
                        shulkerCount,
                        saturatedAdd(endCrystalCount, tier),
                        saturatedAdd(roundCompletedCount, 1)
                );
    }

    EndTransferStacks resetRound() {
        return roundCompletedCount == 0
                ? this
                : new EndTransferStacks(shulkerCount, endCrystalCount, 0);
    }

    double shulkerBonus(EndConfig.StackRule rule) {
        return cappedBonus(shulkerCount, rule);
    }

    double endCrystalBonus(EndConfig.StackRule rule) {
        return cappedBonus(endCrystalCount, rule);
    }

    long attackIntervalReduction(
            EndConfig.AttackSpeedRule permanentRule,
            EndConfig.RoundAttackSpeedRule roundRule
    ) {
        long permanent = (endCrystalCount / (long) permanentRule.stacksPerStep())
                * permanentRule.ticksPerStep();
        long cappedPermanent = Math.min(permanentRule.maximumReductionTicks(), permanent);
        long round = (roundCompletedCount / (long) roundRule.transfersPerStep())
                * roundRule.ticksPerStep();
        return cappedPermanent + Math.min(Integer.MAX_VALUE, round);
    }

    private static double cappedBonus(int stackCount, EndConfig.StackRule rule) {
        int completedSteps = stackCount / rule.stacksPerStep();
        return Math.min(rule.maximum(), completedSteps * rule.bonusPerStep());
    }

    private static int saturatedAdd(int value, int increment) {
        if (increment <= 0 || value == Integer.MAX_VALUE) {
            return value;
        }
        return value > Integer.MAX_VALUE - increment ? Integer.MAX_VALUE : value + increment;
    }
}
