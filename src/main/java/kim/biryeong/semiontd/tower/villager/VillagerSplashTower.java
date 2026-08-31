package kim.biryeong.semiontd.tower.villager;

import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.SplashTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;

import java.util.UUID;

public class VillagerSplashTower extends SplashTower {
    private int attackAttempt = 0;
    private final VillagerSurvivalState survival = new VillagerSurvivalState();

    public VillagerSplashTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public VillagerSplashTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition originalPosition, GridPosition currentPosition) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        if (!this.isDestroyed(lane)) {
            incrementSurvivalBonus();
        }
        super.resetForRound(lane);
    }

    private void incrementSurvivalBonus() {
        survival.increment(maxSurvivalStacks());
    }

    @Override
    public double modifyAttackDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        return VillagerCombat.addPercentBonus(damageAmount, survivalBonus());
    }

    @Override
    public int adjustAttackInterval(int baseIntervalTicks) {
        if (isT3()) {
            return VillagerCombat.reduceInterval(baseIntervalTicks, survivalBonus());
        }

        return super.adjustAttackInterval(baseIntervalTicks);
    }

    @Override
    public java.util.List<String> runtimeDetailLines() {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        double bonus = survivalBonus();
        String effect = isT3() ? "피해/공속 +" + percent(bonus) : "피해 +" + percent(bonus);
        lines.add("생존 스택 " + survival.stacks() + "/" + maxSurvivalStacks() + " (" + effect + ")");
        return lines;
    }

    @Override
    public void onAttack(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount, boolean killedTarget) {
        super.onAttack(towerEntity, target, damageAmount, killedTarget); // splash
        if (isT3()) {
            attackAttempt++; // attack attempt
            int every = Math.max(1, VillagerConfig.RUNTIME.integer(type(), VillagerAbilityKey.EXTRA_ATTACK_EVERY));
            if (!killedTarget && attackAttempt >= every) { // skip if target is dead. but stack attack attempt value
                attackAttempt -= every + 1; // remove stack. it will stack 1 because calls itself
                boolean killed = damageBasicAttackTargetResult(towerEntity, target, damageAmount).killed(); // damage main target
                VillagerVfx.secondaryAttack(towerEntity, target);
                this.onAttack(towerEntity, target, damageAmount, killed); // splash and trigger addition attack if has more stack
                if (killed) {
                    this.onKill(towerEntity, target, damageAmount); // trigger kill event
                }
            }
        }
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (previousTower instanceof VillagerSplashTower splashTower) {
            survival.copyFrom(splashTower.survival, maxSurvivalStacks());
        }
    }

    @Override
    public float getSplashRange() {
        return (float) value(VillagerAbilityKey.SPLASH_RADIUS);
    }

    @Override
    public float getSplashRatio() {
        return (float) value(VillagerAbilityKey.SPLASH_DAMAGE_RATIO);
    }

    private double value(VillagerAbilityKey ability) {
        return VillagerConfig.RUNTIME.value(type(), ability);
    }

    private int maxSurvivalStacks() {
        return VillagerConfig.RUNTIME.integer(type(), VillagerAbilityKey.MAX_SURVIVAL_STACKS);
    }

    private double survivalBonus() {
        return VillagerCombat.survivalBonus(
                value(VillagerAbilityKey.BONUS_PER_SURVIVED_ROUND),
                survival.stacks(),
                VillagerAdvStates.survivalBonusMultiplier(this)
        );
    }

    private boolean isT3() {
        return VillagerTowers.matches(type(), VillagerTowers.T3_CLERIC_TOWER);
    }
}
