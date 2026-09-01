package kim.biryeong.semiontd.tower.villager;

import java.util.List;
import kim.biryeong.semiontd.tower.TowerType;

public final class VillagerAdvTowers {
    private static final List<TowerType> ALL = List.of(
            VillagerTowers.ADV_T1_SPLASH_TOWER,
            VillagerTowers.ADV_T2_LIBRARIAN_TOWER,
            VillagerTowers.ADV_T3_CLERIC_TOWER,
            VillagerTowers.ADV_T1_GOLEM_TOWER,
            VillagerTowers.ADV_T2_GOLEM_TOWER,
            VillagerTowers.ADV_T3_GOLEM_TOWER,
            VillagerTowers.ADV_T1_ALLAY_TOWER,
            VillagerTowers.ADV_T2_ALLAY_TOWER,
            VillagerTowers.ADV_T2_WEAPON_SMITH_TOWER,
            VillagerTowers.ADV_T3_ARMORER_TOWER,
            VillagerTowers.ADV_T3_WEAPON_SMITH_TOWER,
            VillagerTowers.ADV_T1_CAT_TOWER,
            VillagerTowers.ADV_T2_ANTI_TANKER_CAT_TOWER,
            VillagerTowers.ADV_T2_LANE_CLEAR_CAT_TOWER,
            VillagerTowers.ADV_T3_ANTI_TANKER_CAT_TOWER,
            VillagerTowers.ADV_T3_LANE_CLEAR_CAT_TOWER
    );

    private VillagerAdvTowers() {
    }

    public static List<TowerType> all() {
        return ALL;
    }

    public static boolean contains(TowerType type) {
        return type != null && ALL.stream().anyMatch(candidate -> type.id().equals(candidate.id()));
    }
}
