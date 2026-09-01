package kim.biryeong.semiontd.tower.illager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class IllagerRaidStates {
    private static final Map<UUID, IllagerRaidState> STATES = new HashMap<>();

    private IllagerRaidStates() {
    }

    public static void clear(UUID playerId) {
        if (playerId != null) {
            STATES.remove(playerId);
        }
    }

    public static void clearAllForTesting() {
        STATES.clear();
    }

    public static Optional<IllagerRaidState> get(UUID playerId) {
        return Optional.ofNullable(STATES.get(playerId));
    }

    static IllagerRaidState state(UUID playerId) {
        return STATES.computeIfAbsent(playerId, ignored -> new IllagerRaidState());
    }
}
