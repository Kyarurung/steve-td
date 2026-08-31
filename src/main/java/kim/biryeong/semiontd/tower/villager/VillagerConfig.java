package kim.biryeong.semiontd.tower.villager;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.tower.TowerType;

public final class VillagerConfig {
    static final VillagerConfig RUNTIME = new VillagerConfig();

    private VillagerConfig() {
    }

    double value(TowerType type, VillagerAbilityKey ability) {
        return TowerBalanceRuntime.ability(type.id(), ability.key());
    }

    int integer(TowerType type, VillagerAbilityKey ability) {
        return TowerBalanceRuntime.abilityInt(type.id(), ability.key());
    }

    int ticks(TowerType type, VillagerAbilityKey ability) {
        return TowerBalanceRuntime.abilityTicks(type.id(), ability.key());
    }

    TowerBalanceConfig.VillagerAdvConfig advanced() {
        return TowerBalanceRuntime.villagerAdv();
    }

    double advancedUpgradeRequirement(TowerType type, String upgradeId) {
        return TowerBalanceRuntime.villagerAdvUpgradeRequirement(type, upgradeId);
    }
}
