package kim.biryeong.semiontd.game;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import kim.biryeong.semiontd.rating.PlayerRatingProfile;
import kim.biryeong.semiontd.rating.RatingSystemId;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public final class RatingProfileCacheGameTest {
    @GameTest
    public void ratingProfileCacheUsesSnapshotWithoutRuntimeLookups(GameTestHelper context) {
        UUID playerId = UUID.nameUUIDFromBytes("gametest-rating-cache-player".getBytes());
        MutableProfileLookup lookup = new MutableProfileLookup(playerId, profile(playerId, "cache", 1600));
        RatingProfileCache cache = new RatingProfileCache();
        ParticipantSelectionPlan plan = new ParticipantSelectionPlan(
                MatchMode.TEST,
                List.of(new AssignedParticipant(playerId, "cache", TeamId.RED, 1)),
                Set.of(),
                1
        );

        cache.capture(plan, lookup);
        int readsAfterCapture = lookup.calls.get();
        lookup.profile = profile(playerId, "cache", 1700);

        Optional<PlayerRatingProfile> cached = cache.profile(playerId, lookup);
        Optional<PlayerRatingProfile> cachedAgain = cache.profile(playerId, lookup);
        if (cached.isEmpty() || cached.get().displayElo() != 1600) {
            context.fail(Component.literal("Rating placeholder cache should return the match-start ELO snapshot while active."));
            return;
        }
        if (cachedAgain.isEmpty() || cachedAgain.get().displayElo() != 1600) {
            context.fail(Component.literal("Repeated active cache reads should keep using the same snapshot."));
            return;
        }
        if (lookup.calls.get() != readsAfterCapture) {
            context.fail(Component.literal("Active rating placeholder cache should not invoke the persistence lookup after capture."));
            return;
        }

        cache.clear();
        Optional<PlayerRatingProfile> refreshed = cache.profile(playerId, lookup);
        if (refreshed.isEmpty() || refreshed.get().displayElo() != 1700) {
            context.fail(Component.literal("Cleared rating placeholder cache should fall back to the live rating lookup."));
            return;
        }
        context.succeed();
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
        private final UUID playerId;
        private final AtomicInteger calls = new AtomicInteger();
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
