package kim.biryeong.semiontd.tower.warlock;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.game.TowerUpgradeResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.WarlockTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public final class WarlockSacrificeUpgradeGameTest {
    @GameTest
    public void sacrificeProgressTransfersToBranchUpgradeAndRoundStateResets(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("warlock-sacrifice-upgrade");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, WarlockTowerJob.ID, "warlock-sacrifice-upgrade"
            );
            game.players().get(owner).economy().addMineral(20_000);
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            List<BlockPos> positions = BuilderIntegrationGameTestSupport.emptyPositions(
                    lane, BuilderIntegrationGameTestSupport.primaryPosition(lane), 2
            );
            place(game, owner, positions.get(0), WarlockTowers.BASE_WARLOCK_TOWER.id());
            place(game, owner, positions.get(1), WarlockTowers.T1_SLAVE.id());

            WarlockTower core = (WarlockTower) lane.towerAt(GridPosition.from(positions.get(0)));
            WarlockSacrificeTower sacrifice = (WarlockSacrificeTower) lane.towerAt(
                    GridPosition.from(positions.get(1))
            );
            int sacrificeEntityId = sacrifice.entityId().orElseThrow();
            SemionTowerEntity coreEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, core);
            core.syncHealth(10.0);
            coreEntity.setHealth(10.0F);
            core.onDamaged(coreEntity, null, 60.0, 60.0, 0.0);

            BuilderIntegrationGameTestSupport.requireClose(0.0, sacrifice.health(),
                    "A lethal core hit must consume the eligible nearby sacrifice tower.");
            String beforeUpgrade = plainDetails(core);
            BuilderIntegrationGameTestSupport.require(
                    beforeUpgrade.contains("영구 흡수: 1기") && beforeUpgrade.contains("라운드 흡수: 1기"),
                    "Core details must expose both permanent and current-round sacrifice progress."
            );

            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.upgradeTower(
                            game, owner, positions.get(0), WarlockTowers.MELEE_WARLOCK_TOWER.id()
                    ) == TowerUpgradeResult.SUCCESS,
                    "Warlock core must upgrade through the production graph."
            );
            WarlockTower upgraded = (WarlockTower) lane.towerAt(GridPosition.from(positions.get(0)));
            String afterUpgrade = plainDetails(upgraded);
            BuilderIntegrationGameTestSupport.require(
                    afterUpgrade.contains("영구 흡수: 1기") && afterUpgrade.contains("라운드 흡수: 1기"),
                    "Core replacement must transfer both permanent and active round progression."
            );

            game.teams().get(lane.teamId()).resetForRound();
            String afterReset = plainDetails(upgraded);
            BuilderIntegrationGameTestSupport.require(
                    afterReset.contains("영구 흡수: 1기") && afterReset.contains("라운드 흡수: 0기"),
                    "Round reset must preserve permanent progress and clear only round progress."
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    sacrifice.currentMaxHealth(), sacrifice.health(),
                    "The sacrificed tower must respawn at full health for the next round."
            );
            BuilderIntegrationGameTestSupport.require(
                    sacrifice.entityId().isPresent() && sacrifice.entityId().getAsInt() != sacrificeEntityId,
                    "Round respawn must create a fresh Minecraft entity for the sacrifice tower."
            );
            SemionGame scheduledGame = game;
            game = null;
            context.runAfterDelay(3, () -> {
                scheduledGame.close();
                TowerBalanceRuntime.apply(defaults);
                context.succeed();
            });
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Warlock sacrifice integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }

    private static void place(SemionGame game, UUID owner, BlockPos position, String towerId) {
        BuilderIntegrationGameTestSupport.require(
                ProductionTowerService.placeTower(game, owner, position, towerId) == TowerPlacementResult.SUCCESS,
                "Production placement failed for " + towerId + '.'
        );
    }

    private static String plainDetails(WarlockTower tower) {
        return String.join("\n", tower.runtimeDetailLines()).replaceAll("<[^>]+>", "");
    }
}
