package kim.biryeong.semiontd.tower.engineer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.visual.BlockDisplayVisual;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.map.LaneRegionLayout;
import kim.biryeong.semiontd.tower.TowerPlacementPositions;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.map_templates.BlockBounds;

public final class EngineerGameTest {
    @GameTest
    public void engineerPlateIgnoresEntitiesOtherThanCopperGolem(GameTestHelper context) {
        UUID owner = stableUuid("engineer-golem-only-plate");
        PlayerLane lane = testLane(context, owner);
        GridPosition platePosition = floor(context, 6, 2, 6);
        prepareFloor(context, platePosition);
        EngineerCircuitTower plate = new EngineerCircuitTower(
                EngineerTowers.plate(EngineerTowers.PlateKind.WOOD),
                owner,
                TeamId.RED,
                1,
                platePosition,
                platePosition
        );
        lane.addTower(plate);
        Mob intruder = context.spawn(EntityType.ZOMBIE, new BlockPos(6, 3, 6));
        intruder.setNoAi(true);

        context.runAfterDelay(5, () -> {
            try {
                require(!plate.platePressed(lane),
                        "Players and ordinary entities must not power an engineer pressure plate.");
                context.succeed();
            } catch (Throwable failure) {
                context.fail(Component.literal("Engineer golem-only plate GameTest failed: "
                        + failure.getClass().getName() + ": " + failure.getMessage()));
            } finally {
                intruder.discard();
                lane.clearTowers();
            }
        });
    }

    @GameTest
    public void circuitsUseRealBlocksAndPoweredTrapStopsAtFinalDefense(GameTestHelper context) {
        UUID owner = stableUuid("engineer-circuit");
        PlayerLane lane = testLane(context, owner);
        GridPosition dustPosition = floor(context, 2, 2, 3);
        GridPosition repeaterPosition = floor(context, 4, 2, 3);
        GridPosition trapPosition = floor(context, 6, 2, 3);
        GridPosition platePosition = floor(context, 5, 2, 3);
        GridPosition doorPosition = floor(context, 8, 2, 3);
        GridPosition dispenserPosition = floor(context, 10, 2, 3);
        GridPosition pistonPosition = floor(context, 12, 2, 3);
        prepareFloor(context, dustPosition, repeaterPosition, platePosition, trapPosition, doorPosition,
                dispenserPosition, pistonPosition);

        EngineerCircuitTower dust = new EngineerCircuitTower(
                EngineerTowers.REDSTONE_DUST, owner, TeamId.RED, 1, dustPosition, dustPosition
        );
        EngineerCircuitTower repeater = new EngineerCircuitTower(
                EngineerTowers.repeater(Direction.EAST), owner, TeamId.RED, 1, repeaterPosition, repeaterPosition
        );
        EngineerTrapTower slime = new EngineerTrapTower(
                EngineerTowers.trap(EngineerTowers.TrapKind.SLIME, 1),
                owner, TeamId.RED, 1, trapPosition, trapPosition
        );
        EngineerCircuitTower plate = new EngineerCircuitTower(
                EngineerTowers.plate(EngineerTowers.PlateKind.WOOD),
                owner, TeamId.RED, 1, platePosition, platePosition
        );
        EngineerTrapTower door = new EngineerTrapTower(
                EngineerTowers.trap(EngineerTowers.TrapKind.DOOR, 1),
                owner, TeamId.RED, 1, doorPosition, doorPosition
        );
        EngineerTrapTower dispenser = new EngineerTrapTower(
                EngineerTowers.trap(EngineerTowers.TrapKind.DISPENSER, 1),
                owner, TeamId.RED, 1, dispenserPosition, dispenserPosition
        );
        EngineerTrapTower piston = new EngineerTrapTower(
                EngineerTowers.trap(EngineerTowers.TrapKind.PISTON, 1),
                owner, TeamId.RED, 1, pistonPosition, pistonPosition
        );
        try {
            lane.addTower(dust);
            lane.addTower(repeater);
            lane.addTower(plate);
            lane.addTower(slime);
            lane.addTower(door);
            lane.addTower(dispenser);
            lane.addTower(piston);
            require(context.getLevel().getBlockState(dust.circuitPosition()).is(Blocks.REDSTONE_WIRE),
                    "Dust must place a real redstone wire block.");
            require(context.getLevel().getBlockState(repeater.circuitPosition()).is(Blocks.REPEATER),
                    "Repeater must place a real vanilla repeater block.");
            require(context.getLevel().getBlockState(repeater.circuitPosition()).getValue(RepeaterBlock.FACING) == Direction.EAST,
                    "Repeater facing must match the selected direction.");
            require(TowerPlacementPositions.resolveGrid(lane, dust.circuitPosition()).orElseThrow().equals(dustPosition),
                    "Clicking the physical wire must resolve to the logical tower below it.");
            require(door.hasUpperDoorVisual(), "Iron door traps must render both lower and upper halves.");
            require(BlockDisplayVisual.blockState(dispenser.visual()).getValue(DispenserBlock.FACING) == Direction.WEST,
                    "Dispenser front must face the incoming lane spawn.");
            require(BlockDisplayVisual.blockState(piston.visual()).getValue(PistonBaseBlock.FACING) == Direction.WEST,
                    "Piston front must face the incoming lane spawn.");

            lane.markWaveStarted(1);
            context.getLevel().setBlock(slimePosition(trapPosition).east(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            slime.tick(lane);
            require(slime.activeTicksRemaining() == 0,
                    "A powered loop without a recent copper-golem plate press must not activate a trap.");
            context.getLevel().setBlock(slimePosition(trapPosition).east(), Blocks.AIR.defaultBlockState(), 3);
            require(plate.pressPlate(lane), "The engineer plate must create the authorized circuit pulse.");
            slime.tick(lane);
            require(slime.activeTicksRemaining() > 0, "A powered trap must latch for three seconds.");
            require(slime.activationPlateDistance() == 1,
                    "A directly adjacent activated plate must be recorded at circuit distance one.");
            SemionTowerEntity slimeEntity = (SemionTowerEntity) context.getLevel()
                    .getEntity(slime.entityId().orElseThrow());
            require(slimeEntity.getCustomName() != null
                            && slimeEntity.getCustomName().getString().startsWith("활성화된 "),
                    "An active trap name must be prefixed with 활성화된.");
            context.getLevel().setBlock(
                    plate.circuitPosition(),
                    context.getLevel().getBlockState(plate.circuitPosition())
                            .setValue(BlockStateProperties.POWERED, false),
                    3
            );
            for (int tick = 0; tick < 10; tick++) {
                slime.tick(lane);
            }
            require(slime.activeTicksRemaining() > 0, "The trap must remain active after power is removed.");
            require(plate.pressPlate(lane), "The plate must be able to send a second authorized pulse.");
            slime.tick(lane);
            require(slime.activeTicksRemaining() >= EngineerBalance.activeTicks() - 1,
                    "A new power edge must refresh an active trap back to three seconds.");
            lane.moveTowersToFinalDefense();
            require(slime.activeTicksRemaining() == 0, "Forced final defense must stop traps immediately.");
            require(!slime.deployedAtFinalDefense(), "Engineer traps must not consume a final-defense slot.");
            context.succeed();
        } catch (Throwable failure) {
            context.fail(Component.literal("Engineer circuit GameTest failed: "
                    + failure.getClass().getName() + ": " + failure.getMessage()));
        } finally {
            lane.clearTowers();
        }
    }

    @GameTest
    public void copperGolemChoosesGoldBeforeWoodAndIsInvulnerable(GameTestHelper context) {
        UUID owner = stableUuid("engineer-golem-priority");
        PlayerLane lane = testLane(context, owner);
        GridPosition golemPosition = floor(context, 5, 2, 7);
        GridPosition woodPosition = floor(context, 6, 2, 7);
        GridPosition pathOne = floor(context, 7, 2, 7);
        GridPosition pathTwo = floor(context, 8, 2, 7);
        GridPosition goldPosition = floor(context, 9, 2, 7);
        prepareFloor(context, golemPosition, woodPosition, pathOne, pathTwo, goldPosition);

        EngineerGolemTower golem = new EngineerGolemTower(
                EngineerTowers.COPPER_GOLEM, owner, TeamId.RED, 1, golemPosition, golemPosition
        );
        EngineerCircuitTower wood = new EngineerCircuitTower(
                EngineerTowers.plate(EngineerTowers.PlateKind.WOOD), owner, TeamId.RED, 1, woodPosition, woodPosition
        );
        EngineerCircuitTower gold = new EngineerCircuitTower(
                EngineerTowers.plate(EngineerTowers.PlateKind.GOLD), owner, TeamId.RED, 1, goldPosition, goldPosition
        );
        try {
            lane.addTower(golem);
            lane.addTower(wood);
            lane.addTower(gold);
            Mob entity = golem.golemEntity(lane);
            require(entity != null, "Copper golem must spawn when its tower is placed.");
            require(entity.isInvulnerable(), "Copper golem must be invulnerable.");
            require(golem.ownsGolemEntity(entity), "The visible copper golem must resolve to its logical tower for clicking.");
            require(entity.getCustomName() != null && entity.getCustomName().getString().equals("구리 골렘")
                            && entity.isCustomNameVisible(),
                    "Copper golem must show its tower name above the model.");

            lane.markWaveStarted(1);
            require(goldPosition.equals(golem.targetPlate()),
                    "Gold plate must be selected immediately when the wave starts.");
            for (int tick = 0; tick < 80 && golem.pressesThisWave() < 1; tick++) {
                golem.tick(lane);
            }
            require(golem.pressesThisWave() == 1,
                    "The golem must reach and physically power the selected pressure plate.");
            require(Math.abs(entity.getYRot() - entity.getYHeadRot()) < 0.001,
                    "The copper golem head must keep facing its movement direction.");
            for (int tick = 0; tick < 80 && golem.pressesThisWave() < 2; tick++) {
                golem.tick(lane);
            }
            require(golem.pressesThisWave() == 2,
                    "The golem must continue to the remaining pressure plate.");
            Vec3 waitingPosition = entity.position();
            for (int tick = 0; tick < 20; tick++) {
                golem.tick(lane);
            }
            require(entity.position().distanceToSqr(waitingPosition) < 0.01,
                    "With no available plate, the golem must wait on its last plate instead of returning home.");
            context.succeed();
        } catch (Throwable failure) {
            context.fail(Component.literal("Engineer golem GameTest failed: "
                    + failure.getClass().getName() + ": " + failure.getMessage()));
        } finally {
            lane.clearTowers();
        }
    }

    private static PlayerLane testLane(GameTestHelper context, UUID owner) {
        BlockPos min = context.absolutePos(new BlockPos(0, 1, 0));
        BlockPos max = context.absolutePos(new BlockPos(14, 6, 14));
        Vec3 spawn = Vec3.atCenterOf(context.absolutePos(new BlockPos(1, 2, 1)));
        Vec3 waypoint = Vec3.atCenterOf(context.absolutePos(new BlockPos(7, 2, 7)));
        Vec3 boss = Vec3.atCenterOf(context.absolutePos(new BlockPos(7, 2, 13)));
        LaneRegionLayout layout = new LaneRegionLayout(
                1, spawn, List.of(waypoint), boss, BlockBounds.of(min, max),
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
                    Blocks.STONE.defaultBlockState(), 3
            );
            context.getLevel().setBlock(
                    new BlockPos(position.x(), position.y() + 1, position.z()),
                    Blocks.AIR.defaultBlockState(), 3
            );
        }
    }

    private static BlockPos slimePosition(GridPosition position) {
        return new BlockPos(position.x(), position.y() + 1, position.z());
    }

    private static UUID stableUuid(String seed) {
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
