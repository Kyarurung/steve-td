package kim.biryeong.semiontd.tower.queen;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.config.AttackKind;
import kim.biryeong.semiontd.entity.SemionEntityTypes;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.boss.BossMonster;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.game.TeamLaneGroup;
import kim.biryeong.semiontd.map.LaneRegionLayout;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.map_templates.BlockBounds;

public final class QueenGameTest {
    @GameTest(maxTicks = 120)
    public void shrinkPreservesHealthAndGiantExecutesContactedEnemy(GameTestHelper context) {
        UUID owner = UUID.nameUUIDFromBytes("queen-runtime".getBytes(StandardCharsets.UTF_8));
        QueenStates.clear(owner);
        PlayerLane lane = testLane(context, owner);
        TeamLaneGroup group = new TeamLaneGroup(TeamId.RED, BossMonster.defaultBoss(TeamId.RED));
        group.addLane(lane);
        prepareFloor(context);
        QueenTower queen = (QueenTower) ProductionTowerCatalog.find(QueenTowers.QUEEN.id()).orElseThrow()
                .create(owner, TeamId.RED, 1, GridPosition.from(context.absolutePos(new BlockPos(3, 2, 3))));
        QueenCardTower card = (QueenCardTower) ProductionTowerCatalog.find(QueenTowers.RANDOM_CARD_SOLDIER.id()).orElseThrow()
                .create(owner, TeamId.RED, 1, GridPosition.from(context.absolutePos(new BlockPos(4, 2, 3))));
        try {
            lane.addTower(queen);
            lane.addTower(card);
            Monster monster = new Monster("queen-target", TeamId.RED, 1, Optional.empty(), Optional.empty(),
                    80.0, 0.0, 20.0, AttackKind.MELEE, "minecraft:zombie", 5L);
            SemionMonsterEntity target = new SemionMonsterEntity(SemionEntityTypes.MONSTER, context.getLevel());
            target.configureFrom(monster, lane.laneLayout());
            Vec3 spawn = lane.laneLayout().spawn();
            target.setPos(spawn.x, spawn.y, spawn.z);
            require(context.getLevel().addFreshEntity(target), "Target monster must spawn.");
            monster.markMinecraftEntitySpawned(target.getId(), spawn.x, spawn.y, spawn.z);
            lane.activeMonsters().add(monster);
            Monster nearbyMonster = new Monster("queen-nearby-target", TeamId.RED, 1, Optional.empty(), Optional.empty(),
                    80.0, 0.0, 20.0, AttackKind.MELEE, "minecraft:zombie", 5L);
            SemionMonsterEntity nearby = new SemionMonsterEntity(SemionEntityTypes.MONSTER, context.getLevel());
            nearby.configureFrom(nearbyMonster, lane.laneLayout());
            nearby.setPos(spawn.x + 1.0, spawn.y, spawn.z);
            require(context.getLevel().addFreshEntity(nearby), "Nearby target monster must spawn.");
            nearbyMonster.markMinecraftEntitySpawned(nearby.getId(), spawn.x + 1.0, spawn.y, spawn.z);
            lane.activeMonsters().add(nearbyMonster);
            SemionTowerEntity queenEntity = (SemionTowerEntity) context.getLevel().getEntity(queen.entityId().orElseThrow());

            monster.syncHealth(40.0);
            target.setHealth(40.0F);
            queen.onAttackResolved(queenEntity, target, 0.0, 0.0, 0.0, false);
            double factor = Math.pow(QueenBalance.shrinkFactorPerPoint(), QueenBalance.queenShrinkPoints());
            requireClose(80.0 * factor, monster.maxHealth(), "Queen shrink must reduce max health.");
            requireClose(40.0 * factor, monster.health(), "Queen shrink must preserve current health ratio.");
            requireClose(20.0 * factor, monster.attackDamage(), "Queen shrink must reduce attack damage.");
            require(monster.isAlive(), "Shrink must never kill its target directly.");
            SemionTowerEntity cardEntity = (SemionTowerEntity) context.getLevel().getEntity(card.entityId().orElseThrow());
            card.onAttackResolved(cardEntity, target, 0.0, 0.0, 0.0, false);
            require(nearbyMonster.maxHealth() < 80.0,
                    "Every card suit must splash shrink to at least one nearby target.");

            QueenStates.PlayerState state = QueenStates.state(owner);
            state.addCharge(QueenBalance.giantChargeTicks());
            queen.onWaveStarted(lane, 1);
            queen.tick(lane);
            require(state.runnerActive(), "A full gauge must dispatch a Giant.");
            Vec3 laneEnd = lane.laneLayout().waypoints().getFirst();
            require(state.runner().position().distanceToSqr(laneEnd) < 0.01,
                    "The Giant must begin at the player's lane end, not at the central boss position.");
            Vec3 direction = lane.laneLayout().spawn().subtract(laneEnd);
            float expectedYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
            require(angleDifference(state.runner().yaw(), expectedYaw) < 0.1,
                    "The Giant must face the direction it is running.");
            for (int tick = 0; tick < 80 && monster.isAlive(); tick++) queen.tick(lane);
            require(!monster.isAlive(), "The Giant must execute a contacted enemy below its threshold.");
            require(state.executionHealth() > QueenBalance.giantInitialExecutionHealth(),
                    "A successful execution must grow the permanent execution threshold.");
            context.succeed();
        } finally {
            group.closeRuntime();
            QueenStates.clear(owner);
        }
    }

    private static PlayerLane testLane(GameTestHelper context, UUID owner) {
        BlockPos min = context.absolutePos(new BlockPos(0, 1, 0));
        BlockPos max = context.absolutePos(new BlockPos(14, 6, 14));
        LaneRegionLayout layout = new LaneRegionLayout(1,
                Vec3.atCenterOf(context.absolutePos(new BlockPos(2, 2, 2))),
                BlockBounds.of(context.absolutePos(new BlockPos(2, 2, 2)), context.absolutePos(new BlockPos(2, 2, 2))),
                List.of(
                        Vec3.atCenterOf(context.absolutePos(new BlockPos(7, 2, 7))),
                        Vec3.atCenterOf(context.absolutePos(new BlockPos(18, 2, 18)))
                ),
                Vec3.atCenterOf(context.absolutePos(new BlockPos(12, 2, 12))),
                BlockBounds.of(min, max), List.of(GridPosition.from(context.absolutePos(new BlockPos(10, 2, 11)))), 1);
        return new PlayerLane(TeamId.RED, 1, owner, context.getLevel(), layout);
    }

    private static void prepareFloor(GameTestHelper context) {
        for (int x = 0; x <= 14; x++) for (int z = 0; z <= 14; z++) {
            BlockPos floor = context.absolutePos(new BlockPos(x, 1, z));
            context.getLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
            context.getLevel().setBlock(floor.above(), Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void requireClose(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 0.001) throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
    }

    private static double angleDifference(float first, float second) {
        return Math.abs(((first - second + 540.0) % 360.0) - 180.0);
    }
}
