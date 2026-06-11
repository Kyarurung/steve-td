package kim.biryeong.semiontd.trait;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class TraitScalingTest {
    @Test
    void scaleDeltaAppliesSlotScaleToIntegerDelta() {
        assertEquals(300L, TraitScaling.scaleDelta(200L, 300L, TraitSlot.PRIMARY));
        assertEquals(250L, TraitScaling.scaleDelta(200L, 300L, TraitSlot.SECONDARY));
    }

    @Test
    void scaleDeltaAppliesSlotScaleToDoubleDelta() {
        assertEquals(1.5D, TraitScaling.scaleDelta(1.0D, 2.0D, TraitSlot.SECONDARY), 0.0001D);
    }
}
