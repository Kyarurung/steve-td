package kim.biryeong.semiontd.tower.animal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AnimalCombatTest {
    @Test
    void stackDamageAndSplashUseConfiguredLinearRules() {
        assertEquals(130.0, AnimalCombat.addStackDamage(100.0, 3, 10.0), 0.0001);
        assertEquals(65.0, AnimalCombat.splashDamage(130.0, 0.5), 0.0001);
    }

    @Test
    void incomingReductionIsBoundedAndIntervalsStayPositive() {
        assertEquals(50.0, AnimalCombat.reduceIncomingDamage(100.0, 0.5), 0.0001);
        assertEquals(5.0, AnimalCombat.reduceIncomingDamage(100.0, 2.0), 0.0001);
        assertEquals(1, AnimalCombat.clampAttackInterval(-15));
    }
}
