package kim.biryeong.semiontd.tower.legion;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.LegionTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public final class LegionGoatSupportAreaEffectGameTest {
    @GameTest
    public void goatProvidersBuffLiveLegionBodyAndRespectThreeStackCap(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("legion-goat-support-cap");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, LegionTowerJob.ID, "legion-goat-support-cap"
            );
            game.players().get(owner).economy().addMineral(20_000);
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            List<BlockPos> positions = BuilderIntegrationGameTestSupport.emptyPositions(
                    lane, BuilderIntegrationGameTestSupport.primaryPosition(lane), 5
            );
            place(game, owner, positions.get(0), LegionTowers.T1_SLIME_TOWER.id());
            for (int index = 1; index < positions.size(); index++) {
                place(game, owner, positions.get(index), LegionTowers.T1_GOAT_TOWER.id());
            }

            LegionSlimeTower slime = (LegionSlimeTower) lane.towerAt(GridPosition.from(positions.get(0)));
            SemionTowerEntity slimeEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, slime);
            List<LegionGoatTower> goats = positions.subList(1, positions.size()).stream()
                    .map(position -> (LegionGoatTower) lane.towerAt(GridPosition.from(position)))
                    .toList();
            goats.forEach(goat -> goat.tick(lane));

            double damagePerStack = TowerBalanceRuntime.ability(
                    LegionTowers.T1_GOAT_TOWER.id(), "damageBonus"
            );
            double reductionPerStack = TowerBalanceRuntime.ability(
                    LegionTowers.T1_GOAT_TOWER.id(), "damageReduction"
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    damagePerStack * 3.0,
                    slimeEntity.activeTimedEffectMagnitude(TimedEffectType.TOWER_DAMAGE_BONUS),
                    "Four live goat providers must use only three sourced damage-buff stacks."
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    reductionPerStack * 3.0,
                    slimeEntity.activeTimedEffectMagnitude(TimedEffectType.TOWER_DAMAGE_REDUCTION),
                    "Goat reduction must use the same three-provider cap."
            );

            BuilderIntegrationGameTestSupport.require(lane.killTower(goats.get(0)),
                    "The first goat provider must die through the lane lifecycle.");
            int duration = TowerBalanceRuntime.abilityTicks(
                    LegionTowers.T1_GOAT_TOWER.id(), "buffDurationTicks"
            );
            for (int tick = 0; tick <= duration; tick++) {
                slimeEntity.aiStep();
            }
            goats.subList(1, goats.size()).forEach(goat -> {
                goat.resetForRound(lane);
                goat.tick(lane);
            });
            BuilderIntegrationGameTestSupport.requireClose(
                    damagePerStack * 3.0,
                    slimeEntity.activeTimedEffectMagnitude(TimedEffectType.TOWER_DAMAGE_BONUS),
                    "A dead provider must release its stack so the fourth living goat can enter the cap."
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
            context.fail(Component.literal("Legion goat support integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }

    private static void place(SemionGame game, UUID owner, BlockPos position, String towerId) {
        BuilderIntegrationGameTestSupport.require(
                ProductionTowerService.placeTower(game, owner, position, towerId) == TowerPlacementResult.SUCCESS,
                "Production placement failed for " + towerId + '.'
        );
    }
}
