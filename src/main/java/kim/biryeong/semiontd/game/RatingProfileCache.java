package kim.biryeong.semiontd.game;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import kim.biryeong.semiontd.rating.PlayerRatingProfile;

final class RatingProfileCache {
    private final Map<UUID, Optional<PlayerRatingProfile>> profiles = new ConcurrentHashMap<>();
    private boolean enabled;

    Optional<PlayerRatingProfile> profile(UUID playerId, Function<UUID, Optional<PlayerRatingProfile>> fallbackLookup) {
        if (playerId == null) {
            return Optional.empty();
        }
        if (enabled) {
            return profiles.computeIfAbsent(playerId, fallbackLookup);
        }
        return fallbackLookup.apply(playerId);
    }

    void capture(ParticipantSelectionPlan plan, Function<UUID, Optional<PlayerRatingProfile>> profileLookup) {
        if (plan == null) {
            captureIds(null, profileLookup);
            return;
        }
        Set<UUID> playerIds = new HashSet<>();
        plan.activeParticipants().stream()
                .map(AssignedParticipant::uuid)
                .forEach(playerIds::add);
        playerIds.addAll(plan.spectatorIds());
        captureIds(playerIds, profileLookup);
    }

    void captureIds(Collection<UUID> playerIds, Function<UUID, Optional<PlayerRatingProfile>> profileLookup) {
        profiles.clear();
        if (playerIds == null) {
            enabled = false;
            return;
        }
        for (UUID playerId : playerIds) {
            if (playerId != null) {
                profiles.put(playerId, profileLookup.apply(playerId));
            }
        }
        enabled = true;
    }

    void clear() {
        profiles.clear();
        enabled = false;
    }
}
