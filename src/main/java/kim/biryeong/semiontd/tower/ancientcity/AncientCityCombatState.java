package kim.biryeong.semiontd.tower.ancientcity;

final class AncientCityCombatState {
    private int retaliationCooldownTicks;

    int retaliationCooldownTicks() {
        return retaliationCooldownTicks;
    }

    void tick() {
        if (retaliationCooldownTicks > 0) {
            retaliationCooldownTicks--;
        }
    }

    void startRetaliationCooldown(int ticks) {
        retaliationCooldownTicks = Math.max(1, ticks);
    }

    void copyFrom(AncientCityCombatState previous) {
        if (previous != null) {
            retaliationCooldownTicks = previous.retaliationCooldownTicks;
        }
    }
}
