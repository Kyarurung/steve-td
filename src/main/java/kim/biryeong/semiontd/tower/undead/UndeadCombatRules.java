package kim.biryeong.semiontd.tower.undead;

final class UndeadCombatRules {
    private UndeadCombatRules() {
    }

    static double lifeStealAmount(double damageAmount, double ratio) {
        return damageAmount > 0.0 && ratio > 0.0 ? damageAmount * ratio : 0.0;
    }

    static double addCappedDamage(double current, double step, double maximum) {
        return Math.min(maximum, current + step);
    }

    static int addCappedStack(int current, int maximum) {
        return Math.min(maximum, current + 1);
    }
}
