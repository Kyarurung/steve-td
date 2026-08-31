package kim.biryeong.semiontd.tower.undead;

import java.util.UUID;
import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;
import net.minecraft.world.damagesource.DamageSource;

public class UndeadHuskTower extends UndeadCombatTower {
    private int thornCooldownTicks;

    public UndeadHuskTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public UndeadHuskTower(
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
    public void onAttack(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount, boolean killedTarget) {
        healFromDamage(towerEntity, damageAmount, value(UndeadAbilityKey.LIFE_STEAL_RATIO));
    }

    @Override
    public void onDamaged(
            SemionTowerEntity towerEntity,
            DamageSource damageSource,
            double damageAmount,
            double previousHealth,
            double currentHealth
    ) {
        applyFlatDamageBoost(towerEntity, value(UndeadAbilityKey.DAMAGE_BOOST_ON_HIT));
        triggerThorns(towerEntity);
    }

    @Override
    public void tick(kim.biryeong.semiontd.game.PlayerLane lane) {
        super.tick(lane);
        if (thornCooldownTicks > 0) {
            thornCooldownTicks--;
        }
    }

    protected double thornRadius() {
        return value(UndeadAbilityKey.THORN_RADIUS);
    }

    protected int thornCooldownTicks() {
        return ticks(UndeadAbilityKey.THORN_COOLDOWN_TICKS);
    }

    private void triggerThorns(SemionTowerEntity towerEntity) {
        if (thornCooldownTicks > 0 || towerEntity == null) {
            return;
        }
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTower(
                AreaEffectIds.tower(this, "thorns"), towerEntity, thornRadius(),
                UndeadVfx.pulse()
        );
        int hitCount = TowerAreaDamage.applyResolved(
                this,
                towerEntity,
                request,
                target -> resolveBasicAttackOutgoingDamage(towerEntity, target, towerEntity.attackDamageAmount(target)),
                true,
                (target, damage, killed) -> {}
        ).appliedCount();
        if (hitCount > 0) {
            towerEntity.healTarget(towerEntity, value(UndeadAbilityKey.THORN_HEAL_PER_HIT) * hitCount);
            thornCooldownTicks = thornCooldownTicks();
        }
    }

    protected double value(UndeadAbilityKey ability) {
        return UndeadConfig.RUNTIME.value(type(), ability);
    }

    protected int ticks(UndeadAbilityKey ability) {
        return UndeadConfig.RUNTIME.ticks(type(), ability);
    }
}
