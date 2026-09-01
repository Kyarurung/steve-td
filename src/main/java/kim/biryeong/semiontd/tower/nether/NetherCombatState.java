package kim.biryeong.semiontd.tower.nether;

final class NetherCombatState {
    private int attackCounter;
    private int decayReductionTicks;
    private int pulseCooldownTicks;
    private int markCounter;
    private boolean lastAttackWasCritical;

    void tick() {
        if (decayReductionTicks > 0) {
            decayReductionTicks--;
        }
        if (pulseCooldownTicks > 0) {
            pulseCooldownTicks--;
        }
    }

    void recordAttack(boolean critical) {
        lastAttackWasCritical = critical;
        attackCounter++;
    }

    void extendDecayReduction(int ticks) {
        decayReductionTicks = Math.max(decayReductionTicks, ticks);
    }

    void startPulseCooldown(int ticks) {
        pulseCooldownTicks = ticks;
    }

    int nextMarkIndex(int maximumStacks) {
        return markCounter++ % Math.max(1, maximumStacks);
    }

    boolean extraAttackReady(int interval) {
        return interval > 0 && attackCounter % interval == 0;
    }

    boolean pulseReady() {
        return pulseCooldownTicks <= 0;
    }

    boolean lastAttackWasCritical() {
        return lastAttackWasCritical;
    }

    int decayReductionTicks() {
        return decayReductionTicks;
    }

    void resetRound() {
        attackCounter = 0;
        decayReductionTicks = 0;
        pulseCooldownTicks = 0;
        markCounter = 0;
        lastAttackWasCritical = false;
    }

    void copyFrom(NetherCombatState source) {
        attackCounter = source.attackCounter;
        decayReductionTicks = source.decayReductionTicks;
        pulseCooldownTicks = source.pulseCooldownTicks;
        markCounter = source.markCounter;
        lastAttackWasCritical = source.lastAttackWasCritical;
    }
}
