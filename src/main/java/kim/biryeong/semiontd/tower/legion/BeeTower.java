package kim.biryeong.semiontd.tower.legion;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;

public class BeeTower extends EntityBackedTower {
    private int currentSwarmStacks;

    public BeeTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public BeeTower(
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
                abilityTicks("poisonDurationTicks")
        );
        target.applyBeePoison(
                ownerPlayer(),
                this,
                towerEntity.applyTraitOutgoingDamageAgainst(target, poisonDamagePerStack()),
                maxPoisonStacks(),
                abilityTicks("poisonDurationTicks"),
                abilityTicks("poisonTickIntervalTicks")
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
        return TowerBalanceRuntime.abilityInt(type().id(), "maxSwarmStacks");
    }

    private int maxPoisonStacks() {
        return TowerBalanceRuntime.abilityInt(type().id(), "maxPoisonStacks")
                + currentSwarmStacks * abilityInt("poisonStacksPerSwarmStack");
    }

    private double poisonDamagePerStack() {
        return value("poisonDamagePerStack") + currentSwarmStacks * value("poisonDamagePerSwarmStack");
    }

    private int abilityTicks(String key) {
        return TowerBalanceRuntime.abilityTicks(type().id(), key);
    }

    private int abilityInt(String key) {
        return TowerBalanceRuntime.abilityInt(type().id(), key);
    }

    private double value(String key) {
        return TowerBalanceRuntime.ability(type().id(), key);
    }

    static void refreshBeeSwarmStacks(PlayerLane lane) {
        if (lane == null) {
            return;
        }
        for (Tower tower : lane.towers()) {
            if (tower instanceof BeeTower beeTower) {
                beeTower.refreshSwarmStacks(lane);
            }
        }
    }
}
