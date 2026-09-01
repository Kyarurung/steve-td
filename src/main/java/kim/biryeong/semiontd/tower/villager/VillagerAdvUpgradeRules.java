package kim.biryeong.semiontd.tower.villager;

import kim.biryeong.semiontd.game.SemionPlayer;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerUpgradeOption;

public final class VillagerAdvUpgradeRules {
    private VillagerAdvUpgradeRules() {
    }

    public static boolean canUpgrade(SemionPlayer player, Tower tower, TowerUpgradeOption upgrade) {
        if (!VillagerAdvProgressionController.isAdvPlayer(player)) {
            return true;
        }
        if (tower == null || upgrade == null) {
            return false;
        }
        double requirement = VillagerConfig.RUNTIME.advancedUpgradeRequirement(tower.type(), upgrade.id());
        return VillagerAdvRules.meetsUpgradeRequirement(VillagerAdvStates.experience(tower), requirement);
    }
}
