package kim.biryeong.semiontd.tower.animal;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

public final class AnimalConfig {
    static final AnimalConfig RUNTIME = new AnimalConfig();

    private AnimalConfig() {
    }

    double value(TowerType type, AnimalAbilityKey ability) {
        return TowerBalanceRuntime.ability(type.id(), ability.key());
    }

    int integer(TowerType type, AnimalAbilityKey ability) {
        return TowerBalanceRuntime.abilityInt(type.id(), ability.key());
    }

    int ticks(TowerType type, AnimalAbilityKey ability) {
        return TowerBalanceRuntime.abilityTicks(type.id(), ability.key());
    }
}
