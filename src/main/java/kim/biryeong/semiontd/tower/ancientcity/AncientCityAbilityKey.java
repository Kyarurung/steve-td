package kim.biryeong.semiontd.tower.ancientcity;

public enum AncientCityAbilityKey {
    INITIAL_SCULK("initialSculk"),
    WAVE_START_SPREAD("waveStartSpread"),
    DEATH_SPREAD_CAP_PER_ROUND("deathSpreadCapPerRound"),
    MAX_SCULK("maxSculk"),
    FINAL_DEFENSE_SEED_COUNT("finalDefenseSeedCount"),
    RESONANCE_FULL_AT("resonanceFullAt"),
    RESONANCE_DAMAGE_CAP("resonanceDamageCap"),
    MAX_COMBINED_DAMAGE_BONUS("maxCombinedDamageBonus"),
    INCOME_MAGIC_DAMAGE_MULTIPLIER("incomeMagicDamageMultiplier"),
    SCULK_DAMAGE_REDUCTION("sculkDamageReduction"),
    RETALIATION_COOLDOWN_TICKS("retaliationCooldownTicks"),
    RETALIATION_RADIUS("retaliationRadius"),
    MAGIC_DAMAGE("magicDamage"),
    MAGIC_COOLDOWN_TICKS("magicCooldownTicks"),
    MARK_DURATION_TICKS("markDurationTicks"),
    MARK_DAMAGE_BONUS("markDamageBonus"),
    MAGIC_RADIUS("magicRadius"),
    SLOW_DURATION_TICKS("slowDurationTicks"),
    SLOW_MAGNITUDE("slowMagnitude"),
    TARGET_COUNT("targetCount"),
    SCULK_EXTRA_TARGETS("sculkExtraTargets"),
    SECONDARY_DAMAGE_RATIO("secondaryDamageRatio");

    private final String key;

    AncientCityAbilityKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
