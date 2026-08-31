package kim.biryeong.semiontd.tower.villager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VillagerSupportControllerTest {
    @Test
    void reducedIntervalNeverDropsBelowTwentyTicks() {
        assertEquals(100, VillagerSupportController.reducedTicks(100, 0.0, 20));
        assertEquals(50, VillagerSupportController.reducedTicks(100, 0.5, 20));
        assertEquals(20, VillagerSupportController.reducedTicks(100, 0.8, 20));
        assertEquals(20, VillagerSupportController.reducedTicks(100, 2.5, 20));
    }

    @Test
    void supportBlockExpiresExactlyAtCurrentTick() {
        assertFalse(VillagerSupportController.canApplyAt(101L, 100L));
        assertTrue(VillagerSupportController.canApplyAt(100L, 100L));
    }
}
