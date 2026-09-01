package kim.biryeong.semiontd.tower.illager;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.IllagerTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public final class IllagerRaidLifecycleGameTest {
    @GameTest(maxTicks = 100)
    public void activationEffectsRoundEndCloseAndPlayerReuseUseControllerLifecycle(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("illager-raid-lifecycle");
        SemionGame firstGame = null;
        SemionGame secondGame = null;
        boolean firstClosed = false;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            firstGame = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, IllagerTowerJob.ID, "illager-raid-first"
            );
            firstGame.players().get(owner).economy().addMineral(20_000);
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(firstGame, owner);
            BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(firstGame, owner, position, IllagerTowers.T1_VINDICATOR.id())
                            == TowerPlacementResult.SUCCESS,
                    "Illager starter placement must use the production catalog."
            );
            IllagerTower tower = (IllagerTower) lane.towerAt(GridPosition.from(position));
            IllagerRaidController.onRoundStarted(firstGame, firstGame.players().get(owner));
            IllagerRaidState state = IllagerRaidStates.get(owner).orElseThrow();
            BuilderIntegrationGameTestSupport.require(
                    state.roundStartTowerCount() == 1,
                    "Raid round start must snapshot the alive Illager tower count."
            );
            state.addGauge(IllagerRaidController.gaugeMax(), IllagerRaidController.gaugeMax());
            BuilderIntegrationGameTestSupport.require(
                    IllagerRaidController.playPendingActivationEffects(context.getLevel().getServer(), lane) == 1,
                    "Raid activation VFX must reach the live Illager tower exactly once."
            );
            BuilderIntegrationGameTestSupport.require(
                    IllagerRaidController.playPendingActivationEffects(context.getLevel().getServer(), lane) == 0,
                    "Raid activation VFX must not replay after consumption."
            );
            tower.tick(lane);
            var entity = BuilderIntegrationGameTestSupport.towerEntity(lane, tower);
            BuilderIntegrationGameTestSupport.requireClose(
                    0.06,
                    entity.activeTimedEffectMagnitude(TimedEffectType.TOWER_DAMAGE_BONUS),
                    "Raid damage bonus must use the round-start tower snapshot."
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    0.02,
                    entity.activeTimedEffectMagnitude(TimedEffectType.TOWER_ATTACK_SPEED_BONUS),
                    "Raid attack-speed bonus must use the round-start tower snapshot."
            );

            IllagerRaidController.onRoundEnded(owner);
            BuilderIntegrationGameTestSupport.require(
                    IllagerRaidStates.get(owner).isEmpty(),
                    "Round end must clear the keyed raid state."
            );
            IllagerRaidStates.state(owner).resetForRound(9);
            firstGame.close();
            firstClosed = true;
            BuilderIntegrationGameTestSupport.require(
                    IllagerRaidStates.get(owner).isEmpty(),
                    "Match close must clear the keyed raid state."
            );

            IllagerRaidStates.state(owner).resetForRound(11);
            secondGame = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, IllagerTowerJob.ID, "illager-raid-second"
            );
            IllagerRaidStates.get(owner).ifPresent(reusedState -> {
                BuilderIntegrationGameTestSupport.require(
                        reusedState.gauge() == 0 && !reusedState.active()
                                && reusedState.roundStartTowerCount() != 11,
                        "A second match must replace the first match raid state."
                );
            });
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Illager raid lifecycle integration failed: " + failure.getMessage()));
        } finally {
            if (firstGame != null && !firstClosed) {
                firstGame.close();
            }
            if (secondGame != null) {
                secondGame.close();
            }
            IllagerRaidStates.clear(owner);
            TowerBalanceRuntime.apply(defaults);
        }
    }

    @GameTest(maxTicks = 100)
    public void ravagerExposesConfiguredRaidBonusesAsTimedEffects(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("illager-ravager-effects");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, IllagerTowerJob.ID, "illager-ravager-effects"
            );
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            GridPosition position = GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane));
            IllagerTower tower = new IllagerTower(
                    TowerBalanceRuntime.resolve(IllagerTowers.T3_RAVAGER),
                    owner,
                    lane.teamId(),
                    lane.laneId(),
                    position,
                    position
            );
            lane.addTower(tower);
            IllagerRaidController.onRoundStarted(game, game.players().get(owner));
            IllagerRaidState state = IllagerRaidStates.get(owner).orElseThrow();
            state.resetForRound(4);
            state.addGauge(IllagerRaidController.gaugeMax(), IllagerRaidController.gaugeMax());
            BuilderIntegrationGameTestSupport.require(
                    IllagerRaidController.playPendingActivationEffects(context.getLevel().getServer(), lane) == 1,
                    "Raid activation must emit VFX for the live Ravager."
            );
            tower.tick(lane);
            var entity = BuilderIntegrationGameTestSupport.towerEntity(lane, tower);
            BuilderIntegrationGameTestSupport.requireClose(
                    0.24,
                    entity.activeTimedEffectMagnitude(TimedEffectType.TOWER_DAMAGE_BONUS),
                    "Ravager raid damage bonus must use the four-tower snapshot."
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    0.08,
                    entity.activeTimedEffectMagnitude(TimedEffectType.TOWER_ATTACK_SPEED_BONUS),
                    "Ravager raid attack speed must use the four-tower snapshot."
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    0.35,
                    entity.activeTimedEffectMagnitude(TimedEffectType.TOWER_DAMAGE_REDUCTION),
                    "Ravager raid damage reduction must come from tower balance."
            );
            BuilderIntegrationGameTestSupport.require(
                    entity.activeTimedEffectTicks(TimedEffectType.TOWER_DAMAGE_REDUCTION) == 40,
                    "Raid effect duration must come from tower balance."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Illager Ravager effects integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            IllagerRaidStates.clear(owner);
            TowerBalanceRuntime.apply(defaults);
        }
    }
}
