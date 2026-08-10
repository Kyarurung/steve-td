package kim.biryeong.semiontd.tower;

public final class DamageScaling {
    private DamageScaling() {
    }

    public static double logarithmicBonus(double rawBonus, double softCap) {
        if (!Double.isFinite(rawBonus) || rawBonus <= 0.0
                || !Double.isFinite(softCap) || softCap <= 0.0) {
            return 0.0;
        }
        return rawBonus <= softCap
                ? rawBonus
                : softCap + softCap * Math.log1p((rawBonus - softCap) / softCap);
    }
}
