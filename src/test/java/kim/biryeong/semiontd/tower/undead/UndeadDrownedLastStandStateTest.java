package kim.biryeong.semiontd.tower.undead;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UndeadDrownedLastStandStateTest {
    @Test
    void lethalHitActivatesOnceAndImmunityExpiresAtBoundary() {
        UndeadDrownedLastStandState state = new UndeadDrownedLastStandState();
        assertEquals(9.0, state.modifyDamage(100L, 10.0, 10.0, 20));
        assertEquals(0.0, state.modifyDamage(119L, 1.0, 100.0, 20));
        assertEquals(100.0, state.modifyDamage(120L, 1.0, 100.0, 20));
    }

    @Test
    void roundResetRestoresAvailability() {
        UndeadDrownedLastStandState state = new UndeadDrownedLastStandState();
        state.modifyDamage(100L, 10.0, 10.0, 20);
        state.resetRound();
        assertEquals(9.0, state.modifyDamage(101L, 10.0, 10.0, 20));
    }
}
