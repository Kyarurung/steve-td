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

public final class UndeadDrownedLastStandLifecycleGameTest {
    @GameTest(maxTicks = 100)
    public void lastStandExpiresDuringTheFirstRoundAndLaterDamageKills(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("undead-drowned-last-stand-expiry");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, UndeadTowerJob.ID, "undead-drowned-last-stand-expiry"
            );
            DrownedFixture fixture = drowned(game, owner);
            SemionMonsterEntity attacker = attacker(context, fixture.lane(), fixture.entity(), "expiry");

            castFixedDamage(attacker, 100.0);

            BuilderIntegrationGameTestSupport.requireClose(1.0, fixture.drowned().health(),
                    "Lethal damage must leave the Drowned runtime tower at one health.");
            BuilderIntegrationGameTestSupport.requireClose(1.0, fixture.entity().getHealth(),
                    "Lethal damage must leave the live Drowned entity at one health.");
            BuilderIntegrationGameTestSupport.require(
                    String.join("\n", fixture.drowned().runtimeDetailLines()).contains("최후의 저항"),
                    "The tower details must expose active Last Stand immunity."
            );

            fixture.entity().hurtIgnoringReductions(attacker.damageSources().mobAttack(attacker), 100.0);
            BuilderIntegrationGameTestSupport.requireClose(1.0, fixture.entity().getHealth(),
                    "Damage during Last Stand must be blocked.");

            int durationTicks = TowerBalanceRuntime.abilityTicks(
                    fixture.drowned().type().id(), UndeadAbilityKey.LAST_STAND_TICKS.key()
            );
            for (int tick = 1; tick < durationTicks; tick++) {
                fixture.drowned().tick(fixture.lane());
            }
            fixture.entity().hurtIgnoringReductions(attacker.damageSources().mobAttack(attacker), 100.0);
            BuilderIntegrationGameTestSupport.requireClose(1.0, fixture.entity().getHealth(),
                    "Last Stand must remain active until the configured final tick.");

            fixture.drowned().tick(fixture.lane());
            fixture.entity().hurtIgnoringReductions(attacker.damageSources().mobAttack(attacker), 100.0);
            BuilderIntegrationGameTestSupport.require(
                    fixture.drowned().health() <= 0.0 && fixture.entity().getHealth() <= 0.0F
                            && !fixture.entity().isAlive(),
                    "Damage after Last Stand expires must kill the one-health Drowned in the first round."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Undead Drowned Last Stand expiry failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }

    @GameTest(maxTicks = 100)
    public void roundResetRestoresOneLastStandActivation(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("undead-drowned-last-stand-reset");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, UndeadTowerJob.ID, "undead-drowned-last-stand-reset"
            );
            DrownedFixture fixture = drowned(game, owner);
            SemionMonsterEntity firstAttacker = attacker(context, fixture.lane(), fixture.entity(), "first-round");

            castFixedDamage(firstAttacker, 100.0);
            BuilderIntegrationGameTestSupport.requireClose(1.0, fixture.entity().getHealth(),
                    "The first round must activate Last Stand once.");

            fixture.lane().resetForRound();
            SemionTowerEntity nextRoundEntity = BuilderIntegrationGameTestSupport.towerEntity(
                    fixture.lane(), fixture.drowned()
            );
            fixture.drowned().syncHealth(10.0);
            nextRoundEntity.setHealth(10.0F);
            SemionMonsterEntity secondAttacker = attacker(
                    context, fixture.lane(), nextRoundEntity, "second-round"
            );
            castFixedDamage(secondAttacker, 100.0);

            BuilderIntegrationGameTestSupport.requireClose(1.0, fixture.drowned().health(),
                    "Round reset must restore Last Stand for the runtime tower.");
            BuilderIntegrationGameTestSupport.requireClose(1.0, nextRoundEntity.getHealth(),
                    "Round reset must restore Last Stand for the live entity.");
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Undead Drowned Last Stand reset failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }

    private static DrownedFixture drowned(SemionGame game, UUID owner) {
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
        return new DrownedFixture(lane, drowned, entity);
    }

    private static SemionMonsterEntity attacker(
            GameTestHelper context,
            PlayerLane lane,
            SemionTowerEntity target,
            String suffix
    ) {
        SemionMonsterEntity attacker = BuilderIntegrationGameTestSupport.spawnMonster(
                context, lane, "undead-drowned-last-stand-" + suffix, lane.laneId(), 100.0,
                target.getX() + 1.0, target.getY(), target.getZ()
        );
        attacker.setNoAi(true);
        attacker.setTarget(target);
        return attacker;
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

    private record DrownedFixture(PlayerLane lane, UndeadDrownedTower drowned, SemionTowerEntity entity) {
    }
}
