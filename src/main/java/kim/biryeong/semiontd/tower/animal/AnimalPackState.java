package kim.biryeong.semiontd.tower.animal;

/** Derived tower-scoped pack state. It is recomputed from the lane and is not copied on upgrade. */
final class AnimalPackState {
    private int stacks;
    private boolean leaderAuraActive;
    private boolean livingLeaderExists;

    int stacks() { return stacks; }

    void stacks(int stacks) { this.stacks = Math.max(0, stacks); }

    boolean leaderAuraActive() { return leaderAuraActive; }

    void leaderAuraActive(boolean active) { leaderAuraActive = active; }

    boolean livingLeaderExists() { return livingLeaderExists; }

    void livingLeaderExists(boolean exists) { livingLeaderExists = exists; }
}
