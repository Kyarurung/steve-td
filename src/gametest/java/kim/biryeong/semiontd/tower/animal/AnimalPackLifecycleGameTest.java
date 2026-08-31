package kim.biryeong.semiontd.tower.animal;

import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.game.TowerSellResult;
import kim.biryeong.semiontd.game.TowerUpgradeResult;
import kim.biryeong.semiontd.job.AnimalTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import kim.biryeong.semiontd.tower.animal.AnimalTowers;
import kim.biryeong.semiontd.tower.animal.AnimalRabbitTower;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public final class AnimalPackLifecycleGameTest {
    @GameTest
    public void packRecomputesAcrossProductionUpgradesSaleAndPlayerReuse(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("animal-pack-production-lifecycle");
        SemionGame firstGame = null;
        SemionGame secondGame = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            firstGame = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, AnimalTowerJob.ID, "animal-pack-first"
            );
            firstGame.players().get(owner).economy().addMineral(20_000);
            PlayerLane firstLane = BuilderIntegrationGameTestSupport.lane(firstGame, owner);
            List<BlockPos> positions = BuilderIntegrationGameTestSupport.emptyPositions(
                    firstLane, BuilderIntegrationGameTestSupport.primaryPosition(firstLane), 5
            );
            for (BlockPos position : positions) {
                BuilderIntegrationGameTestSupport.require(
                        ProductionTowerService.placeTower(firstGame, owner, position, AnimalTowers.T1_RABBIT_TOWER.id())
                                == TowerPlacementResult.SUCCESS,
                        "Five rabbit family towers must place through ProductionTowerService."
                );
            }
            AnimalRabbitTower primary = (AnimalRabbitTower) firstLane.towerAt(GridPosition.from(positions.get(0)));
            BuilderIntegrationGameTestSupport.require(
                    String.join("\n", primary.runtimeDetailLines()).contains("무리 스택 4/4"),
                    "Five same-owner rabbits must give the primary rabbit maximum pack stacks."
            );

            for (String targetId : List.of(
                    AnimalTowers.T2_RABBIT_TOWER.id(),
                    AnimalTowers.T3_RABBIT_TOWER.id(),
                    AnimalTowers.T4_RABBIT_LEADER_TOWER.id()
            )) {
                BuilderIntegrationGameTestSupport.require(
                        ProductionTowerService.upgradeTower(firstGame, owner, positions.get(0), targetId)
                                == TowerUpgradeResult.SUCCESS,
                        "Rabbit production upgrade must succeed for " + targetId + '.'
                );
                primary = (AnimalRabbitTower) firstLane.towerAt(GridPosition.from(positions.get(0)));
                BuilderIntegrationGameTestSupport.require(
                        String.join("\n", primary.runtimeDetailLines()).contains("무리 스택 4/4"),
                        "Derived pack stacks must recompute after replacing the tower with " + targetId + '.'
                );
            }
            BuilderIntegrationGameTestSupport.require(
                    String.join("\n", primary.runtimeDetailLines()).contains("우두머리 오라 활성"),
                    "A living rabbit leader at maximum pack stacks must expose an active aura."
            );

            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.sellTower(firstGame, owner, positions.get(1)).result()
                            == TowerSellResult.SUCCESS,
                    "Selling one pack member must use the production sale path."
            );
            BuilderIntegrationGameTestSupport.require(
                    String.join("\n", primary.runtimeDetailLines()).contains("무리 스택 3/4"),
                    "Selling a family member must immediately recompute the leader's derived stacks."
            );
            BuilderIntegrationGameTestSupport.require(
                    String.join("\n", primary.runtimeDetailLines()).contains("우두머리 오라 비활성"),
                    "Dropping below maximum stacks must deactivate the leader aura."
            );

            firstGame.close();
            firstGame = null;
            secondGame = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, AnimalTowerJob.ID, "animal-pack-reuse"
            );
            secondGame.players().get(owner).economy().addMineral(1_000);
            PlayerLane secondLane = BuilderIntegrationGameTestSupport.lane(secondGame, owner);
            BlockPos secondPosition = BuilderIntegrationGameTestSupport.primaryPosition(secondLane);
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(
                            secondGame, owner, secondPosition, AnimalTowers.T1_RABBIT_TOWER.id()
                    ) == TowerPlacementResult.SUCCESS,
                    "The reused player must be able to place a fresh rabbit in a second match."
            );
            AnimalRabbitTower freshRabbit = (AnimalRabbitTower) secondLane.towerAt(GridPosition.from(secondPosition));
            BuilderIntegrationGameTestSupport.require(
                    String.join("\n", freshRabbit.runtimeDetailLines()).contains("무리 스택 0/4"),
                    "A second match must not inherit the previous match's derived pack state."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Animal builder integration failed: " + failure.getMessage()));
        } finally {
            if (firstGame != null) {
                firstGame.close();
            }
            if (secondGame != null) {
                secondGame.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }
}
