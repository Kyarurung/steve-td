package kim.biryeong.semiontd.tower.legion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IllusionSpawnRulesTest {
    @Test
    void spreadsClonesAcrossConfiguredTicksWithoutDelayingFirstClone() {
        assertEquals(50, IllusionSpawnRules.dueTick(50, 0, 4, 10));
        assertEquals(53, IllusionSpawnRules.dueTick(50, 1, 4, 10));
        assertEquals(56, IllusionSpawnRules.dueTick(50, 2, 4, 10));
        assertEquals(58, IllusionSpawnRules.dueTick(50, 3, 4, 10));
    }

    @Test
    void moreClonesThanSpreadTicksShareDueTicks() {
        assertEquals(0, IllusionSpawnRules.dueTick(0, 0, 12, 10));
        assertEquals(0, IllusionSpawnRules.dueTick(0, 1, 12, 10));
        assertEquals(2, IllusionSpawnRules.dueTick(0, 2, 12, 10));
    }
}
