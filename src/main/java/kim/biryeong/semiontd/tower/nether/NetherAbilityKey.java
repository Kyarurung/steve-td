package kim.biryeong.semiontd.tower.nether;

enum NetherAbilityKey {
    NETHER_DECAY("netherDecayMaxHealthRatioPerSecond"),
    ZOMBIE_DECAY("zombieDecayMaxHealthRatioPerSecond"),
    ZOMBIE_REVIVE_HEALTH("zombieReviveHealthRatio"),
    LOW_HEALTH_THRESHOLD("lowHealthThreshold"),
    CRITICAL_HEALTH_THRESHOLD("criticalHealthThreshold"),
    DAMAGE_PER_MISSING_HEALTH("damagePerMissingHealth"),
    LOW_HEALTH_DAMAGE_CAP("lowHealthDamageBonusCap"),
    NETHER_LIFE_STEAL("netherLifeStealRatio"),
    ZOMBIE_LIFE_STEAL("zombieLifeStealRatio"),
    LIFE_STEAL_PER_MISSING_HEALTH("lifeStealPerMissingHealth"),
    LIFE_STEAL_BONUS_CAP("lifeStealBonusCap"),
    EFFECT_REFRESH_TICKS("effectRefreshTicks"),
    LIFE_STEAL_BONUS("lifeStealBonus"),
    DECAY_REDUCTION_RATIO("decayReductionRatio"),
    DECAY_REDUCTION_TICKS("decayReductionTicks"),
    INCOME_DAMAGE_BONUS("incomeDamageBonus"),
    KILL_DAMAGE_BONUS("killDamageBonus"),
    KILL_DAMAGE_BONUS_TICKS("killDamageBonusTicks"),
    ZOMBIE_ATTACK_SPEED_BONUS("zombieAttackSpeedBonus"),
    TANK_DAMAGE_BONUS("tankDamageBonus"),
    TANK_LIFE_STEAL_BONUS("tankLifeStealBonus"),
    HIGH_HEALTH_THRESHOLD("highHealthThreshold"),
    ZOMBIE_TRANSITION_DAMAGE_BONUS("zombieTransitionDamageBonus"),
    ZOMBIE_TRANSITION_DAMAGE_BONUS_TICKS("zombieTransitionDamageBonusTicks"),
    SPLASH_RADIUS("splashRadius"),
    SPLASH_DAMAGE_RATIO("splashDamageRatio"),
    CRITICAL_DAMAGE_REDUCTION("criticalDamageReduction"),
    MISSING_HEALTH_ATTACK_SPEED_CAP("missingHealthAttackSpeedBonusCap"),
    ZOMBIE_MISSING_HEALTH_ATTACK_SPEED_CAP("zombieMissingHealthAttackSpeedBonusCap"),
    ZOMBIE_SPLASH_RADIUS_BONUS("zombieSplashRadiusBonus"),
    PULSE_RADIUS("pulseRadius"),
    PULSE_DAMAGE_RATIO("pulseDamageRatio"),
    PULSE_INTERVAL_TICKS("pulseIntervalTicks"),
    ZOMBIE_TRANSITION_PULSE_RADIUS("zombieTransitionPulseRadius"),
    ZOMBIE_TRANSITION_PULSE_DAMAGE_RATIO("zombieTransitionPulseDamageRatio"),
    EXTRA_ATTACK_EVERY("extraAttackEvery"),
    SECONDARY_RANGE("secondaryRange"),
    EXTRA_ATTACK_DAMAGE_RATIO("extraAttackDamageRatio"),
    LOW_HEALTH_SPLASH_RADIUS_BONUS("lowHealthSplashRadiusBonus"),
    CRITICAL_MARK_DAMAGE_TAKEN_BONUS("criticalMarkDamageTakenBonus"),
    MARK_DURATION_TICKS("markDurationTicks"),
    LOW_TARGET_HEALTH_THRESHOLD("lowTargetHealthThreshold"),
    LOW_TARGET_DAMAGE_BONUS("lowTargetDamageBonus"),
    CRITICAL_KILL_LIFE_STEAL_RATIO("criticalKillLifeStealRatio"),
    MARK_DAMAGE_TAKEN_BONUS("markDamageTakenBonus"),
    MAX_MARK_STACKS("maxMarkStacks"),
    ZOMBIE_MARK_DAMAGE_TAKEN_BONUS("zombieMarkDamageTakenBonus"),
    HIGH_HEALTH_DAMAGE_BONUS("highHealthDamageBonus"),
    CRITICAL_SPLASH_RADIUS("criticalSplashRadius"),
    CRITICAL_SPLASH_DAMAGE_RATIO("criticalSplashDamageRatio"),
    ZOMBIE_EXECUTE_THRESHOLD("zombieExecuteThreshold"),
    ZOMBIE_EXECUTE_DAMAGE_BONUS("zombieExecuteDamageBonus");

    private final String key;

    NetherAbilityKey(String key) {
        this.key = key;
    }

    String key() {
        return key;
    }
}
