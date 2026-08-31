package kim.biryeong.semiontd.tower.undead;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.effect.TimedEffectType;
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

public final class UndeadAnimalDebuffGameTest {
    @GameTest
    public void configuredDebuffsUseLiveMonsterAreaEffectPipeline(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("undead-animal-area-effect");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, UndeadTowerJob.ID, "undead-animal-area-effect"
            );
            game.players().get(owner).economy().addMineral(20_000);
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
            placeAndUpgrade(game, owner, position);
            BlockPos secondPosition = BuilderIntegrationGameTestSupport.emptyPositions(lane, position, 1).getFirst();
            placeAndUpgrade(game, owner, secondPosition);
            UndeadAnimalTower animal = (UndeadAnimalTower) lane.towerAt(GridPosition.from(position));
            UndeadAnimalTower secondAnimal = (UndeadAnimalTower) lane.towerAt(GridPosition.from(secondPosition));
            SemionTowerEntity towerEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, animal);
            SemionMonsterEntity monster = BuilderIntegrationGameTestSupport.spawnMonster(
                    context, lane, "undead-animal-debuff-target", lane.laneId(), 1_000.0, 20.0,
                    towerEntity.getX() + 1.0, towerEntity.getY(), towerEntity.getZ()
            );

            animal.tick(lane);
            secondAnimal.tick(lane);
            double attackReduction = TowerBalanceRuntime.ability(animal.type().id(), "attackDamageReduction");
            double damageTakenBonus = TowerBalanceRuntime.ability(animal.type().id(), "towerDamageTakenBonus");
            BuilderIntegrationGameTestSupport.requireClose(
                    attackReduction,
                    monster.activeTimedEffectMagnitude(TimedEffectType.MONSTER_ATTACK_DAMAGE_REDUCTION),
                    "Two Undead animals must keep the configured non-stacking attack debuff magnitude."
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    damageTakenBonus,
                    monster.activeTimedEffectMagnitude(TimedEffectType.MONSTER_TOWER_DAMAGE_TAKEN_BONUS),
                    "Two T2 Undead animals must keep the configured non-stacking vulnerability magnitude."
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    20.0 * (1.0 - attackReduction),
                    monster.attackDamageAmount(),
                    "The live monster attack pipeline must consume the configured reduction."
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    100.0 * (1.0 + damageTakenBonus),
                    monster.towerDamageTaken(100.0),
                    "The live tower-damage pipeline must consume the configured vulnerability."
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
            context.fail(Component.literal("Undead animal integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }

    private static void placeAndUpgrade(SemionGame game, UUID owner, BlockPos position) {
        BuilderIntegrationGameTestSupport.require(
                ProductionTowerService.placeTower(game, owner, position, UndeadTowers.T1_UNDEAD_ANIMAL_TOWER.id())
                        == TowerPlacementResult.SUCCESS,
                "Undead animal must place through the production service."
        );
        BuilderIntegrationGameTestSupport.require(
                ProductionTowerService.upgradeTower(game, owner, position, UndeadTowers.T2_UNDEAD_ANIMAL_TOWER.id())
                        == TowerUpgradeResult.SUCCESS,
                "Undead animal must upgrade through the production graph."
        );
    }
}
