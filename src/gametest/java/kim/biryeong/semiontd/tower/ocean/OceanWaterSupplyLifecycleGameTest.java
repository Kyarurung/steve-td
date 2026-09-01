package kim.biryeong.semiontd.tower.ocean;

import java.util.ArrayList;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.OceanTowerJob;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class OceanWaterSupplyLifecycleGameTest {
    @GameTest
    public void waveSnapshotRecapturesNextRoundAndStopsAtFinalDefense(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("ocean-supply-lifecycle-owner");
        SemionGame game = BuilderIntegrationGameTestSupport.startedGame(context, owner, OceanTowerJob.ID, "ocean-supply");
        try {
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            GridPosition origin = GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane));
            OceanWaterTower source = new OceanWaterTower(
                    TowerBalanceRuntime.resolve(OceanTowers.T1_WATER), owner, TeamId.RED, 1, origin
            );
            OceanTower captured = new OceanTower(
                    TowerBalanceRuntime.resolve(OceanTowers.T1_COD), owner, TeamId.RED, 1,
                    new GridPosition(origin.x() + 1, origin.y(), origin.z())
            );
            lane.addTower(source);
            lane.addTower(captured);
            source.onWaveStarted(lane, 1);
            BuilderIntegrationGameTestSupport.requireClose(107.0, captured.water(),
                    "Wave start must supply the captured nearby tower.");

            captured.syncPosition(new GridPosition(origin.x() + 20, origin.y(), origin.z()));
            OceanTower late = new OceanTower(
                    TowerBalanceRuntime.resolve(OceanTowers.T1_SALMON), owner, TeamId.RED, 1,
                    new GridPosition(origin.x() + 1, origin.y(), origin.z())
            );
            lane.addTower(late);
            source.tick(lane);
            BuilderIntegrationGameTestSupport.requireClose(108.5, captured.water(),
                    "A captured tower must continue receiving water after moving.");
            BuilderIntegrationGameTestSupport.requireClose(100.0, late.water(),
                    "A tower entering range mid-wave must not enter the supply snapshot.");

            source.resetForRound(lane);
            source.onWaveStarted(lane, 2);
            BuilderIntegrationGameTestSupport.requireClose(107.0, late.water(),
                    "The next round must recapture nearby supply targets.");
            source.moveToFinalDefense(lane, lane.nextFinalDefenseTowerPosition(source));
            double before = late.water();
            source.tick(lane);
            BuilderIntegrationGameTestSupport.requireClose(before, late.water(),
                    "Water supply must stop at final defense.");
            context.succeed();
        } finally {
            game.close();
        }
    }

    @GameTest
    public void sixSourcesUseDiminishingStackingAndRecoverAfterRemoval(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("ocean-supply-stacking-owner");
        SemionGame game = BuilderIntegrationGameTestSupport.startedGame(context, owner, OceanTowerJob.ID, "ocean-stacking");
        try {
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            GridPosition origin = GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane));
            OceanTower target = new OceanTower(
                    TowerBalanceRuntime.resolve(OceanTowers.T1_COD), owner, TeamId.RED, 1, origin
            );
            lane.addTower(target);
            int[][] offsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-2, 0}, {2, 0}};
            ArrayList<OceanWaterTower> sources = new ArrayList<>();
            for (int[] offset : offsets) {
                OceanWaterTower source = new OceanWaterTower(
                        TowerBalanceRuntime.resolve(OceanTowers.T1_WATER), owner, TeamId.RED, 1,
                        new GridPosition(origin.x() + offset[0], origin.y(), origin.z() + offset[1])
                );
                sources.add(source);
                lane.addTower(source);
            }
            sources.forEach(source -> source.onWaveStarted(lane, 1));
            double expected = 100.0 + 7.0 * 2.38336;
            BuilderIntegrationGameTestSupport.requireClose(expected, target.water(),
                    "Six ocean sources must use the configured geometric stacking decay.");

            for (int index = 1; index < sources.size(); index++) {
                lane.removeTower(sources.get(index));
            }
            double before = target.water();
            sources.getFirst().onWaveStarted(lane, 2);
            BuilderIntegrationGameTestSupport.requireClose(before + 7.0, target.water(),
                    "Removing five sources must restore the remaining source to full weight below the soft cap.");
            context.succeed();
        } finally {
            game.close();
        }
    }
}
