package kim.biryeong.semiontd.tower.adversary;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Shared rival score and per-fox evolution commitments for one player. */
public final class AdversaryProgressState {
    private final Map<UUID, RivalContribution> contributionByRival = new LinkedHashMap<>();
    private final EnumMap<RivalKind, Integer> scores = emptyScores();
    private final Map<UUID, FoxProgress> foxes = new LinkedHashMap<>();
    private long evolutionSequence;

    public synchronized int score(RivalKind kind) {
        return kind == null ? 0 : scores.getOrDefault(kind, 0);
    }

    public synchronized int spentScore(RivalKind kind) {
        if (kind == null) {
            return 0;
        }
        return foxes.values().stream()
                .flatMap(progress -> progress.steps.stream())
                .mapToInt(step -> step.cost.getOrDefault(kind, 0))
                .sum();
    }

    public synchronized int availableScore(RivalKind kind) {
        return Math.max(0, score(kind) - spentScore(kind));
    }

    public synchronized Map<RivalKind, Integer> scores() {
        return Collections.unmodifiableMap(new EnumMap<>(scores));
    }

    public synchronized int postEvolutionBonusScore() {
        int total = 0;
        for (RivalKind kind : RivalKind.values()) {
            total += availableScore(kind);
        }
        return total;
    }

    public synchronized int foxCount() {
        return foxes.size();
    }

    public synchronized void registerFox(UUID foxId, FoxForm form) {
        if (foxId == null) {
            return;
        }
        foxes.computeIfAbsent(foxId, ignored -> new FoxProgress(form == null ? FoxForm.BASE : form));
    }

    public synchronized void unregisterFox(UUID foxId) {
        if (foxId != null) {
            foxes.remove(foxId);
        }
    }

    public synchronized Optional<FoxProgressSnapshot> foxProgress(UUID foxId) {
        FoxProgress progress = foxes.get(foxId);
        return progress == null ? Optional.empty() : Optional.of(progress.snapshot());
    }

    public synchronized Optional<UUID> routeOwner(FoxRoute route) {
        if (route == null) {
            return Optional.empty();
        }
        return foxes.entrySet().stream()
                .filter(entry -> entry.getValue().lockedRoute == route)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public synchronized boolean canEvolve(UUID foxId, FoxForm current, FoxForm target) {
        FoxProgress progress = foxes.get(foxId);
        if (progress == null || current == null || target == null || progress.currentForm != current) {
            return false;
        }
        if (target.parentForm() != current || target.stage() != current.stage() + 1) {
            return false;
        }
        FoxRoute route = target.route().orElse(null);
        if (route == null
                || progress.lockedRoute != null && progress.lockedRoute != route
                || routeClaimedByAnotherFox(foxId, route)) {
            return false;
        }
        if (target.isFinal()
                && (!progress.intermediateWaveCompleted
                || progress.lockedFinalForm != null && progress.lockedFinalForm != target)) {
            return false;
        }
        return evolutionCost(current, target).entrySet().stream()
                .allMatch(entry -> availableScore(entry.getKey()) >= entry.getValue());
    }

    public synchronized boolean commitEvolution(UUID foxId, FoxForm current, FoxForm target) {
        if (!canEvolve(foxId, current, target)) {
            return false;
        }
        FoxProgress progress = foxes.get(foxId);
        FoxRoute route = target.route().orElseThrow();
        if (progress.lockedRoute == null) {
            progress.lockedRoute = route;
        }
        if (target.isFinal() && progress.lockedFinalForm == null) {
            progress.lockedFinalForm = target;
        }
        progress.steps.addLast(new EvolutionStep(
                current,
                target,
                evolutionCost(current, target),
                ++evolutionSequence
        ));
        progress.currentForm = target;
        return true;
    }

    public synchronized void recordCompletedWave(UUID foxId, FoxForm currentForm) {
        FoxProgress progress = foxes.get(foxId);
        if (progress != null && progress.currentForm == currentForm && currentForm.isIntermediate()) {
            progress.intermediateWaveCompleted = true;
        }
    }

    public synchronized Map<RivalKind, Integer> evolutionCost(FoxForm current, FoxForm target) {
        return costBetween(current, target);
    }

    public synchronized int contribution(UUID rivalId) {
        RivalContribution contribution = contributionByRival.get(rivalId);
        return contribution == null ? 0 : contribution.score();
    }

    public synchronized Optional<RivalContribution> rivalContribution(UUID rivalId) {
        return Optional.ofNullable(contributionByRival.get(rivalId));
    }

    public synchronized Collection<RivalContribution> rivalContributions() {
        return List.copyOf(contributionByRival.values());
    }

    synchronized void registerRival(UUID rivalId, RivalKind kind) {
        if (rivalId == null || kind == null || contributionByRival.containsKey(rivalId)) {
            return;
        }
        contributionByRival.put(rivalId, new RivalContribution(rivalId, kind, 0));
    }

    synchronized int recordRivalKill(UUID rivalId, RivalKind kind, boolean enhanced) {
        if (rivalId == null || kind == null) {
            return score(kind);
        }
        RivalContribution previous = contributionByRival.get(rivalId);
        int previousScore = previous != null && previous.kind() == kind ? previous.score() : 0;
        contributionByRival.put(rivalId, new RivalContribution(
                rivalId,
                kind,
                previousScore + kind.scorePerKill(enhanced)
        ));
        rebuildScores();
        return score(kind);
    }

    public synchronized int removeRival(UUID rivalId) {
        RivalContribution removed = contributionByRival.remove(rivalId);
        if (removed == null) {
            return 0;
        }
        rebuildScores();
        reconcileDemotions();
        return removed.score();
    }

    public synchronized void transferRival(UUID previousRivalId, UUID nextRivalId) {
        if (previousRivalId == null || nextRivalId == null || previousRivalId.equals(nextRivalId)) {
            return;
        }
        RivalContribution contribution = contributionByRival.remove(previousRivalId);
        if (contribution != null) {
            contributionByRival.put(
                    nextRivalId,
                    new RivalContribution(nextRivalId, contribution.kind(), contribution.score())
            );
        }
    }

    /** Replaces installed rival ledgers and returns every required parent-step demotion in order. */
    public synchronized List<FoxDemotion> reconcileRivals(Collection<RivalContribution> snapshots) {
        contributionByRival.clear();
        if (snapshots != null) {
            for (RivalContribution snapshot : snapshots) {
                if (snapshot != null) {
                    contributionByRival.put(snapshot.rivalId(), snapshot);
                }
            }
        }
        rebuildScores();
        return reconcileDemotions();
    }

    private List<FoxDemotion> reconcileDemotions() {
        List<FoxDemotion> demotions = new ArrayList<>();
        while (hasScoreDeficit()) {
            Map.Entry<UUID, FoxProgress> candidate = newestFoxUsingDeficientScore();
            if (candidate == null) {
                break;
            }
            EvolutionStep step = candidate.getValue().steps.pollLast();
            if (step == null) {
                break;
            }
            candidate.getValue().currentForm = step.previous;
            demotions.add(new FoxDemotion(candidate.getKey(), step.current, step.previous));
        }
        return List.copyOf(demotions);
    }

    private Map.Entry<UUID, FoxProgress> newestFoxUsingDeficientScore() {
        Map.Entry<UUID, FoxProgress> newest = null;
        long newestSequence = Long.MIN_VALUE;
        for (Map.Entry<UUID, FoxProgress> entry : foxes.entrySet()) {
            FoxProgress progress = entry.getValue();
            EvolutionStep top = progress.steps.peekLast();
            if (top == null || !usesDeficientScore(progress) || top.sequence <= newestSequence) {
                continue;
            }
            newest = entry;
            newestSequence = top.sequence;
        }
        return newest;
    }

    private boolean usesDeficientScore(FoxProgress progress) {
        for (RivalKind kind : RivalKind.values()) {
            if (spentScore(kind) > score(kind)
                    && progress.steps.stream().anyMatch(step -> step.cost.getOrDefault(kind, 0) > 0)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasScoreDeficit() {
        for (RivalKind kind : RivalKind.values()) {
            if (spentScore(kind) > score(kind)) {
                return true;
            }
        }
        return false;
    }

    private boolean routeClaimedByAnotherFox(UUID foxId, FoxRoute route) {
        return foxes.entrySet().stream()
                .anyMatch(entry -> !entry.getKey().equals(foxId) && entry.getValue().lockedRoute == route);
    }

    private static Map<RivalKind, Integer> costBetween(FoxForm current, FoxForm target) {
        EvolutionRecipe currentRecipe = current == null ? null : current.recipe().orElse(null);
        EvolutionRecipe targetRecipe = target == null ? null : target.recipe().orElse(null);
        if (targetRecipe == null) {
            return Map.of();
        }
        EnumMap<RivalKind, Integer> result = new EnumMap<>(RivalKind.class);
        for (RivalKind kind : RivalKind.values()) {
            int previous = currentRecipe == null ? 0 : currentRecipe.required(kind);
            int delta = Math.max(0, targetRecipe.required(kind) - previous);
            if (delta > 0) {
                result.put(kind, delta);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private void rebuildScores() {
        scores.clear();
        scores.putAll(emptyScores());
        for (RivalContribution contribution : contributionByRival.values()) {
            scores.merge(contribution.kind(), contribution.score(), Integer::sum);
        }
    }

    private static EnumMap<RivalKind, Integer> emptyScores() {
        EnumMap<RivalKind, Integer> result = new EnumMap<>(RivalKind.class);
        for (RivalKind kind : RivalKind.values()) {
            result.put(kind, 0);
        }
        return result;
    }

    private static final class FoxProgress {
        private FoxForm currentForm;
        private FoxRoute lockedRoute;
        private FoxForm lockedFinalForm;
        private boolean intermediateWaveCompleted;
        private final Deque<EvolutionStep> steps = new ArrayDeque<>();

        private FoxProgress(FoxForm currentForm) {
            this.currentForm = Objects.requireNonNull(currentForm, "currentForm");
            this.lockedRoute = currentForm.route().orElse(null);
            this.lockedFinalForm = currentForm.isFinal() ? currentForm : null;
        }

        private FoxProgressSnapshot snapshot() {
            return new FoxProgressSnapshot(
                    currentForm,
                    Optional.ofNullable(lockedRoute),
                    Optional.ofNullable(lockedFinalForm),
                    intermediateWaveCompleted
            );
        }
    }

    private record EvolutionStep(
            FoxForm previous,
            FoxForm current,
            Map<RivalKind, Integer> cost,
            long sequence
    ) {
    }

    public record FoxProgressSnapshot(
            FoxForm currentForm,
            Optional<FoxRoute> lockedRoute,
            Optional<FoxForm> lockedFinalForm,
            boolean intermediateWaveCompleted
    ) {
        public FoxProgressSnapshot {
            Objects.requireNonNull(currentForm, "currentForm");
            lockedRoute = lockedRoute == null ? Optional.empty() : lockedRoute;
            lockedFinalForm = lockedFinalForm == null ? Optional.empty() : lockedFinalForm;
        }
    }

    public record FoxDemotion(UUID foxId, FoxForm previous, FoxForm current) {
        public FoxDemotion {
            Objects.requireNonNull(foxId, "foxId");
            Objects.requireNonNull(previous, "previous");
            Objects.requireNonNull(current, "current");
        }
    }
}
