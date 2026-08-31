package kim.biryeong.semiontd.tower.legion;

import java.util.UUID;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;

public class LegionBeeTower extends EntityBackedTower {
    private int currentSwarmStacks;

    public LegionBeeTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public LegionBeeTower(
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
    public void onPlaced(PlayerLane lane) {
        super.onPlaced(lane);
        refreshSwarmStacks(lane);
    }

    @Override
    public void tick(PlayerLane lane) {
        refreshSwarmStacks(lane);
        super.tick(lane);
    }

    @Override
    public void onAttack(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount, boolean killedTarget) {
        if (target == null || killedTarget) {
            return;
        }
        target.applyTimedEffect(
                TimedEffectType.MONSTER_POISONED,
                1.0,
                abilityTicks(LegionAbilityKey.POISON_DURATION_TICKS)
        );
        target.applyBeePoison(
                ownerPlayer(),
                this,
                towerEntity.applyTraitOutgoingDamageAgainst(target, poisonDamagePerStack()),
                maxPoisonStacks(),
                abilityTicks(LegionAbilityKey.POISON_DURATION_TICKS),
                abilityTicks(LegionAbilityKey.POISON_TICK_INTERVAL_TICKS)
        );
    }

    @Override
    public void onRemoved(PlayerLane lane) {
        super.onRemoved(lane);
        refreshBeeSwarmStacks(lane);
    }

    private void refreshSwarmStacks(PlayerLane lane) {
        if (lane == null) {
            currentSwarmStacks = 0;
            return;
        }
        long matchingBees = lane.towers().stream()
                .filter(tower -> tower != this)
                .filter(tower -> ownerPlayer().equals(tower.ownerPlayer()))
                .filter(this::isSwarmFamily)
                .count();
        currentSwarmStacks = Math.min(maxSwarmStacks(), (int) matchingBees);
    }

    private boolean isSwarmFamily(Tower tower) {
        return tower != null && (
                tower.type().id().equals(LegionTowers.T1_BEE_TOWER.id())
                        || tower.type().id().equals(LegionTowers.T2_BEE_TOWER.id())
                        || tower.type().id().equals(LegionTowers.T3_BEE_TOWER.id())
        );
    }

    private int maxSwarmStacks() {
        return LegionConfig.RUNTIME.integer(type(), LegionAbilityKey.MAX_SWARM_STACKS);
    }

    private int maxPoisonStacks() {
        return LegionConfig.RUNTIME.integer(type(), LegionAbilityKey.MAX_POISON_STACKS)
                + currentSwarmStacks * abilityInt(LegionAbilityKey.POISON_STACKS_PER_SWARM_STACK);
    }

    private double poisonDamagePerStack() {
        return value(LegionAbilityKey.POISON_DAMAGE_PER_STACK)
                + currentSwarmStacks * value(LegionAbilityKey.POISON_DAMAGE_PER_SWARM_STACK);
    }

    private int abilityTicks(LegionAbilityKey ability) {
        return LegionConfig.RUNTIME.ticks(type(), ability);
    }

    private int abilityInt(LegionAbilityKey ability) {
        return LegionConfig.RUNTIME.integer(type(), ability);
    }

    private double value(LegionAbilityKey ability) {
        return LegionConfig.RUNTIME.value(type(), ability);
    }

    static void refreshBeeSwarmStacks(PlayerLane lane) {
        if (lane == null) {
            return;
        }
        for (Tower tower : lane.towers()) {
            if (tower instanceof LegionBeeTower beeTower) {
                beeTower.refreshSwarmStacks(lane);
            }
        }
    }
}
