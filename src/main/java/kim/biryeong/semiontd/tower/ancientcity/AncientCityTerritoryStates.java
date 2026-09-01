package kim.biryeong.semiontd.tower.ancientcity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;

public final class AncientCityTerritoryStates {
    private static final Map<UUID, AncientCityTerritoryState> STATES = new HashMap<>();

    private AncientCityTerritoryStates() {
    }

    static AncientCityTerritoryState state(UUID playerId) {
        return STATES.computeIfAbsent(playerId, ignored -> new AncientCityTerritoryState());
    }

    static Optional<AncientCityTerritoryState> get(UUID playerId) {
        return Optional.ofNullable(STATES.get(playerId));
    }

    public static int territoryCount(UUID playerId) {
        return get(playerId).map(state -> state.territory().size()).orElse(0);
    }

    public static Set<BlockPos> territoryPositions(UUID playerId) {
        return get(playerId).map(state -> Set.copyOf(state.territory())).orElse(Set.of());
    }

    public static void clear(UUID playerId) {
        if (playerId != null) {
            STATES.remove(playerId);
        }
    }

    public static void clearAllForTesting() {
        STATES.clear();
    }
}
