package kim.biryeong.semiontd.tower.villager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AllayTowerIntervalTest {
    @Test
    void reducedIntervalNeverDropsBelowTwentyTicks() {
        assertEquals(100, AllayTower.reducedTicks(100, 0.0, 20));
        assertEquals(50, AllayTower.reducedTicks(100, 0.5, 20));
        assertEquals(20, AllayTower.reducedTicks(100, 0.8, 20));
        assertEquals(20, AllayTower.reducedTicks(100, 2.5, 20));
    }
}
