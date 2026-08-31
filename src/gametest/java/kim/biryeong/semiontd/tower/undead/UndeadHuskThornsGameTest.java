package kim.biryeong.semiontd.tower.undead;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.UndeadTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public final class UndeadHuskThornsGameTest {
    @GameTest
    public void thornsHealPerHitRespectLaneAndCooldown(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("undead-husk-thorns-runtime");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, UndeadTowerJob.ID, "undead-husk-integration"
            );
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            UndeadHuskTower husk = new UndeadHuskTower(
                    TowerBalanceRuntime.resolve(UndeadTowers.T2_ZOMBIE_TOWER),
                    owner,
                    lane.teamId(),
                    lane.laneId(),
                    GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane))
            );
            lane.addTower(husk);
            SemionTowerEntity towerEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, husk);
            husk.syncHealth(50.0);
            towerEntity.setHealth(50.0F);
            SemionMonsterEntity first = BuilderIntegrationGameTestSupport.spawnMonster(
                    context, lane, "undead-husk-thorn-first", lane.laneId(), 1_000.0,
                    towerEntity.getX() + 1.0, towerEntity.getY(), towerEntity.getZ()
            );
            SemionMonsterEntity second = BuilderIntegrationGameTestSupport.spawnMonster(
                    context, lane, "undead-husk-thorn-second", lane.laneId(), 1_000.0,
                    towerEntity.getX() - 1.0, towerEntity.getY(), towerEntity.getZ()
            );
            SemionMonsterEntity otherLane = BuilderIntegrationGameTestSupport.spawnMonster(
                    context, lane, "undead-husk-thorn-other-lane", lane.laneId() + 1, 1_000.0,
                    towerEntity.getX(), towerEntity.getY(), towerEntity.getZ() + 1.0
            );

            husk.onDamaged(towerEntity, null, 10.0, 60.0, 50.0);
            BuilderIntegrationGameTestSupport.require(first.runtimeMonster().health() < 1_000.0,
                    "Husk thorns must damage a nearby defended-lane monster.");
            BuilderIntegrationGameTestSupport.require(second.runtimeMonster().health() < 1_000.0,
                    "Husk thorns must damage every nearby defended-lane monster.");
            BuilderIntegrationGameTestSupport.requireClose(1_000.0, otherLane.runtimeMonster().health(),
                    "Husk thorns must preserve lane ownership through the shared area pipeline.");
            BuilderIntegrationGameTestSupport.requireClose(54.0, husk.health(),
                    "Husk thorns must heal exactly once per applied target.");

            double firstHealthAfterPulse = first.runtimeMonster().health();
            husk.onDamaged(towerEntity, null, 10.0, 54.0, 54.0);
            BuilderIntegrationGameTestSupport.requireClose(firstHealthAfterPulse, first.runtimeMonster().health(),
                    "Husk thorns must not fire again during cooldown.");
            BuilderIntegrationGameTestSupport.requireClose(54.0, husk.health(),
                    "A blocked thorn pulse must not heal the tower.");

            int cooldownTicks = TowerBalanceRuntime.abilityTicks(husk.type().id(), "thornCooldownTicks");
            for (int tick = 0; tick < cooldownTicks; tick++) {
                husk.tick(lane);
            }
            husk.onDamaged(towerEntity, null, 10.0, 54.0, 54.0);
            BuilderIntegrationGameTestSupport.require(first.runtimeMonster().health() < firstHealthAfterPulse,
                    "Husk thorns must become available after the configured cooldown.");
            BuilderIntegrationGameTestSupport.requireClose(58.0, husk.health(),
                    "The next valid thorn pulse must heal per applied target again.");
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Undead husk integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }
}
