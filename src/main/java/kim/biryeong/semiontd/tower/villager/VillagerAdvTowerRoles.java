package kim.biryeong.semiontd.tower.villager;

import java.util.List;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;

final class VillagerAdvTowerRoles {
    private VillagerAdvTowerRoles() {
    }

    static boolean isGolem(Tower tower) {
        return matches(tower, VillagerTowers.T1_GOLEM_TOWER, VillagerTowers.T2_GOLEM_TOWER, VillagerTowers.T3_GOLEM_TOWER);
    }

    static boolean isRanged(Tower tower) {
        return matches(tower, VillagerTowers.T1_SPLASH_TOWER, VillagerTowers.T2_LIBRARIAN_TOWER, VillagerTowers.T3_CLERIC_TOWER);
    }

    static boolean isCat(Tower tower) {
        return matches(tower,
                VillagerTowers.T1_CAT_TOWER,
                VillagerTowers.T2_ANTI_TANKER_CAT_TOWER,
                VillagerTowers.T3_ANTI_TANKER_CAT_TOWER,
                VillagerTowers.T2_LANE_CLEAR_CAT_TOWER,
                VillagerTowers.T3_LANE_CLEAR_CAT_TOWER);
    }

    static boolean isAntiTankerCat(Tower tower) {
        return matches(tower, VillagerTowers.T2_ANTI_TANKER_CAT_TOWER, VillagerTowers.T3_ANTI_TANKER_CAT_TOWER);
    }

    static boolean isLaneClearCat(Tower tower) {
        return matches(tower, VillagerTowers.T2_LANE_CLEAR_CAT_TOWER, VillagerTowers.T3_LANE_CLEAR_CAT_TOWER);
    }

    static boolean isAllayLine(Tower tower) {
        return matches(tower,
                VillagerTowers.T1_ALLAY_TOWER,
                VillagerTowers.T2_ALLAY_TOWER,
                VillagerTowers.T2_WEAPON_SMITH_TOWER,
                VillagerTowers.T3_ARMORER_TOWER,
                VillagerTowers.T3_WEAPON_SMITH_TOWER);
    }

    static int tier(Tower tower) {
        if (matches(tower,
                VillagerTowers.T3_CLERIC_TOWER,
                VillagerTowers.T3_GOLEM_TOWER,
                VillagerTowers.T3_ARMORER_TOWER,
                VillagerTowers.T3_WEAPON_SMITH_TOWER,
                VillagerTowers.T3_ANTI_TANKER_CAT_TOWER,
                VillagerTowers.T3_LANE_CLEAR_CAT_TOWER)) {
            return 3;
        }
        if (matches(tower,
                VillagerTowers.T2_LIBRARIAN_TOWER,
                VillagerTowers.T2_GOLEM_TOWER,
                VillagerTowers.T2_ALLAY_TOWER,
                VillagerTowers.T2_WEAPON_SMITH_TOWER,
                VillagerTowers.T2_ANTI_TANKER_CAT_TOWER,
                VillagerTowers.T2_LANE_CLEAR_CAT_TOWER)) {
            return 2;
        }
        return 1;
    }

    private static boolean matches(Tower tower, TowerType... types) {
        return tower != null && List.of(types).stream().anyMatch(type -> VillagerTowers.matches(tower.type(), type));
    }
}
