package kim.biryeong.semiontd.tower.undead;

import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.damagesource.DamageSource;

public class UndeadDrownedTower extends UndeadHuskTower {
    private final UndeadDrownedRevivalController revival = new UndeadDrownedRevivalController();

    public UndeadDrownedTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public UndeadDrownedTower(
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
    public double modifyIncomingDamage(SemionTowerEntity towerEntity, DamageSource damageSource, double damageAmount) {
        return applyRevival(towerEntity, damageAmount);
    }

    @Override
    public double modifyIncomingDamageIgnoringReductions(
            SemionTowerEntity towerEntity,
            DamageSource damageSource,
            double damageAmount
    ) {
        return applyRevival(towerEntity, damageAmount);
    }

    private double applyRevival(SemionTowerEntity towerEntity, double damageAmount) {
        return revival.modifyIncomingDamage(this, towerEntity, damageAmount);
    }

    @Override
    public void tick(PlayerLane lane) {
        revival.tick(this, lane);
        super.tick(lane);
    }

    @Override
    public void onDamaged(
            SemionTowerEntity towerEntity,
            DamageSource damageSource,
            double damageAmount,
            double previousHealth,
            double currentHealth
    ) {
        super.onDamaged(towerEntity, damageSource, damageAmount, previousHealth, currentHealth);
        applyFlatDamageBoost(towerEntity, value(UndeadAbilityKey.DAMAGE_BOOST_ON_HIT));
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        revival.resetRound();
        super.resetForRound(lane);
    }

    @Override
    public java.util.List<String> runtimeDetailLines() {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>(super.runtimeDetailLines());
        if (revival.reviving()) {
            lines.add("부활 상태: 체력 붕괴 " + oneDecimal(revival.remainingTicks() / 20.0) + "초");
        }
        return lines;
    }

    @Override
    protected int thornCooldownTicks() {
        return ticks(UndeadAbilityKey.THORN_COOLDOWN_TICKS);
    }
}
