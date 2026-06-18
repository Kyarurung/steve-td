package kim.biryeong.semiontd.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import kim.biryeong.semiontd.rating.PlayerRatingProfile;
import kim.biryeong.semiontd.rating.RatingSystemId;
import org.junit.jupiter.api.Test;

final class SemionGameManagerRatingCacheTest {
    @Test
    void ratingProfileUsesMatchStartSnapshotUntilCacheClears() {
        UUID playerId = UUID.nameUUIDFromBytes("rating-cache-player".getBytes());
        MutableProfileLookup lookup = new MutableProfileLookup(playerId, profile(playerId, "cache", 1600));
        RatingProfileCache cache = new RatingProfileCache();
        ParticipantSelectionPlan plan = new ParticipantSelectionPlan(
                MatchMode.TEST,
                List.of(new AssignedParticipant(playerId, "cache", TeamId.RED, 1)),
                Set.of(),
                1
        );

        cache.capture(plan, lookup);
        int readsAfterSnapshot = lookup.calls.get();
        lookup.profile = profile(playerId, "cache", 1700);

        Optional<PlayerRatingProfile> cached = cache.profile(playerId, lookup);
        Optional<PlayerRatingProfile> cachedAgain = cache.profile(playerId, lookup);

        assertTrue(cached.isPresent());
        assertEquals(1600, cached.get().displayElo());
        assertEquals(1600, cachedAgain.orElseThrow().displayElo());
        assertEquals(readsAfterSnapshot, lookup.calls.get());

        cache.clear();

        assertEquals(1700, cache.profile(playerId, lookup).orElseThrow().displayElo());
        assertEquals(readsAfterSnapshot + 1, lookup.calls.get());
    }

    @Test
    void activeRatingProfileCacheFallsBackForUncachedPlayers() {
        UUID cachedPlayerId = UUID.nameUUIDFromBytes("rating-cache-known".getBytes());
        UUID latePlayerId = UUID.nameUUIDFromBytes("rating-cache-late".getBytes());
        MutableProfileLookup lookup = new MutableProfileLookup(cachedPlayerId, profile(cachedPlayerId, "known", 1550));
        RatingProfileCache cache = new RatingProfileCache();
        ParticipantSelectionPlan plan = new ParticipantSelectionPlan(
                MatchMode.TEST,
                List.of(new AssignedParticipant(cachedPlayerId, "known", TeamId.RED, 1)),
                Set.of(),
                1
        );

        cache.capture(plan, lookup);
        int readsAfterSnapshot = lookup.calls.get();
        lookup.playerId = latePlayerId;
        lookup.profile = profile(latePlayerId, "late", 1650);

        assertEquals(1650, cache.profile(latePlayerId, lookup).orElseThrow().displayElo());
        assertEquals(readsAfterSnapshot + 1, lookup.calls.get());

        lookup.profile = profile(latePlayerId, "late", 1700);

        assertEquals(1650, cache.profile(latePlayerId, lookup).orElseThrow().displayElo());
        assertEquals(readsAfterSnapshot + 1, lookup.calls.get());
    }

    private static PlayerRatingProfile profile(UUID playerId, String name, int displayElo) {
        return new PlayerRatingProfile(
                playerId,
                name,
                RatingSystemId.ELO,
                0,
                1,
                1,
                0,
                displayElo,
                350.0,
                displayElo,
                null,
                System.currentTimeMillis()
        );
    }

    private static final class MutableProfileLookup implements Function<UUID, Optional<PlayerRatingProfile>> {
        private final AtomicInteger calls = new AtomicInteger();
        private UUID playerId;
        private PlayerRatingProfile profile;

        private MutableProfileLookup(UUID playerId, PlayerRatingProfile profile) {
            this.playerId = playerId;
            this.profile = profile;
        }

        @Override
        public Optional<PlayerRatingProfile> apply(UUID requestedPlayerId) {
            calls.incrementAndGet();
            if (!playerId.equals(requestedPlayerId)) {
                return Optional.empty();
            }
            return Optional.ofNullable(profile);
        }
    }
}
