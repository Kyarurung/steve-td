package kim.biryeong.semiontd.tower.undead;

import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;

final class UndeadDrownedLastStandController {
    private final UndeadDrownedLastStandState state = new UndeadDrownedLastStandState();

    double modifyIncomingDamage(
            UndeadDrownedTower owner,
            SemionTowerEntity towerEntity,
            double damageAmount
    ) {
        if (towerEntity == null || damageAmount <= 0.0) {
            return damageAmount;
        }
        if (state.active()) {
            return 0.0;
        }
        if (!state.tryActivate(
                towerEntity.getHealth(),
                damageAmount,
                UndeadConfig.RUNTIME.ticks(owner.type(), UndeadAbilityKey.LAST_STAND_TICKS)
        )) {
            return damageAmount;
        }
        owner.syncHealth(1.0);
        towerEntity.setHealth(1.0F);
        return 0.0;
    }

    void tick() {
        state.tick();
    }

    boolean active() {
        return state.active();
    }

    int remainingTicks() {
        return state.remainingTicks();
    }

    void resetRound() {
        state.resetRound();
    }
}
