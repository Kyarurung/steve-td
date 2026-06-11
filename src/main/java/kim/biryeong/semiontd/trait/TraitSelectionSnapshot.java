package kim.biryeong.semiontd.trait;

import java.util.Map;
import java.util.UUID;

public record TraitSelectionSnapshot(Map<UUID, TraitLoadout> loadouts) {
    public TraitSelectionSnapshot {
        loadouts = loadouts == null ? Map.of() : Map.copyOf(loadouts);
    }

    public static TraitSelectionSnapshot empty() {
        return new TraitSelectionSnapshot(Map.of());
    }

    public TraitLoadout loadoutOrDefault(UUID playerId) {
        return loadouts.getOrDefault(playerId, TraitLoadout.none());
    }
}
