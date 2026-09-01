package kim.biryeong.semiontd.tower.resonance;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

final class ResonanceConfig {
    static final ResonanceConfig RUNTIME = new ResonanceConfig();

    private ResonanceConfig() {
    }

    double value(TowerType type, ResonanceAbilityKey ability) {
        return TowerBalanceRuntime.ability(type.id(), ability.key());
    }

    int integer(TowerType type, ResonanceAbilityKey ability) {
        return TowerBalanceRuntime.abilityInt(type.id(), ability.key());
    }

    int ticks(TowerType type, ResonanceAbilityKey ability) {
        return TowerBalanceRuntime.abilityTicks(type.id(), ability.key());
    }
}
