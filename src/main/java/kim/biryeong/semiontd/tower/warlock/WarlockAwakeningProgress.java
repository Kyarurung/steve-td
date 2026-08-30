package kim.biryeong.semiontd.tower.warlock;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WarlockAwakeningProgress {
    private static final Map<UUID, State> PROGRESS = new ConcurrentHashMap<>();

    private WarlockAwakeningProgress() {
    }

    public static boolean recordKill(UUID ownerPlayer) {
        if (ownerPlayer == null) {
            return false;
        }
        long requiredKills = WarlockConfig.RUNTIME.requiredAwakeningKills();
        boolean[] newlyUnlocked = {false};
        PROGRESS.compute(ownerPlayer, (ignored, current) -> {
            State previous = current == null ? State.EMPTY : current;
            long kills = saturatedIncrement(previous.kills());
            boolean unlocked = previous.unlocked() || kills >= requiredKills;
            newlyUnlocked[0] = !previous.unlocked() && unlocked;
            return new State(kills, unlocked);
        });
        return newlyUnlocked[0];
    }

    public static Snapshot snapshot(UUID ownerPlayer) {
        State state = ownerPlayer == null ? State.EMPTY : PROGRESS.getOrDefault(ownerPlayer, State.EMPTY);
        long requiredKills = WarlockConfig.RUNTIME.requiredAwakeningKills();
        return new Snapshot(state.kills(), requiredKills, state.unlocked());
    }

    public static void clear(UUID ownerPlayer) {
        if (ownerPlayer != null) {
            PROGRESS.remove(ownerPlayer);
        }
    }

    public static void clearAllForTesting() {
        PROGRESS.clear();
    }

    private static long saturatedIncrement(long value) {
        return value == Long.MAX_VALUE ? value : value + 1L;
    }

    private record State(long kills, boolean unlocked) {
        private static final State EMPTY = new State(0L, false);
    }

    public record Snapshot(long kills, long requiredKills, boolean unlocked) {
    }
}
