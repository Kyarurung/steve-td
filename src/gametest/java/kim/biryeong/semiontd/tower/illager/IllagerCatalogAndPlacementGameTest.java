package kim.biryeong.semiontd.tower.illager;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.IllagerTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import kim.biryeong.semiontd.tower.animal.AnimalTowers;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public final class IllagerCatalogAndPlacementGameTest {
    @GameTest
    public void jobExposesIllagerStartersRejectsOtherFamiliesAndLinksPillagerBranches(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("illager-catalog-placement");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, IllagerTowerJob.ID, "illager-catalog-placement"
            );
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
            Set<String> starterIds = ProductionTowerService.availableTowers(game, owner).stream()
                    .map(entry -> entry.type().id())
                    .collect(Collectors.toSet());
            BuilderIntegrationGameTestSupport.require(
                    starterIds.equals(Set.of(
                            IllagerTowers.T1_VINDICATOR.id(),
                            IllagerTowers.T1_PILLAGER.id(),
                            IllagerTowers.T1_VEX.id()
                    )),
                    "Illager job must expose exactly its three starter families."
            );
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, AnimalTowers.T1_PIG_TOWER.id())
                            == TowerPlacementResult.TOWER_NOT_ALLOWED,
                    "Illager job must reject non-Illager placement."
            );
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, IllagerTowers.T1_PILLAGER.id())
                            == TowerPlacementResult.SUCCESS,
                    "Illager job must place its Pillager starter."
            );
            Set<String> upgradeIds = ProductionTowerService.availableUpgrades(game, owner, position).stream()
                    .map(option -> option.targetType().id())
                    .collect(Collectors.toSet());
            BuilderIntegrationGameTestSupport.require(
                    upgradeIds.equals(Set.of(
                            IllagerTowers.T2_PILLAGER_CAPTAIN_SINGLE.id(),
                            IllagerTowers.T2_PILLAGER_CAPTAIN_SPLASH.id()
                    )),
                    "Pillager starter must retain both configured upgrade branches."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Illager catalog placement integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }
}
