package kim.biryeong.semiontd.tower.undead;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UndeadCombatTest {
    @Test
    void lifeStealUsesAttemptedDamageSemanticsAndRejectsNonPositiveInputs() {
        assertEquals(4.0, UndeadCombat.lifeStealAmount(10.0, 0.4));
        assertEquals(0.0, UndeadCombat.lifeStealAmount(0.0, 0.4));
        assertEquals(0.0, UndeadCombat.lifeStealAmount(10.0, 0.0));
    }

    @Test
    void permanentDeathGrowthStopsAtConfiguredCaps() {
        assertEquals(5.0, UndeadCombat.addCappedDamage(4.5, 1.0, 5.0));
        assertEquals(3, UndeadCombat.addCappedStack(3, 3));
    }
}
