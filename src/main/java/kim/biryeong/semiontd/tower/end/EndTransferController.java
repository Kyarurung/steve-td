package kim.biryeong.semiontd.tower.end;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.LogarithmicScaling;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerDataKey;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.resources.ResourceLocation;

final class EndTransferController {
    private static final TowerDataKey<Double> PROGRESS = TowerDataKey.of(ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "end_transfer_progress"), Double.class);

    private final EndTransferState state = new EndTransferState();
    private final EndConfig config;
    private int shulkerCount;
    private int endCrystalCount;
    private int roundCompletedCount;

    EndTransferController(EndConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    TickResult tick(
            EndTower core,
            PlayerLane lane,
            BiConsumer<PlayerLane, Tower> particleEmitter
    ) {
        if (lane == null) {
            return TickResult.NONE;
        }
        captureTargets(core, lane);
        return advanceTransfers(lane, particleEmitter);
    }

    private void captureTargets(EndTower core, PlayerLane lane) {
        state.beginSnapshot();
        for (Tower tower : lane.towers()) {
            state.markPresent(tower);
            if (isEligibleTarget(core, tower)) {
                state.ensureProgress(tower, this::newProgress);
            }
        }
    }

    private TickResult advanceTransfers(
            PlayerLane lane,
            BiConsumer<PlayerLane, Tower> particleEmitter
    ) {
        TransferTick tick = new TransferTick();
        advanceActiveTransfers(lane, particleEmitter, tick);
        resolveCompletions(lane, tick);
        return tick.result();
    }

    private void advanceActiveTransfers(
            PlayerLane lane,
            BiConsumer<PlayerLane, Tower> particleEmitter,
            TransferTick tick
    ) {
        var iterator = state.progressEntries().iterator();
        while (iterator.hasNext()) {
            advanceTransfer(iterator, lane, particleEmitter, tick);
        }
    }

    private void advanceTransfer(
            Iterator<Map.Entry<Tower, EndTransferState.Progress>> iterator,
            PlayerLane lane,
            BiConsumer<PlayerLane, Tower> particleEmitter,
            TransferTick tick
    ) {
        Map.Entry<Tower, EndTransferState.Progress> entry = iterator.next();
        Tower source = entry.getKey();
        EndTransferState.Progress progress = entry.getValue();
        if (isInterrupted(source)) {
            interruptTransfer(iterator, source, progress, tick);
            return;
        }

        advanceProgress(source, progress, tick);
        if (progress.isComplete()) {
            collectCompletion(iterator, source, progress, tick);
        } else {
            recordActiveTransfer(lane, source, progress, particleEmitter, tick);
        }
    }

    private void advanceProgress(
            Tower source,
            EndTransferState.Progress progress,
            TransferTick tick
    ) {
        progress.advance();
        tick.markStatsChanged(state.apply(progress));
        source.setData(PROGRESS, progress.appliedRatio);
    }

    private static void recordActiveTransfer(
            PlayerLane lane,
            Tower source,
            EndTransferState.Progress progress,
            BiConsumer<PlayerLane, Tower> particleEmitter,
            TransferTick tick
    ) {
        tick.addPeriodicHealing(progress.periodicHealingPerSecond);
        if (shouldEmitParticles(source, progress.elapsedTicks)) {
            particleEmitter.accept(lane, source);
        }
    }

    private void interruptTransfer(
            Iterator<Map.Entry<Tower, EndTransferState.Progress>> iterator,
            Tower source,
            EndTransferState.Progress progress,
            TransferTick tick
    ) {
        removeTransfer(iterator, source);
        rollback(progress, tick);
    }

    private void collectCompletion(
            Iterator<Map.Entry<Tower, EndTransferState.Progress>> iterator,
            Tower source,
            EndTransferState.Progress progress,
            TransferTick tick
    ) {
        removeTransfer(iterator, source);
        if (isInterrupted(source)) {
            rollback(progress, tick);
        } else {
            tick.collect(new Completion(source, progress));
        }
    }

    private static void removeTransfer(
            Iterator<Map.Entry<Tower, EndTransferState.Progress>> iterator,
            Tower source
    ) {
        iterator.remove();
        source.removeData(PROGRESS);
    }

    private void rollback(EndTransferState.Progress progress, TransferTick tick) {
        tick.markStatsChanged(state.rollback(progress));
    }

    private void resolveCompletions(PlayerLane lane, TransferTick tick) {
        if (tick.completions().isEmpty()) {
            return;
        }
        Set<Tower> killed = Collections.newSetFromMap(new IdentityHashMap<>());
        killed.addAll(lane.killTowers(tick.completionSources()));
        for (Completion completion : tick.completions()) {
            if (killed.contains(completion.source())) {
                registerCompletion(completion, tick);
            } else {
                rollback(completion.progress(), tick);
            }
        }
    }

    private void registerCompletion(Completion completion, TransferTick tick) {
        tick.addCompletionHealing(completion.progress().completionHealing);
        registerCompleted(completion.source().type());
        tick.markCountsChanged();
    }

    private boolean isInterrupted(Tower source) {
        return !state.isPresent(source) || source.health() <= 0.0;
    }

    private boolean isEligibleTarget(EndTower core, Tower tower) {
        return tower != null
                && tower != core
                && Objects.equals(tower.ownerPlayer(), core.ownerPlayer())
                && tower.health() > 0.0
                && EndTowers.isTransferableTower(tower.type());
    }

    private EndTransferState.Progress newProgress(Tower tower) {
        EndConfig.TransferRule rule = config.transfer();
        boolean shulkerLine = EndTowers.isShulkerLine(tower.type());
        boolean endCrystalLine = EndTowers.isEndCrystalLine(tower.type());
        double maxHealth = tower.type().maxHealth();
        double damage = tower.type().damage();
        tower.setData(PROGRESS, 0.0);
        return new EndTransferState.Progress(
                rule.durationTicks(),
                shulkerLine ? maxHealth * rule.roundHealthRatio() : 0.0,
                shulkerLine ? maxHealth * rule.permanentHealthRatio() : 0.0,
                endCrystalLine ? damage * rule.roundDamageRatio() : 0.0,
                endCrystalLine ? damage * rule.permanentDamageRatio() : 0.0,
                rule.completionHealing(),
                shulkerLine ? maxHealth * rule.periodicHealingRatio() : 0.0
        );
    }

    private void registerCompleted(TowerType sourceType) {
        int tier = EndTowers.transferTier(sourceType);
        roundCompletedCount = saturatedAdd(roundCompletedCount, 1);
        if (EndTowers.isShulkerLine(sourceType)) {
            shulkerCount = saturatedAdd(shulkerCount, tier);
        } else {
            endCrystalCount = saturatedAdd(endCrystalCount, tier);
        }
    }

    boolean rollbackIncomplete() {
        boolean changed = false;
        for (Map.Entry<Tower, EndTransferState.Progress> entry : state.progressEntries()) {
            entry.getKey().removeData(PROGRESS);
            changed |= state.rollback(entry.getValue());
        }
        state.clearProgress();
        return changed;
    }

    void resetRound() {
        roundCompletedCount = 0;
        state.resetRoundContributions();
    }

    void copyFrom(EndTransferController source) {
        state.copyBonusesFrom(source.state);
        shulkerCount = source.shulkerCount;
        endCrystalCount = source.endCrystalCount;
        roundCompletedCount = source.roundCompletedCount;
    }

    int shulkerCount() {
        return shulkerCount;
    }

    int endCrystalCount() {
        return endCrystalCount;
    }

    int roundCompletedCount() {
        return roundCompletedCount;
    }

    double permanentHealthBonus() {
        return scaleHealthBonus(state.permanentHealthBonus());
    }

    double permanentDamageBonus() {
        return scaleDamageBonus(state.permanentDamageBonus());
    }

    double roundHealthBonus() {
        double permanent = state.permanentHealthBonus();
        double total = permanent + state.roundHealthContribution();
        return Math.max(0.0, scaleHealthBonus(total) - scaleHealthBonus(permanent));
    }

    double roundDamageBonus() {
        double permanent = state.permanentDamageBonus();
        double total = permanent + state.roundDamageContribution();
        return Math.max(0.0, scaleDamageBonus(total) - scaleDamageBonus(permanent));
    }

    EndTransferStats stats() {
        return new EndTransferStats(
                shulkerCount,
                endCrystalCount,
                roundCompletedCount,
                permanentHealthBonus(),
                roundHealthBonus(),
                permanentDamageBonus(),
                roundDamageBonus()
        );
    }

    private double scaleDamageBonus(double raw) {
        EndConfig.ScalingRule rule = config.damageScaling();
        return LogarithmicScaling.logarithmicBonus(raw, rule.threshold(), rule.scale());
    }

    private double scaleHealthBonus(double raw) {
        EndConfig.ScalingRule rule = config.healthScaling();
        return LogarithmicScaling.logarithmicBonus(raw, rule.threshold(), rule.scale());
    }

    static double progress(Tower tower) {
        return Math.clamp(tower.getDataOrDefault(PROGRESS, 0.0), 0.0, 1.0);
    }

    static void clearProgress(Tower tower) {
        tower.removeData(PROGRESS);
    }

    private static boolean shouldEmitParticles(Tower source, int elapsedTicks) {
        return Math.floorMod(elapsedTicks + System.identityHashCode(source), 5) == 0;
    }

    private static int saturatedAdd(int value, int increment) {
        if (increment <= 0 || value == Integer.MAX_VALUE) {
            return value;
        }
        return value > Integer.MAX_VALUE - increment ? Integer.MAX_VALUE : value + increment;
    }

    record TickResult(
            boolean statsChanged,
            boolean countsChanged,
            double completionHealing,
            double periodicHealingPerSecond
    ) {
        private static final TickResult NONE = new TickResult(false, false, 0.0, 0.0);
    }

    private record Completion(Tower source, EndTransferState.Progress progress) {
    }

    private static final class TransferTick {
        private final List<Completion> completions = new ArrayList<>();
        private boolean statsChanged;
        private boolean countsChanged;
        private double completionHealing;
        private double periodicHealingPerSecond;

        private void markStatsChanged(boolean changed) {
            statsChanged |= changed;
        }

        private void addPeriodicHealing(double healingPerSecond) {
            periodicHealingPerSecond += healingPerSecond;
        }

        private void collect(Completion completion) {
            completions.add(completion);
        }

        private List<Completion> completions() {
            return completions;
        }

        private List<Tower> completionSources() {
            return completions.stream().map(Completion::source).toList();
        }

        private void addCompletionHealing(double healing) {
            completionHealing += healing;
        }

        private void markCountsChanged() {
            countsChanged = true;
        }

        private TickResult result() {
            return new TickResult(
                    statsChanged,
                    countsChanged,
                    completionHealing,
                    periodicHealingPerSecond
            );
        }
    }
}
