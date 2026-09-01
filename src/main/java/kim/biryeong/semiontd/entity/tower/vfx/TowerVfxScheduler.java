package kim.biryeong.semiontd.entity.tower.vfx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import kim.biryeong.semiontd.config.VfxConfig;

final class TowerVfxScheduler<E extends TowerVfxScheduler.ScheduledEvent> {
    interface ScheduledEvent {
        VfxLaneKey lane();

        long gameTime();

        int phaseOrder();

        long sequence();

        void assignSequence(long sequence);
    }

    private final int maxQueueDepthPerLane;
    private final long maxEventAgeTicks;
    private final ConcurrentLinkedQueue<E> events = new ConcurrentLinkedQueue<>();
    private final Map<VfxLaneKey, Integer> queuedByLane = new HashMap<>();
    private final Map<VfxLaneKey, TowerVfxBudget> vanillaBudgets = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    TowerVfxScheduler(int maxQueueDepthPerLane, long maxEventAgeTicks) {
        this.maxQueueDepthPerLane = Math.max(1, maxQueueDepthPerLane);
        this.maxEventAgeTicks = Math.max(0L, maxEventAgeTicks);
    }

    int enqueue(E event) {
        int depth;
        synchronized (queuedByLane) {
            depth = queuedByLane.getOrDefault(event.lane(), 0);
            if (depth >= maxQueueDepthPerLane) {
                return -1;
            }
            queuedByLane.put(event.lane(), depth + 1);
        }
        event.assignSequence(sequence.incrementAndGet());
        events.add(event);
        return depth + 1;
    }

    List<E> drain(long gameTime, Consumer<E> expiredEventConsumer) {
        List<E> batch = new ArrayList<>();
        E event;
        while ((event = events.poll()) != null) {
            synchronized (queuedByLane) {
                queuedByLane.computeIfPresent(event.lane(), (lane, count) -> count <= 1 ? null : count - 1);
            }
            if (gameTime - event.gameTime() > maxEventAgeTicks) {
                expiredEventConsumer.accept(event);
                continue;
            }
            batch.add(event);
        }
        batch.sort(Comparator
                .comparingLong(ScheduledEvent::gameTime)
                .thenComparingInt(ScheduledEvent::phaseOrder)
                .thenComparingLong(ScheduledEvent::sequence));
        return batch;
    }

    int claimVanillaPoints(
            VfxLaneKey lane,
            long gameTime,
            VfxConfig config,
            int preferred,
            int minimum,
            boolean essential
    ) {
        TowerVfxBudget bucket = vanillaBudgets.computeIfAbsent(
                lane,
                ignored -> new TowerVfxBudget(config.vanilla().burstCapacityPoints(), gameTime)
        );
        return bucket.claim(
                preferred,
                minimum,
                essential,
                gameTime,
                config.vanilla().refillPointsPerTick(),
                config.vanilla().burstCapacityPoints()
        );
    }

    void removeBudget(VfxLaneKey lane) {
        vanillaBudgets.remove(lane);
    }

    void clear() {
        events.clear();
        synchronized (queuedByLane) {
            queuedByLane.clear();
        }
        vanillaBudgets.clear();
    }
}
