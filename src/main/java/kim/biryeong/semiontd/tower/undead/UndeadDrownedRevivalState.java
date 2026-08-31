package kim.biryeong.semiontd.tower.undead;

import java.util.OptionalDouble;

final class UndeadDrownedRevivalState {
    private boolean usedThisRound;
    private int totalDecayTicks;
    private int elapsedDecayTicks;

    boolean tryRevive(double health, double damageAmount, int decayTicks) {
        if (usedThisRound || damageAmount <= 0.0 || damageAmount < health) {
            return false;
        }
        usedThisRound = true;
        totalDecayTicks = Math.max(1, decayTicks);
        elapsedDecayTicks = 0;
        return true;
    }

    OptionalDouble tickHealthCeiling(double maximumHealth) {
        if (!reviving()) {
            return OptionalDouble.empty();
        }
        elapsedDecayTicks++;
        int remainingTicks = remainingTicks();
        double ceiling = Math.max(0.0, maximumHealth) * remainingTicks / totalDecayTicks;
        return OptionalDouble.of(ceiling);
    }

    boolean reviving() {
        return usedThisRound && elapsedDecayTicks < totalDecayTicks;
    }

    int remainingTicks() {
        return Math.max(0, totalDecayTicks - elapsedDecayTicks);
    }

    void resetRound() {
        usedThisRound = false;
        totalDecayTicks = 0;
        elapsedDecayTicks = 0;
    }
}
