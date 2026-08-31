package kim.biryeong.semiontd.tower.villager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VillagerGolemSurvivalStateTest {
    @Test
    void incrementsToCapAndCopiesOnlyUpToNewTowerCap() {
        VillagerGolemSurvivalState source = new VillagerGolemSurvivalState();
        assertTrue(source.increment(2));
        assertTrue(source.increment(2));
        assertFalse(source.increment(2));
        assertEquals(2, source.stacks());

        VillagerGolemSurvivalState upgraded = new VillagerGolemSurvivalState();
        upgraded.copyFrom(source, 1);
        assertEquals(1, upgraded.stacks());
    }
}
