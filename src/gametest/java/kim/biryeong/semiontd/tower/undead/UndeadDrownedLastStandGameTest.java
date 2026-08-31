package kim.biryeong.semiontd.tower.undead;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.goal.SiegeTrueDamageGoal;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
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

public final class UndeadDrownedLastStandGameTest {
    @GameTest
    public void lastStandHandlesNormalAndFixedDamageAndResetsForRound(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("undead-drowned-last-stand");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, UndeadTowerJob.ID, "undead-drowned-last-stand"
            );
            game.players().get(owner).economy().addMineral(20_000);
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
            place(game, owner, position, UndeadTowers.T1_ZOMBIE_TOWER.id());
            upgrade(game, owner, position, UndeadTowers.T2_ZOMBIE_TOWER.id());
            upgrade(game, owner, position, UndeadTowers.T3_ZOMBIE_TOWER.id());
            UndeadDrownedTower drowned = (UndeadDrownedTower) lane.towerAt(GridPosition.from(position));
            SemionTowerEntity entity = BuilderIntegrationGameTestSupport.towerEntity(lane, drowned);
            drowned.syncHealth(10.0);
            entity.setHealth(10.0F);

            BuilderIntegrationGameTestSupport.requireClose(
                    9.0, drowned.modifyIncomingDamage(entity, null, 100.0),
                    "First lethal hit must leave the Drowned at one health."
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    0.0, drowned.modifyIncomingDamageIgnoringReductions(entity, null, 100.0),
                    "Fixed damage must also be blocked while Last Stand is active."
            );
            drowned.resetForRound(lane);
            drowned.syncHealth(10.0);
            entity.setHealth(10.0F);
            BuilderIntegrationGameTestSupport.requireClose(
                    9.0, drowned.modifyIncomingDamage(entity, null, 100.0),
                    "Round reset must make Last Stand available again."
            );

            drowned.resetForRound(lane);
            drowned.syncHealth(50.0);
            entity.setHealth(50.0F);
            SemionMonsterEntity warden = BuilderIntegrationGameTestSupport.spawnMonster(
                    context, lane, "undead-drowned-warden", lane.laneId(), 100.0,
                    entity.getX() + 1.0, entity.getY(), entity.getZ()
            );
            warden.setTarget(entity);
            new SiegeTrueDamageGoal(warden, 100.0, 60, 1, 0.0).tick();
            float lastStandHealth = entity.getHealth();
            BuilderIntegrationGameTestSupport.require(
                    lastStandHealth > 0.0F,
                    "The real siege fixed-damage goal must trigger Drowned Last Stand."
            );
            new SiegeTrueDamageGoal(warden, 100.0, 60, 1, 0.0).tick();
            BuilderIntegrationGameTestSupport.requireClose(
                    lastStandHealth, entity.getHealth(),
                    "The real siege fixed-damage goal must be ignored while Last Stand is active."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Undead Drowned integration failed: " + failure.getMessage()));
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

    private static void upgrade(SemionGame game, UUID owner, BlockPos position, String towerId) {
        BuilderIntegrationGameTestSupport.require(
                ProductionTowerService.upgradeTower(game, owner, position, towerId) == TowerUpgradeResult.SUCCESS,
                "Production upgrade failed for " + towerId + '.'
        );
    }
}
