package kim.biryeong.semiontd.tower.nether;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

public final class NetherConfig {
    public static final String CONFIG_ID = "nether_global";
    static final NetherConfig RUNTIME = new NetherConfig();

    private NetherConfig() {
    }

    double value(TowerType type, NetherAbilityKey ability) {
        return TowerBalanceRuntime.ability(type.id(), ability.key());
    }

    double valueOrGlobal(TowerType type, NetherAbilityKey ability) {
        double value = TowerBalanceRuntime.ability(type.id(), ability.key(), Double.NaN);
        return Double.isNaN(value) ? global(ability) : value;
    }

    int integer(TowerType type, NetherAbilityKey ability) {
        return TowerBalanceRuntime.abilityInt(type.id(), ability.key(), 0);
    }

    int ticks(TowerType type, NetherAbilityKey ability) {
        return TowerBalanceRuntime.abilityTicks(type.id(), ability.key());
    }

    double global(NetherAbilityKey ability) {
        return TowerBalanceRuntime.ability(CONFIG_ID, ability.key());
    }
}
