package kim.biryeong.semiontd.tower.ancientcity;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.game.TowerUpgradeResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.AncientCityTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class AncientCityTerritoryLifecycleGameTest {
    @GameTest
    public void territorySeedsGrowsCapsDeathsAndSurvivesProductionUpgrade(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("ancient-territory-upgrade-owner");
        SemionGame game = BuilderIntegrationGameTestSupport.startedGame(
                context, owner, AncientCityTowerJob.ID, "ancient-territory"
        );
        try {
            game.players().get(owner).economy().addMineral(20_000);
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, AncientCityTowers.CATALYST_T1.id())
                            == TowerPlacementResult.SUCCESS,
                    "Ancient-city catalyst placement must use the production catalog."
            );
            AncientCityTower catalyst = (AncientCityTower) lane.towerAt(GridPosition.from(position));
            BuilderIntegrationGameTestSupport.require(
                    AncientCityTerritoryStates.territoryCount(owner) == 9,
                    "The first ancient-city tower must seed nine real sculk cells."
            );
            BuilderIntegrationGameTestSupport.require(
                    AncientCityTerritoryStates.territoryPositions(owner).stream()
                            .allMatch(block -> lane.arenaWorld().getBlockState(block).is(Blocks.SCULK)),
                    "Every recorded ancient-city territory cell must exist in the world."
            );

            lane.markWaveStarted(1);
            BuilderIntegrationGameTestSupport.require(
                    AncientCityTerritoryStates.territoryCount(owner) == 13,
                    "Ancient-city wave start must grow four territory cells once."
            );
            Vec3 deathPosition = lane.laneLayout().positionAt(0.75);
            for (int death = 0; death < 8; death++) {
                AncientCityTerritoryController.recordAttributedDeath(owner, lane, 1, deathPosition);
            }
            BuilderIntegrationGameTestSupport.require(
                    AncientCityTerritoryStates.territoryCount(owner) == 19,
                    "Ancient-city attributed deaths must stop at six successful spreads per round."
            );

            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.upgradeTower(game, owner, position, AncientCityTowers.CATALYST_T2.id())
                            == TowerUpgradeResult.SUCCESS,
                    "Ancient-city upgrade must use the production upgrade edge."
            );
            AncientCityTower upgraded = (AncientCityTower) lane.towerAt(GridPosition.from(position));
            BuilderIntegrationGameTestSupport.require(
                    AncientCityTerritoryStates.territoryCount(owner) == 19
                            && AncientCityTerritoryController.resonanceActive(upgraded),
                    "Ancient-city player territory and resonance must survive tower upgrade."
            );
            context.succeed();
        } finally {
            game.close();
        }
    }

    @GameTest
    public void finalDefenseReseedsSculkAndMatchClosePreventsStateLeak(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("ancient-final-defense-state-owner");
        SemionGame game = BuilderIntegrationGameTestSupport.startedGame(
                context, owner, AncientCityTowerJob.ID, "ancient-final-defense"
        );
        PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
        AncientCityTower tower = new AncientCityTower(
                TowerBalanceRuntime.resolve(AncientCityTowers.CATALYST_T1), owner, TeamId.RED, 1,
                GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane))
        );
        lane.addTower(tower);
        tower.moveToFinalDefense(lane, lane.nextFinalDefenseTowerPosition(tower));
        BuilderIntegrationGameTestSupport.require(AncientCityTerritoryController.resonanceActive(tower),
                "Ancient-city final defense deployment must seed sculk under the moved tower.");
        game.close();
        BuilderIntegrationGameTestSupport.require(
                AncientCityTerritoryStates.territoryCount(owner) == 0,
                "Ancient-city match close must clear player territory before a second match."
        );
        AncientCityTerritoryController.onMatchStarted(owner);
        BuilderIntegrationGameTestSupport.require(
                AncientCityTerritoryStates.territoryCount(owner) == 0,
                "Ancient-city second match start must not inherit territory."
        );
        context.succeed();
    }
}
