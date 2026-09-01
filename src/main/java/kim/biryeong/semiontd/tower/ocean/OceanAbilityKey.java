package kim.biryeong.semiontd.tower.ocean;

public enum OceanAbilityKey {
    INITIAL_WATER("initialWater"),
    WATER_SCALE("waterScale"),
    WATER_SOFT_CAP("waterSoftCap"),
    WATER_SUPPLY_STOP_THRESHOLD("waterSupplyStopThreshold"),
    WATER_SUPPLY_STACK_DECAY("waterSupplyStackDecay"),
    DEHYDRATED_DAMAGE_MULTIPLIER("dehydratedDamageMultiplier"),
    DEHYDRATED_ATTACK_SPEED_REDUCTION("dehydratedAttackSpeedReduction"),
    DEHYDRATION_MAX_HEALTH_DAMAGE_PER_SECOND("dehydrationMaxHealthDamagePerSecond"),
    EMPOWERED_ABILITY_WATER_THRESHOLD("empoweredAbilityWaterThreshold"),
    EMPOWERED_ABILITY_WATER_COST_MULTIPLIER("empoweredAbilityWaterCostMultiplier"),
    EMPOWERED_ABILITY_EFFECT_MULTIPLIER("empoweredAbilityEffectMultiplier"),
    INCOME_COEFFICIENT_MULTIPLIER("incomeCoefficientMultiplier"),
    SUPPLY_RADIUS("supplyRadius"),
    WAVE_START_WATER("waveStartWater"),
    WATER_PER_SUPPLY("waterPerSupply"),
    SUPPLY_INTERVAL_TICKS("supplyIntervalTicks"),
    ATTACK_WATER_COST("attackWaterCost"),
    WATER_DAMAGE_COEFFICIENT("waterDamageCoefficient"),
    INCOME_WATER_COST("incomeWaterCost"),
    SPLASH_WATER_COST("splashWaterCost"),
    SPLASH_RADIUS("splashRadius"),
    SPLASH_DAMAGE_RATIO("splashDamageRatio"),
    ABILITY_WATER_COST("abilityWaterCost"),
    SUPPORT_RADIUS("supportRadius"),
    BUFF_DURATION_TICKS("buffDurationTicks"),
    DAMAGE_BONUS("damageBonus"),
    ATTACK_SPEED_BONUS("attackSpeedBonus"),
    SUPPORT_INTERVAL_TICKS("supportIntervalTicks"),
    HEAL_RADIUS("healRadius"),
    HEAL_AMOUNT("healAmount"),
    HEAL_INTERVAL_TICKS("healIntervalTicks"),
    DAMAGE_REDUCTION("damageReduction"),
    TRANSFER_CAP("transferCap"),
    TRANSFER_RADIUS("transferRadius"),
    TRANSFER_WATER_COST("transferWaterCost"),
    TRANSFER_COOLDOWN_TICKS("transferCooldownTicks");

    private final String key;

    OceanAbilityKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
