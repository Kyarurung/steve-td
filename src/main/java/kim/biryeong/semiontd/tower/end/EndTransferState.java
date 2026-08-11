package kim.biryeong.semiontd.tower.end;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import kim.biryeong.semiontd.tower.Tower;

final class EndTransferState {
    private final Map<Tower, Progress> progressByTower = new IdentityHashMap<>();
    private final Set<Tower> presentTowerSnapshot =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private double permanentHealthBonus;
    private double permanentDamageBonus;
    private double roundHealthContribution;
    private double roundDamageContribution;

    void beginSnapshot() {
        presentTowerSnapshot.clear();
    }

    void markPresent(Tower tower) {
        presentTowerSnapshot.add(tower);
    }

    boolean isPresent(Tower tower) {
        return presentTowerSnapshot.contains(tower);
    }

    void ensureProgress(Tower tower, Function<Tower, Progress> factory) {
        progressByTower.computeIfAbsent(tower, factory);
    }

    Set<Map.Entry<Tower, Progress>> progressEntries() {
        return progressByTower.entrySet();
    }

    void clearProgress() {
        progressByTower.clear();
    }

    void resetRoundContributions() {
        roundHealthContribution = 0.0;
        roundDamageContribution = 0.0;
    }

    boolean apply(Progress progress) {
        double ratio = Math.min(1.0, progress.elapsedTicks / (double) progress.durationTicks);
        double delta = Math.max(0.0, ratio - progress.appliedRatio);
        if (delta <= 0.0) {
            return false;
        }
        progress.appliedRatio = ratio;
        roundHealthContribution += progress.roundHealthBonus * delta;
        roundDamageContribution += progress.roundDamageBonus * delta;
        permanentHealthBonus += progress.permanentHealthBonus * delta;
        permanentDamageBonus += progress.permanentDamageBonus * delta;
        return true;
    }

    boolean rollback(Progress progress) {
        if (progress == null || progress.appliedRatio <= 0.0) {
            return false;
        }
        roundHealthContribution = subtract(
                roundHealthContribution,
                progress.roundHealthBonus * progress.appliedRatio
        );
        roundDamageContribution = subtract(
                roundDamageContribution,
                progress.roundDamageBonus * progress.appliedRatio
        );
        permanentHealthBonus = subtract(
                permanentHealthBonus,
                progress.permanentHealthBonus * progress.appliedRatio
        );
        permanentDamageBonus = subtract(
                permanentDamageBonus,
                progress.permanentDamageBonus * progress.appliedRatio
        );
        progress.appliedRatio = 0.0;
        return true;
    }

    void copyBonusesFrom(EndTransferState source) {
        permanentHealthBonus = source.permanentHealthBonus;
        permanentDamageBonus = source.permanentDamageBonus;
        roundHealthContribution = source.roundHealthContribution;
        roundDamageContribution = source.roundDamageContribution;
    }

    double permanentHealthBonus() {
        return permanentHealthBonus;
    }

    double permanentDamageBonus() {
        return permanentDamageBonus;
    }

    double roundHealthContribution() {
        return roundHealthContribution;
    }

    double roundDamageContribution() {
        return roundDamageContribution;
    }

    private static double subtract(double value, double amount) {
        return Math.max(0.0, value - amount);
    }

    static final class Progress {
        final int durationTicks;
        final double roundHealthBonus;
        final double roundDamageBonus;
        final double permanentHealthBonus;
        final double permanentDamageBonus;
        final double completionHealing;
        final double periodicHealingPerSecond;
        int elapsedTicks;
        double appliedRatio;

        Progress(
                int durationTicks,
                double roundHealthBonus,
                double roundDamageBonus,
                double permanentHealthBonus,
                double permanentDamageBonus,
                double completionHealing,
                double periodicHealingPerSecond
        ) {
            this.durationTicks = durationTicks;
            this.roundHealthBonus = roundHealthBonus;
            this.roundDamageBonus = roundDamageBonus;
            this.permanentHealthBonus = permanentHealthBonus;
            this.permanentDamageBonus = permanentDamageBonus;
            this.completionHealing = completionHealing;
            this.periodicHealingPerSecond = periodicHealingPerSecond;
        }
    }
}
