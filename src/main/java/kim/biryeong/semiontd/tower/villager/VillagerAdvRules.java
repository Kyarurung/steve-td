package kim.biryeong.semiontd.tower.villager;

import kim.biryeong.semiontd.config.TowerBalanceConfig;

final class VillagerAdvRules {
    private VillagerAdvRules() {
    }

    static double nextExperience(
            double currentExperience,
            int tier,
            TowerBalanceConfig.VillagerAdvConfig config
    ) {
        return Math.min(
                config.resolvedExperienceMax(),
                currentExperience
                        + config.resolvedExperiencePerTower()
                        + Math.max(1, tier) * config.resolvedExperiencePerTier()
        );
    }

    static double nextReputation(
            double currentReputation,
            double change,
            TowerBalanceConfig.VillagerAdvConfig config
    ) {
        return Math.max(0.0, Math.min(config.resolvedReputationMax(), currentReputation + change));
    }

    static double buff(double points, double interval, double amount, double cap) {
        return Math.min(cap, Math.max(0.0, points) / interval * amount);
    }

    static boolean meetsUpgradeRequirement(double experience, double requirement) {
        return requirement <= 0.0 || experience + 1.0E-6 >= requirement;
    }
}
