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

public final class UndeadDrownedRevivalLifecycleGameTest {
    @GameTest(maxTicks = 100)
    public void lethalDamageRevivesThenConfiguredDecayKillsWithoutInvulnerability(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("undead-drowned-revival-lifecycle");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, UndeadTowerJob.ID, "undead-drowned-revival-lifecycle"
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

            SemionMonsterEntity attacker = BuilderIntegrationGameTestSupport.spawnMonster(
                    context, lane, "undead-drowned-revival-attacker", lane.laneId(), 100.0,
                    entity.getX() + 1.0, entity.getY(), entity.getZ()
            );
            attacker.setNoAi(true);
            attacker.setTarget(entity);
            castFixedDamage(attacker, 100.0);

            double maximumHealth = drowned.currentMaxHealth();
            BuilderIntegrationGameTestSupport.requireClose(maximumHealth, drowned.health(),
                    "Lethal damage must restore the Drowned runtime tower to full health.");
            BuilderIntegrationGameTestSupport.requireClose(maximumHealth, entity.getHealth(),
                    "Lethal damage must restore the live Drowned entity to full health.");
            BuilderIntegrationGameTestSupport.require(
                    String.join("\n", drowned.runtimeDetailLines()).contains("부활 상태"),
                    "The tower details must expose the active revival decay."
            );

            double healthBeforeDamage = entity.getHealth();
            attacker.setPos(entity.getX() + 100.0, entity.getY(), entity.getZ());
            entity.hurtIgnoringReductions(attacker.damageSources().mobAttack(attacker), 1.0);
            BuilderIntegrationGameTestSupport.require(
                    entity.getHealth() < healthBeforeDamage,
                    "Revival must not grant invulnerability; later fixed damage must still apply."
            );

            int decayTicks = TowerBalanceRuntime.abilityTicks(
                    drowned.type().id(), UndeadAbilityKey.LAST_STAND_TICKS.key()
            );
            int midpoint = Math.max(1, decayTicks / 2);
            for (int tick = 0; tick < midpoint; tick++) {
                drowned.tick(lane);
            }
            BuilderIntegrationGameTestSupport.require(
                    drowned.health() > 0.0 && drowned.health() < maximumHealth,
                    "Revived health must progressively decrease before the configured lifetime ends."
            );
            for (int tick = midpoint; tick < decayTicks; tick++) {
                drowned.tick(lane);
            }
            BuilderIntegrationGameTestSupport.require(
                    drowned.health() <= 0.0 && entity.getHealth() <= 0.0F && !entity.isAlive(),
                    "Configured revival decay must reach zero and kill the Drowned."
            );

            lane.resetForRound();
            SemionTowerEntity nextRoundEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, drowned);
            drowned.syncHealth(10.0);
            nextRoundEntity.setHealth(10.0F);
            nextRoundEntity.hurtIgnoringReductions(attacker.damageSources().mobAttack(attacker), 100.0);
            BuilderIntegrationGameTestSupport.requireClose(
                    drowned.currentMaxHealth(), nextRoundEntity.getHealth(),
                    "A new round must restore the one-time Drowned revival."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Undead Drowned revival integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }

    private static void castFixedDamage(SemionMonsterEntity attacker, double damage) {
        new SiegeTrueDamageGoal(attacker, damage, 60, 1, 0.0).tick();
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
