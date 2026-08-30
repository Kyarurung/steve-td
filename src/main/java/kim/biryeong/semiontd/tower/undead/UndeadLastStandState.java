package kim.biryeong.semiontd.tower.undead;

/** Round-scoped last-stand state for a drowned tower. */
final class UndeadLastStandState {
    private boolean used;
    private long endsAt;

    double modifyDamage(long gameTime, double health, double damageAmount, int durationTicks) {
        if (damageAmount <= 0.0) {
            return damageAmount;
        }
        if (endsAt > gameTime) {
            return 0.0;
        }
        if (!used && damageAmount >= health) {
            used = true;
            endsAt = gameTime + durationTicks;
            return Math.max(0.0, health - 1.0);
        }
        return damageAmount;
    }

    void resetRound() {
        used = false;
        endsAt = 0L;
    }
}
