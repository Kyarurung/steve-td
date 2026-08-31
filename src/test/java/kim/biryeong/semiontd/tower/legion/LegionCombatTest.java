package kim.biryeong.semiontd.tower.legion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LegionCombatTest {
    @Test
    void parrotMultiplierDrivesDamageAndIntervalFromOneFormula() {
        double multiplier = LegionCombat.attackMultiplier(6, 0.2);

        assertEquals(2.2, multiplier, 0.0001);
        assertEquals(10, LegionCombat.attackInterval(22, multiplier));
    }

    @Test
    void parrotMultiplierIgnoresNegativeStackCounts() {
        assertEquals(1.0, LegionCombat.attackMultiplier(-1, 0.2), 0.0001);
    }
}
