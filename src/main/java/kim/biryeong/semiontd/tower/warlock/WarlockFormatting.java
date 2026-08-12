package kim.biryeong.semiontd.tower.warlock;

public final class WarlockFormatting {
    private static final String WARLOCK_COLOR = "dark_purple";
    private static final String WARNING_COLOR = "dark_red";

    private WarlockFormatting() {
    }

    public static String warlockText(String text) {
        return "<" + WARLOCK_COLOR + ">" + text + "</" + WARLOCK_COLOR + ">";
    }

    public static String warningText(String text) {
        return "<" + WARNING_COLOR + ">" + text + "</" + WARNING_COLOR + ">";
    }
}
