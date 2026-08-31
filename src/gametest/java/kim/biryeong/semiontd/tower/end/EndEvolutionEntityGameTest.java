package kim.biryeong.semiontd.tower.end;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.EndTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

public final class EndEvolutionEntityGameTest {
    @GameTest
    public void eggPhantomAndDragonReuseOneRuntimeEntityAcrossRoundLifecycle(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("end-evolution-runtime-entity");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, EndTowerJob.ID, "end-evolution-runtime-entity"
            );
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, EndTowers.BASE_END_TOWER.id())
                            == TowerPlacementResult.SUCCESS,
                    "End core must place through the production service."
            );
            EndTower core = (EndTower) lane.towerAt(GridPosition.from(position));
            SemionTowerEntity entity = BuilderIntegrationGameTestSupport.towerEntity(lane, core);
            int entityId = entity.getId();
            BuilderIntegrationGameTestSupport.require(core.state() == EndTowerState.EGG,
                    "Placed End core must begin in EGG state.");
            BuilderIntegrationGameTestSupport.require(entity.getPolymerEntityType(null) == EntityType.ARMOR_STAND,
                    "EGG state must expose the production armor-stand proxy.");

            core.onWaveStarted(lane, 1);
            core.tick(lane);
            entity.syncTowerState(core);
            BuilderIntegrationGameTestSupport.require(core.state() == EndTowerState.PHANTOM,
                    "Wave start must hatch the core into PHANTOM state.");
            BuilderIntegrationGameTestSupport.require(entity.getId() == entityId,
                    "Hatching must retain the same runtime entity identity.");
            BuilderIntegrationGameTestSupport.require(entity.getPolymerEntityType(null) == EntityType.PHANTOM,
                    "PHANTOM state must update the live entity proxy.");

            core.syncMaxHealth(2_000.0, true);
            core.tick(lane);
            entity.syncTowerState(core);
            BuilderIntegrationGameTestSupport.require(core.state() == EndTowerState.DRAGON,
                    "Configured evolution threshold must promote the live core to DRAGON.");
            BuilderIntegrationGameTestSupport.require(entity.getId() == entityId,
                    "Evolution must not replace the runtime tower entity.");
            BuilderIntegrationGameTestSupport.require(entity.getPolymerEntityType(null) == EntityType.ENDER_DRAGON,
                    "DRAGON state must update the live entity proxy.");
            BuilderIntegrationGameTestSupport.require(entity.hasEndCoreInteractionHitbox(),
                    "DRAGON state must expose its production interaction hitbox.");

            game.teams().get(lane.teamId()).resetForRound();
            entity.syncTowerState(core);
            BuilderIntegrationGameTestSupport.require(core.state() == EndTowerState.EGG,
                    "Round reset must return the same core to EGG state.");
            BuilderIntegrationGameTestSupport.require(entity.getId() == entityId,
                    "Round reset must retain the runtime entity identity.");
            BuilderIntegrationGameTestSupport.require(entity.getPolymerEntityType(null) == EntityType.ARMOR_STAND,
                    "Reset EGG state must restore its production proxy.");
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("End evolution entity integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }
}
