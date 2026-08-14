package kim.biryeong.semiontd.tower.insect;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.map.LaneRegionLayout;
import kim.biryeong.semiontd.tower.ProductionTower;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.map_templates.BlockBounds;

public final class InsectGameTest {
    @GameTest
    public void freshUnitRevivesAtDeathPositionAndSpawnerLossCancelsNextRevival(GameTestHelper context) {
        UUID owner = UUID.nameUUIDFromBytes("insect-revival".getBytes(StandardCharsets.UTF_8));
        PlayerLane lane = testLane(context, owner);
        GridPosition spawnerPosition = floor(context, 3, 2, 4);
        GridPosition unitPosition = floor(context, 5, 2, 4);
        prepareFloor(context, spawnerPosition, unitPosition);

        ProductionTower spawner = new ProductionTower(
                InsectTowers.SPAWNER, owner, TeamId.RED, 1, spawnerPosition, spawnerPosition);
        InsectUnitTower unit = new InsectUnitTower(
                InsectTowers.SILVERFISH, owner, TeamId.RED, 1, unitPosition, unitPosition);
        try {
            unit.recordPlacementEconomy(30, 1);
            lane.addTower(spawner);
            lane.addTower(unit);
            lane.markWaveStarted(1);
            require(unit.freshPowerActive(), "A newly placed T1 unit must receive first-wave power.");
            require(close(unit.currentMaxHealth(), 225.0), "Fresh Silverfish must have 250% max health.");

            towerEntity(context, unit).setHealth(0.0f);
            require(!unit.isDestroyed(lane), "A valid nearby spawner must suppress permanent death.");
            require(unit.reviveTicksRemaining() == 60, "First revival must take exactly three seconds.");
            for (int tick = 0; tick < 60; tick++) {
                unit.tick(lane);
            }
            require(unit.health() == unit.currentMaxHealth(), "Revival must restore full current-tier health.");
            require(unit.position().equals(unitPosition), "Revival must use the death position.");
            require(towerEntity(context, unit).isAlive(), "Revival must respawn the tower entity.");

            towerEntity(context, unit).setHealth(0.0f);
            require(!unit.isDestroyed(lane), "Second death must initially schedule another revival.");
            towerEntity(context, spawner).setHealth(0.0f);
            require(unit.isDestroyed(lane), "Destroying every linked spawner must cancel pending revival.");
            context.succeed();
        } finally {
            lane.clearTowers();
        }
    }

    private static SemionTowerEntity towerEntity(GameTestHelper context, kim.biryeong.semiontd.tower.EntityBackedTower tower) {
        return (SemionTowerEntity) context.getLevel().getEntity(tower.entityId().orElseThrow());
    }

    private static PlayerLane testLane(GameTestHelper context, UUID owner) {
        BlockPos min = context.absolutePos(new BlockPos(0, 1, 0));
        BlockPos max = context.absolutePos(new BlockPos(14, 6, 14));
        LaneRegionLayout layout = new LaneRegionLayout(
                1,
                Vec3.atCenterOf(context.absolutePos(new BlockPos(1, 2, 1))),
                List.of(Vec3.atCenterOf(context.absolutePos(new BlockPos(7, 2, 7)))),
                Vec3.atCenterOf(context.absolutePos(new BlockPos(7, 2, 13))),
                BlockBounds.of(min, max),
                List.of(GridPosition.from(context.absolutePos(new BlockPos(10, 2, 11))))
        );
        return new PlayerLane(TeamId.RED, 1, owner, context.getLevel(), layout);
    }

    private static GridPosition floor(GameTestHelper context, int x, int y, int z) {
        return GridPosition.from(context.absolutePos(new BlockPos(x, y, z)));
    }

    private static void prepareFloor(GameTestHelper context, GridPosition... positions) {
        for (GridPosition position : positions) {
            context.getLevel().setBlock(
                    new BlockPos(position.x(), position.y(), position.z()),
                    Blocks.STONE.defaultBlockState(), 3);
            context.getLevel().setBlock(
                    new BlockPos(position.x(), position.y() + 1, position.z()),
                    Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static boolean close(double first, double second) {
        return Math.abs(first - second) < 0.0001;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
