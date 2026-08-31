package kim.biryeong.semiontd.tower.legion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LegionBeeStingPolicyTest {
    @Test
    void applyingStingsStacksUpToConfiguredMaximumAndRefreshesDuration() {
        LegionBeeStingPolicy.State state = null;

        state = LegionBeeStingPolicy.applySting(state, 3, 80, 20);
        state = LegionBeeStingPolicy.applySting(state, 3, 80, 20);
        state = LegionBeeStingPolicy.applySting(state, 3, 40, 20);
        state = LegionBeeStingPolicy.applySting(state, 3, 80, 20);

        assertEquals(3, state.stacks());
        assertEquals(80, state.remainingTicks());
        assertEquals(20, state.ticksUntilDamage());
    }

    @Test
    void tickingDealsStackScaledDamageOnIntervalUntilDurationEnds() {
        LegionBeeStingPolicy.State state = LegionBeeStingPolicy.applySting(null, 4, 5, 2);
        state = LegionBeeStingPolicy.applySting(state, 4, 5, 2);

        LegionBeeStingPolicy.TickResult first = LegionBeeStingPolicy.tick(state, 1.5, 2);
        assertEquals(0.0, first.damage(), 0.0001);
        assertTrue(first.state().isPresent());

        LegionBeeStingPolicy.TickResult second = LegionBeeStingPolicy.tick(first.state().orElseThrow(), 1.5, 2);
        assertEquals(3.0, second.damage(), 0.0001);
        assertEquals(2, second.state().orElseThrow().stacks());

        LegionBeeStingPolicy.TickResult current = second;
        while (current.state().isPresent()) {
            current = LegionBeeStingPolicy.tick(current.state().orElseThrow(), 1.5, 2);
        }
        assertTrue(current.state().isEmpty());
    }
}
