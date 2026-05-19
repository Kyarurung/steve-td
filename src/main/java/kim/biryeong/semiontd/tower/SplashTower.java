package kim.biryeong.semiontd.tower;

import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public abstract class SplashTower extends EntityBackedTower {

    protected SplashTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition originalPosition, GridPosition currentPosition) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    protected SplashTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    @Override
    public void onAttack(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount, boolean killedTarget) {
        AABB splashBox = target.getBoundingBox().inflate(getSplashRange());
        double splashRadiusSqr = getSplashRange() * getSplashRange();
        towerEntity.level().getEntities(towerEntity, splashBox,
                entity -> entity instanceof SemionMonsterEntity splashTarget && splashTarget.isAlive() &&
                        splashTarget != target && splashTarget.runtimeMonster() != null &&
                        towerEntity.defendsLane(splashTarget.runtimeMonster().targetLaneId()) &&
                        splashTarget.distanceToSqr(target) <= splashRadiusSqr)
                .stream()
                .filter(SemionMonsterEntity.class::isInstance)
                .map(SemionMonsterEntity.class::cast)
                .forEach(entity -> damage(towerEntity, entity, damageAmount));
    }

    protected void damage(SemionTowerEntity tower, SemionMonsterEntity monster, double damage) {
        double splashDamage = damage * getSplashRatio();
        if (damageTarget(tower, monster, splashDamage)) {
            onKill(tower, monster, splashDamage);
        }
    }

    public abstract float getSplashRange();
    public abstract float getSplashRatio();
}
