package kim.biryeong.semiontd.tower.undead;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

public final class UndeadConfig {
    static final UndeadConfig RUNTIME = new UndeadConfig();

    private UndeadConfig() {
    }

    double value(TowerType type, UndeadAbilityKey ability) {
        return TowerBalanceRuntime.ability(type.id(), ability.key());
    }

    int integer(TowerType type, UndeadAbilityKey ability) {
        return TowerBalanceRuntime.abilityInt(type.id(), ability.key());
    }

    int ticks(TowerType type, UndeadAbilityKey ability) {
        return TowerBalanceRuntime.abilityTicks(type.id(), ability.key());
    }
}
