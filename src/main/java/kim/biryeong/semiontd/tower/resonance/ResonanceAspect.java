package kim.biryeong.semiontd.tower.resonance;

public enum ResonanceAspect {
    FOCUS("집중"),
    WAVE("파동"),
    FROST("결빙"),
    AMPLIFY("만개");

    private final String displayName;

    ResonanceAspect(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
