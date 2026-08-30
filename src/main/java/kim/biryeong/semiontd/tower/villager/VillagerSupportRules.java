package kim.biryeong.semiontd.tower.villager;

final class VillagerSupportRules {
    private VillagerSupportRules() {
    }

    static int reducedTicks(int baseTicks, double reduction, int minimumTicks) {
        return Math.max(minimumTicks, (int) Math.ceil(baseTicks * Math.max(0.01, 1.0 - reduction)));
    }

    static boolean canApplyAt(long blockedUntil, long gameTime) {
        return blockedUntil <= gameTime;
    }
}
