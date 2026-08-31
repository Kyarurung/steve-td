package kim.biryeong.semiontd.tower.undead;

import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.TowerType;

abstract class UndeadCombatTower extends EntityBackedTower {
    private long damageBoostExpiresAt;
    private double flatDamageBoost;

    protected UndeadCombatTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    protected UndeadCombatTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    @Override
    public double modifyAttackDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        if (towerEntity == null || damageBoostExpiresAt <= towerEntity.level().getGameTime()) {
            return damageAmount;
        }
        return damageAmount + flatDamageBoost;
    }

    protected final void healFromDamage(SemionTowerEntity towerEntity, double damageAmount, double ratio) {
        double healing = UndeadCombat.lifeStealAmount(damageAmount, ratio);
        if (towerEntity == null || healing <= 0.0) {
            return;
        }
        towerEntity.healTarget(towerEntity, healing);
    }

    protected final void applyFlatDamageBoost(SemionTowerEntity towerEntity, double amount) {
        if (towerEntity == null || amount <= 0.0) {
            return;
        }
        flatDamageBoost = amount;
        damageBoostExpiresAt = towerEntity.level().getGameTime()
                + UndeadConfig.RUNTIME.ticks(type(), UndeadAbilityKey.DAMAGE_BOOST_TICKS);
    }

}
