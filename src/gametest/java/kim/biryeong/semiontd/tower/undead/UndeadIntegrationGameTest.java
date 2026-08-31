package kim.biryeong.semiontd.tower.undead;

import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;

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
import kim.biryeong.semiontd.job.UndeadTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import kim.biryeong.semiontd.tower.undead.UndeadHuskTower;
import kim.biryeong.semiontd.tower.undead.UndeadMeleeSkeletonTower;
import kim.biryeong.semiontd.tower.undead.UndeadRangedSkeletonTower;
import kim.biryeong.semiontd.tower.undead.UndeadTowers;
import kim.biryeong.semiontd.tower.undead.UndeadZombieTower;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public final class UndeadIntegrationGameTest {
    @GameTest(maxTicks = 140)
    public void zombieLifeStealAndKillBoostUseLiveEntityTime(GameTestHelper context) {
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

    @GameTest
    public void huskThornsHealPerHitRespectLaneAndCooldown(GameTestHelper context) {
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

    @GameTest
    public void skeletonBranchesTransferDeathStateThroughProductionUpgrade(GameTestHelper context) {
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
