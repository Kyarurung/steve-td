package kim.biryeong.semiontd.tower.ocean;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.game.TowerSellResult;
import kim.biryeong.semiontd.game.TowerUpgradeResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.OceanTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import kim.biryeong.semiontd.tower.TowerType;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

public final class OceanCatalogAndPlacementGameTest {
    @GameTest
    public void catalogPlacementAndUpgradeUseProductionPipelines(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("ocean-catalog-upgrade-owner");
        SemionGame game = BuilderIntegrationGameTestSupport.startedGame(context, owner, OceanTowerJob.ID, "ocean-catalog");
        try {
            game.players().get(owner).economy().addMineral(20_000);
            Set<String> starters = ProductionTowerService.availableTowers(game, owner).stream()
                    .map(ProductionTowerCatalog.CatalogEntry::type)
                    .map(TowerType::id)
                    .collect(Collectors.toSet());
            BuilderIntegrationGameTestSupport.require(starters.equals(Set.of(
                    OceanTowers.T1_WATER.id(), OceanTowers.T1_PUFFERFISH.id(),
                    OceanTowers.T1_TROPICAL_FISH.id(), OceanTowers.T1_SQUID.id(),
                    OceanTowers.T1_SALMON.id(), OceanTowers.T1_COD.id()
            )), "Ocean catalog must expose exactly six starter families.");

            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, OceanTowers.T1_COD.id())
                            == TowerPlacementResult.SUCCESS,
                    "Ocean combat placement must use the production catalog."
            );
            OceanTower tower = (OceanTower) lane.towerAt(GridPosition.from(position));
            tower.addWater(321.5);
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.upgradeTower(game, owner, position, OceanTowers.T2_LARGE_COD.id())
                            == TowerUpgradeResult.SUCCESS,
                    "Ocean combat upgrade must use the production upgrade edge."
            );
            OceanTower upgraded = (OceanTower) lane.towerAt(GridPosition.from(position));
            BuilderIntegrationGameTestSupport.requireClose(
                    421.5,
                    upgraded.water(),
                    "Ocean water state must transfer through production upgrade."
            );
            context.succeed();
        } finally {
            game.close();
        }
    }

    @GameTest
    public void waterMarkerRejectsBlockedPlacementWithoutChargingAndRestoresOnSell(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("ocean-water-placement-owner");
        SemionGame game = BuilderIntegrationGameTestSupport.startedGame(context, owner, OceanTowerJob.ID, "ocean-water");
        try {
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
            BlockPos waterPosition = position.above();
            lane.arenaWorld().setBlock(waterPosition, Blocks.STONE.defaultBlockState(), 3);
            long before = game.players().get(owner).economy().diamond();
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, OceanTowers.T1_WATER.id())
                            == TowerPlacementResult.OCCUPIED,
                    "Ocean water placement must reject an occupied marker cell."
            );
            BuilderIntegrationGameTestSupport.require(
                    game.players().get(owner).economy().diamond() == before && lane.towers().isEmpty(),
                    "Rejected ocean placement must not charge or create a tower."
            );

            lane.arenaWorld().setBlock(waterPosition, Blocks.AIR.defaultBlockState(), 3);
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, OceanTowers.T1_WATER.id())
                            == TowerPlacementResult.SUCCESS,
                    "Ocean water placement must succeed after the marker cell is cleared."
            );
            OceanWaterTower tower = (OceanWaterTower) lane.towerAt(GridPosition.from(position));
            var entity = BuilderIntegrationGameTestSupport.towerEntity(lane, tower);
            BuilderIntegrationGameTestSupport.require(entity.isNoAi() && entity.canBreatheUnderwater(),
                    "Ocean water entity must remain fixed and breathe in its marker.");
            BuilderIntegrationGameTestSupport.require(
                    lane.arenaWorld().getBlockState(waterPosition).equals(OceanWaterTower.waterMarker())
                            && lane.arenaWorld().getFluidState(waterPosition).isSource(),
                    "Ocean water placement must create the configured source-water marker."
            );
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.sellTower(game, owner, tower.position()).result() == TowerSellResult.SUCCESS,
                    "Ocean water tower must remain sellable through the shared pipeline."
            );
            BuilderIntegrationGameTestSupport.require(lane.arenaWorld().getBlockState(waterPosition).isAir(),
                    "Selling an ocean water tower must restore the replaced block.");
            context.succeed();
        } finally {
            game.close();
        }
    }
}
