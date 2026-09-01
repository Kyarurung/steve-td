package kim.biryeong.semiontd.tower.nether;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NetherCombatStateTest {
    @Test
    void attackIntervalsCooldownsAndMarksAdvanceIndependently() {
        NetherCombatState state = new NetherCombatState();

        state.recordAttack(true);
        state.extendDecayReduction(2);
        state.startPulseCooldown(2);

        assertFalse(state.extraAttackReady(2));
        assertTrue(state.lastAttackWasCritical());
        assertEquals(0, state.nextMarkIndex(2));
        assertEquals(1, state.nextMarkIndex(2));
        assertEquals(0, state.nextMarkIndex(2));

        state.tick();
        assertEquals(1, state.decayReductionTicks());
        assertFalse(state.pulseReady());
        state.tick();
        assertTrue(state.pulseReady());
    }

    @Test
    void upgradeCopyPreservesRuntimeCountersAndRoundResetClearsThem() {
        NetherCombatState source = new NetherCombatState();
        source.recordAttack(true);
        source.recordAttack(false);
        source.extendDecayReduction(5);
        source.startPulseCooldown(3);
        source.nextMarkIndex(4);
        NetherCombatState target = new NetherCombatState();

        target.copyFrom(source);

        assertTrue(target.extraAttackReady(2));
        assertEquals(5, target.decayReductionTicks());
        assertFalse(target.pulseReady());
        assertEquals(1, target.nextMarkIndex(4));
        target.resetRound();
        target.recordAttack(false);
        assertFalse(target.extraAttackReady(2));
        assertEquals(0, target.decayReductionTicks());
        assertTrue(target.pulseReady());
        assertFalse(target.lastAttackWasCritical());
        assertEquals(0, target.nextMarkIndex(4));
    }
}
