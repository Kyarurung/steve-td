package kim.biryeong.semiontd.tower.nether;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.game.TowerUpgradeResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.NetherTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

public final class NetherLifecycleAndUpgradeGameTest {
    @GameTest(maxTicks = 100)
    public void decayTransitionUpgradeTransferAndRoundResetUseProductionLifecycle(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("nether-lifecycle-upgrade");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, NetherTowerJob.ID, "nether-lifecycle-upgrade"
            );
            game.players().get(owner).economy().addMineral(20_000);
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, NetherTowers.T1_MAGMA_CUBE.id())
                            == TowerPlacementResult.SUCCESS,
                    "Nether starter placement must use the production catalog."
            );
            NetherTower tower = (NetherTower) lane.towerAt(GridPosition.from(position));
            var entity = BuilderIntegrationGameTestSupport.towerEntity(lane, tower);
            BuilderIntegrationGameTestSupport.spawnMonster(
                    context,
                    lane,
                    "nether-decay-target",
                    lane.laneId(),
                    1_000.0,
                    entity.getX() + 1.0,
                    entity.getY(),
                    entity.getZ()
            );
            tower.syncHealth(0.01);
            entity.setHealth(0.01F);

            tower.tick(lane);

            BuilderIntegrationGameTestSupport.require(
                    tower.state() == NetherTowerState.ZOMBIE,
                    "Lethal Nether decay must transition the tower to zombie state."
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    tower.currentMaxHealth(),
                    tower.health(),
                    "Zombie transition must restore the configured full health ratio."
            );
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.upgradeTower(game, owner, position, NetherTowers.T2_BLAZE.id())
                            == TowerUpgradeResult.SUCCESS,
                    "Nether upgrade must use the production graph."
            );
            NetherTower upgraded = (NetherTower) lane.towerAt(GridPosition.from(position));
            BuilderIntegrationGameTestSupport.require(
                    upgraded.state() == NetherTowerState.ZOMBIE,
                    "Nether upgrade must preserve the zombie runtime state."
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    1.0,
                    upgraded.health() / upgraded.currentMaxHealth(),
                    "Nether upgrade must preserve the runtime health ratio."
            );

            game.teams().get(lane.teamId()).resetForRound();

            BuilderIntegrationGameTestSupport.require(
                    upgraded.state() == NetherTowerState.NETHER,
                    "Round reset must restore the Nether state."
            );
            BuilderIntegrationGameTestSupport.require(
                    BuilderIntegrationGameTestSupport.towerEntity(lane, upgraded).isAlive(),
                    "Round reset must leave one live upgraded tower entity."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Nether lifecycle integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }

    @GameTest(maxTicks = 100)
    public void discardedTowerRespawnsWithConfiguredProxyAndSingleEntity(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("nether-proxy-respawn");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, NetherTowerJob.ID, "nether-proxy-respawn"
            );
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            NetherTower tower = new NetherTower(
                    TowerBalanceRuntime.resolve(NetherTowers.T1_STRIDER),
                    owner,
                    lane.teamId(),
                    lane.laneId(),
                    GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane))
            );
            lane.addTower(tower);
            int originalEntityId = tower.entityId().orElseThrow();
            var originalEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, tower);
            BuilderIntegrationGameTestSupport.require(
                    originalEntity.getPolymerEntityType(null) == EntityType.STRIDER,
                    "Initial Nether tower proxy must be a strider."
            );
            originalEntity.discard();

            game.teams().get(lane.teamId()).resetForRound();

            int respawnedEntityId = tower.entityId().orElseThrow();
            var respawnedEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, tower);
            BuilderIntegrationGameTestSupport.require(
                    respawnedEntityId != originalEntityId,
                    "Destroyed Nether tower must use a fresh entity id after reset."
            );
            BuilderIntegrationGameTestSupport.require(
                    respawnedEntity.getPolymerEntityType(null) == EntityType.STRIDER,
                    "Respawned Nether tower proxy must remain a strider."
            );
            long visibleEntities = lane.arenaWorld().getEntitiesOfClass(
                    kim.biryeong.semiontd.entity.tower.SemionTowerEntity.class,
                    respawnedEntity.getBoundingBox().inflate(64.0),
                    entity -> !entity.isRemoved() && entity.runtimeTower() == tower
            ).size();
            BuilderIntegrationGameTestSupport.require(
                    visibleEntities == 1L,
                    "Round reset must leave one live Nether tower entity."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Nether proxy respawn integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }
}
