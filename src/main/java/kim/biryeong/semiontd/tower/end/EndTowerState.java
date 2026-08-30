package kim.biryeong.semiontd.tower.end;

public enum EndTowerState {
    EGG,
    PHANTOM,
    DRAGON;

    public boolean hatched() {
        return this == PHANTOM || this == DRAGON;
    }
}
