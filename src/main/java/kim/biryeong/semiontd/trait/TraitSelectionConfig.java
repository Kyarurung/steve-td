package kim.biryeong.semiontd.trait;

import kim.biryeong.semiontd.config.BundledBalanceDefaults;

public record TraitSelectionConfig(boolean enabled, int selectionDurationSeconds) {
    public static final int DEFAULT_SELECTION_DURATION_SECONDS = 45;

    public TraitSelectionConfig {
        selectionDurationSeconds = selectionDurationSeconds <= 0
                ? DEFAULT_SELECTION_DURATION_SECONDS
                : selectionDurationSeconds;
    }

    public static TraitSelectionConfig defaultConfig() {
        TraitSelectionConfig fallback = new TraitSelectionConfig(true, DEFAULT_SELECTION_DURATION_SECONDS);
        return BundledBalanceDefaults.load("traits.json", TraitSelectionConfig.class, fallback);
    }

    public int selectionDurationTicks() {
        return selectionDurationSeconds * 20;
    }
}
