package kim.biryeong.semiontd.tower.resonance;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.game.TowerUpgradeResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import kim.biryeong.semiontd.tower.Tower;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

public final class ResonanceLifecycleAndUpgradeGameTest extends ResonanceGameTestSupport {
    @GameTest
    public void placementWaveSnapshotAndUpgradeTransferUseProductionPipeline(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("resonance-lifecycle-upgrade");
        SemionGame game = startedGame(context, owner);
        PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
        BlockPos focusPosition = BuilderIntegrationGameTestSupport.primaryPosition(lane);
        game.players().get(owner).economy().addMineral(1_000);
        Set<String> starters = ProductionTowerService.availableTowers(game, owner).stream()
                .map(entry -> entry.type().id())
                .collect(Collectors.toSet());
        BuilderIntegrationGameTestSupport.require(starters.equals(Set.of(
                ResonanceTowers.FOCUS_CRYSTAL.id(),
                ResonanceTowers.WAVE_CRYSTAL.id(),
                ResonanceTowers.FROST_CRYSTAL.id(),
                ResonanceTowers.AMPLIFY_CRYSTAL.id()
        )), "Resonance builder should expose exactly four Moobloom starters.");
        BuilderIntegrationGameTestSupport.require(
                ProductionTowerService.placeTower(game, owner, focusPosition, ResonanceTowers.FOCUS_CRYSTAL.id())
                        == TowerPlacementResult.SUCCESS,
                "Focus Moobloom placement should succeed."
        );
        addTower(lane, owner, ResonanceTowers.WAVE_CRYSTAL, focusPosition.offset(1, 0, 0));
        addTower(lane, owner, ResonanceTowers.FROST_CRYSTAL, focusPosition.offset(-1, 0, 0));
        addTower(lane, owner, ResonanceTowers.AMPLIFY_CRYSTAL, focusPosition.offset(0, 0, 1));
        addTower(lane, owner, ResonanceTowers.WAVE_PRISM, focusPosition.offset(0, 0, -1));
        addTower(lane, owner, ResonanceTowers.FROST_PRISM, focusPosition.offset(1, 0, 1));
        addTower(lane, owner, ResonanceTowers.FOCUS_CRYSTAL, focusPosition.offset(-1, 0, -1));
        lane.markWaveStarted(1);
        ResonanceTower focus = (ResonanceTower) lane.towerAt(GridPosition.from(focusPosition));
        BuilderIntegrationGameTestSupport.require(focus.resonanceLevel() == 1 && focus.resonanceLinks() == 5,
                "T1 focus should capture five links and cap at resonance level one.");
        BuilderIntegrationGameTestSupport.require(
                ProductionTowerService.upgradeTower(game, owner, focusPosition, ResonanceTowers.FOCUS_PRISM.id())
                        == TowerUpgradeResult.SUCCESS,
                "Focus should upgrade to tier two."
        );
        focus = (ResonanceTower) lane.towerAt(GridPosition.from(focusPosition));
        BuilderIntegrationGameTestSupport.require(focus.resonanceLevel() == 1 && focus.resonanceLinks() == 5,
                "First upgrade should retain the wave snapshot.");
        BuilderIntegrationGameTestSupport.require(
                ProductionTowerService.upgradeTower(game, owner, focusPosition, ResonanceTowers.FOCUS_CORE.id())
                        == TowerUpgradeResult.SUCCESS,
                "Focus should upgrade to tier three."
        );
        focus = (ResonanceTower) lane.towerAt(GridPosition.from(focusPosition));
        for (Tower tower : List.copyOf(lane.towers())) {
            if (tower instanceof ResonanceTower && tower != focus) {
                lane.killTower(tower);
            }
        }
        BuilderIntegrationGameTestSupport.require(focus.resonanceLinks() == 5,
                "Captured links should survive provider deaths during the wave.");
        lane.markWaveStarted(2);
        BuilderIntegrationGameTestSupport.require(focus.resonanceLinks() == 0,
                "Next wave should replace the previous snapshot.");
        context.succeed();
    }
}
