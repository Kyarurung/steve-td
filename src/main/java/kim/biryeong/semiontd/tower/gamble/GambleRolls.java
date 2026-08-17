package kim.biryeong.semiontd.tower.gamble;

public final class GambleRolls {
    private GambleRolls() {
    }

    public static double oddEvenDelta(GambleBet bet, int die) {
        requireDie(die);
        if (bet != GambleBet.ODD && bet != GambleBet.EVEN) {
            throw new IllegalArgumentException("Odd/even roll requires an odd or even bet.");
        }
        return bet.matchesParity(die) ? GambleBalance.oddEvenWinScore() : -GambleBalance.oddEvenLossScore();
    }

    public static double twoDiceDelta(int first, int second) {
        requireDie(first);
        requireDie(second);
        double score = GambleBalance.twoDiceScore(first + second);
        return first == second ? score * 2.0 : score;
    }

    public static double defaultTwoDiceDelta(int first, int second) {
        requireDie(first);
        requireDie(second);
        double delta = switch (first + second) {
            case 2 -> -80.0;
            case 3 -> -60.0;
            case 4 -> -40.0;
            case 5 -> -28.0;
            case 6 -> 20.0;
            case 7 -> 40.0;
            case 8 -> 50.0;
            case 9 -> 60.0;
            case 10 -> 75.0;
            case 11 -> 90.0;
            case 12 -> 100.0;
            default -> throw new IllegalStateException("Unreachable dice sum");
        };
        return first == second ? delta * 2.0 : delta;
    }

    public static double defaultExpectedTwoDiceDelta() {
        double total = 0.0;
        for (int first = 1; first <= 6; first++) {
            for (int second = 1; second <= 6; second++) {
                total += defaultTwoDiceDelta(first, second);
            }
        }
        return total / 36.0;
    }

    public static double expectedTwoDiceDelta() {
        double total = 0.0;
        for (int first = 1; first <= 6; first++) {
            for (int second = 1; second <= 6; second++) {
                total += twoDiceDelta(first, second);
            }
        }
        return total / 36.0;
    }

    public static boolean luckyStrike(int die) {
        requireDie(die);
        return die == 6;
    }

    public static double luckyStrikeDamage(double finalizedDamage, int die, double multiplier) {
        requireDie(die);
        double safeDamage = Double.isFinite(finalizedDamage) ? Math.max(0.0, finalizedDamage) : 0.0;
        double safeMultiplier = Double.isFinite(multiplier) ? Math.max(1.0, multiplier) : 1.0;
        return luckyStrike(die) ? safeDamage * safeMultiplier : safeDamage;
    }

    private static void requireDie(int die) {
        if (die < 1 || die > 6) {
            throw new IllegalArgumentException("Die must be between 1 and 6: " + die);
        }
    }
}
