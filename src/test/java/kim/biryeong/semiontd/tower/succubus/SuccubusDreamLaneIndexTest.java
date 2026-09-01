package kim.biryeong.semiontd.tower.succubus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.map.LaneRegionLayout;
import kim.biryeong.semiontd.tower.succubus.SuccubusDreamLaneIndex.Snapshot;
import kim.biryeong.semiontd.tower.succubus.SuccubusDreamLaneIndex.TowerKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import xyz.nucleoid.map_templates.BlockBounds;

class SuccubusDreamLaneIndexTest {
    @Test
    void snapshotsContainOnlyDreamsOwnedByTheRequestedLane() {
        SuccubusDreamLaneIndex index = new SuccubusDreamLaneIndex();
        PlayerLane first = lane(1);
        PlayerLane second = lane(2);
        TowerKey firstTower = towerKey("first", 1);
        TowerKey secondTower = towerKey("second", 2);
        UUID firstMonster = UUID.nameUUIDFromBytes("first-monster".getBytes());
        UUID secondMonster = UUID.nameUUIDFromBytes("second-monster".getBytes());

        index.indexTower(first, firstTower);
        index.indexMonster(first, firstMonster);
        index.indexLullaby(first, firstTower);
        index.indexTower(second, secondTower);
        index.indexMonster(second, secondMonster);

        assertEquals(new Snapshot(Set.of(firstTower), Set.of(firstMonster), Set.of(firstTower)),
                index.snapshot(first));
        assertEquals(new Snapshot(Set.of(secondTower), Set.of(secondMonster), Set.of()),
                index.snapshot(second));
    }

    @Test
    void movingAKeyReassignsItWithoutLeavingThePreviousLaneIndexed() {
        SuccubusDreamLaneIndex index = new SuccubusDreamLaneIndex();
        PlayerLane first = lane(1);
        PlayerLane second = lane(2);
        TowerKey tower = towerKey("tower", 1);
        UUID monster = UUID.nameUUIDFromBytes("monster".getBytes());

        index.indexTower(first, tower);
        index.indexMonster(first, monster);
        index.indexLullaby(first, tower);
        index.indexTower(second, tower);
        index.indexMonster(second, monster);
        index.indexLullaby(second, tower);

        assertEquals(new Snapshot(Set.of(), Set.of(), Set.of()),
                index.snapshot(first));
        assertEquals(new Snapshot(Set.of(tower), Set.of(monster), Set.of(tower)),
                index.snapshot(second));
    }

    @Test
    void removingALaneClearsReverseOwnershipAndKeepsOtherLanes() {
        SuccubusDreamLaneIndex index = new SuccubusDreamLaneIndex();
        PlayerLane first = lane(1);
        PlayerLane second = lane(2);
        TowerKey firstTower = towerKey("first", 1);
        TowerKey secondTower = towerKey("second", 2);
        UUID firstMonster = UUID.nameUUIDFromBytes("first-monster".getBytes());

        index.indexTower(first, firstTower);
        index.indexMonster(first, firstMonster);
        index.indexLullaby(first, firstTower);
        index.indexTower(second, secondTower);

        Snapshot removed = index.removeLane(first);
        index.indexTower(second, firstTower);
        index.indexMonster(second, firstMonster);
        index.indexLullaby(second, firstTower);

        assertEquals(new Snapshot(Set.of(firstTower), Set.of(firstMonster), Set.of(firstTower)), removed);
        assertTrue(index.snapshot(first).towerKeys().isEmpty());
        assertEquals(Set.of(firstTower, secondTower), index.snapshot(second).towerKeys());
        assertEquals(Set.of(firstMonster), index.snapshot(second).monsterIds());
        assertEquals(Set.of(firstTower), index.snapshot(second).lullabyKeys());
    }

    private static TowerKey towerKey(String owner, int x) {
        return new TowerKey(UUID.nameUUIDFromBytes(owner.getBytes()), new GridPosition(x, 64, x));
    }

    private static PlayerLane lane(int laneId) {
        Vec3 spawn = new Vec3(laneId + 0.5, 64.0, 0.5);
        LaneRegionLayout layout = new LaneRegionLayout(
                laneId,
                spawn,
                List.of(new Vec3(laneId + 0.5, 64.0, 2.5)),
                new Vec3(laneId + 0.5, 64.0, 10.5),
                BlockBounds.of(new BlockPos(laneId, 63, 0), new BlockPos(laneId + 2, 66, 10)),
                List.of(new GridPosition(laneId, 63, 10))
        );
        return new PlayerLane(TeamId.BLUE, laneId,
                UUID.nameUUIDFromBytes(("lane-" + laneId).getBytes()), null, layout);
    }
}
