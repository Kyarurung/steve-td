package kim.biryeong.semiontd.tower.futureagency;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.map.LaneRegionLayout;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.map_templates.BlockBounds;

public final class FutureAgencyGameTest {
    @GameTest
    public void survivingDuplicatesCarryIndependentlyAndDeadAgentReturnsFull(GameTestHelper context) {
        UUID owner = UUID.nameUUIDFromBytes("future-agency-carry".getBytes(StandardCharsets.UTF_8));
        FutureAgencyStates.clear(owner);
        FutureAgencyStates.state(owner).reconstruct();
        PlayerLane lane = testLane(context, owner);
        GridPosition firstOrigin = floor(context, 3, 2, 3);
        GridPosition secondOrigin = floor(context, 5, 2, 3);
        GridPosition deadOrigin = floor(context, 7, 2, 3);
        GridPosition firstCarry = floor(context, 3, 2, 8);
        GridPosition secondCarry = floor(context, 5, 2, 9);
        prepareFloor(context, firstOrigin, secondOrigin, deadOrigin, firstCarry, secondCarry);

        FutureAgencyAgentTower first = agent(owner, FutureAgencyRole.COMBAT, firstOrigin);
        FutureAgencyAgentTower second = agent(owner, FutureAgencyRole.COMBAT, secondOrigin);
        FutureAgencyAgentTower dead = agent(owner, FutureAgencyRole.PROTECTION, deadOrigin);
        try {
            lane.addTower(first);
            lane.addTower(second);
            lane.addTower(dead);
            require(first.idleMovementTarget(towerEntity(context, first)).isEmpty(),
                    "Agents must remain still during preparation.");
            lane.markWaveStarted(1);
            require(first.idleMovementTarget(towerEntity(context, first)).isPresent(),
                    "Agents must start advancing when the wave begins.");
            moveAndDamage(first, lane, firstCarry, 31.0);
            moveAndDamage(second, lane, secondCarry, 47.0);
            towerEntity(context, dead).setHealth(0.0f);
            require(dead.isDestroyed(lane), "The third agent must be dead before round reset.");

            first.onLaneCleared(lane);
            second.onLaneCleared(lane);
            lane.resetForRound();

            require(first.position().equals(firstOrigin) && close(first.health(), first.currentMaxHealth()),
                    "The installed first agent must return at its origin with full health.");
            require(second.position().equals(secondOrigin) && close(second.health(), second.currentMaxHealth()),
                    "The installed second agent must return at its origin with full health.");
            require(dead.position().equals(deadOrigin) && close(dead.health(), dead.currentMaxHealth()),
                    "Dead agent must return at its original position with full health.");
            List<FutureAgencyAgentTower> carried = lane.towers().stream()
                    .filter(FutureAgencyAgentTower.class::isInstance)
                    .map(FutureAgencyAgentTower.class::cast)
                    .filter(FutureAgencyAgentTower::carriedCopy)
                    .toList();
            require(carried.size() == 2, "Each surviving installed agent must add one carried copy.");
            require(carried.stream().anyMatch(agent -> agent.position().equals(firstCarry) && close(agent.health(), 31.0)),
                    "The first survivor copy must preserve its position and health.");
            require(carried.stream().anyMatch(agent -> agent.position().equals(secondCarry) && close(agent.health(), 47.0)),
                    "The second survivor copy must preserve its position and health.");
            require(towerEntity(context, first).isAlive() && towerEntity(context, second).isAlive()
                            && towerEntity(context, dead).isAlive()
                            && carried.stream().allMatch(agent -> towerEntity(context, agent).isAlive()),
                    "Original and carried agents must appear together during preparation.");
            context.succeed();
        } finally {
            lane.clearTowers();
            FutureAgencyStates.clear(owner);
        }
    }

    private static FutureAgencyAgentTower agent(UUID owner, FutureAgencyRole role, GridPosition position) {
        return new FutureAgencyAgentTower(
                FutureAgencyTowers.agent(role, 5), owner, TeamId.RED, 1, position, position);
    }

    private static void moveAndDamage(FutureAgencyAgentTower agent, PlayerLane lane,
                                      GridPosition position, double health) {
        agent.syncPosition(position);
        agent.syncHealth(health);
        agent.onStateChanged(lane);
        towerEntity(lane, agent).setHealth((float) health);
    }

    private static SemionTowerEntity towerEntity(GameTestHelper context, FutureAgencyAgentTower tower) {
        return (SemionTowerEntity) context.getLevel().getEntity(tower.entityId().orElseThrow());
    }

    private static SemionTowerEntity towerEntity(PlayerLane lane, FutureAgencyAgentTower tower) {
        return (SemionTowerEntity) lane.arenaWorld().getEntity(tower.entityId().orElseThrow());
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
            context.getLevel().setBlock(new BlockPos(position.x(), position.y(), position.z()),
                    Blocks.STONE.defaultBlockState(), 3);
            context.getLevel().setBlock(new BlockPos(position.x(), position.y() + 1, position.z()),
                    Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static boolean close(double first, double second) {
        return Math.abs(first - second) < 0.0001;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
