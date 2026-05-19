package kim.biryeong.semiontd.tower.villager;

import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.SplashTower;
import kim.biryeong.semiontd.tower.TowerType;

import java.util.UUID;

public class VillagerSplashTower extends SplashTower {
    private static final float T2_BONUS_PER_ROUND = 0.05f;
    private static final float T3_BONUS_PER_ROUND = 0.075f;
    private static final int MAX_BONUS_SCALING = 6;
    private static final float T2_SPLASH_RADIUS = 1.25f;
    private static final float T3_SPLASH_RADIUS = 1.75f;
    private int attackAttempt = 0;
    private int survivalBouns = 0;
    private final boolean isT3;

    public VillagerSplashTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
        this.isT3 = type == VillagerTowers.T3_CLERIC_TOWER;
    }

    public VillagerSplashTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition originalPosition, GridPosition currentPosition) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
        this.isT3 = type == VillagerTowers.T3_CLERIC_TOWER;
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        if (!this.isDestroyed(lane)) {
            incrementSurvivalBonus();
        }
        super.resetForRound(lane);
    }

    private void incrementSurvivalBonus() {
        this.survivalBouns = Math.min(MAX_BONUS_SCALING, survivalBouns + 1);
    }

    @Override
    public double modifyAttackDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        double scale = isT3 ? T3_BONUS_PER_ROUND : T2_BONUS_PER_ROUND;
        return damageAmount * (1 + scale * survivalBouns);
    }

    @Override
    public int adjustAttackInterval(int baseIntervalTicks) {
        if (isT3) {
            return (int) (baseIntervalTicks * (1 - T3_BONUS_PER_ROUND * survivalBouns));
        }

        return super.adjustAttackInterval(baseIntervalTicks);
    }

    @Override
    public void onAttack(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount, boolean killedTarget) {
        super.onAttack(towerEntity, target, damageAmount, killedTarget); // splash
        if (isT3) {
            attackAttempt++; // attack attempt
            if (!killedTarget && attackAttempt >= 3) { // skip if target is dead. but stack attack attempt value
                attackAttempt -= 4; // remove 4 stack. it will stack 1 because calls it self
                boolean killed = damageTarget(towerEntity, target, damageAmount); // damage main target
                this.onAttack(towerEntity, target, damageAmount, killed); // splash and trigger addition attack if has more stack
                if (killed) {
                    this.onKill(towerEntity, target, damageAmount); // trigger kill event
                }
            }
        }
    }

    @Override
    public float getSplashRange() {
        return isT3 ? T3_SPLASH_RADIUS : T2_SPLASH_RADIUS;
    }

    @Override
    public float getSplashRatio() {
        return 0.75f;
    }
}
