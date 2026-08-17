package kim.biryeong.semiontd.tower.demonlord;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player demon lord state, keyed by player id.
 *
 * <p>Static like the other builder state holders ({@code PlantSoilStates}, {@code HeroPartyStates})
 * so towers, goals and services can all reach it without threading a reference through the game.
 * Entries are dropped when a match starts, when the player is eliminated, and by tests.
 */
public final class DemonLordStates {
    private static final Map<UUID, DemonLordState> STATES = new ConcurrentHashMap<>();

    private DemonLordStates() {
    }

    public static DemonLordState get(UUID playerId) {
        return playerId == null ? null : STATES.get(playerId);
    }

    public static DemonLordState getOrCreate(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        return STATES.computeIfAbsent(playerId, DemonLordState::new);
    }

    /** True only while the player is a demon lord who is currently fightable. */
    public static boolean isInCombat(UUID playerId) {
        DemonLordState state = get(playerId);
        return state != null && state.inCombat();
    }

    public static void markLoadoutDirty(UUID playerId) {
        DemonLordState state = get(playerId);
        if (state != null) {
            state.markLoadoutDirty();
        }
    }

    /**
     * Drops the state and the boss bar together.
     *
     * <p>The bar is a {@code ServerBossEvent} the player stays subscribed to until it is explicitly
     * removed, so forgetting this leaves a demon lord health bar stuck on screen for the rest of the
     * session - across match end, job change and even other dimensions.
     */
    public static void clear(UUID playerId) {
        if (playerId != null) {
            STATES.remove(playerId);
            DemonLordService.clearBossBar(playerId);
        }
    }

    public static void clearAllForTesting() {
        STATES.clear();
    }
}
