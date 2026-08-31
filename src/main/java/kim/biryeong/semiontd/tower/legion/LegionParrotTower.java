package kim.biryeong.semiontd.tower.legion;

import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.TowerType;

public class LegionParrotTower extends EntityBackedTower {
    private int attackStacks;

    public LegionParrotTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public LegionParrotTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    public int attackStacks() {
        return attackStacks;
    }

    @Override
    public java.util.List<String> runtimeDetailLines() {
        return java.util.List.of("공격 스택 " + attackStacks + "/" + maxAttackStacks()
                + " (피해/공속 +" + percent(attackStacks * LegionConfig.RUNTIME.value(type(), LegionAbilityKey.ATTACK_STACK_BONUS)) + ")");
    }

    @Override
    public double modifyAttackDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        return damageAmount * attackMultiplier();
    }

    @Override
    public int adjustAttackInterval(int baseIntervalTicks) {
        return LegionCombat.attackInterval(baseIntervalTicks, attackMultiplier());
    }

    @Override
    public void onAttack(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount, boolean killedTarget) {
        attackStacks = Math.min(maxAttackStacks(), attackStacks + 1);
        if (towerEntity != null) {
            towerEntity.syncTowerState(this);
        }
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        attackStacks = 0;
        super.resetForRound(lane);
    }

    private double attackMultiplier() {
        return LegionCombat.attackMultiplier(
                attackStacks,
                LegionConfig.RUNTIME.value(type(), LegionAbilityKey.ATTACK_STACK_BONUS)
        );
    }

    private int maxAttackStacks() {
        return LegionConfig.RUNTIME.integer(type(), LegionAbilityKey.MAX_ATTACK_STACKS);
    }
}
