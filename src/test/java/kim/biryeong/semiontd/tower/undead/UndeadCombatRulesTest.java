package kim.biryeong.semiontd.tower.undead;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UndeadCombatRulesTest {
    @Test
    void lifeStealUsesAttemptedDamageSemanticsAndRejectsNonPositiveInputs() {
        assertEquals(4.0, UndeadCombatRules.lifeStealAmount(10.0, 0.4));
        assertEquals(0.0, UndeadCombatRules.lifeStealAmount(0.0, 0.4));
        assertEquals(0.0, UndeadCombatRules.lifeStealAmount(10.0, 0.0));
    }

    @Test
    void permanentDeathGrowthStopsAtConfiguredCaps() {
        assertEquals(5.0, UndeadCombatRules.addCappedDamage(4.5, 1.0, 5.0));
        assertEquals(3, UndeadCombatRules.addCappedStack(3, 3));
    }
}
