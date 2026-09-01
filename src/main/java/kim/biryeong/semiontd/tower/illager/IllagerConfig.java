package kim.biryeong.semiontd.tower.illager;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

public final class IllagerConfig {
    public static final String CONFIG_ID = "illager_raid";
    static final IllagerConfig RUNTIME = new IllagerConfig();

    private IllagerConfig() {
    }

    double value(TowerType type, IllagerAbilityKey ability) {
        return TowerBalanceRuntime.ability(type.id(), ability.key());
    }

    int ticks(TowerType type, IllagerAbilityKey ability) {
        return TowerBalanceRuntime.abilityTicks(type.id(), ability.key());
    }

    double global(IllagerAbilityKey ability) {
        return TowerBalanceRuntime.ability(CONFIG_ID, ability.key());
    }

    int globalInt(IllagerAbilityKey ability) {
        return TowerBalanceRuntime.abilityInt(CONFIG_ID, ability.key());
    }

    int globalTicks(IllagerAbilityKey ability) {
        return TowerBalanceRuntime.abilityTicks(CONFIG_ID, ability.key());
    }
}
