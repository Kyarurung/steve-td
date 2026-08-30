package kim.biryeong.semiontd.tower.villager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AllayTowerIntervalTest {
    @Test
    void reducedIntervalNeverDropsBelowTwentyTicks() {
        assertEquals(100, VillagerSupportRules.reducedTicks(100, 0.0, 20));
        assertEquals(50, VillagerSupportRules.reducedTicks(100, 0.5, 20));
        assertEquals(20, VillagerSupportRules.reducedTicks(100, 0.8, 20));
        assertEquals(20, VillagerSupportRules.reducedTicks(100, 2.5, 20));
    }

    @Test
    void supportBlockExpiresExactlyAtCurrentTick() {
        org.junit.jupiter.api.Assertions.assertFalse(VillagerSupportRules.canApplyAt(101L, 100L));
        org.junit.jupiter.api.Assertions.assertTrue(VillagerSupportRules.canApplyAt(100L, 100L));
    }
}
