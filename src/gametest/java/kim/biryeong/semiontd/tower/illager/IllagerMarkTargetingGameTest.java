package kim.biryeong.semiontd.tower.illager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.tower.goal.TowerAttackMonsterGoal;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.IllagerTowerJob;
import kim.biryeong.semiontd.summon.SummonRole;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public final class IllagerMarkTargetingGameTest {
    @GameTest(maxTicks = 100)
    public void activeMarkReplacesCachedEntityTarget(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("illager-mark-targeting");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, IllagerTowerJob.ID, "illager-mark-targeting"
            );
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            BlockPos blockPosition = BuilderIntegrationGameTestSupport.primaryPosition(lane);
            GridPosition towerPosition = GridPosition.from(blockPosition);
            IllagerTower tower = new IllagerTower(
                    TowerBalanceRuntime.resolve(IllagerTowers.T1_VINDICATOR),
                    owner,
                    lane.teamId(),
                    lane.laneId(),
                    towerPosition,
                    towerPosition
            );
            lane.addTower(tower);
            var towerEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, tower);
            var cachedTarget = BuilderIntegrationGameTestSupport.spawnMonster(
                    context, lane, "illager-cached-target", lane.laneId(), 100.0,
                    towerEntity.getX() + 1.0, towerEntity.getY(), towerEntity.getZ()
            );
            TowerAttackMonsterGoal targetingGoal = new TowerAttackMonsterGoal(towerEntity);
            targetingGoal.tick();
            BuilderIntegrationGameTestSupport.require(
                    towerEntity.currentAttackTarget() == cachedTarget,
                    "Illager tower must cache its first valid entity target."
            );
            var markedTarget = BuilderIntegrationGameTestSupport.spawnMonster(
                    context, lane, "illager-marked-target", lane.laneId(), 100.0,
                    towerEntity.getX() + 1.5, towerEntity.getY(), towerEntity.getZ()
            );
            IllagerMarkDomain.apply(markedTarget.runtimeMonster(), owner, 0.2, 100, towerPosition, 2.0);

            targetingGoal.tick();

            BuilderIntegrationGameTestSupport.require(
                    towerEntity.currentAttackTarget() == markedTarget,
                    "An active Illager mark must replace the cached entity target."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Illager mark targeting integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            IllagerRaidStates.clear(owner);
            TowerBalanceRuntime.apply(defaults);
        }
    }

    @GameTest(maxTicks = 100)
    public void highHealthWitchTargetsMaximumHealthInsteadOfCurrentHealth(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            Vec3 origin = Vec3.atCenterOf(context.absolutePos(BlockPos.ZERO));
            var lowerMaximum = BuilderIntegrationGameTestSupport.spawnRoleMonster(
                    context, "illager-lower-maximum", Optional.empty(), kim.biryeong.semiontd.game.TeamId.RED, 1,
                    100.0, 0.0, 0.0, List.of(SummonRole.RUSH), origin.x, origin.y, origin.z
            );
            var higherMaximum = BuilderIntegrationGameTestSupport.spawnRoleMonster(
                    context, "illager-higher-maximum", Optional.empty(), kim.biryeong.semiontd.game.TeamId.RED, 1,
                    200.0, 0.0, 0.0, List.of(SummonRole.TANK), origin.x + 1.0, origin.y, origin.z
            );
            lowerMaximum.setHealth(80.0F);
            higherMaximum.setHealth(20.0F);
            var tower = ProductionTowerCatalog.find(IllagerTowers.T2_WITCH_HIGH.id()).orElseThrow().create(
                    BuilderIntegrationGameTestSupport.stableUuid("illager-high-health-witch"),
                    kim.biryeong.semiontd.game.TeamId.RED,
                    1,
                    GridPosition.from(context.absolutePos(BlockPos.ZERO))
            );
            BuilderIntegrationGameTestSupport.require(
                    tower.selectAttackTarget(null, List.of(lowerMaximum, higherMaximum)).orElse(null) == higherMaximum,
                    "High-health Witch must prioritize maximum health rather than current health."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Illager maximum-health targeting failed: " + failure.getMessage()));
        } finally {
            TowerBalanceRuntime.apply(defaults);
        }
    }
}
