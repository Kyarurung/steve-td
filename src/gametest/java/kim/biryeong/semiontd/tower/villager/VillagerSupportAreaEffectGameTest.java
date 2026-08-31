package kim.biryeong.semiontd.tower.villager;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.VillagerTowerJob;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

public final class VillagerSupportAreaEffectGameTest {
    @GameTest
    public void allayHealingUsesRegisteredTowerAreaEffectAndBlocksDuplicateApplication(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("villager-support-area-effect");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, VillagerTowerJob.ID, "villager-support-area-effect"
            );
            game.players().get(owner).economy().addMineral(10_000);
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            List<BlockPos> positions = BuilderIntegrationGameTestSupport.emptyPositions(
                    lane, BuilderIntegrationGameTestSupport.primaryPosition(lane), 2
            );
            double radius = TowerBalanceRuntime.ability(VillagerTowers.T1_ALLAY_TOWER.id(), "radius");
            BlockPos farPosition = farPosition(lane, positions.get(0), radius);
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(
                            game, owner, positions.get(0), VillagerTowers.T1_ALLAY_TOWER.id()
                    ) == TowerPlacementResult.SUCCESS,
                    "Allay must place through the production service."
            );
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(
                            game, owner, positions.get(1), VillagerTowers.T1_GOLEM_TOWER.id()
                    ) == TowerPlacementResult.SUCCESS,
                    "Healing target must place through the production service."
            );
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(
                            game, owner, farPosition, VillagerTowers.T1_GOLEM_TOWER.id()
                    ) == TowerPlacementResult.SUCCESS,
                    "Out-of-range control target must place through the production service."
            );

            VillagerAllayTower allay = (VillagerAllayTower) lane.towerAt(GridPosition.from(positions.get(0)));
            EntityBackedTower target = (EntityBackedTower) lane.towerAt(GridPosition.from(positions.get(1)));
            EntityBackedTower farTarget = (EntityBackedTower) lane.towerAt(GridPosition.from(farPosition));
            SemionTowerEntity allayEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, allay);
            SemionTowerEntity targetEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, target);
            SemionTowerEntity farEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, farTarget);
            BuilderIntegrationGameTestSupport.require(allayEntity.getPolymerEntityType(null) == EntityType.ALLAY,
                    "Allay support must use its production Allay entity proxy.");
            lane.markWaveStarted(1);
            target.syncHealth(target.currentMaxHealth() - 30.0);
            targetEntity.setHealth((float) target.health());
            farTarget.syncHealth(farTarget.currentMaxHealth() - 30.0);
            farEntity.setHealth((float) farTarget.health());
            double farHealth = farTarget.health();

            allay.tick(lane);
            double healedHealth = target.health();
            BuilderIntegrationGameTestSupport.require(healedHealth > target.currentMaxHealth() - 30.0,
                    "Shared area support must synchronize healing to runtime tower state.");
            BuilderIntegrationGameTestSupport.require(allay.roundMetricsTracker().snapshot().healingDone() > 0.0,
                    "Shared healing must be attributed to the Allay tower metrics.");
            BuilderIntegrationGameTestSupport.requireClose(farHealth, farTarget.health(),
                    "Allay support must not heal a registered tower outside its configured radius.");

            target.syncHealth(healedHealth - 5.0);
            targetEntity.setHealth((float) target.health());
            double blockedHealth = target.health();
            for (int tick = 0; tick <= allay.type().attackIntervalTicks(); tick++) {
                allay.tick(lane);
            }
            BuilderIntegrationGameTestSupport.requireClose(blockedHealth, target.health(),
                    "The tower hook must reject another support pulse during its configured block window.");
            SemionGame scheduledGame = game;
            game = null;
            context.runAfterDelay(3, () -> {
                scheduledGame.close();
                TowerBalanceRuntime.apply(defaults);
                context.succeed();
            });
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Villager support integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }

    private static BlockPos farPosition(PlayerLane lane, BlockPos origin, double radius) {
        double minimumDistanceSqr = (radius + 1.0) * (radius + 1.0);
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                BlockPos candidate = origin.offset(dx, 0, dz);
                if (dx * dx + dz * dz > minimumDistanceSqr
                        && lane.canPlaceTowerAt(candidate)
                        && !lane.hasTowerAt(GridPosition.from(candidate))) {
                    return candidate;
                }
            }
        }
        throw new IllegalStateException("Could not find an out-of-range Villager support control position.");
    }
}
