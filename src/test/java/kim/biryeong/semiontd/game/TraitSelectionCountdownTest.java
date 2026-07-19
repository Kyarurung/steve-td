package kim.biryeong.semiontd.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class TraitSelectionCountdownTest {
    @Test
    void usesRequestedChatAndChimeIntervals() {
        for (int second : new int[]{30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1}) {
            assertTrue(SemionGameManager.shouldAnnounceTraitSelectionCountdown(second));
        }
        for (int second : new int[]{45, 40, 35, 30, 25, 20, 15, 10, 5}) {
            assertTrue(SemionGameManager.shouldPlayTraitSelectionChime(second));
        }

        assertFalse(SemionGameManager.shouldAnnounceTraitSelectionCountdown(29));
        assertFalse(SemionGameManager.shouldAnnounceTraitSelectionCountdown(14));
        assertFalse(SemionGameManager.shouldPlayTraitSelectionChime(4));
        assertFalse(SemionGameManager.shouldPlayTraitSelectionChime(0));
    }
}
