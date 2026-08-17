package kim.biryeong.semiontd.tower.gamble;

public enum GambleAbility {
    LOSS_INSURANCE("손실 보험"),
    LUCKY_STRIKE("행운의 일격"),
    SPREAD_BET("분산 배당");

    private final String displayName;

    GambleAbility(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
