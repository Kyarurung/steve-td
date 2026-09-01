package kim.biryeong.semiontd.tower.villager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerDataKey;
import net.minecraft.resources.ResourceLocation;

public final class VillagerAdvStates {
    public static final TowerDataKey<Double> EXPERIENCE = TowerDataKey.of(id("experience"), Double.class);
    static final TowerDataKey<Boolean> ADV_TOWER = TowerDataKey.of(id("tower"), Boolean.class);
    private static final double SURVIVAL_BONUS_MULTIPLIER = 0.5;
    private static final Map<UUID, Double> REPUTATION = new ConcurrentHashMap<>();

    private VillagerAdvStates() {
    }

    public static void clear(UUID playerId) {
        if (playerId != null) {
            REPUTATION.remove(playerId);
        }
    }

    public static void clearAll() {
        REPUTATION.clear();
    }

    public static double experience(Tower tower) {
        return tower == null ? 0.0 : tower.getDataOrDefault(EXPERIENCE, 0.0);
    }

    public static boolean isAdvTower(Tower tower) {
        return tower != null && tower.getDataOrDefault(ADV_TOWER, false);
    }

    public static double reputation(UUID playerId) {
        return playerId == null ? 0.0 : REPUTATION.getOrDefault(playerId, 0.0);
    }

    public static double survivalBonusMultiplier(Tower tower) {
        return isAdvTower(tower) ? SURVIVAL_BONUS_MULTIPLIER : 1.0;
    }

    static void addReputation(UUID playerId, double amount, TowerBalanceConfig.VillagerAdvConfig config) {
        if (playerId == null || amount == 0.0) {
            return;
        }
        REPUTATION.compute(playerId, (ignored, previous) -> VillagerAdvRules.nextReputation(
                previous == null ? 0.0 : previous,
                amount,
                config
        ));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "villager_adv/" + path);
    }
}
