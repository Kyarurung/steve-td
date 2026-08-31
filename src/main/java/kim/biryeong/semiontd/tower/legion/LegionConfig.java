package kim.biryeong.semiontd.tower.legion;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

public final class LegionConfig {
    static final LegionConfig RUNTIME = new LegionConfig();

    private LegionConfig() {
    }

    double value(TowerType type, LegionAbilityKey ability) {
        return TowerBalanceRuntime.ability(type.id(), ability.key());
    }

    double value(TowerType type, LegionAbilityKey ability, double fallback) {
        return TowerBalanceRuntime.ability(type.id(), ability.key(), fallback);
    }

    int integer(TowerType type, LegionAbilityKey ability) {
        return TowerBalanceRuntime.abilityInt(type.id(), ability.key());
    }

    int integer(TowerType type, LegionAbilityKey ability, int fallback) {
        return TowerBalanceRuntime.abilityInt(type.id(), ability.key(), fallback);
    }

    int ticks(TowerType type, LegionAbilityKey ability) {
        return TowerBalanceRuntime.abilityTicks(type.id(), ability.key());
    }

    int ticks(TowerType type, LegionAbilityKey ability, int fallback) {
        return TowerBalanceRuntime.abilityTicks(type.id(), ability.key(), fallback);
    }

    int illusionSpawnSpreadTicks() {
        return TowerBalanceRuntime.illusionCloneSpawnSpreadTicks();
    }

    int illusionMaximumSpawnsPerTick() {
        return TowerBalanceRuntime.illusionCloneMaxSpawnsPerTick();
    }

    LegionIllusionProfile illusionProfile(TowerType type, LegionIllusionProfile defaults) {
        return new LegionIllusionProfile(
                integer(type, LegionAbilityKey.CLONE_COUNT, defaults.cloneCount()),
                ticks(type, LegionAbilityKey.CLONE_DURATION_TICKS, defaults.durationTicks()),
                value(type, LegionAbilityKey.CLONE_HEALTH_RATIO, defaults.healthRatio()),
                value(type, LegionAbilityKey.CLONE_DAMAGE_RATIO, defaults.damageRatio()),
                value(type, LegionAbilityKey.CLONE_RANGE_RATIO, defaults.rangeRatio()),
                value(type, LegionAbilityKey.CLONE_ATTACK_INTERVAL_MULTIPLIER, defaults.attackIntervalMultiplier()),
                value(type, LegionAbilityKey.CLONE_SPAWN_RADIUS, defaults.spawnRadius()),
                integer(type, LegionAbilityKey.CLONE_AGGRO_PRIORITY_BONUS, defaults.aggroPriorityBonus())
        );
    }
}
