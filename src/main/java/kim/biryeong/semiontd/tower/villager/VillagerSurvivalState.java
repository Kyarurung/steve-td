package kim.biryeong.semiontd.tower.villager;

/** Tower-scoped survival progress that must survive upgrades but not a new tower. */
final class VillagerSurvivalState {
    private int stacks;

    int stacks() {
        return stacks;
    }

    boolean increment(int maximum) {
        int previous = stacks;
        stacks = Math.min(Math.max(0, maximum), stacks + 1);
        return stacks > previous;
    }

    void copyFrom(VillagerSurvivalState source, int maximum) {
        if (source != null) {
            stacks = Math.min(Math.max(0, maximum), source.stacks);
        }
    }
}
