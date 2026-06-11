package kim.biryeong.semiontd.trait;

public enum TraitSlot {
    PRIMARY(1.0D, "주특성"),
    SECONDARY(0.5D, "부특성");

    private final double effectScale;
    private final String displayName;

    TraitSlot(double effectScale, String displayName) {
        this.effectScale = effectScale;
        this.displayName = displayName;
    }

    public double effectScale() {
        return effectScale;
    }

    public String displayName() {
        return displayName;
    }
}
