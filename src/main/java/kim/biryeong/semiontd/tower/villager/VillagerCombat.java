package kim.biryeong.semiontd.tower.villager;

final class VillagerCombat {
    private VillagerCombat() {
    }

    static double survivalBonus(double bonusPerRound, int stacks, double multiplier) {
        return bonusPerRound * Math.max(0, stacks) * multiplier;
    }

    static double addPercentBonus(double base, double bonus) {
        return base * (1.0 + bonus);
    }

    static int reduceInterval(int baseTicks, double reduction) {
        return (int) (baseTicks * (1.0 - reduction));
    }

    static double addCappedDamage(double current, double step, double maximum) {
        return Math.min(maximum, current + step);
    }
}
