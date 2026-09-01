package kim.biryeong.semiontd.tower.resonance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ResonanceStateTest {
    @Test
    void snapshotRestoresWaveStateAurasAndAttackCharge() {
        ResonanceState original = new ResonanceState();
        original.updateResonance(3, 5);
        original.updateAuras(0.5, 1.5);
        assertFalse(original.chargeReady(3));
        ResonanceState restored = new ResonanceState();

        restored.restore(original.snapshot());

        assertEquals(new ResonanceSnapshot(3, 5, 1, 0.5, 1.5), restored.snapshot());
        assertFalse(restored.chargeReady(3));
        assertTrue(restored.chargeReady(3));
    }

    @Test
    void stateRejectsNegativeRuntimeValues() {
        ResonanceState state = new ResonanceState();
        state.updateResonance(-1, -2);
        state.updateAuras(-0.5, -1.0);
        assertEquals(new ResonanceSnapshot(0, 0, 0, 0.0, 0.0), state.snapshot());
    }
}
