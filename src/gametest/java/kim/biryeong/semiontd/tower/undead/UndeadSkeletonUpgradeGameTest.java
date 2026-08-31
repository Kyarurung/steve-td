package kim.biryeong.semiontd.tower.undead;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.game.TowerUpgradeResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.UndeadTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public final class UndeadSkeletonUpgradeGameTest {
    @GameTest
    public void branchesTransferDeathStateThroughProductionUpgrade(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("undead-skeleton-production-upgrade");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, UndeadTowerJob.ID, "undead-skeleton-integration"
            );
            game.players().get(owner).economy().addMineral(10_000);
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            List<BlockPos> positions = BuilderIntegrationGameTestSupport.emptyPositions(
                    lane, BuilderIntegrationGameTestSupport.primaryPosition(lane), 2
            );
            for (BlockPos position : positions) {
                BuilderIntegrationGameTestSupport.require(
                        ProductionTowerService.placeTower(game, owner, position, UndeadTowers.T1_SKELETON_TOWER.id())
                                == TowerPlacementResult.SUCCESS,
                        "Both skeleton starters must place through the production service."
                );
            }
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.upgradeTower(
                            game, owner, positions.get(0), UndeadTowers.T2_RANGED_SKELETON_TOWER.id()
                    ) == TowerUpgradeResult.SUCCESS,
                    "Skeleton must enter the ranged branch."
            );
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.upgradeTower(
                            game, owner, positions.get(1), UndeadTowers.T2_MELEE_TOWER.id()
                    ) == TowerUpgradeResult.SUCCESS,
                    "Skeleton must enter the melee branch."
            );
            UndeadRangedSkeletonTower ranged = (UndeadRangedSkeletonTower) lane.towerAt(
                    GridPosition.from(positions.get(0))
            );
            UndeadMeleeSkeletonTower melee = (UndeadMeleeSkeletonTower) lane.towerAt(
                    GridPosition.from(positions.get(1))
            );
            for (int death = 0; death < 3; death++) {
                ranged.onNearbyMonsterDeath(lane, null, center(ranged.position()));
                melee.onNearbyMonsterDeath(lane, null, center(melee.position()));
            }
            double rangedStoredDamage = ranged.modifyAttackDamage(null, null, 0.0);
            BuilderIntegrationGameTestSupport.require(
                    String.join("\n", melee.runtimeDetailLines()).contains("사망 스택 3/"),
                    "Melee skeleton must own three death stacks before upgrade."
            );

            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.upgradeTower(
                            game, owner, positions.get(0), UndeadTowers.T3_RANGED_SKELETON_TOWER.id()
                    ) == TowerUpgradeResult.SUCCESS,
                    "Ranged skeleton must upgrade to stray."
            );
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.upgradeTower(
                            game, owner, positions.get(1), UndeadTowers.T3_MELEE_TOWER.id()
                    ) == TowerUpgradeResult.SUCCESS,
                    "Melee skeleton must upgrade to reinforced wither skeleton."
            );
            UndeadRangedSkeletonTower stray = (UndeadRangedSkeletonTower) lane.towerAt(
                    GridPosition.from(positions.get(0))
            );
            UndeadMeleeSkeletonTower reinforced = (UndeadMeleeSkeletonTower) lane.towerAt(
                    GridPosition.from(positions.get(1))
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    rangedStoredDamage,
                    stray.modifyAttackDamage(null, null, 0.0),
                    "Ranged upgrade must retain its accumulated flat death-stack damage."
            );
            BuilderIntegrationGameTestSupport.require(
                    String.join("\n", reinforced.runtimeDetailLines()).contains("사망 스택 3/"),
                    "Melee upgrade must retain its death-stack count."
            );
            double expectedMaximumHealth = reinforced.type().maxHealth()
                    + 3 * TowerBalanceRuntime.ability(reinforced.type().id(), "healthPerStack");
            BuilderIntegrationGameTestSupport.requireClose(
                    expectedMaximumHealth,
                    reinforced.currentMaxHealth(),
                    "Retained melee stacks must be recalculated with the upgraded tower's health value."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Undead skeleton integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }

    private static Vec3 center(GridPosition position) {
        return new Vec3(position.x() + 0.5, position.y() + 1.0, position.z() + 0.5);
    }
}
