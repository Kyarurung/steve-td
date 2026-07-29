package kim.biryeong.semiontd.tower.end;

public enum EndTowerState {
    EGG,
    PHANTOM,
    DRAGON;

    public boolean hatched() {
        return this == PHANTOM || this == DRAGON;
    }

    public static EndTowerState evolvedState(double currentMaxHealth, double evolutionThreshold) {
        return currentMaxHealth >= evolutionThreshold ? DRAGON : PHANTOM;
    }
}
