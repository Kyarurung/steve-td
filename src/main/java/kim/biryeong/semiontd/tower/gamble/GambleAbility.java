package kim.biryeong.semiontd.tower.gamble;

public enum GambleAbility {
    LOSS_INSURANCE("손실 보험");

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
                    + percent(GambleBalance.lossInsuranceReduction())
                    + " 줄이며 홀수·짝수 맞히기와 주사위 두 개 굴리기에 모두 적용됩니다.";
        };
    }

    public String defaultDescription() {
        return switch (this) {
            case LOSS_INSURANCE -> "도박 실패로 줄어드는 능력치의 감소량을 "
                    + percent(GambleBalance.LOSS_INSURANCE_REDUCTION)
                    + " 줄이며 홀수·짝수 맞히기와 주사위 두 개 굴리기에 모두 적용됩니다.";
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
