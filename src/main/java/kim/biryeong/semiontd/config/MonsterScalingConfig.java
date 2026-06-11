package kim.biryeong.semiontd.config;

public record MonsterScalingConfig(
        boolean enabled,
        int survivalDelayTicks,
        int laneBreachDelayTicks,
        int intervalTicks,
        double healthGrowthPercentPerInterval,
        double attackDamageGrowthPercentPerInterval,
        boolean scaleWaveMonsters,
        boolean scaleIncomeMonsters
) {
    private static final int DEFAULT_DELAY_TICKS = 30 * 20;
    private static final int DEFAULT_INTERVAL_TICKS = 20;
    private static final double DEFAULT_GROWTH_PERCENT = 3.0;

    public MonsterScalingConfig {
        survivalDelayTicks = survivalDelayTicks < 0 ? DEFAULT_DELAY_TICKS : survivalDelayTicks;
        laneBreachDelayTicks = laneBreachDelayTicks < 0 ? DEFAULT_DELAY_TICKS : laneBreachDelayTicks;
        intervalTicks = intervalTicks <= 0 ? DEFAULT_INTERVAL_TICKS : intervalTicks;
        healthGrowthPercentPerInterval = Math.max(0.0, healthGrowthPercentPerInterval);
        attackDamageGrowthPercentPerInterval = Math.max(0.0, attackDamageGrowthPercentPerInterval);
    }

    public static MonsterScalingConfig defaultConfig() {
        return new MonsterScalingConfig(
                true,
                DEFAULT_DELAY_TICKS,
                DEFAULT_DELAY_TICKS,
                DEFAULT_INTERVAL_TICKS,
                DEFAULT_GROWTH_PERCENT,
                DEFAULT_GROWTH_PERCENT,
                true,
                true
        );
    }
}
