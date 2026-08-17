package kim.biryeong.semiontd.tower.gamble;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record GambleState(
        double maxHealthDelta,
        double damageDelta,
        double rangeDelta,
        double splashRadiusDelta,
        double cumulativeScore,
        Set<GambleAbility> abilities,
        int totalBets,
        String lastResult
) {
    public static final GambleState EMPTY = new GambleState(
            0.0, 0.0, 0.0, 0.0, 0.0, Set.of(), 0, "도박 전");

    public GambleState {
        maxHealthDelta = sanitizeDelta(maxHealthDelta);
        damageDelta = sanitizeDelta(damageDelta);
        rangeDelta = sanitizeDelta(rangeDelta);
        splashRadiusDelta = sanitizeDelta(splashRadiusDelta);
        cumulativeScore = sanitizeDelta(cumulativeScore);
        EnumSet<GambleAbility> copied = abilities == null || abilities.isEmpty()
                ? EnumSet.noneOf(GambleAbility.class)
                : EnumSet.copyOf(abilities);
        abilities = Collections.unmodifiableSet(copied);
        totalBets = Math.max(0, totalBets);
        lastResult = lastResult == null || lastResult.isBlank() ? "도박 전" : lastResult;
    }

    public double resolvedValue(GambleStat stat, double baseValue) {
        double safeBase = Math.max(0.0, baseValue);
        return Math.max(safeBase * 0.20, safeBase + delta(stat));
    }

    public double delta(GambleStat stat) {
        return switch (stat) {
            case MAX_HEALTH -> maxHealthDelta;
            case DAMAGE -> damageDelta;
            case RANGE -> rangeDelta;
            case SPLASH_RADIUS -> splashRadiusDelta;
        };
    }

    public boolean has(GambleAbility ability) {
        return abilities.contains(ability);
    }

    public GambleState recordStat(
            GambleStat stat, double amount, double baseValue, double score, String result
    ) {
        return recordStats(List.of(new StatChange(stat, amount, baseValue)), score, result);
    }

    public GambleState recordStats(List<StatChange> changes, double score, String result) {
        double health = maxHealthDelta;
        double damage = damageDelta;
        double range = rangeDelta;
        double splashRadius = splashRadiusDelta;
        for (StatChange change : changes == null ? List.<StatChange>of() : changes) {
            if (change == null || change.stat() == null) {
                continue;
            }
            double minimumDelta = -Math.max(0.0, change.baseValue()) * 0.80;
            switch (change.stat()) {
                case MAX_HEALTH -> health = Math.max(
                        minimumDelta, sanitizeDelta(health + change.amount()));
                case DAMAGE -> damage = Math.max(
                        minimumDelta, sanitizeDelta(damage + change.amount()));
                case RANGE -> range = Math.max(
                        minimumDelta, sanitizeDelta(range + change.amount()));
                case SPLASH_RADIUS -> splashRadius = Math.max(
                        minimumDelta, sanitizeDelta(splashRadius + change.amount()));
            }
        }
        return new GambleState(health, damage, range, splashRadius,
                cumulativeScore + sanitizeDelta(score), abilities, totalBets + 1, result);
    }

    public GambleState recordAbility(GambleAbility ability, double score, String result) {
        EnumSet<GambleAbility> updated = abilities.isEmpty()
                ? EnumSet.noneOf(GambleAbility.class)
                : EnumSet.copyOf(abilities);
        if (ability != null) {
            updated.add(ability);
        }
        return new GambleState(maxHealthDelta, damageDelta, rangeDelta, splashRadiusDelta,
                cumulativeScore + sanitizeDelta(score), updated, totalBets + 1, result);
    }

    private static double sanitizeDelta(double value) {
        return Double.isFinite(value) ? value : 0.0;
    }

    public record StatChange(GambleStat stat, double amount, double baseValue) {
    }
}
