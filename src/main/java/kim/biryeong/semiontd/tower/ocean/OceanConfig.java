package kim.biryeong.semiontd.tower.ocean;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

public final class OceanConfig {
    public static final String GLOBAL_ID = "ocean_global";
    public static final OceanConfig RUNTIME = new OceanConfig();

    private OceanConfig() {
    }

    public double global(OceanAbilityKey key) {
        return TowerBalanceRuntime.ability(GLOBAL_ID, key.key());
    }

    public double value(TowerType type, OceanAbilityKey key) {
        return TowerBalanceRuntime.ability(type.id(), key.key());
    }

    public int ticks(TowerType type, OceanAbilityKey key) {
        return TowerBalanceRuntime.abilityTicks(type.id(), key.key());
    }
}
