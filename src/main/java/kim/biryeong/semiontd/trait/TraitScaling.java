package kim.biryeong.semiontd.trait;

public final class TraitScaling {
    private TraitScaling() {
    }

    public static long scaleDelta(long base, long modifiedAtFullPower, TraitSlot slot) {
        long delta = modifiedAtFullPower - base;
        return base + Math.round(delta * slot.effectScale());
    }

    public static double scaleDelta(double base, double modifiedAtFullPower, TraitSlot slot) {
        double delta = modifiedAtFullPower - base;
        return base + delta * slot.effectScale();
    }
}
