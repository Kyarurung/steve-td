package kim.biryeong.semiontd.tower.animal;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.game.TowerUpgradeResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.AnimalTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public final class AnimalFoxCombatAndUpgradeGameTest {
    @GameTest
    public void foxSelectsExecutableTargetAndTransfersNearbyDeathDamageOnUpgrade(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("animal-fox-combat-upgrade");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, AnimalTowerJob.ID, "animal-fox-combat-upgrade"
            );
            game.players().get(owner).economy().addMineral(20_000);
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, AnimalTowers.T1_FOX_TOWER.id())
                            == TowerPlacementResult.SUCCESS,
                    "Fox must place through the production service."
            );
            AnimalFoxTower fox = (AnimalFoxTower) lane.towerAt(GridPosition.from(position));
            SemionTowerEntity towerEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, fox);
            SemionMonsterEntity healthy = BuilderIntegrationGameTestSupport.spawnMonster(
                    context, lane, "animal-fox-healthy", lane.laneId(), 1_000.0,
                    towerEntity.getX() + 1.0, towerEntity.getY(), towerEntity.getZ()
            );
            SemionMonsterEntity executable = BuilderIntegrationGameTestSupport.spawnMonster(
                    context, lane, "animal-fox-executable", lane.laneId(), 1_000.0,
                    towerEntity.getX() + 2.0, towerEntity.getY(), towerEntity.getZ()
            );
            executable.runtimeMonster().damage(900.0);
            executable.setHealth((float) executable.runtimeMonster().health());

            BuilderIntegrationGameTestSupport.require(
                    fox.selectAttackTarget(towerEntity, List.of(healthy, executable)).orElseThrow() == executable,
                    "Fox targeting must prefer the executable live monster over a healthier nearer target."
            );
            double baseDamage = fox.modifyAttackDamage(towerEntity, executable, fox.type().damage());
            executable.runtimeMonster().syncHealth(0.0);
            executable.setHealth(0.0F);
            lane.tick(context.getLevel().getServer());
            double stackedDamage = fox.modifyAttackDamage(towerEntity, executable, fox.type().damage());
            BuilderIntegrationGameTestSupport.require(stackedDamage > baseDamage,
                    "A nearby death processed by the lane must increase Fox damage.");

            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.upgradeTower(game, owner, position, AnimalTowers.T2_FOX_TOWER.id())
                            == TowerUpgradeResult.SUCCESS,
                    "Fox must upgrade through the production graph."
            );
            AnimalFoxTower upgraded = (AnimalFoxTower) lane.towerAt(GridPosition.from(position));
            BuilderIntegrationGameTestSupport.require(
                    String.join("\n", upgraded.runtimeDetailLines()).contains("사망 보너스 1/"),
                    "Fox upgrade must retain its tower-scoped nearby-death stack."
            );
            BuilderIntegrationGameTestSupport.require(
                    upgraded.modifyAttackDamage(null, null, upgraded.type().damage()) > upgraded.type().damage(),
                    "Transferred Fox state must affect the upgraded runtime tower."
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
            context.fail(Component.literal("Animal Fox integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }

    @GameTest(maxTicks = 80)
    public void foxEntityGoalTargetsAndDamagesExecutableMonster(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("animal-fox-entity-goal");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, AnimalTowerJob.ID, "animal-fox-entity-goal"
            );
            game.players().get(owner).economy().addMineral(20_000);
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, AnimalTowers.T1_FOX_TOWER.id())
                            == TowerPlacementResult.SUCCESS,
                    "Fox must place through the production service."
            );
            AnimalFoxTower fox = (AnimalFoxTower) lane.towerAt(GridPosition.from(position));
            SemionTowerEntity towerEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, fox);
            SemionMonsterEntity healthy = BuilderIntegrationGameTestSupport.spawnMonster(
                    context, lane, "animal-fox-goal-healthy", lane.laneId(), 1_000.0,
                    towerEntity.getX() + 1.0, towerEntity.getY(), towerEntity.getZ()
            );
            SemionMonsterEntity executable = BuilderIntegrationGameTestSupport.spawnMonster(
                    context, lane, "animal-fox-goal-executable", lane.laneId(), 1_000.0,
                    towerEntity.getX() + 2.0, towerEntity.getY(), towerEntity.getZ()
            );
            executable.runtimeMonster().damage(900.0);
            executable.setHealth(100.0F);

            BuilderIntegrationGameTestSupport.require(
                    towerEntity.selectAttackTarget(List.of(healthy, executable)) == executable,
                    "The entity hook must expose the Fox executable-target policy."
            );

            SemionGame scheduledGame = game;
            game = null;
            context.runAfterDelay(18, () -> {
                try {
                    BuilderIntegrationGameTestSupport.require(
                            towerEntity.currentAttackTarget() == executable,
                            "The live attack goal must retain the executable target."
                    );
                    BuilderIntegrationGameTestSupport.require(
                            executable.getHealth() < 100.0F,
                            "The live attack goal must damage the executable target."
                    );
                    BuilderIntegrationGameTestSupport.require(
                            executable.getHealth() < healthy.getHealth(),
                            "The executable target must take damage before the healthier nearer target."
                    );
                    context.succeed();
                } catch (RuntimeException | Error failure) {
                    failure.printStackTrace();
                    context.fail(Component.literal("Animal Fox entity goal failed: " + failure.getMessage()));
                } finally {
                    scheduledGame.close();
                    TowerBalanceRuntime.apply(defaults);
                }
            });
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Animal Fox entity goal setup failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }
}
