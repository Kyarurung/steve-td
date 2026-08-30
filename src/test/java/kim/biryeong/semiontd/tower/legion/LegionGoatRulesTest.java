package kim.biryeong.semiontd.tower.legion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import kim.biryeong.semiontd.game.GridPosition;
import org.junit.jupiter.api.Test;

class LegionGoatRulesTest {
    @Test
    void configuredStackCountIsClampedToSupportedSourceCount() {
        assertEquals(1, LegionGoatRules.maxStacks(0, 3));
        assertEquals(2, LegionGoatRules.maxStacks(2, 3));
        assertEquals(3, LegionGoatRules.maxStacks(9, 3));
    }

    @Test
    void deterministicProviderSlotsStopAtMaximumAndUseIdentity() {
        Object first = new Object();
        Object second = new Object();
        Object fourth = new Object();
        List<Object> providers = List.of(first, second, new Object(), fourth);
        assertEquals(1, LegionGoatRules.providerIndex(providers, second, 3).orElseThrow());
        assertTrue(LegionGoatRules.providerIndex(providers, fourth, 3).isEmpty());
    }

    @Test
    void buffRangeIncludesExactThreeDimensionalBoundary() {
        GridPosition source = new GridPosition(0, 64, 0);
        assertTrue(LegionGoatRules.withinRange(source, new GridPosition(3, 68, 0), 5.0));
        assertFalse(LegionGoatRules.withinRange(source, new GridPosition(3, 68, 1), 5.0));
    }
}
