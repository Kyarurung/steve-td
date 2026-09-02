package kim.biryeong.semiontd.tower.undead;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UndeadDrownedLastStandStateTest {
    @Test
    void lethalDamageLeavesOneHealthAndBlocksDamageForConfiguredTicks() {
        UndeadDrownedLastStandState state = new UndeadDrownedLastStandState();

        assertTrue(state.tryActivate(10.0, 100.0, 3));
        assertTrue(state.active());
        state.tick();
        assertTrue(state.active());
        state.tick();
        assertTrue(state.active());
        state.tick();

        assertFalse(state.active());
        assertFalse(state.tryActivate(1.0, 100.0, 3));
    }

    @Test
    void nonlethalDamageDoesNotConsumeLastStand() {
        UndeadDrownedLastStandState state = new UndeadDrownedLastStandState();

        assertFalse(state.tryActivate(10.0, 9.0, 3));
        assertTrue(state.tryActivate(10.0, 100.0, 3));
        assertTrue(state.active());
    }

    @Test
    void roundResetRestoresAvailability() {
        UndeadDrownedLastStandState state = new UndeadDrownedLastStandState();
        assertTrue(state.tryActivate(10.0, 100.0, 3));
        state.tick();
        state.resetRound();

        assertFalse(state.active());
        assertTrue(state.tryActivate(10.0, 100.0, 3));
        assertEquals(3, state.remainingTicks());
    }
}
