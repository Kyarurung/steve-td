package kim.biryeong.semiontd.config;

public enum CurrencyType {
    DIAMOND,
    EMERALD,
    MINERAL,
    GAS;

    public boolean spendsDiamond() {
        return this == DIAMOND || this == MINERAL;
    }

    public String displayName() {
        return spendsDiamond() ? "diamond" : "emerald";
    }
}
