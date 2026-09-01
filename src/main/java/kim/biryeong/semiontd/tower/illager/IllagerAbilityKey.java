package kim.biryeong.semiontd.tower.illager;

enum IllagerAbilityKey {
    GAUGE_MAX("gaugeMax"),
    WAVE_KILL_GAUGE("waveKillGauge"),
    INCOME_KILL_GAUGE("incomeKillGauge"),
    MARKED_KILL_BONUS_GAUGE("markedKillBonusGauge"),
    ILLAGER_TOWER_DEATH_GAUGE("illagerTowerDeathGauge"),
    ATTACK_SPEED_PERCENT_PER_TOWER("attackSpeedPercentPerTower"),
    DAMAGE_PERCENT_PER_TOWER("damagePercentPerTower"),
    ATTACK_SPEED_BONUS_CAP("attackSpeedBonusCap"),
    DAMAGE_BONUS_CAP("damageBonusCap"),
    TIMED_EFFECT_DURATION_TICKS("timedEffectDurationTicks"),
    RAID_DAMAGE_REDUCTION("raidDamageReduction"),
    RAID_MARKED_DAMAGE_BONUS("raidMarkedDamageBonus"),
    INCOME_DAMAGE_BONUS("incomeDamageBonus"),
    RAID_INCOME_DAMAGE_BONUS("raidIncomeDamageBonus"),
    SPLASH_RADIUS("splashRadius"),
    SPLASH_DAMAGE_RATIO("splashDamageRatio"),
    RAID_SPLASH_RADIUS_BONUS("raidSplashRadiusBonus"),
    RAID_SPLASH_DAMAGE_RATIO_BONUS("raidSplashDamageRatioBonus"),
    MARK_DURATION_TICKS("markDurationTicks"),
    MARK_DAMAGE_TAKEN_BONUS("markDamageTakenBonus"),
    RAID_MARK_DURATION_BONUS_TICKS("raidMarkDurationBonusTicks"),
    RAID_MARK_DAMAGE_TAKEN_BONUS("raidMarkDamageTakenBonus"),
    FORCE_TARGET_RADIUS("forceTargetRadius"),
    RAID_FORCE_TARGET_RADIUS_BONUS("raidForceTargetRadiusBonus"),
    RAID_LOW_HEALTH_MARK_DAMAGE_TAKEN_BONUS("raidLowHealthMarkDamageTakenBonus"),
    RAID_HIGH_HEALTH_MARK_DAMAGE_TAKEN_BONUS("raidHighHealthMarkDamageTakenBonus");

    private final String key;

    IllagerAbilityKey(String key) {
        this.key = key;
    }

    String key() {
        return key;
    }
}
