package kim.biryeong.semiontd.tower.ocean;

public final class OceanRules {
    private static final double EPSILON = 1.0E-9;

    private OceanRules() {
    }

    public static double waterRoot(double water, double softCap, double scale) {
        double boundedCap = Math.max(EPSILON, softCap);
        double effectiveWater = water <= boundedCap
                ? Math.max(0.0, water)
                : boundedCap + boundedCap * Math.log1p((water - boundedCap) / boundedCap);
        return Math.sqrt(effectiveWater / Math.max(EPSILON, scale));
    }

    public static double damageMultiplier(double water, double coefficient, double softCap, double scale) {
        return 1.0 + coefficient * waterRoot(water, softCap, scale);
    }

    public static double stackedSupplyMultiplier(int sourceCount, double decay) {
        int sources = Math.max(1, sourceCount);
        double clampedDecay = Math.max(0.0, Math.min(1.0, decay));
        if (sources == 1 || 1.0 - clampedDecay <= EPSILON) {
            return 1.0;
        }
        return (1.0 - Math.pow(clampedDecay, sources)) / (sources * (1.0 - clampedDecay));
    }

    public static double supplyEfficiency(double water, double softCap, double stopThreshold) {
        double boundedSoftCap = Math.max(0.0, softCap);
        double boundedStop = Math.max(boundedSoftCap, stopThreshold);
        double efficiency = (boundedStop - water) / Math.max(EPSILON, boundedStop - boundedSoftCap);
        return Math.max(0.0, Math.min(1.0, efficiency));
    }

    public static double distanceSquared(int firstX, int firstY, int firstZ, int secondX, int secondY, int secondZ) {
        double x = firstX - secondX;
        double y = firstY - secondY;
        double z = firstZ - secondZ;
        return x * x + y * y + z * z;
    }
}
