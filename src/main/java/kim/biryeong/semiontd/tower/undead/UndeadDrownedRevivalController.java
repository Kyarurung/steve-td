package kim.biryeong.semiontd.tower.undead;

import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.PlayerLane;

final class UndeadDrownedRevivalController {
    private final UndeadDrownedRevivalState state = new UndeadDrownedRevivalState();

    double modifyIncomingDamage(
            UndeadDrownedTower owner,
            SemionTowerEntity towerEntity,
            double damageAmount
    ) {
        if (towerEntity == null || damageAmount <= 0.0) {
            return damageAmount;
        }
        int decayTicks = UndeadConfig.RUNTIME.ticks(owner.type(), UndeadAbilityKey.LAST_STAND_TICKS);
        if (!state.tryRevive(towerEntity.getHealth(), damageAmount, decayTicks)) {
            return damageAmount;
        }
        double revivedHealth = owner.currentMaxHealth();
        owner.syncHealth(revivedHealth);
        towerEntity.setHealth((float) revivedHealth);
        return 0.0;
    }

    void tick(UndeadDrownedTower owner, PlayerLane lane) {
        if (!state.reviving()) {
            return;
        }
        SemionTowerEntity towerEntity = owner.runtimeEntity(lane).orElse(null);
        if (towerEntity == null || !towerEntity.isAlive()) {
            return;
        }
        state.tickHealthCeiling(owner.currentMaxHealth()).ifPresent(healthCeiling -> {
            double nextHealth = Math.min(owner.health(), healthCeiling);
            owner.syncHealth(nextHealth);
            towerEntity.setHealth((float) nextHealth);
        });
    }

    boolean reviving() {
        return state.reviving();
    }

    int remainingTicks() {
        return state.remainingTicks();
    }

    void resetRound() {
        state.resetRound();
    }
}
