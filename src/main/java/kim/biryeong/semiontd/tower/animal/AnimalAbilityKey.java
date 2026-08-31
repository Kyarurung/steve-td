package kim.biryeong.semiontd.tower.animal;

enum AnimalAbilityKey {
    MAX_STACKS("maxStacks"),
    HEALTH_PER_STACK("healthPerStack"),
    DAMAGE_PER_STACK("damagePerStack"),
    DAMAGE_REDUCTION("damageReduction"),
    SPLASH_RADIUS("splashRadius"),
    SPLASH_DAMAGE_RATIO("splashDamageRatio"),
    INTERVAL_REDUCTION_PER_STACK("intervalReductionPerStack"),
    MAX_STACK_EXTRA_INTERVAL_REDUCTION("maxStackExtraIntervalReduction"),
    MAX_STACK_DAMAGE_BONUS("maxStackDamageBonus"),
    EXTRA_ATTACK_DAMAGE_RATIO("extraAttackDamageRatio"),
    EXECUTE_DAMAGE_BONUS_RATIO("executeDamageBonusRatio"),
    EXECUTE_DAMAGE_BONUS_PER_STACK("executeDamageBonusPerStack"),
    EXECUTE_HEALTH_THRESHOLD("executeHealthThreshold"),
    EXECUTE_THRESHOLD_PER_STACK("executeThresholdPerStack"),
    MAX_EXECUTE_HEALTH_THRESHOLD("maxExecuteHealthThreshold"),
    KILL_BONUS_DAMAGE("killBonusDamage"),
    KILL_BONUS_DAMAGE_CAP("killBonusDamageCap"),
    LEADER_AURA_RADIUS("leaderAuraRadius"),
    LEADER_MAX_HEALTH_BONUS("leaderMaxHealthBonus"),
    LEADER_DAMAGE_REDUCTION_BONUS("leaderDamageReductionBonus"),
    LEADER_ATTACK_INTERVAL_REDUCTION_TICKS("leaderAttackIntervalReductionTicks"),
    LEADER_SPLASH_DAMAGE_RATIO_BONUS("leaderSplashDamageRatioBonus"),
    LEADER_DAMAGE_BONUS("leaderDamageBonus"),
    LEADER_RANGE_BONUS("leaderRangeBonus"),
    LEADER_EXECUTE_DAMAGE_BONUS("leaderExecuteDamageBonus"),
    LEADER_EXECUTE_THRESHOLD_BONUS("leaderExecuteThresholdBonus"),
    LEADER_EXECUTE_THRESHOLD_CAP("leaderExecuteThresholdCap");

    private final String key;

    AnimalAbilityKey(String key) {
        this.key = key;
    }

    String key() {
        return key;
    }
}
