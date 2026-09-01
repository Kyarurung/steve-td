package kim.biryeong.semiontd.tower.ancientcity;

public final class AncientCityRules {
    private AncientCityRules() {
    }

    public static double resonanceBonus(int territoryCount, int maxSculk, int resonanceFullAt, double cap) {
        int boundedMax = Math.max(1, maxSculk);
        int fullAt = Math.min(boundedMax, Math.max(1, resonanceFullAt));
        return Math.min(Math.max(0.0, cap), Math.max(0, territoryCount) / (double) fullAt * Math.max(0.0, cap));
    }

    public static double combinedMagicBonus(double bonus, double cap) {
        return Math.min(Math.max(0.0, cap), Math.max(0.0, bonus));
    }

    public static double incomeAdjustedMagicDamage(double damage, boolean incomeTarget, double multiplier) {
        return incomeTarget ? damage * Math.max(0.0, multiplier) : damage;
    }
}
