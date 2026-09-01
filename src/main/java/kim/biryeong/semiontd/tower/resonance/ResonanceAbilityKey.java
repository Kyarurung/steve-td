package kim.biryeong.semiontd.tower.resonance;

enum ResonanceAbilityKey {
    LINK_RANGE("linkRange"),
    MAX_LINKS_PER_TOWER("maxLinksPerTower"),
    MAX_RESONANCE_LEVEL("maxResonanceLevel"),
    LEVEL_1_REQUIRED_LINKS("level1RequiredLinks"),
    LEVEL_2_REQUIRED_LINKS("level2RequiredLinks"),
    LEVEL_3_REQUIRED_LINKS("level3RequiredLinks"),
    FOCUS_LEVEL_1_ATTACK_SPEED_BONUS("focusLevel1AttackSpeedBonus"),
    FOCUS_LEVEL_2_ATTACK_SPEED_BONUS("focusLevel2AttackSpeedBonus"),
    FOCUS_LEVEL_3_ATTACK_SPEED_BONUS("focusLevel3AttackSpeedBonus"),
    FOCUS_LEVEL_2_DAMAGE_BONUS("focusLevel2DamageBonus"),
    FOCUS_LEVEL_3_DAMAGE_BONUS("focusLevel3DamageBonus"),
    FOCUS_STRIKE_EVERY_ATTACKS("focusStrikeEveryAttacks"),
    FOCUS_STRIKE_DAMAGE_RATIO("focusStrikeDamageRatio"),
    WAVE_LEVEL_1_ATTACK_SPEED_BONUS("waveLevel1AttackSpeedBonus"),
    WAVE_LEVEL_2_SPLASH_RADIUS("waveLevel2SplashRadius"),
    WAVE_LEVEL_2_SPLASH_DAMAGE_RATIO("waveLevel2SplashDamageRatio"),
    WAVE_LEVEL_3_SPLASH_RADIUS("waveLevel3SplashRadius"),
    WAVE_LEVEL_3_SPLASH_DAMAGE_RATIO("waveLevel3SplashDamageRatio"),
    WAVE_PULSE_EVERY_ATTACKS("wavePulseEveryAttacks"),
    WAVE_PULSE_RADIUS("wavePulseRadius"),
    WAVE_PULSE_DAMAGE_RATIO("wavePulseDamageRatio"),
    FROST_LEVEL_1_SLOW_TICKS("frostLevel1SlowTicks"),
    FROST_LEVEL_2_SLOW_TICKS("frostLevel2SlowTicks"),
    FROST_LEVEL_3_SLOW_TICKS("frostLevel3SlowTicks"),
    FROST_LEVEL_1_SLOW_MAGNITUDE("frostLevel1SlowMagnitude"),
    FROST_LEVEL_2_SLOW_MAGNITUDE("frostLevel2SlowMagnitude"),
    FROST_LEVEL_3_SLOW_MAGNITUDE("frostLevel3SlowMagnitude"),
    FROST_LEVEL_1_ATTACK_SPEED_REDUCTION("frostLevel1AttackSpeedReductionMagnitude"),
    FROST_LEVEL_2_ATTACK_SPEED_REDUCTION("frostLevel2AttackSpeedReductionMagnitude"),
    FROST_LEVEL_3_ATTACK_SPEED_REDUCTION("frostLevel3AttackSpeedReductionMagnitude"),
    FROST_AURA_RANGE("frostAuraRange"),
    FROST_LEVEL_2_AURA_DAMAGE_BONUS("frostLevel2AuraDamageVsSlowedBonus"),
    FROST_LEVEL_3_AURA_DAMAGE_BONUS("frostLevel3AuraDamageVsSlowedBonus"),
    FROST_PULSE_EVERY_ATTACKS("frostPulseEveryAttacks"),
    FROST_PULSE_RADIUS("frostPulseRadius"),
    FROST_PULSE_DAMAGE_RATIO("frostPulseDamageRatio"),
    FROST_PULSE_SLOW_TICKS("frostPulseSlowTicks"),
    FROST_PULSE_SLOW_MAGNITUDE("frostPulseSlowMagnitude"),
    FROST_PULSE_ATTACK_SPEED_REDUCTION("frostPulseAttackSpeedReductionMagnitude"),
    BLOOM_LEVEL_1_DAMAGE_REDUCTION("bloomLevel1DamageReduction"),
    BLOOM_LEVEL_2_DAMAGE_REDUCTION("bloomLevel2DamageReduction"),
    BLOOM_LEVEL_3_DAMAGE_REDUCTION("bloomLevel3DamageReduction"),
    BLOOM_AURA_RANGE("bloomAuraRange"),
    BLOOM_LEVEL_2_AURA_ATTACK_SPEED_BONUS("bloomLevel2AuraAttackSpeedBonus"),
    BLOOM_LEVEL_3_AURA_ATTACK_SPEED_BONUS("bloomLevel3AuraAttackSpeedBonus"),
    BLOOM_PROTECT_EVERY_ATTACKS("bloomProtectEveryAttacks"),
    BLOOM_PROTECT_RADIUS("bloomProtectRadius"),
    BLOOM_PROTECT_HEAL_RATIO("bloomProtectHealRatio"),
    BLOOM_PROTECT_DAMAGE_REDUCTION("bloomProtectDamageReduction"),
    BLOOM_PROTECT_TICKS("bloomProtectTicks");

    private final String key;

    ResonanceAbilityKey(String key) {
        this.key = key;
    }

    String key() {
        return key;
    }
}
