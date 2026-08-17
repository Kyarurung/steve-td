package kim.biryeong.semiontd.tower.gamble;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import kim.biryeong.semiontd.game.PlayerEconomy;
import kim.biryeong.semiontd.tower.TowerType;

public final class GambleSpectatorRewards {
    private static final ConcurrentMap<UUID, PlayerEconomy> ACTIVE_ECONOMIES = new ConcurrentHashMap<>();

    private GambleSpectatorRewards() {
    }

    public static void openRound(UUID playerId, PlayerEconomy economy) {
        if (playerId != null && economy != null) {
            ACTIVE_ECONOMIES.put(playerId, economy);
        }
    }

    public static void closeRound(UUID playerId) {
        if (playerId != null) {
            ACTIVE_ECONOMIES.remove(playerId);
        }
    }

    public static long awardFaceSix(UUID playerId, TowerType type, int face) {
        if (face != 6 || playerId == null) {
            return 0L;
        }
        long reward = GambleBalance.spectatorFaceSixDiamondReward(type);
        PlayerEconomy economy = ACTIVE_ECONOMIES.get(playerId);
        if (reward <= 0L || economy == null) {
            return 0L;
        }
        economy.addDiamond(reward);
        return reward;
    }
}
