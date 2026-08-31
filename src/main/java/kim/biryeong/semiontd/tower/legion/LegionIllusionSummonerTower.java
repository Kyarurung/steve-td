package kim.biryeong.semiontd.tower.legion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.SummonerTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.phys.Vec3;

public abstract class LegionIllusionSummonerTower extends SummonerTower {
    private final LegionIllusionCloneController clones = new LegionIllusionCloneController(this);

    protected LegionIllusionSummonerTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    protected LegionIllusionSummonerTower(
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
    public void onWaveStarted(PlayerLane lane, int currentRound) {
        cleanupClones(lane);
        LegionIllusionProfile profile = illusionProfile(lane);
        if (profile.cloneCount() <= 0 || health() <= 0.0) {
            return;
        }
        clones.enqueue(lane, this, profile, resolvedSpawnOffsets(profile));
    }

    @Override
    public void tick(PlayerLane lane) {
        super.tick(lane);
        tickClones(lane);
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        cleanupClones(lane);
        super.resetForRound(lane);
    }

    @Override
    public void moveToFinalDefense(PlayerLane lane, GridPosition position) {
        clones.cancelPending();
        super.moveToFinalDefense(lane, position);
        clones.moveToFinalDefense(lane);
    }

    @Override
    public void onRemoved(PlayerLane lane) {
        cleanupClones(lane);
        super.onRemoved(lane);
    }

    protected LegionIllusionProfile illusionProfile(PlayerLane lane) {
        LegionIllusionProfile defaults = defaultIllusionProfile(lane);
        return LegionConfig.RUNTIME.illusionProfile(type(), defaults);
    }

    protected LegionIllusionProfile defaultIllusionProfile(PlayerLane lane) {
        return LegionIllusionProfile.defaults();
    }

    protected List<Vec3> spawnOffsets(LegionIllusionProfile profile) {
        return defaultSpawnOffsets(profile);
    }

    protected void onCloneSpawned(PlayerLane lane, SemionTowerEntity cloneEntity, Tower cloneTower) {
    }

    @Override
    protected boolean execute(PlayerLane lane) {
        return false;
    }

    protected final void spawnClones(PlayerLane lane, Tower sourceTower, LegionIllusionProfile profile) {
        clones.enqueue(lane, sourceTower, profile, resolvedSpawnOffsets(profile));
    }

    final void spawnQueuedClone(PlayerLane lane, Tower sourceTower, LegionIllusionProfile profile, Vec3 offset) {
        clones.spawnQueued(lane, sourceTower, profile, offset);
    }

    protected final void tickClones(PlayerLane lane) {
        clones.tick(lane);
    }

    protected final void cleanupClones(PlayerLane lane) {
        clones.cleanup(lane);
    }

    private List<Vec3> resolvedSpawnOffsets(LegionIllusionProfile profile) {
        List<Vec3> offsets = spawnOffsets(profile);
        return offsets == null || offsets.isEmpty() ? defaultSpawnOffsets(profile) : offsets;
    }

    private List<Vec3> defaultSpawnOffsets(LegionIllusionProfile profile) {
        if (profile.cloneCount() <= 0) {
            return List.of();
        }
        List<Vec3> offsets = new ArrayList<>(profile.cloneCount());
        for (int index = 0; index < profile.cloneCount(); index++) {
            double angle = (Math.PI * 2.0 * index) / profile.cloneCount();
            offsets.add(new Vec3(
                    Math.cos(angle) * profile.spawnRadius(),
                    0.0,
                    Math.sin(angle) * profile.spawnRadius()
            ));
        }
        return offsets;
    }
}
