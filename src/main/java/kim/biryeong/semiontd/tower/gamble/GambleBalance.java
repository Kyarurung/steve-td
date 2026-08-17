package kim.biryeong.semiontd.tower.gamble;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.tower.TowerType;

public final class GambleBalance {
    public static final String GLOBAL_ID = "gamble_global";
    public static final double ODD_EVEN_WIN_SCORE = 10.0;
    public static final double ODD_EVEN_LOSS_SCORE = 8.0;
    public static final double MAX_HEALTH_PER_SCORE = 1.0;
    public static final double DAMAGE_PER_SCORE = 0.10;
    public static final double RANGE_PER_SCORE = 0.025;
    public static final double ABILITY_REWARD_CHANCE = 0.25;
    public static final double LOSS_INSURANCE_REDUCTION = 0.20;
    public static final double LUCKY_STRIKE_MULTIPLIER = 2.0;
    public static final int SPREAD_EVERY_ATTACKS = 4;
    public static final double SPREAD_DAMAGE_RATIO = 0.50;
    public static final double SPREAD_RADIUS = 3.0;

    private GambleBalance() {
    }

    public static double oddEvenWinScore() {
        return global("oddEvenWinScore", ODD_EVEN_WIN_SCORE);
    }

    public static double oddEvenLossScore() {
        return global("oddEvenLossScore", ODD_EVEN_LOSS_SCORE);
    }

    public static double abilityRewardChance() {
        return global("abilityRewardChance", ABILITY_REWARD_CHANCE);
    }

    public static double lossInsuranceReduction() {
        return global("lossInsuranceReduction", LOSS_INSURANCE_REDUCTION);
    }

    public static double luckyStrikeMultiplier() {
        return global("luckyStrikeMultiplier", LUCKY_STRIKE_MULTIPLIER);
    }

    public static int spreadEveryAttacks() {
        return Math.max(1, TowerBalanceRuntime.abilityInt(
                GLOBAL_ID, "spreadEveryAttacks", SPREAD_EVERY_ATTACKS
        ));
    }

    public static double spreadDamageRatio() {
        return global("spreadDamageRatio", SPREAD_DAMAGE_RATIO);
    }

    public static double spreadRadius() {
        return global("spreadRadius", SPREAD_RADIUS);
    }

    public static double twoDiceScore(int sum) {
        if (sum < 2 || sum > 12) {
            throw new IllegalArgumentException("Two-dice sum must be between 2 and 12: " + sum);
        }
        if (sum <= 5) {
            double[] defaults = {0.0, 0.0, 40.0, 25.0, 15.0, 5.0};
            return -global("twoDiceLoss" + sum, defaults[sum]);
        }
        double[] defaults = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0, 4.0, 7.0, 11.0, 16.0, 24.0, 40.0};
        return global("twoDiceGain" + sum, defaults[sum]);
    }

    public static double statDelta(GambleStat stat, double score) {
        double perScore = switch (stat) {
            case MAX_HEALTH -> global("maxHealthPerScore", MAX_HEALTH_PER_SCORE);
            case DAMAGE -> global("damagePerScore", DAMAGE_PER_SCORE);
            case RANGE -> global("rangePerScore", RANGE_PER_SCORE);
        };
        return score * perScore;
    }

    public static int minimumRoll(TowerType type) {
        return Math.max(1, Math.min(6, TowerBalanceRuntime.abilityInt(type.id(), "minimumRoll", 1)));
    }

    public static double positiveMultiplier(TowerType type) {
        return Math.max(0.0, TowerBalanceRuntime.ability(type.id(), "positiveMultiplier", 1.0));
    }

    public static TimedEffectType supportEffectType(int face) {
        return switch (face) {
            case 1 -> TimedEffectType.TOWER_DAMAGE_TAKEN_BONUS;
            case 2 -> TimedEffectType.TOWER_ATTACK_SPEED_REDUCTION;
            case 3 -> TimedEffectType.TOWER_RANGE_BONUS;
            case 4 -> TimedEffectType.TOWER_MAX_HEALTH_BONUS;
            case 5 -> TimedEffectType.TOWER_ATTACK_SPEED_BONUS;
            case 6 -> TimedEffectType.TOWER_DAMAGE_BONUS;
            default -> throw new IllegalArgumentException("Support die must be between 1 and 6: " + face);
        };
    }

    public static double supportMagnitude(int face, double positiveMultiplier) {
        double base = switch (face) {
            case 1 -> global("supportFace1DamageTaken", 0.30);
            case 2 -> global("supportFace2AttackSpeedReduction", 0.15);
            case 3 -> global("supportFace3RangeBonus", 0.05);
            case 4 -> global("supportFace4MaxHealthBonus", 0.10);
            case 5 -> global("supportFace5AttackSpeedBonus", 0.15);
            case 6 -> global("supportFace6DamageBonus", 0.25);
            default -> throw new IllegalArgumentException("Support die must be between 1 and 6: " + face);
        };
        return face <= 2 ? base : base * Math.max(0.0, positiveMultiplier);
    }

    private static double global(String key, double fallback) {
        return TowerBalanceRuntime.ability(GLOBAL_ID, key, fallback);
    }
}
