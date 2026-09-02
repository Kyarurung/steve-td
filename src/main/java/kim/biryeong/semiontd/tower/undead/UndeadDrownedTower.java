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
    private final UndeadDrownedLastStandController lastStand = new UndeadDrownedLastStandController();

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
        return applyLastStand(towerEntity, damageAmount);
    }

    @Override
    public double modifyIncomingDamageIgnoringReductions(
            SemionTowerEntity towerEntity,
            DamageSource damageSource,
            double damageAmount
    ) {
        return applyLastStand(towerEntity, damageAmount);
    }

    private double applyLastStand(SemionTowerEntity towerEntity, double damageAmount) {
        return lastStand.modifyIncomingDamage(this, towerEntity, damageAmount);
    }

    @Override
    public void tick(PlayerLane lane) {
        lastStand.tick();
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
        lastStand.resetRound();
        super.resetForRound(lane);
    }

    @Override
    public java.util.List<String> runtimeDetailLines() {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>(super.runtimeDetailLines());
        if (lastStand.active()) {
            lines.add("최후의 저항: 피해 무효 " + oneDecimal(lastStand.remainingTicks() / 20.0) + "초");
        }
        return lines;
    }

    @Override
    protected int thornCooldownTicks() {
        return ticks(UndeadAbilityKey.THORN_COOLDOWN_TICKS);
    }
}
