package kim.biryeong.semiontd.tower.villager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VillagerSurvivalStateTest {
    @Test
    void incrementsToCapAndCopiesOnlyUpToNewTowerCap() {
        VillagerSurvivalState source = new VillagerSurvivalState();
        assertTrue(source.increment(2));
        assertTrue(source.increment(2));
        assertFalse(source.increment(2));
        assertEquals(2, source.stacks());

        VillagerSurvivalState upgraded = new VillagerSurvivalState();
        upgraded.copyFrom(source, 1);
        assertEquals(1, upgraded.stacks());
    }
}
