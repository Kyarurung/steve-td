package kim.biryeong.semiontd.tower.animal;

final class AnimalCombat {
    private AnimalCombat() {
    }

    static double addStackDamage(double baseDamage, int stacks, double damagePerStack) {
        return baseDamage + Math.max(0, stacks) * damagePerStack;
    }

    static double reduceIncomingDamage(double damageAmount, double reduction) {
        return damageAmount * (1.0 - Math.min(0.95, Math.max(0.0, reduction)));
    }

    static int clampAttackInterval(int adjustedTicks) {
        return Math.max(1, adjustedTicks);
    }

    static double splashDamage(double damageAmount, double ratio) {
        return damageAmount * ratio;
    }
}
