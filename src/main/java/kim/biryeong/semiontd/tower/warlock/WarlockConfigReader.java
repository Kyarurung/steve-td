package kim.biryeong.semiontd.tower.warlock;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

final class WarlockConfigReader {
    double value(WarlockAbilityKey ability) {
        return TowerBalanceRuntime.ability(ability.configId(), ability.key());
    }

    double ratio(WarlockAbilityKey ability) {
        return Math.clamp(value(ability), 0.0, 1.0);
    }

    double nonNegative(WarlockAbilityKey ability) {
        return Math.max(0.0, value(ability));
    }

    int positiveInteger(WarlockAbilityKey ability) {
        return Math.max(1, integer(ability));
    }

    int nonNegativeInteger(WarlockAbilityKey ability) {
        return Math.max(0, integer(ability));
    }

    double towerRatio(TowerType type, String key) {
        return Math.clamp(TowerBalanceRuntime.ability(type.id(), key), 0.0, 1.0);
    }

    double towerNonNegative(TowerType type, String key) {
        return Math.max(0.0, TowerBalanceRuntime.ability(type.id(), key));
    }

    int towerPositiveTicks(TowerType type, String key) {
        return Math.max(1, TowerBalanceRuntime.abilityTicks(type.id(), key));
    }

    int requiredAfter(WarlockAbilityKey ability) {
        int configured = nonNegativeInteger(ability);
        return configured == Integer.MAX_VALUE ? configured : configured + 1;
    }

    private int integer(WarlockAbilityKey ability) {
        return TowerBalanceRuntime.abilityInt(ability.configId(), ability.key());
    }
}
