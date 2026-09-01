package kim.biryeong.semiontd.tower.resonance;

import kim.biryeong.semiontd.game.GridPosition;

final class ResonanceRules {
    private ResonanceRules() {
    }

    static int level(int linkedTowers, int maximumLevel, int level1Links, int level2Links, int level3Links) {
        if (maximumLevel >= 3 && linkedTowers >= level3Links) {
            return 3;
        }
        if (maximumLevel >= 2 && linkedTowers >= level2Links) {
            return 2;
        }
        if (maximumLevel >= 1 && linkedTowers >= level1Links) {
            return 1;
        }
        return 0;
    }

    static int adjustedAttackInterval(int baseIntervalTicks, double speedBonus) {
        return Math.max(1, (int) Math.ceil(baseIntervalTicks / Math.max(0.01, 1.0 + speedBonus)));
    }

    static double adjustedDamage(double damage, double bonus) {
        return damage * Math.max(0.0, 1.0 + bonus);
    }

    static double reducedDamage(double damage, double reduction) {
        return damage * Math.max(0.0, 1.0 - reduction);
    }

    static int distance(GridPosition first, GridPosition second) {
        if (first == null || second == null) {
            return Integer.MAX_VALUE;
        }
        return Math.max(
                Math.abs(first.x() - second.x()),
                Math.max(Math.abs(first.y() - second.y()), Math.abs(first.z() - second.z()))
        );
    }
}
