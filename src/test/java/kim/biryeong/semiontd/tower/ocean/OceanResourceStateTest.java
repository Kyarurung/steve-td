package kim.biryeong.semiontd.tower.ocean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class OceanResourceStateTest {
    @Test
    void resourceMutationRejectsInvalidAmountsAndNeverDropsBelowZero() {
        OceanResourceState state = new OceanResourceState(100.0);
        state.addWater(Double.NaN);
        state.addWater(-10.0);
        assertEquals(100.0, state.water());
        assertFalse(state.spendWater(100.01));
        assertEquals(100.0, state.water());
        assertTrue(state.spendWater(100.0));
        assertEquals(0.0, state.water());
        assertTrue(state.spendWater(Double.NaN));
    }

    @Test
    void snapshotTransfersEveryUpgradePersistentField() {
        OceanResourceState previous = new OceanResourceState(100.0);
        UUID target = UUID.nameUUIDFromBytes("ocean-snapshot-target".getBytes());
        previous.addWater(45.0);
        previous.startWave();
        previous.tickDehydration(20);
        previous.startTransferCooldown(100);
        previous.captureSupplyTargets(Set.of(target));

        OceanResourceState upgraded = new OceanResourceState(0.0);
        upgraded.restore(previous.snapshot());

        assertEquals(145.0, upgraded.water());
        assertTrue(upgraded.waveActive());
        assertEquals(1, upgraded.dehydrationTicks());
        assertEquals(100, upgraded.transferCooldownTicks());
        assertEquals(Set.of(target), upgraded.supplyTargetIds());
    }

    @Test
    void roundResetClearsOnlyRoundScopedFields() {
        OceanResourceState state = new OceanResourceState(100.0);
        UUID target = UUID.nameUUIDFromBytes("ocean-round-target".getBytes());
        state.startWave();
        state.tickDehydration(20);
        state.startTransferCooldown(100);
        state.captureSupplyTargets(Set.of(target));

        state.resetRound();

        assertEquals(100.0, state.water());
        assertFalse(state.waveActive());
        assertEquals(0, state.dehydrationTicks());
        assertEquals(0, state.transferCooldownTicks());
        assertEquals(Set.of(target), state.supplyTargetIds());
    }
}
