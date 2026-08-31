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

public final class UndeadZombieCombatGameTest {
    @GameTest(maxTicks = 140)
    public void lifeStealAndKillBoostUseLiveEntityTime(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("undead-zombie-combat-lifecycle");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, UndeadTowerJob.ID, "undead-zombie-integration"
            );
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            UndeadZombieTower zombie = new UndeadZombieTower(
                    TowerBalanceRuntime.resolve(UndeadTowers.T1_ZOMBIE_TOWER),
                    owner,
                    lane.teamId(),
                    lane.laneId(),
                    GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane))
            );
            lane.addTower(zombie);
            SemionTowerEntity towerEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, zombie);
            zombie.syncHealth(50.0);
            towerEntity.setHealth(50.0F);
            SemionMonsterEntity target = BuilderIntegrationGameTestSupport.spawnMonster(
                    context,
                    lane,
                    "undead-zombie-life-steal-target",
                    lane.laneId(),
                    1_000.0,
                    towerEntity.getX() + 1.0,
                    towerEntity.getY(),
                    towerEntity.getZ()
            );

            zombie.onAttack(towerEntity, target, 20.0, false);
            BuilderIntegrationGameTestSupport.requireClose(
                    54.0,
                    zombie.health(),
                    "Zombie attempted-damage life steal must heal the live tower entity."
            );
            zombie.onKill(towerEntity, target, 20.0);
            BuilderIntegrationGameTestSupport.requireClose(
                    zombie.type().damage() + 2.0,
                    zombie.modifyAttackDamage(towerEntity, target, zombie.type().damage()),
                    "Zombie kill boost must apply before its configured expiry."
            );

            SemionGame scheduledGame = game;
            int expiryTicks = TowerBalanceRuntime.abilityTicks(zombie.type().id(), "damageBoostTicks");
            context.runAfterDelay(expiryTicks + 1, () -> {
                try {
                    BuilderIntegrationGameTestSupport.requireClose(
                            zombie.type().damage(),
                            zombie.modifyAttackDamage(towerEntity, target, zombie.type().damage()),
                            "Zombie kill boost must expire against live server game time."
                    );
                    context.succeed();
                } catch (RuntimeException | Error failure) {
                    failure.printStackTrace();
                    context.fail(Component.literal("Undead zombie expiry failed: " + failure.getMessage()));
                } finally {
                    scheduledGame.close();
                    TowerBalanceRuntime.apply(defaults);
                }
            });
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
            context.fail(Component.literal("Undead zombie integration failed: " + failure.getMessage()));
        }
    }
}
