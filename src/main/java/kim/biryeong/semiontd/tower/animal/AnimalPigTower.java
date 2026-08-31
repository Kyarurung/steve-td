package kim.biryeong.semiontd.tower.animal;

import java.util.UUID;
import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;
import net.minecraft.world.damagesource.DamageSource;

public class AnimalPigTower extends AnimalPackTower {
    public AnimalPigTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public AnimalPigTower(
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
    public double currentMaxHealth() {
        double value = applyTraitMaxHealth(maxHealth() + currentStacks() * value(AnimalAbilityKey.HEALTH_PER_STACK));
        return hasLeaderAura() ? value * (1.0 + leaderValue(AnimalAbilityKey.LEADER_MAX_HEALTH_BONUS)) : value;
    }

    @Override
    public double modifyAttackDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        return AnimalCombat.addStackDamage(
                damageAmount, currentStacks(), value(AnimalAbilityKey.DAMAGE_PER_STACK)
        );
    }

    @Override
    public double modifyIncomingDamage(SemionTowerEntity towerEntity, DamageSource damageSource, double damageAmount) {
        double reduction = hasLeaderAura() ? leaderValue(AnimalAbilityKey.LEADER_DAMAGE_REDUCTION_BONUS) : 0.0;
        if (!is(AnimalTowers.T1_PIG_TOWER) && atMaxStacks()) {
            reduction += value(AnimalAbilityKey.DAMAGE_REDUCTION);
        }
        return AnimalCombat.reduceIncomingDamage(damageAmount, reduction);
    }

    @Override
    public java.util.List<String> runtimeDetailLines() {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>(super.runtimeDetailLines());
        lines.add("무리 효과 체력 +" + oneDecimal(currentStacks() * value(AnimalAbilityKey.HEALTH_PER_STACK))
                + ", 공격력 +" + oneDecimal(currentStacks() * value(AnimalAbilityKey.DAMAGE_PER_STACK)));
        if (!is(AnimalTowers.T1_PIG_TOWER) && atMaxStacks()) {
            lines.add("최대 무리 효과 받는 피해 -" + percent(value(AnimalAbilityKey.DAMAGE_REDUCTION)));
        }
        if (isT3OrLeader() && atMaxStacks()) {
            lines.add("최대 무리 효과 스플래시 활성");
        }
        if (hasLeaderAura()) {
            lines.add("우두머리 효과 최대 체력 +" + percent(leaderValue(AnimalAbilityKey.LEADER_MAX_HEALTH_BONUS))
                    + ", 받는 피해 -" + percent(leaderValue(AnimalAbilityKey.LEADER_DAMAGE_REDUCTION_BONUS)) + "p");
        }
        return lines;
    }

    @Override
    public void onAttack(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount, boolean killedTarget) {
        if (isT3OrLeader() && atMaxStacks()) {
            splash(towerEntity, target, damageAmount);
        }
    }

    @Override
    protected void onStacksChanged(PlayerLane lane, int previousStacks, int currentStacks) {
        double healthDelta = (currentStacks - previousStacks) * value(AnimalAbilityKey.HEALTH_PER_STACK);
        if (healthDelta > 0.0) {
            syncHealth(health() + healthDelta);
        } else if (healthDelta < 0.0) {
            syncHealth(health());
        }
        if (previousStacks != currentStacks) {
            onStateChanged(lane);
        }
    }

    @Override
    protected void onLeaderAuraChanged(PlayerLane lane, boolean previousActive, boolean currentActive) {
        double baseMaxHealth = applyTraitMaxHealth(maxHealth() + currentStacks() * value(AnimalAbilityKey.HEALTH_PER_STACK));
        double previousMaxHealth = baseMaxHealth * (previousActive ? 1.0 + leaderValue(AnimalAbilityKey.LEADER_MAX_HEALTH_BONUS) : 1.0);
        double currentMaxHealth = baseMaxHealth * (currentActive ? 1.0 + leaderValue(AnimalAbilityKey.LEADER_MAX_HEALTH_BONUS) : 1.0);
        if (currentMaxHealth > previousMaxHealth) {
            syncHealth(health() + currentMaxHealth - previousMaxHealth);
        } else {
            syncHealth(health());
        }
    }

    @Override
    protected boolean isStackFamily(Tower tower) {
        return tower != null && (
                tower.type().id().equals(AnimalTowers.T1_PIG_TOWER.id())
                        || tower.type().id().equals(AnimalTowers.T2_PIG_TOWER.id())
                        || tower.type().id().equals(AnimalTowers.T3_PIG_TOWER.id())
                        || tower.type().id().equals(AnimalTowers.T4_PIG_LEADER_TOWER.id())
        );
    }

    @Override
    protected int maxStacks() {
        return AnimalConfig.RUNTIME.integer(type(), AnimalAbilityKey.MAX_STACKS);
    }

    @Override
    protected TowerType leaderBaseType() {
        return AnimalTowers.T3_PIG_TOWER;
    }

    @Override
    protected TowerType leaderType() {
        return AnimalTowers.T4_PIG_LEADER_TOWER;
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
                        damageAmount, value(AnimalAbilityKey.SPLASH_DAMAGE_RATIO)
                ), true);
    }

    private boolean is(TowerType towerType) {
        return type().id().equals(towerType.id());
    }

    private boolean isT3OrLeader() {
        return is(AnimalTowers.T3_PIG_TOWER) || isLeader();
    }

    private double value(AnimalAbilityKey ability) {
        return AnimalConfig.RUNTIME.value(type(), ability);
    }
}
