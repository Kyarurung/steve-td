package kim.biryeong.semiontd.tower.legion;

import java.util.ArrayList;
import java.util.List;
import kim.biryeong.semiontd.entity.SemionEntityTypes;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerCategory;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Owns the tower-scoped clone entities and their spawn/move/expiry transitions. */
final class LegionIllusionCloneController {
    private final LegionIllusionSummonerTower owner;
    private final List<CloneInstance> clones = new ArrayList<>();

    LegionIllusionCloneController(LegionIllusionSummonerTower owner) {
        this.owner = owner;
    }

    void enqueue(PlayerLane lane, Tower sourceTower, LegionIllusionProfile profile, List<Vec3> offsets) {
        if (sourceTower == null || profile.cloneCount() <= 0 || sourceTower.health() <= 0.0) {
            return;
        }
        LegionIllusionSpawnQueue.enqueue(owner, lane, sourceTower, profile, offsets);
    }

    void spawnQueued(PlayerLane lane, Tower sourceTower, LegionIllusionProfile profile, Vec3 offset) {
        Vec3 spawnPosition = new Vec3(
                sourceTower.position().x() + 0.5 + offset.x,
                sourceTower.position().y() + 1.0 + offset.y,
                sourceTower.position().z() + 0.5 + offset.z
        );
        GridPosition clonePosition = GridPosition.from(BlockPos.containing(
                spawnPosition.x, spawnPosition.y - 1.0, spawnPosition.z
        ));
        Tower cloneTower = createCloneTower(sourceTower, profile, clonePosition);
        cloneTower.attachToLane(lane, lane.traitLoadout());

        SemionTowerEntity entity = new SemionTowerEntity(SemionEntityTypes.TOWER, lane.arenaWorld());
        entity.configure(cloneTower, lane.laneLayout());
        entity.markIllusionClone();
        sourceTowerEntity(lane, sourceTower).ifPresent(sourceEntity -> {
            entity.useAttackTargetFrom(sourceEntity);
            entity.inheritTraitEffectsFrom(sourceEntity);
        });
        entity.setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);

        if (lane.arenaWorld().addFreshEntity(entity)) {
            clones.add(new CloneInstance(entity.getId(), profile.durationTicks()));
            owner.onCloneSpawned(lane, entity, cloneTower);
        }
    }

    void tick(PlayerLane lane) {
        for (int index = clones.size() - 1; index >= 0; index--) {
            CloneInstance clone = clones.get(index);
            var entity = lane.arenaWorld().getEntity(clone.entityId());
            if (!(entity instanceof SemionTowerEntity towerEntity) || towerEntity.isRemoved()) {
                clones.remove(index);
                continue;
            }
            if (!towerEntity.isAlive()) {
                towerEntity.discard();
                clones.remove(index);
                continue;
            }
            Tower cloneTower = towerEntity.runtimeTower();
            if (cloneTower == null || cloneTower.health() <= 0.0) {
                towerEntity.discard();
                clones.remove(index);
                continue;
            }
            cloneTower.syncHealth(towerEntity.getHealth());
            cloneTower.syncPosition(GridPosition.from(BlockPos.containing(
                    towerEntity.getX(), towerEntity.getY() - 1.0, towerEntity.getZ()
            )));
            cloneTower.tick(lane);
            towerEntity.syncTowerState(cloneTower);
            if (clone.durationTicks() <= 0) {
                continue;
            }
            clone.incrementAge();
            if (clone.ageTicks() >= clone.durationTicks()) {
                towerEntity.discard();
                clones.remove(index);
            }
        }
    }

    void moveToFinalDefense(PlayerLane lane) {
        for (CloneInstance clone : clones) {
            var entity = lane.arenaWorld().getEntity(clone.entityId());
            if (!(entity instanceof SemionTowerEntity towerEntity) || towerEntity.isRemoved() || !towerEntity.isAlive()) {
                continue;
            }
            Tower cloneTower = towerEntity.runtimeTower();
            if (cloneTower == null) {
                continue;
            }
            GridPosition finalDefensePosition = lane.nextFinalDefenseTowerPosition(cloneTower);
            cloneTower.moveToFinalDefense(lane, finalDefensePosition);
            towerEntity.syncTowerState(cloneTower);
            towerEntity.setPos(
                    finalDefensePosition.x() + 0.5,
                    finalDefensePosition.y() + 1.0,
                    finalDefensePosition.z() + 0.5
            );
            towerEntity.getNavigation().stop();
        }
    }

    void cleanup(PlayerLane lane) {
        LegionIllusionSpawnQueue.cancel(owner);
        for (CloneInstance clone : clones) {
            var entity = lane.arenaWorld().getEntity(clone.entityId());
            if (entity != null) {
                entity.discard();
            }
        }
        clones.clear();
    }

    void cancelPending() {
        LegionIllusionSpawnQueue.cancel(owner);
    }

    private java.util.Optional<SemionTowerEntity> sourceTowerEntity(PlayerLane lane, Tower sourceTower) {
        if (!(sourceTower instanceof EntityBackedTower entityBackedTower)) {
            return java.util.Optional.empty();
        }
        return entityBackedTower.entityId().stream()
                .mapToObj(lane.arenaWorld()::getEntity)
                .filter(SemionTowerEntity.class::isInstance)
                .map(SemionTowerEntity.class::cast)
                .findFirst();
    }

    private Tower createCloneTower(Tower sourceTower, LegionIllusionProfile profile, GridPosition clonePosition) {
        TowerType cloneType = cloneType(sourceTower, profile);
        return ProductionTowerCatalog.entry(sourceTower.type())
                .map(entry -> entry.factory().create(
                        cloneType,
                        sourceTower.ownerPlayer(),
                        sourceTower.teamId(),
                        sourceTower.laneId(),
                        clonePosition,
                        clonePosition
                ))
                .orElseGet(() -> new LegionIllusionRuntimeTower(
                        fallbackCloneType(sourceTower, profile),
                        sourceTower.ownerPlayer(),
                        sourceTower.teamId(),
                        sourceTower.laneId(),
                        clonePosition
                ));
    }

    private TowerType cloneType(Tower sourceTower, LegionIllusionProfile profile) {
        TowerType source = sourceTower.type();
        return new TowerType(
                source.id(),
                source.displayName(),
                source.category() == null ? TowerCategory.DIRECT : source.category(),
                0,
                Math.max(0.01, sourceTower.currentMaxHealth() * profile.healthRatio()),
                Math.max(0.0, source.range() * profile.rangeRatio()),
                Math.max(0.0, source.damage() * profile.damageRatio()),
                Math.max(1, (int) Math.ceil(source.attackIntervalTicks() * profile.attackIntervalMultiplier())),
                sourceTower.aggroPriority() + profile.aggroPriorityBonus(),
                source.description(),
                source.visual(),
                List.of()
        );
    }

    private TowerType fallbackCloneType(Tower sourceTower, LegionIllusionProfile profile) {
        TowerType cloneType = cloneType(sourceTower, profile);
        return new TowerType(
                sourceTower.type().id() + "#illusion",
                cloneType.displayName(),
                cloneType.category(),
                cloneType.mineralCost(),
                cloneType.maxHealth(),
                cloneType.range(),
                cloneType.damage(),
                cloneType.attackIntervalTicks(),
                cloneType.aggroPriority(),
                cloneType.description(),
                cloneType.visual(),
                cloneType.upgradeOptions()
        );
    }

    private static final class CloneInstance {
        private final int entityId;
        private final int durationTicks;
        private int ageTicks;

        private CloneInstance(int entityId, int durationTicks) {
            this.entityId = entityId;
            this.durationTicks = durationTicks;
        }

        private int entityId() { return entityId; }

        private int durationTicks() { return durationTicks; }

        private int ageTicks() { return ageTicks; }

        private void incrementAge() { ageTicks++; }
    }
}
