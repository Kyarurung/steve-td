package kim.biryeong.semiontd.tower.undead;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UndeadDrownedRevivalStateTest {
    @Test
    void lethalDamageStartsOneRevivalWithoutBlockingLaterDamage() {
        UndeadDrownedRevivalState state = new UndeadDrownedRevivalState();

        assertFalse(state.tryRevive(10.0, 9.0, 4));
        assertTrue(state.tryRevive(10.0, 10.0, 4));
        assertFalse(state.tryRevive(100.0, 100.0, 4));
    }

    @Test
    void revivedHealthCeilingFallsToZeroAtConfiguredLifetime() {
        UndeadDrownedRevivalState state = new UndeadDrownedRevivalState();
        assertTrue(state.tryRevive(10.0, 10.0, 4));

        assertEquals(75.0, state.tickHealthCeiling(100.0).orElseThrow());
        assertEquals(50.0, state.tickHealthCeiling(100.0).orElseThrow());
        assertEquals(25.0, state.tickHealthCeiling(100.0).orElseThrow());
        assertEquals(0.0, state.tickHealthCeiling(100.0).orElseThrow());
        assertFalse(state.reviving());
        assertTrue(state.tickHealthCeiling(100.0).isEmpty());
    }

    @Test
    void roundResetRestoresRevivalAvailability() {
        UndeadDrownedRevivalState state = new UndeadDrownedRevivalState();
        assertTrue(state.tryRevive(10.0, 10.0, 60));
        state.resetRound();

        assertTrue(state.tryRevive(10.0, 10.0, 60));
        assertEquals(60, state.remainingTicks());
    }
}
