package kim.biryeong.semiontd.tower.end;

import java.util.List;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

public final class EndConfig {
    static final EndConfig RUNTIME = new EndConfig();

    private EndConfig() {
    }

    DragonRule dragon() {
        return new DragonRule(nonNegative(value(EndAbilityKey.DRAGON_EVOLUTION)), nonNegative(value(EndAbilityKey.DRAGON_FINAL_DAMAGE)), nonNegative(value(EndAbilityKey.DRAGON_RANGE_BONUS)));
    }

    PhantomScaleRule phantomScale() {
        return new PhantomScaleRule(value(EndAbilityKey.PHANTOM_SCALE_HEALTH), nonNegative(value(EndAbilityKey.PHANTOM_SCALE_STEP)), nonNegative(value(EndAbilityKey.PHANTOM_SCALE_BASE)), nonNegative(value(EndAbilityKey.PHANTOM_SCALE_CAP)));
    }

    TransferRule transfer() {
        return new TransferRule(Math.max(1, TowerBalanceRuntime.abilityTicks(EndTowers.CONFIG_ID, EndAbilityKey.TRANSFER_TICKS.key())), nonNegative(value(EndAbilityKey.TRANSFER_HEAL)), nonNegative(value(EndAbilityKey.TRANSFER_HEAL_RATIO)), nonNegative(value(EndAbilityKey.ROUND_HEALTH_RATIO)), nonNegative(value(EndAbilityKey.PERMANENT_HEALTH_RATIO)), nonNegative(value(EndAbilityKey.ROUND_DAMAGE_RATIO)), nonNegative(value(EndAbilityKey.PERMANENT_DAMAGE_RATIO)));
    }

    ScalingRule healthScaling() {
        return new ScalingRule(value(EndAbilityKey.HEALTH_THRESHOLD), value(EndAbilityKey.HEALTH_SCALE));
    }

    ScalingRule damageScaling() {
        return new ScalingRule(value(EndAbilityKey.DAMAGE_THRESHOLD), value(EndAbilityKey.DAMAGE_SCALE));
    }

    StackRule lifeSteal() {
        return stackRule(EndAbilityKey.LIFE_STEAL_STACKS, EndAbilityKey.LIFE_STEAL_STEP, EndAbilityKey.LIFE_STEAL_CAP);
    }

    StackRule damageReduction() {
        return stackRule(EndAbilityKey.DAMAGE_REDUCTION_STACKS, EndAbilityKey.DAMAGE_REDUCTION_STEP, EndAbilityKey.DAMAGE_REDUCTION_CAP);
    }

    StackRule regeneration() {
        return stackRule(EndAbilityKey.REGENERATION_STACKS, EndAbilityKey.REGENERATION_STEP, EndAbilityKey.REGENERATION_CAP);
    }

    StackRule attackRange() {
        return stackRule(EndAbilityKey.ATTACK_RANGE_STACKS, EndAbilityKey.ATTACK_RANGE_STEP, EndAbilityKey.ATTACK_RANGE_CAP);
    }

    SplashRule splash() {
        return new SplashRule(List.of(positiveInteger(EndAbilityKey.SPLASH_1), positiveInteger(EndAbilityKey.SPLASH_2), positiveInteger(EndAbilityKey.SPLASH_3), positiveInteger(EndAbilityKey.SPLASH_4), positiveInteger(EndAbilityKey.SPLASH_5)), nonNegative(value(EndAbilityKey.SPLASH_STEP)), nonNegative(value(EndAbilityKey.SPLASH_CAP)), nonNegative(value(EndAbilityKey.SPLASH_DAMAGE_RATIO)));
    }

    AttackSpeedRule attackSpeed() {
        return new AttackSpeedRule(positiveInteger(EndAbilityKey.ATTACK_SPEED_STACKS), nonNegativeInteger(EndAbilityKey.ATTACK_SPEED_STEP), nonNegativeInteger(EndAbilityKey.ATTACK_SPEED_CAP), positiveInteger(EndAbilityKey.ATTACK_SPEED_MINIMUM_TICKS));
    }

    RoundAttackSpeedRule roundAttackSpeed() {
        return new RoundAttackSpeedRule(positiveInteger(EndAbilityKey.TRANSFER_ATTACK_SPEED_STACKS), nonNegativeInteger(EndAbilityKey.TRANSFER_ATTACK_SPEED_STEP));
    }

    double towerDamageReduction(TowerType type) {
        return Math.clamp(TowerBalanceRuntime.ability(type.id(), "damageReduction"), 0.0, 1.0);
    }

    private StackRule stackRule(EndAbilityKey stacks, EndAbilityKey step, EndAbilityKey cap) {
        return new StackRule(positiveInteger(stacks), nonNegative(value(step)), nonNegative(value(cap)));
    }

    private int positiveInteger(EndAbilityKey ability) {
        return Math.max(1, integer(ability));
    }

    private int nonNegativeInteger(EndAbilityKey ability) {
        return Math.max(0, integer(ability));
    }

    private double value(EndAbilityKey ability) {
        return TowerBalanceRuntime.ability(EndTowers.CONFIG_ID, ability.key());
    }

    private int integer(EndAbilityKey ability) {
        return TowerBalanceRuntime.abilityInt(EndTowers.CONFIG_ID, ability.key());
    }

    private static double nonNegative(double value) {
        return Math.max(0.0, value);
    }

    record DragonRule(double evolutionHealth, double finalDamageBonus, double rangeBonus) {
    }

    record PhantomScaleRule(double healthInterval, double step, double base, double cap) {
    }

    record TransferRule(
            int durationTicks,
            double completionHealing,
            double periodicHealingRatio,
            double roundHealthRatio,
            double permanentHealthRatio,
            double roundDamageRatio,
            double permanentDamageRatio
    ) {
    }

    record ScalingRule(double threshold, double scale) {
    }

    record StackRule(int stacksPerStep, double bonusPerStep, double maximum) {
    }

    record SplashRule(List<Integer> thresholds, double radiusPerStep, double maximumRadius, double damageRatio) {
        SplashRule {thresholds = List.copyOf(thresholds);}
        int unlockedSteps(int stackCount) {
            int unlocked = 0;
            for (int threshold : thresholds) {
                if (stackCount >= threshold) {unlocked++;}
            }
            return unlocked;
        }
    }

    record AttackSpeedRule(int stacksPerStep, int ticksPerStep, int maximumReductionTicks, int minimumIntervalTicks) {
    }

    record RoundAttackSpeedRule(int transfersPerStep, int ticksPerStep) {
    }

}
