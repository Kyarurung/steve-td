package kim.biryeong.semiontd.tower.animal;

import java.util.UUID;
import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;

public class AnimalWolfTower extends AnimalPackTower {
    public AnimalWolfTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public AnimalWolfTower(
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
        if (isT3OrLeader() && atMaxStacks()) {
            amount += value(AnimalAbilityKey.MAX_STACK_DAMAGE_BONUS);
        }
        return amount;
    }

    @Override
    public int adjustAttackInterval(int baseIntervalTicks) {
        int interval = baseIntervalTicks - (int) Math.round(currentStacks() * value(AnimalAbilityKey.INTERVAL_REDUCTION_PER_STACK));
        if (!is(AnimalTowers.T1_WOLF_TOWER) && atMaxStacks()) {
            interval -= ticks(AnimalAbilityKey.MAX_STACK_EXTRA_INTERVAL_REDUCTION);
        }
        if (hasLeaderAura()) {
            interval -= (int) Math.round(leaderValue(AnimalAbilityKey.LEADER_ATTACK_INTERVAL_REDUCTION_TICKS));
        }
        return AnimalCombat.clampAttackInterval(interval);
    }

    @Override
    public java.util.List<String> runtimeDetailLines() {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>(super.runtimeDetailLines());
        lines.add("무리 효과 공격력 +" + oneDecimal(currentStacks() * value(AnimalAbilityKey.DAMAGE_PER_STACK))
                + ", 공격 간격 -" + Math.round(currentStacks() * value(AnimalAbilityKey.INTERVAL_REDUCTION_PER_STACK)) + "틱");
        if (!is(AnimalTowers.T1_WOLF_TOWER) && atMaxStacks()) {
            lines.add("최대 무리 효과 공격 간격 추가 -" + ticks(AnimalAbilityKey.MAX_STACK_EXTRA_INTERVAL_REDUCTION) + "틱");
        }
        if (isT3OrLeader() && atMaxStacks()) {
            lines.add("최대 무리 효과 공격력 추가 +" + oneDecimal(value(AnimalAbilityKey.MAX_STACK_DAMAGE_BONUS)));
        }
        if (hasLeaderAura()) {
            lines.add("우두머리 효과 공격 간격 -" + Math.round(leaderValue(AnimalAbilityKey.LEADER_ATTACK_INTERVAL_REDUCTION_TICKS))
                    + "틱, 기존 스플래시 +" + percent(leaderValue(AnimalAbilityKey.LEADER_SPLASH_DAMAGE_RATIO_BONUS)) + "p");
        }
        return lines;
    }

    @Override
    public void onAttack(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount, boolean killedTarget) {
        if (is(AnimalTowers.T2_WOLF_DPS_TOWER) || isT3OrLeader()) {
            splash(towerEntity, target, damageAmount);
        }
    }

    @Override
    protected boolean isStackFamily(Tower tower) {
        return tower != null && (
                tower.type().id().equals(AnimalTowers.T1_WOLF_TOWER.id())
                        || tower.type().id().equals(AnimalTowers.T2_WOLF_DPS_TOWER.id())
                        || tower.type().id().equals(AnimalTowers.T3_WOLF_DPS_TOWER.id())
                        || tower.type().id().equals(AnimalTowers.T4_WOLF_LEADER_TOWER.id())
        );
    }

    @Override
    protected int maxStacks() {
        return AnimalConfig.RUNTIME.integer(type(), AnimalAbilityKey.MAX_STACKS);
    }

    @Override
    protected TowerType leaderBaseType() {
        return AnimalTowers.T3_WOLF_DPS_TOWER;
    }

    @Override
    protected TowerType leaderType() {
        return AnimalTowers.T4_WOLF_LEADER_TOWER;
    }

    private void splash(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        if (towerEntity == null || target == null) {
            return;
        }
        double radius = value(AnimalAbilityKey.SPLASH_RADIUS);
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTarget(
                AreaEffectIds.tower(this, "splash"), towerEntity, target, radius,
                AnimalVfx.splash()
        );
        TowerAreaDamage.applyBasicAttackSplash(this, towerEntity, request,
                monster -> AnimalCombat.splashDamage(
                        damageAmount,
                        value(AnimalAbilityKey.SPLASH_DAMAGE_RATIO)
                                + (hasLeaderAura()
                                ? leaderValue(AnimalAbilityKey.LEADER_SPLASH_DAMAGE_RATIO_BONUS)
                                : 0.0)
                ), true);
    }

    private boolean is(TowerType towerType) {
        return type().id().equals(towerType.id());
    }

    private boolean isT3OrLeader() {
        return is(AnimalTowers.T3_WOLF_DPS_TOWER) || isLeader();
    }

    private double value(AnimalAbilityKey ability) {
        return AnimalConfig.RUNTIME.value(type(), ability);
    }

    private int ticks(AnimalAbilityKey ability) {
        return AnimalConfig.RUNTIME.ticks(type(), ability);
    }
}
