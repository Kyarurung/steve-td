package kim.biryeong.semiontd.config;

public record ProgressionConfig(
        long playReward,
        long winBonusReward,
        long lossReward
) {
    public ProgressionConfig {
        if (playReward < 0 || winBonusReward < 0 || lossReward < 0) {
            throw new IllegalArgumentException("Progression rewards cannot be negative.");
        }
    }

    public static ProgressionConfig defaultConfig() {
        ProgressionConfig fallback = new ProgressionConfig(10, 15, 5);
        return BundledBalanceDefaults.load("progression.json", ProgressionConfig.class, fallback);
    }

    public long rewardForWin() {
        return playReward + winBonusReward;
    }

    public long rewardForLoss() {
        return playReward + lossReward;
    }
}
