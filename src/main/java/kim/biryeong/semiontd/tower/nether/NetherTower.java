package kim.biryeong.semiontd.tower.nether;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.damagesource.DamageSource;

public class NetherTower extends EntityBackedTower {
    private final NetherConfig config = NetherConfig.RUNTIME;
    private final NetherCombatState combatState = new NetherCombatState();
    private final NetherCombat combat = new NetherCombat(config, combatState);
    private final NetherDecayController decay = new NetherDecayController(config, combat, combatState);

    public NetherTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
        decay.initialize(this);
    }

    public NetherTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
        decay.initialize(this);
    }

    public NetherTowerState state() {
        return decay.state(this);
    }

    @Override
    public void tick(PlayerLane lane) {
        SemionTowerEntity entity = runtimeEntity(lane).orElse(null);
        combat.tick(this, entity);
        decay.tick(this, lane, entity);
        super.tick(lane);
    }

    @Override
    public boolean isDestroyed(PlayerLane lane) {
        if (decay.shouldReviveDestroyed(this, lane)) {
            decay.reviveDestroyed(this, lane);
            return false;
        }
        return super.isDestroyed(lane);
    }

    @Override
    public void onDamaged(
            SemionTowerEntity towerEntity,
            DamageSource damageSource,
            double damageAmount,
            double previousHealth,
            double currentHealth
    ) {
        decay.onDamaged(this, towerEntity, currentHealth);
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        decay.resetRound(this);
        combatState.resetRound();
        super.resetForRound(lane);
    }

    @Override
    public double modifyAttackDamage(
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        return combat.modifyAttackDamage(this, target, damageAmount);
    }

    @Override
    public Optional<SemionMonsterEntity> selectAttackTarget(
            SemionTowerEntity towerEntity,
            List<SemionMonsterEntity> candidates
    ) {
        return combat.selectAttackTarget(this, candidates);
    }

    @Override
    public void onAttack(
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount,
            boolean killedTarget
    ) {
        combat.onAttack(this, towerEntity, target, damageAmount);
    }

    @Override
    public void onKill(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        combat.onKill(this, towerEntity, target, damageAmount);
    }

    @Override
    public List<String> runtimeDetailLines() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("상태 " + (state() == NetherTowerState.NETHER ? "네더" : "좀비"));
        lines.add(deployedAtFinalDefense()
                ? "체력 감소 최종 방어선에서 중단"
                : "체력 감소 초당 " + percent(decay.decayRatioPerSecond(this)));
        double damageBonus = combat.lowHealthDamageBonus(this);
        if (damageBonus > 0.0) {
            lines.add("저체력 피해 +" + percent(damageBonus));
        }
        lines.add("흡혈 " + percent(combat.lifeStealRatio(this, null)));
        if (combatState.decayReductionTicks() > 0) {
            lines.add("체력 감소 완화 " + oneDecimal(combatState.decayReductionTicks() / 20.0) + "초");
        }
        return lines;
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (!(previousTower instanceof NetherTower netherTower)) {
            return;
        }
        combatState.copyFrom(netherTower.combatState);
        double previousMax = Math.max(1.0, netherTower.currentMaxHealth());
        syncHealth(currentMaxHealth() * Math.max(0.0, netherTower.health() / previousMax));
    }

    boolean isCritical() {
        return healthRatio() <= config.global(NetherAbilityKey.CRITICAL_HEALTH_THRESHOLD);
    }

    double healthRatio() {
        return currentMaxHealth() <= 0.0 ? 0.0 : health() / currentMaxHealth();
    }

    double missingHealthRatio() {
        return 1.0 - healthRatio();
    }

    boolean isType(TowerType towerType) {
        return towerType != null && type().id().equals(towerType.id());
    }

    boolean wasEntityUnloaded() {
        return entityWasUnloaded();
    }
}
