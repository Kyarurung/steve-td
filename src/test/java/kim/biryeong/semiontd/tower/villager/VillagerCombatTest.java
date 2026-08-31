package kim.biryeong.semiontd.tower.villager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class VillagerCombatTest {
    @Test
    void survivalScalingPreservesDamageAndIntervalRules() {
        double bonus = VillagerCombat.survivalBonus(0.05, 4, 1.5);

        assertEquals(0.30, bonus, 0.0001);
        assertEquals(130.0, VillagerCombat.addPercentBonus(100.0, bonus), 0.0001);
        assertEquals(70, VillagerCombat.reduceInterval(100, bonus));
    }

    @Test
    void deathStackDamageStopsAtConfiguredCap() {
        assertEquals(5.0, VillagerCombat.addCappedDamage(4.5, 1.0, 5.0), 0.0001);
    }
}
