package kim.biryeong.semiontd.tower.resonance;

final class ResonanceState {
    private int level;
    private int links;
    private int pulseCharge;
    private double auraAttackSpeedBonus;
    private double auraDamageVsSlowedBonus;

    int level() {
        return level;
    }

    int links() {
        return links;
    }

    double auraAttackSpeedBonus() {
        return auraAttackSpeedBonus;
    }

    double auraDamageVsSlowedBonus() {
        return auraDamageVsSlowedBonus;
    }

    void updateResonance(int level, int links) {
        this.level = Math.max(0, level);
        this.links = Math.max(0, links);
    }

    void updateAuras(double attackSpeedBonus, double damageVsSlowedBonus) {
        auraAttackSpeedBonus = Math.max(0.0, attackSpeedBonus);
        auraDamageVsSlowedBonus = Math.max(0.0, damageVsSlowedBonus);
    }

    boolean chargeReady(int every) {
        pulseCharge++;
        if (pulseCharge < Math.max(1, every)) {
            return false;
        }
        pulseCharge = 0;
        return true;
    }

    ResonanceSnapshot snapshot() {
        return new ResonanceSnapshot(level, links, pulseCharge, auraAttackSpeedBonus, auraDamageVsSlowedBonus);
    }

    void restore(ResonanceSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        level = snapshot.level();
        links = snapshot.links();
        pulseCharge = snapshot.pulseCharge();
        auraAttackSpeedBonus = snapshot.auraAttackSpeedBonus();
        auraDamageVsSlowedBonus = snapshot.auraDamageVsSlowedBonus();
    }
}
