package kim.biryeong.semiontd.tower.gamble;

import kim.biryeong.semiontd.effect.TimedEffectType;

public record GambleSupportEffect(GambleSupportStat stat, boolean positive, double magnitude) {
    public GambleSupportEffect {
        if (stat == null) {
            throw new IllegalArgumentException("Gamble support stat is required.");
        }
        magnitude = Double.isFinite(magnitude) ? Math.max(0.0, magnitude) : 0.0;
    }

    public TimedEffectType type() {
        return stat.effectType(positive);
    }

    public String displayLine() {
        return stat.displayName() + " " + (positive ? "+" : "-") + number(magnitude);
    }

    private static String number(double value) {
        return java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
