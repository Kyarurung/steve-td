package kim.biryeong.semiontd.tower.legion;

enum LegionAbilityKey {
    SPLASH_RADIUS("splashRadius"),
    SPLASH_DAMAGE_RATIO("splashDamageRatio"),
    ATTACK_STACK_BONUS("attackStackBonus"),
    MAX_ATTACK_STACKS("maxAttackStacks"),
    RADIUS("radius"),
    DAMAGE_BONUS("damageBonus"),
    DAMAGE_REDUCTION("damageReduction"),
    CLONE_DAMAGE_BONUS("cloneDamageBonus"),
    CLONE_DAMAGE_REDUCTION("cloneDamageReduction"),
    BUFF_DURATION_TICKS("buffDurationTicks"),
    MAX_STACKS("maxStacks"),
    MAX_SWARM_STACKS("maxSwarmStacks"),
    MAX_POISON_STACKS("maxPoisonStacks"),
    POISON_STACKS_PER_SWARM_STACK("poisonStacksPerSwarmStack"),
    POISON_DAMAGE_PER_STACK("poisonDamagePerStack"),
    POISON_DAMAGE_PER_SWARM_STACK("poisonDamagePerSwarmStack"),
    POISON_DURATION_TICKS("poisonDurationTicks"),
    POISON_TICK_INTERVAL_TICKS("poisonTickIntervalTicks"),
    CLONE_COUNT("cloneCount"),
    CLONE_DURATION_TICKS("cloneDurationTicks"),
    CLONE_HEALTH_RATIO("cloneHealthRatio"),
    CLONE_DAMAGE_RATIO("cloneDamageRatio"),
    CLONE_RANGE_RATIO("cloneRangeRatio"),
    CLONE_ATTACK_INTERVAL_MULTIPLIER("cloneAttackIntervalMultiplier"),
    CLONE_SPAWN_RADIUS("cloneSpawnRadius"),
    CLONE_AGGRO_PRIORITY_BONUS("cloneAggroPriorityBonus"),
    REGEN_INTERVAL_TICKS("regenIntervalTicks"),
    REGEN_AMOUNT("regenAmount");

    private final String key;

    LegionAbilityKey(String key) {
        this.key = key;
    }

    String key() {
        return key;
    }
}
