package kim.biryeong.semiontd.tower.undead;

final class UndeadDrownedLastStandState {
    private boolean usedThisRound;
    private int remainingTicks;

    boolean tryActivate(double health, double damageAmount, int durationTicks) {
        if (usedThisRound || damageAmount <= 0.0 || damageAmount < health) {
            return false;
        }
        usedThisRound = true;
        remainingTicks = Math.max(1, durationTicks);
        return true;
    }

    void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    boolean active() {
        return remainingTicks > 0;
    }

    int remainingTicks() {
        return remainingTicks;
    }

    void resetRound() {
        usedThisRound = false;
        remainingTicks = 0;
    }
}
