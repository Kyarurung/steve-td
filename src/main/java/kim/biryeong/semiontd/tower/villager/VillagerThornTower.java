package kim.biryeong.semiontd.tower.villager;

import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.damagesource.DamageSource;

import java.util.UUID;

public class VillagerThornTower extends EntityBackedTower {
    private static final int T2_THORN_COOLDOWN_TICKS = 100;
    private static final int T3_THORN_COOLDOWN_TICKS = 80;
    private static final int T2_THORN_DAMAGE = 10;
    private static final int T3_THORN_DAMAGE = 15;
    private static final int T2_THORN_RADIUS = 3;
    private static final int T3_THORN_RADIUS = 4;

    private int thornCooldownTicks = 0;
    private final boolean isT3;
    public VillagerThornTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition originalPosition, GridPosition currentPosition) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
        this.isT3 = type == VillagerTowers.T3_GOLEM_TOWER;
    }

    public VillagerThornTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
        this.isT3 = type == VillagerTowers.T3_GOLEM_TOWER;
    }

    @Override
    public void onDamaged(SemionTowerEntity towerEntity, DamageSource damageSource, double damageAmount, double previousHealth, double currentHealth) {
        if (this.thornCooldownTicks > 0) {
            return;
        }
        int range = isT3 ? T3_THORN_RADIUS : T2_THORN_RADIUS;
        int splashRadiusSqr = range * range;
        double damage = isT3 ? T3_THORN_DAMAGE : T2_THORN_DAMAGE;

        var box = towerEntity.getBoundingBox().inflate(range);
        towerEntity.level().getEntities(towerEntity, box, entity ->
            entity instanceof SemionMonsterEntity monster &&
                    monster.isAlive() && monster.runtimeMonster() != null &&
                    towerEntity.defendsLane(monster.runtimeMonster().targetLaneId()) &&
                    monster.distanceToSqr(towerEntity) <= splashRadiusSqr
        )
                .stream()
                .filter(SemionMonsterEntity.class::isInstance)
                .map(SemionMonsterEntity.class::cast)
                .forEach(entity -> damageTarget(towerEntity, entity, damage));

        this.thornCooldownTicks = this.isT3 ? T3_THORN_COOLDOWN_TICKS : T2_THORN_COOLDOWN_TICKS;
    }

    @Override
    public void tick(PlayerLane lane) {
        if (this.thornCooldownTicks > 0) {
            this.thornCooldownTicks--;
        }
    }
}
