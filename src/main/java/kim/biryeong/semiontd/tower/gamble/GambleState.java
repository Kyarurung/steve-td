package kim.biryeong.semiontd.tower.gamble;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public record GambleState(
        double maxHealthDelta,
        double damageDelta,
        double rangeDelta,
        Set<GambleAbility> abilities,
        int totalBets,
        String lastResult
) {
    public static final GambleState EMPTY = new GambleState(0.0, 0.0, 0.0, Set.of(), 0, "도박 전");

    public GambleState {
        maxHealthDelta = sanitizeDelta(maxHealthDelta);
        damageDelta = sanitizeDelta(damageDelta);
        rangeDelta = sanitizeDelta(rangeDelta);
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
        };
    }

    public boolean has(GambleAbility ability) {
        return abilities.contains(ability);
    }

    public GambleState recordStat(GambleStat stat, double amount, double baseValue, String result) {
        double health = maxHealthDelta;
        double damage = damageDelta;
        double range = rangeDelta;
        double minimumDelta = -Math.max(0.0, baseValue) * 0.80;
        switch (stat) {
            case MAX_HEALTH -> health = Math.max(minimumDelta, sanitizeDelta(health + amount));
            case DAMAGE -> damage = Math.max(minimumDelta, sanitizeDelta(damage + amount));
            case RANGE -> range = Math.max(minimumDelta, sanitizeDelta(range + amount));
        }
        return new GambleState(health, damage, range, abilities, totalBets + 1, result);
    }

    public GambleState recordAbility(GambleAbility ability, String result) {
        EnumSet<GambleAbility> updated = abilities.isEmpty()
                ? EnumSet.noneOf(GambleAbility.class)
                : EnumSet.copyOf(abilities);
        if (ability != null) {
            updated.add(ability);
        }
        return new GambleState(maxHealthDelta, damageDelta, rangeDelta, updated, totalBets + 1, result);
    }

    private static double sanitizeDelta(double value) {
        return Double.isFinite(value) ? value : 0.0;
    }
}
