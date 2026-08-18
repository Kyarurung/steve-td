package kim.biryeong.semiontd.tower.engineer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class EngineerPressStates {
    private static final Map<UUID, Integer> PRESSES = new HashMap<>();

    private EngineerPressStates() {
    }

    public static int recordPress(UUID playerId) {
        if (playerId == null) {
            return 0;
        }
        return PRESSES.merge(playerId, 1, Integer::sum);
    }

    public static int count(UUID playerId) {
        return playerId == null ? 0 : PRESSES.getOrDefault(playerId, 0);
    }

    public static void clear(UUID playerId) {
        if (playerId != null) {
            PRESSES.remove(playerId);
        }
    }
}
