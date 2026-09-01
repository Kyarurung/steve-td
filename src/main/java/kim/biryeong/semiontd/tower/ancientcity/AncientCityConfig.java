package kim.biryeong.semiontd.tower.ancientcity;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

public final class AncientCityConfig {
    public static final String GLOBAL_ID = "ancient_city_global";
    public static final AncientCityConfig RUNTIME = new AncientCityConfig();

    private AncientCityConfig() {
    }

    public double global(AncientCityAbilityKey key) {
        return TowerBalanceRuntime.ability(GLOBAL_ID, key.key());
    }

    public int globalInt(AncientCityAbilityKey key) {
        return TowerBalanceRuntime.abilityInt(GLOBAL_ID, key.key());
    }

    public double value(TowerType type, AncientCityAbilityKey key) {
        return TowerBalanceRuntime.ability(type.id(), key.key());
    }

    public int integer(TowerType type, AncientCityAbilityKey key) {
        return TowerBalanceRuntime.abilityInt(type.id(), key.key());
    }

    public int ticks(TowerType type, AncientCityAbilityKey key) {
        return TowerBalanceRuntime.abilityTicks(type.id(), key.key());
    }
}
