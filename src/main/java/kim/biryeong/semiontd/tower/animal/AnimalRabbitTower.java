package kim.biryeong.semiontd.tower.animal;

import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;

public class AnimalRabbitTower extends AnimalPackTower {
    public AnimalRabbitTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public AnimalRabbitTower(
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
        double amount = AnimalCombat.addStackDamage(
                damageAmount, currentStacks(), value(AnimalAbilityKey.DAMAGE_PER_STACK)
        );
        return hasLeaderAura() ? amount * (1.0 + leaderValue(AnimalAbilityKey.LEADER_DAMAGE_BONUS)) : amount;
    }

    @Override
    public int adjustAttackInterval(int baseIntervalTicks) {
        if ((is(AnimalTowers.T2_RABBIT_TOWER) || isT3OrLeader()) && atMaxStacks()) {
            return Math.max(1, baseIntervalTicks - ticks(AnimalAbilityKey.MAX_STACK_EXTRA_INTERVAL_REDUCTION));
        }
        return baseIntervalTicks;
    }

    @Override
    public double adjustAttackRange(double baseRange) {
        return baseRange + (hasLeaderAura() ? leaderValue(AnimalAbilityKey.LEADER_RANGE_BONUS) : 0.0);
    }

    @Override
    public java.util.List<String> runtimeDetailLines() {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>(super.runtimeDetailLines());
        lines.add("무리 효과 공격력 +" + oneDecimal(currentStacks() * value(AnimalAbilityKey.DAMAGE_PER_STACK)));
        if ((is(AnimalTowers.T2_RABBIT_TOWER) || isT3OrLeader()) && atMaxStacks()) {
            lines.add("최대 무리 효과 공격 간격 -" + ticks(AnimalAbilityKey.MAX_STACK_EXTRA_INTERVAL_REDUCTION) + "틱");
        }
        if (isT3OrLeader() && atMaxStacks()) {
            lines.add("최대 무리 효과 추가 공격 피해 " + percent(value(AnimalAbilityKey.EXTRA_ATTACK_DAMAGE_RATIO)));
        }
        if (hasLeaderAura()) {
            lines.add("우두머리 효과 공격 피해 +" + percent(leaderValue(AnimalAbilityKey.LEADER_DAMAGE_BONUS))
                    + ", 사거리 +" + oneDecimal(leaderValue(AnimalAbilityKey.LEADER_RANGE_BONUS)));
        }
        return lines;
    }

    @Override
    public void onAttack(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount, boolean killedTarget) {
        if (!isT3OrLeader() || !atMaxStacks() || killedTarget || towerEntity == null || target == null || !target.isAlive()) {
            return;
        }
        boolean killed = damageBasicAttackTargetResult(
                towerEntity, target, damageAmount * value(AnimalAbilityKey.EXTRA_ATTACK_DAMAGE_RATIO)
        ).killed();
        AnimalVfx.secondaryAttack(towerEntity, target);
        if (killed) {
            onKill(towerEntity, target, damageAmount);
        }
    }

    @Override
    protected boolean isStackFamily(Tower tower) {
        return tower != null && (
                tower.type().id().equals(AnimalTowers.T1_RABBIT_TOWER.id())
                        || tower.type().id().equals(AnimalTowers.T2_RABBIT_TOWER.id())
                        || tower.type().id().equals(AnimalTowers.T3_RABBIT_TOWER.id())
                        || tower.type().id().equals(AnimalTowers.T4_RABBIT_LEADER_TOWER.id())
        );
    }

    @Override
    protected int maxStacks() {
        return AnimalConfig.RUNTIME.integer(type(), AnimalAbilityKey.MAX_STACKS);
    }

    @Override
    protected TowerType leaderBaseType() {
        return AnimalTowers.T3_RABBIT_TOWER;
    }

    @Override
    protected TowerType leaderType() {
        return AnimalTowers.T4_RABBIT_LEADER_TOWER;
    }

    private boolean is(TowerType towerType) {
        return type().id().equals(towerType.id());
    }

    private boolean isT3OrLeader() {
        return is(AnimalTowers.T3_RABBIT_TOWER) || isLeader();
    }

    private double value(AnimalAbilityKey ability) {
        return AnimalConfig.RUNTIME.value(type(), ability);
    }

    private int ticks(AnimalAbilityKey ability) {
        return AnimalConfig.RUNTIME.ticks(type(), ability);
    }
}
