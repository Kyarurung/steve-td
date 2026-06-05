package kim.biryeong.semiontd.music;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class SemionMusicServiceTest {
    @Test
    void playsEveryTrackBeforeRepeatingPlaylistSongs() {
        SemionMusicTrack first = track("first", 40L);
        SemionMusicTrack second = track("second", 60L);
        SemionMusicTrack third = track("third", 50L);
        SemionMusicService service = new SemionMusicService(
                new SemionMusicLibrary(List.of(first, second, third)),
                () -> 100L,
                bound -> 1
        );
        UUID playerId = UUID.nameUUIDFromBytes("music-random-next".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        SemionMusicService.PlaybackDecision initial = service.decisionFor(playerId, 0L, true);
        SemionMusicService.PlaybackDecision next = service.decisionFor(playerId, 140L, true);
        SemionMusicService.PlaybackDecision lastUnplayed = service.decisionFor(playerId, 290L, true);
        SemionMusicService.PlaybackDecision afterFullCycle = service.decisionFor(playerId, 450L, true);

        assertEquals(SemionMusicService.PlaybackAction.START_TRACK, initial.action());
        assertEquals(first.eventId(), initial.track().eventId());
        assertEquals(SemionMusicService.PlaybackAction.START_TRACK, next.action());
        assertEquals(third.eventId(), next.track().eventId());
        assertEquals(SemionMusicService.PlaybackAction.START_TRACK, lastUnplayed.action());
        assertEquals(second.eventId(), lastUnplayed.track().eventId());
        assertEquals(SemionMusicService.PlaybackAction.START_TRACK, afterFullCycle.action());
        assertEquals(third.eventId(), afterFullCycle.track().eventId());
    }

    private static SemionMusicTrack track(String id, long durationTicks) {
        return new SemionMusicTrack(
                id,
                Path.of(id + ".ogg"),
                ResourceLocation.fromNamespaceAndPath("semion-td", "music." + id),
                ResourceLocation.fromNamespaceAndPath("semion-td", "music/" + id),
                durationTicks
        );
    }
}
