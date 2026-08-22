package kim.biryeong.semiontd.tower.end;

import java.util.List;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

public final class EndConfig {
    static final EndConfig RUNTIME = new EndConfig();

    private EndConfig() {
    }

    DragonRule dragon() {
        return new DragonRule(nonNegative(value(Ability.DRAGON_EVOLUTION)), nonNegative(value(Ability.DRAGON_FINAL_DAMAGE)), nonNegative(value(Ability.DRAGON_RANGE_BONUS)));
    }

    PhantomScaleRule phantomScale() {
        return new PhantomScaleRule(value(Ability.PHANTOM_SCALE_HEALTH), nonNegative(value(Ability.PHANTOM_SCALE_STEP)), nonNegative(value(Ability.PHANTOM_SCALE_BASE)), nonNegative(value(Ability.PHANTOM_SCALE_CAP)));
    }

    TransferRule transfer() {
        return new TransferRule(Math.max(1, TowerBalanceRuntime.abilityTicks(EndTowers.CONFIG_ID, Ability.TRANSFER_TICKS.key())), nonNegative(value(Ability.TRANSFER_HEAL)), nonNegative(value(Ability.TRANSFER_HEAL_RATIO)), nonNegative(value(Ability.ROUND_HEALTH_RATIO)), nonNegative(value(Ability.PERMANENT_HEALTH_RATIO)), nonNegative(value(Ability.ROUND_DAMAGE_RATIO)), nonNegative(value(Ability.PERMANENT_DAMAGE_RATIO)));
    }

    ScalingRule healthScaling() {
        return new ScalingRule(value(Ability.HEALTH_THRESHOLD), value(Ability.HEALTH_SCALE));
    }

    ScalingRule damageScaling() {
        return new ScalingRule(value(Ability.DAMAGE_THRESHOLD), value(Ability.DAMAGE_SCALE));
    }

    StackRule lifeSteal() {
        return stackRule(Ability.LIFE_STEAL_STACKS, Ability.LIFE_STEAL_STEP, Ability.LIFE_STEAL_CAP);
    }

    StackRule damageReduction() {
        return stackRule(Ability.DAMAGE_REDUCTION_STACKS, Ability.DAMAGE_REDUCTION_STEP, Ability.DAMAGE_REDUCTION_CAP);
    }

    StackRule regeneration() {
        return stackRule(Ability.REGENERATION_STACKS, Ability.REGENERATION_STEP, Ability.REGENERATION_CAP);
    }

    StackRule attackRange() {
        return stackRule(Ability.ATTACK_RANGE_STACKS, Ability.ATTACK_RANGE_STEP, Ability.ATTACK_RANGE_CAP);
    }

    SplashRule splash() {
        return new SplashRule(List.of(positiveInteger(Ability.SPLASH_1), positiveInteger(Ability.SPLASH_2), positiveInteger(Ability.SPLASH_3), positiveInteger(Ability.SPLASH_4), positiveInteger(Ability.SPLASH_5)), nonNegative(value(Ability.SPLASH_STEP)), nonNegative(value(Ability.SPLASH_CAP)), nonNegative(value(Ability.SPLASH_DAMAGE_RATIO)));
    }

    AttackSpeedRule attackSpeed() {
        return new AttackSpeedRule(positiveInteger(Ability.ATTACK_SPEED_STACKS), nonNegativeInteger(Ability.ATTACK_SPEED_STEP), nonNegativeInteger(Ability.ATTACK_SPEED_CAP), positiveInteger(Ability.ATTACK_SPEED_MINIMUM_TICKS));
    }

    RoundAttackSpeedRule roundAttackSpeed() {
        return new RoundAttackSpeedRule(positiveInteger(Ability.TRANSFER_ATTACK_SPEED_STACKS), nonNegativeInteger(Ability.TRANSFER_ATTACK_SPEED_STEP));
    }

    double towerDamageReduction(TowerType type) {
        return Math.clamp(TowerBalanceRuntime.ability(type.id(), "damageReduction"), 0.0, 1.0);
    }

    private StackRule stackRule(Ability stacks, Ability step, Ability cap) {
        return new StackRule(positiveInteger(stacks), nonNegative(value(step)), nonNegative(value(cap)));
    }

    private int positiveInteger(Ability ability) {
        return Math.max(1, integer(ability));
    }

    private int nonNegativeInteger(Ability ability) {
        return Math.max(0, integer(ability));
    }

    private double value(Ability ability) {
        return TowerBalanceRuntime.ability(EndTowers.CONFIG_ID, ability.key());
    }

    private int integer(Ability ability) {
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

    public enum Ability {
        DRAGON_EVOLUTION("dragonEvolution"),
        PHANTOM_SCALE_HEALTH("phantomScaleHealth"),
        PHANTOM_SCALE_STEP("phantomScaleStep"),
        PHANTOM_SCALE_BASE("phantomScaleBase"),
        PHANTOM_SCALE_CAP("phantomScaleCap"),
        TRANSFER_TICKS("transferTicks"),
        TRANSFER_HEAL("transferHeal"),
        TRANSFER_HEAL_RATIO("transferHealRatio"),
        ROUND_HEALTH_RATIO("roundHealthRatio"),
        PERMANENT_HEALTH_RATIO("permanentHealthRatio"),
        HEALTH_THRESHOLD("healthThreshold"),
        HEALTH_SCALE("healthScale"),
        ROUND_DAMAGE_RATIO("roundDamageRatio"),
        PERMANENT_DAMAGE_RATIO("permanentDamageRatio"),
        DAMAGE_THRESHOLD("damageThreshold"),
        DAMAGE_SCALE("damageScale"),
        LIFE_STEAL_STACKS("lifeStealStacks"),
        LIFE_STEAL_STEP("lifeStealStep"),
        LIFE_STEAL_CAP("lifeStealCap"),
        DAMAGE_REDUCTION_STACKS("damageReductionStacks"),
        DAMAGE_REDUCTION_STEP("damageReductionStep"),
        DAMAGE_REDUCTION_CAP("damageReductionCap"),
        REGENERATION_STACKS("regenerationStacks"),
        REGENERATION_STEP("regenerationStep"),
        REGENERATION_CAP("regenerationCap"),
        SPLASH_1("splash1"),
        SPLASH_2("splash2"),
        SPLASH_3("splash3"),
        SPLASH_4("splash4"),
        SPLASH_5("splash5"),
        SPLASH_STEP("splashStep"),
        SPLASH_CAP("splashCap"),
        SPLASH_DAMAGE_RATIO("splashDamageRatio"),
        ATTACK_SPEED_STACKS("attackSpeedStacks"),
        ATTACK_SPEED_STEP("attackSpeedStep"),
        ATTACK_SPEED_CAP("attackSpeedCap"),
        ATTACK_SPEED_MINIMUM_TICKS("attackSpeedMinimumTicks"),
        TRANSFER_ATTACK_SPEED_STACKS("transferAttackSpeedStacks"),
        TRANSFER_ATTACK_SPEED_STEP("transferAttackSpeedStep"),
        ATTACK_RANGE_STACKS("attackRangeStacks"),
        ATTACK_RANGE_STEP("attackRangeStep"),
        ATTACK_RANGE_CAP("attackRangeCap"),
        DRAGON_FINAL_DAMAGE("dragonFinalDamage"),
        DRAGON_RANGE_BONUS("dragonRangeBonus");

        private final String key;
        Ability(String key) {
            this.key = key;
        }
        public String key() {return key;}
    }
}
