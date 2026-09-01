package kim.biryeong.semiontd.tower.villager;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.game.TowerUpgradeResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

public final class VillagerAdvCatalogAndUpgradeGameTest extends VillagerAdvGameTestSupport {
    @GameTest
    public void catalogExposesOnlyAdvStartersAndUsesDedicatedRuntimeIds(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("villager-adv-catalog-owner");
        SemionGame game = startedGame(context, owner);
        Set<String> starters = ProductionTowerService.availableTowers(game, owner).stream()
                .map(ProductionTowerCatalog.CatalogEntry::type)
                .map(TowerType::id)
                .collect(Collectors.toSet());
        BuilderIntegrationGameTestSupport.require(starters.equals(Set.of(
                VillagerTowers.ADV_T1_SPLASH_TOWER.id(),
                VillagerTowers.ADV_T1_GOLEM_TOWER.id(),
                VillagerTowers.ADV_T1_ALLAY_TOWER.id(),
                VillagerTowers.ADV_T1_CAT_TOWER.id()
        )), "Villager ADV should expose exactly four ADV starters.");
        BuilderIntegrationGameTestSupport.require(
                ProductionTowerCatalog.find(VillagerTowers.T1_SPLASH_TOWER.id()).isPresent()
                        && ProductionTowerCatalog.find(VillagerTowers.ADV_T1_SPLASH_TOWER.id()).isPresent(),
                "Base and ADV villager catalogs should remain separately registered."
        );
        context.succeed();
    }

    @GameTest(maxTicks = 240)
    public void placementAndUpgradeGateUseTowerExperienceAndTransferIt(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("villager-adv-experience-upgrade");
        SemionGame game = startedGame(context, owner);
        PlayerLane lane = lane(game, owner);
        BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
        BuilderIntegrationGameTestSupport.require(
                ProductionTowerService.placeTower(game, owner, position, VillagerTowers.ADV_T1_SPLASH_TOWER.id())
                        == TowerPlacementResult.SUCCESS,
                "Villager ADV ranged starter placement should succeed."
        );
        Tower tower = lane.towerAt(GridPosition.from(position));
        BuilderIntegrationGameTestSupport.require(
                ProductionTowerService.upgradeTower(game, owner, position, "villager_splash_t2")
                        == TowerUpgradeResult.NOT_ENOUGH_ADV_EXPERIENCE,
                "ADV upgrade should be blocked below its experience requirement."
        );
        tower.setData(VillagerAdvStates.EXPERIENCE, 15.0);
        BuilderIntegrationGameTestSupport.require(
                ProductionTowerService.upgradeTower(game, owner, position, "villager_splash_t2")
                        == TowerUpgradeResult.SUCCESS,
                "ADV upgrade should succeed at its experience requirement."
        );
        requireClose(15.0, VillagerAdvStates.experience(lane.towerAt(GridPosition.from(position))),
                "ADV experience should transfer through the shared upgrade pipeline.");
        context.succeed();
    }
}
