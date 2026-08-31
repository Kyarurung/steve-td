package kim.biryeong.semiontd.tower.undead;

enum UndeadAbilityKey {
    DAMAGE_BOOST_TICKS("damageBoostTicks"),
    LIFE_STEAL_RATIO("lifeStealRatio"),
    KILL_DAMAGE_BOOST("killDamageBoost"),
    DAMAGE_BOOST_ON_HIT("damageBoostOnHit"),
    THORN_RADIUS("thornRadius"),
    THORN_COOLDOWN_TICKS("thornCooldownTicks"),
    THORN_HEAL_PER_HIT("thornHealPerHit"),
    LAST_STAND_TICKS("lastStandTicks"),
    SCAN_INTERVAL_TICKS("scanIntervalTicks"),
    RADIUS("radius"),
    ATTACK_DAMAGE_REDUCTION("attackDamageReduction"),
    DEBUFF_DURATION_TICKS("debuffDurationTicks"),
    TOWER_DAMAGE_TAKEN_BONUS("towerDamageTakenBonus"),
    STACK_DAMAGE("stackDamage"),
    STACK_DAMAGE_CAP("stackDamageCap"),
    EXTRA_TARGET_RANGE_BONUS("extraTargetRangeBonus"),
    EXTRA_TARGETS("extraTargets"),
    DEATH_STACK_RANGE("deathStackRange"),
    DAMAGE_PER_STACK("damagePerStack"),
    HEALTH_PER_STACK("healthPerStack"),
    STACK_CAP("stackCap"),
    SPLASH_RADIUS("splashRadius"),
    SPLASH_DAMAGE_RATIO("splashDamageRatio");

    private final String key;

    UndeadAbilityKey(String key) {
        this.key = key;
    }

    String key() {
        return key;
    }
}
