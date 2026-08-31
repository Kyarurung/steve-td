package kim.biryeong.semiontd.tower.villager;

import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;
import net.minecraft.world.damagesource.DamageSource;

import java.util.UUID;

public class VillagerThornTower extends EntityBackedTower {
    private int thornCooldownTicks = 0;
    private final VillagerGolemSurvivalState survival = new VillagerGolemSurvivalState();
    public VillagerThornTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition originalPosition, GridPosition currentPosition) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    public VillagerThornTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    @Override
    public void onDamaged(SemionTowerEntity towerEntity, DamageSource damageSource, double damageAmount, double previousHealth, double currentHealth) {
        if (this.thornCooldownTicks > 0) {
            return;
        }
        float range = (float) value(VillagerAbilityKey.THORN_RADIUS);
        double damage = value(VillagerAbilityKey.THORN_DAMAGE);
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTower(
                AreaEffectIds.tower(this, "thorns"), towerEntity, range,
                VillagerVfx.pulse()
        );
        TowerAreaDamage.apply(this, towerEntity, request, monster -> damage, false);

        this.thornCooldownTicks = ticks(VillagerAbilityKey.THORN_COOLDOWN_TICKS);
    }

    @Override
    public double currentMaxHealth() {
        return applyTraitMaxHealth(maxHealth() * (1.0 + survivalHealthBonus()));
    }

    @Override
    public java.util.List<String> runtimeDetailLines() {
        double bonus = survivalHealthBonus();
        return java.util.List.of("생존 스택 " + survival.stacks() + "/" + maxSurvivalStacks()
                + " (체력 +" + percent(bonus) + ")");
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        if (!deployedAtFinalDefense()) {
            increaseSurvivalBonus();
        }
        super.resetForRound(lane);
    }

    @Override
    public void moveToFinalDefense(PlayerLane lane, GridPosition position) {
        increaseSurvivalBonus();
        super.moveToFinalDefense(lane, position);
    }

    @Override
    public void tick(PlayerLane lane) {
        super.tick(lane);
        if (this.thornCooldownTicks > 0) {
            this.thornCooldownTicks--;
        }
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (previousTower instanceof VillagerThornTower thornTower) {
            survival.copyFrom(thornTower.survival, maxSurvivalStacks());
            syncHealth(currentMaxHealth());
        }
    }

    private double value(VillagerAbilityKey ability) {
        return VillagerConfig.RUNTIME.value(type(), ability);
    }

    private int ticks(VillagerAbilityKey ability) {
        return VillagerConfig.RUNTIME.ticks(type(), ability);
    }

    private int maxSurvivalStacks() {
        return VillagerConfig.RUNTIME.integer(type(), VillagerAbilityKey.MAX_SURVIVAL_STACKS);
    }

    private double survivalHealthBonus() {
        return VillagerCombat.survivalBonus(
                value(VillagerAbilityKey.HEALTH_BONUS_PER_SURVIVED_ROUND),
                survival.stacks(),
                VillagerAdvStates.survivalBonusMultiplier(this)
        );
    }

    private void increaseSurvivalBonus() {
        double previousMaxHealth = currentMaxHealth();
        if (survival.increment(maxSurvivalStacks())) {
            syncHealth(health() + Math.max(0.0, currentMaxHealth() - previousMaxHealth));
        }
    }
}
