package kim.biryeong.semiontd.tower.end;

import static kim.biryeong.semiontd.tower.end.EndConfig.Ability.*;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerDataKey;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.resources.ResourceLocation;

final class EndTransferController {
    private static final TowerDataKey<Double> PROGRESS = TowerDataKey.of(
            ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "end_transfer_progress"),
            Double.class
    );

    private final EndTransferState state = new EndTransferState();
    private final EndConfig config;
    private int endCrystalCount;
    private int shulkerCount;
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
        double completionHealing = 0.0;
        double periodicHealingPerSecond = 0.0;
        boolean statsChanged = false;
        boolean countsChanged = false;
        List<Completion> completions = null;
        var iterator = state.progressEntries().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Tower, EndTransferState.Progress> entry = iterator.next();
            Tower source = entry.getKey();
            EndTransferState.Progress progress = entry.getValue();
            if (isInterrupted(source)) {
                iterator.remove();
                source.removeData(PROGRESS);
                statsChanged |= state.rollback(progress);
                continue;
            }

            progress.elapsedTicks++;
            statsChanged |= state.apply(progress);
            source.setData(PROGRESS, progress.appliedRatio);
            if (progress.elapsedTicks < progress.durationTicks) {
                periodicHealingPerSecond += progress.periodicHealingPerSecond;
                if (shouldEmitParticles(source, progress.elapsedTicks)) {
                    particleEmitter.accept(lane, source);
                }
                continue;
            }

            iterator.remove();
            source.removeData(PROGRESS);
            if (isInterrupted(source)) {
                statsChanged |= state.rollback(progress);
                continue;
            }
            if (completions == null) {completions = new ArrayList<>();}
            completions.add(new Completion(source, progress));
        }
        if (completions != null) {
            List<Tower> completionSources = new ArrayList<>(completions.size());
            for (Completion completion : completions) {completionSources.add(completion.source());}
            Set<Tower> killed = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
            killed.addAll(lane.killTowers(completionSources));
            for (Completion completion : completions) {
                if (!killed.contains(completion.source())) {
                    statsChanged |= state.rollback(completion.progress());
                    continue;
                }
                completionHealing += completion.progress().completionHealing;
                registerCompleted(completion.source().type());
                countsChanged = true;
            }
        }
        return new TickResult(
                statsChanged,
                countsChanged,
                completionHealing,
                periodicHealingPerSecond
        );
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
        int durationTicks = Math.max(1, config.ticks(TRANSFER_TICKS));
        boolean endCrystalLine = EndTowers.isEndCrystalLine(tower.type());
        boolean shulkerLine = EndTowers.isShulkerLine(tower.type());
        double maxHealth = tower.type().maxHealth();
        double damage = tower.type().damage();
        tower.setData(PROGRESS, 0.0);
        return new EndTransferState.Progress(
                durationTicks,
                shulkerLine ? maxHealth * nonNegative(config.value(ROUND_HEALTH_RATIO)) : 0.0,
                endCrystalLine ? damage * nonNegative(config.value(ROUND_DAMAGE_RATIO)) : 0.0,
                shulkerLine ? maxHealth * nonNegative(config.value(PERMANENT_HEALTH_RATIO)) : 0.0,
                endCrystalLine ? damage * nonNegative(config.value(PERMANENT_DAMAGE_RATIO)) : 0.0,
                nonNegative(config.value(TRANSFER_HEAL)),
                shulkerLine ? maxHealth * nonNegative(config.value(TRANSFER_HEAL_RATIO)) : 0.0
        );
    }

    private void registerCompleted(TowerType sourceType) {
        int tier = EndTowers.transferTier(sourceType);
        roundCompletedCount = saturatedAdd(roundCompletedCount, 1);
        if (EndTowers.isEndCrystalLine(sourceType)) {
            endCrystalCount = saturatedAdd(endCrystalCount, tier);
        } else {
            shulkerCount = saturatedAdd(shulkerCount, tier);
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
        endCrystalCount = source.endCrystalCount;
        shulkerCount = source.shulkerCount;
        roundCompletedCount = source.roundCompletedCount;
    }

    int endCrystalCount() {
        return endCrystalCount;
    }

    int shulkerCount() {
        return shulkerCount;
    }

    int roundCompletedCount() {
        return roundCompletedCount;
    }

    double permanentHealthBonus() {
        return state.permanentHealthBonus();
    }

    double permanentDamageBonus() {
        return state.permanentDamageBonus();
    }

    double roundHealthBonus() {
        return state.roundHealthContribution();
    }

    double roundDamageBonus() {
        return state.roundDamageContribution();
    }

    static double progress(Tower tower) {
        return Math.clamp(tower.getDataOrDefault(PROGRESS, 0.0), 0.0, 1.0);
    }

    static void clearProgress(Tower tower) {
        tower.removeData(PROGRESS);
    }

    private static double nonNegative(double value) {
        return Math.max(0.0, value);
    }

    private static boolean shouldEmitParticles(Tower source, int elapsedTicks) {
        return Math.floorMod(elapsedTicks + System.identityHashCode(source), 5) == 0;
    }

    private static int saturatedAdd(int value, int increment) {
        if (increment <= 0 || value >= Integer.MAX_VALUE) {
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
}
