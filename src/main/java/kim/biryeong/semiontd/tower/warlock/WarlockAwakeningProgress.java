package kim.biryeong.semiontd.tower.warlock;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WarlockAwakeningProgress {
    private static final Map<UUID, Long> KILLS = new ConcurrentHashMap<>();

    private WarlockAwakeningProgress() {
    }

    public static boolean recordKill(UUID ownerPlayer) {
        if (ownerPlayer == null) {
            return false;
        }
        boolean previouslyUnlocked = snapshot(ownerPlayer).unlocked();
        KILLS.compute(ownerPlayer, (ignored, kills) -> saturatedIncrement(kills == null ? 0L : kills));
        return !previouslyUnlocked && snapshot(ownerPlayer).unlocked();
    }

    public static Snapshot snapshot(UUID ownerPlayer) {
        long kills = ownerPlayer == null ? 0L : KILLS.getOrDefault(ownerPlayer, 0L);
        long requiredKills = WarlockConfig.RUNTIME.requiredAwakeningKills();
        return new Snapshot(kills, requiredKills, kills >= requiredKills);
    }

    public static void clear(UUID ownerPlayer) {
        if (ownerPlayer != null) {
            KILLS.remove(ownerPlayer);
        }
    }

    public static void clearAllForTesting() {
        KILLS.clear();
    }

    private static long saturatedIncrement(long value) {
        return value == Long.MAX_VALUE ? value : value + 1L;
    }

    public record Snapshot(long kills, long requiredKills, boolean unlocked) {
    }
}
