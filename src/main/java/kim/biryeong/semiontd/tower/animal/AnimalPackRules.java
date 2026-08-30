package kim.biryeong.semiontd.tower.animal;

final class AnimalPackRules {
    private AnimalPackRules() {
    }

    static int cappedStacks(long matchingTowerCount, int maximum) {
        return (int) Math.min(Math.max(0, maximum), Math.max(0L, matchingTowerCount));
    }

    static boolean canUpgradeToLeader(boolean isLeaderBase, int stacks, int maximum, boolean livingLeaderExists) {
        return isLeaderBase && stacks >= maximum && !livingLeaderExists;
    }

    static boolean withinAura(double squaredDistance, double radius) {
        double nonNegativeRadius = Math.max(0.0, radius);
        return squaredDistance <= nonNegativeRadius * nonNegativeRadius;
    }
}
