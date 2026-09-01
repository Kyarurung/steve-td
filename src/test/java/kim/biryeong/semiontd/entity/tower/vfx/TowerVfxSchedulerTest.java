package kim.biryeong.semiontd.entity.tower.vfx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import kim.biryeong.semiontd.config.VfxConfig;
import kim.biryeong.semiontd.game.TeamId;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class TowerVfxSchedulerTest {
    private static final VfxLaneKey LANE = new VfxLaneKey(Level.OVERWORLD, TeamId.RED, 1);

    @Test
    void enforcesLaneDepthAndOrdersDrainedEvents() {
        TowerVfxScheduler<TestEvent> scheduler = new TowerVfxScheduler<>(2, 2L);
        TestEvent latePhase = new TestEvent(LANE, 5L, 2);
        TestEvent earlyPhase = new TestEvent(LANE, 5L, 1);

        assertEquals(1, scheduler.enqueue(latePhase));
        assertEquals(2, scheduler.enqueue(earlyPhase));
        assertEquals(-1, scheduler.enqueue(new TestEvent(LANE, 5L, 0)));
        assertEquals(List.of(earlyPhase, latePhase), scheduler.drain(5L, ignored -> { }));
    }

    @Test
    void expiresOldEventsAndOwnsVanillaBudget() {
        TowerVfxScheduler<TestEvent> scheduler = new TowerVfxScheduler<>(2, 2L);
        TestEvent expired = new TestEvent(LANE, 1L, 0);
        List<TestEvent> expiredEvents = new ArrayList<>();
        scheduler.enqueue(expired);

        assertEquals(List.of(), scheduler.drain(4L, expiredEvents::add));
        assertEquals(List.of(expired), expiredEvents);
        VfxConfig config = VfxConfig.defaultConfig();
        assertEquals(
                10,
                scheduler.claimVanillaPoints(LANE, 4L, config, 10, 0, false)
        );
    }

    private static final class TestEvent implements TowerVfxScheduler.ScheduledEvent {
        private final VfxLaneKey lane;
        private final long gameTime;
        private final int phaseOrder;
        private long sequence;

        private TestEvent(VfxLaneKey lane, long gameTime, int phaseOrder) {
            this.lane = lane;
            this.gameTime = gameTime;
            this.phaseOrder = phaseOrder;
        }

        @Override
        public VfxLaneKey lane() {
            return lane;
        }

        @Override
        public long gameTime() {
            return gameTime;
        }

        @Override
        public int phaseOrder() {
            return phaseOrder;
        }

        @Override
        public long sequence() {
            return sequence;
        }

        @Override
        public void assignSequence(long sequence) {
            this.sequence = sequence;
        }
    }
}
