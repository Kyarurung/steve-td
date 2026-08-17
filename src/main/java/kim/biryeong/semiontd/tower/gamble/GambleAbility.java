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

    public String description() {
        return switch (this) {
            case LOSS_INSURANCE -> "도박 실패로 줄어드는 능력치의 감소량을 "
                    + percent(GambleBalance.lossInsuranceReduction()) + " 줄입니다.";
            case LUCKY_STRIKE -> "일반 공격마다 주사위를 굴려 6이 나오면 주 대상 최종 피해가 "
                    + number(GambleBalance.luckyStrikeMultiplier()) + "배가 됩니다.";
            case SPREAD_BET -> GambleBalance.spreadEveryAttacks() + "번째 공격마다 주 대상 반경 "
                    + number(GambleBalance.spreadRadius()) + "칸 안의 가장 가까운 추가 적 1기에게 "
                    + "확정된 주 대상 피해의 " + percent(GambleBalance.spreadDamageRatio()) + "를 줍니다.";
        };
    }

    public String defaultDescription() {
        return switch (this) {
            case LOSS_INSURANCE -> "도박 실패로 줄어드는 능력치의 감소량을 "
                    + percent(GambleBalance.LOSS_INSURANCE_REDUCTION) + " 줄입니다.";
            case LUCKY_STRIKE -> "일반 공격마다 주사위를 굴려 6이 나오면 주 대상 최종 피해가 "
                    + number(GambleBalance.LUCKY_STRIKE_MULTIPLIER) + "배가 됩니다.";
            case SPREAD_BET -> GambleBalance.SPREAD_EVERY_ATTACKS + "번째 공격마다 주 대상 반경 "
                    + number(GambleBalance.SPREAD_RADIUS) + "칸 안의 가장 가까운 추가 적 1기에게 "
                    + "확정된 주 대상 피해의 " + percent(GambleBalance.SPREAD_DAMAGE_RATIO) + "를 줍니다.";
        };
    }

    public String detailLine() {
        return displayName + ": " + description();
    }

    public String defaultDetailLine() {
        return displayName + ": " + defaultDescription();
    }

    private static String percent(double ratio) {
        return number(ratio * 100.0) + "%";
    }

    private static String number(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) {
            return Long.toString(Math.round(value));
        }
        return java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
