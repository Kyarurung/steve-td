package kim.biryeong.semiontd.tower.animal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AnimalPackRulesTest {
    @Test
    void stackCountClampsAtBothBoundaries() {
        assertEquals(0, AnimalPackRules.cappedStacks(-1L, 4));
        assertEquals(3, AnimalPackRules.cappedStacks(3L, 4));
        assertEquals(4, AnimalPackRules.cappedStacks(9L, 4));
    }

    @Test
    void leaderUpgradeRequiresBaseMaximumAndNoExistingLeader() {
        assertTrue(AnimalPackRules.canUpgradeToLeader(true, 4, 4, false));
        assertFalse(AnimalPackRules.canUpgradeToLeader(false, 4, 4, false));
        assertFalse(AnimalPackRules.canUpgradeToLeader(true, 3, 4, false));
        assertFalse(AnimalPackRules.canUpgradeToLeader(true, 4, 4, true));
    }

    @Test
    void auraIncludesExactRadiusBoundary() {
        assertTrue(AnimalPackRules.withinAura(16.0, 4.0));
        assertFalse(AnimalPackRules.withinAura(16.01, 4.0));
    }
}
