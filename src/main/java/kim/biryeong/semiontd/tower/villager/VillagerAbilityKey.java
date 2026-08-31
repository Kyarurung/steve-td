package kim.biryeong.semiontd.tower.villager;

enum VillagerAbilityKey {
    RADIUS("radius"),
    HEAL_AMOUNT("healAmount"),
    WEAPON_BUFF("weaponBuff"),
    BUFF_DURATION_TICKS("buffDurationTicks"),
    DAMAGE_REDUCTION("damageReduction"),
    SUPPORT_BLOCK_TICKS("supportBlockTicks"),
    TANK_BONUS("tankBonus"),
    NON_WAVE_BONUS("nonWaveBonus"),
    WAVE_BONUS("waveBonus"),
    STACK_DAMAGE("stackDamage"),
    STACK_DAMAGE_CAP("stackDamageCap"),
    EXPLOSION_RADIUS("explosionRadius"),
    THORN_RADIUS("thornRadius"),
    THORN_DAMAGE("thornDamage"),
    THORN_COOLDOWN_TICKS("thornCooldownTicks"),
    MAX_SURVIVAL_STACKS("maxSurvivalStacks"),
    HEALTH_BONUS_PER_SURVIVED_ROUND("healthBonusPerSurvivedRound"),
    BONUS_PER_SURVIVED_ROUND("bonusPerSurvivedRound"),
    EXTRA_ATTACK_EVERY("extraAttackEvery"),
    SPLASH_RADIUS("splashRadius"),
    SPLASH_DAMAGE_RATIO("splashDamageRatio");

    private final String key;

    VillagerAbilityKey(String key) {
        this.key = key;
    }

    String key() {
        return key;
    }
}
