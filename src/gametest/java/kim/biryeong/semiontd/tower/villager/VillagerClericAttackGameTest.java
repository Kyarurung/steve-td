package kim.biryeong.semiontd.tower.villager;

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
import kim.biryeong.semiontd.job.VillagerTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public final class VillagerClericAttackGameTest {
    @GameTest
    public void clericExtraAttackUsesLiveEntityDamagePipeline(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("villager-cleric-extra-attack");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, VillagerTowerJob.ID, "villager-cleric-attack"
            );
            game.players().get(owner).economy().addMineral(10_000);
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);

            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, VillagerTowers.T1_SPLASH_TOWER.id())
                            == TowerPlacementResult.SUCCESS,
                    "Villager splash starter must place through ProductionTowerService."
            );
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.upgradeTower(game, owner, position, VillagerTowers.T2_LIBRARIAN_TOWER.id())
                            == TowerUpgradeResult.SUCCESS,
                    "Villager splash starter must upgrade to librarian."
            );
            for (int round = 0; round < 2; round++) {
                game.teams().get(lane.teamId()).resetForRound();
            }
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.upgradeTower(game, owner, position, VillagerTowers.T3_CLERIC_TOWER.id())
                            == TowerUpgradeResult.SUCCESS,
                    "Librarian must upgrade to cleric through the production graph."
            );

            VillagerSplashTower cleric = (VillagerSplashTower) lane.towerAt(GridPosition.from(position));
            SemionTowerEntity towerEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, cleric);
            SemionMonsterEntity target = BuilderIntegrationGameTestSupport.spawnMonster(
                    context,
                    lane,
                    "villager-cleric-extra-attack-target",
                    lane.laneId(),
                    1_000.0,
                    towerEntity.getX() + 1.0,
                    towerEntity.getY(),
                    towerEntity.getZ()
            );
            double beforeExtraAttack = target.runtimeMonster().health();
            cleric.onAttack(towerEntity, target, cleric.type().damage(), false);
            cleric.onAttack(towerEntity, target, cleric.type().damage(), false);
            BuilderIntegrationGameTestSupport.requireClose(
                    beforeExtraAttack,
                    target.runtimeMonster().health(),
                    "The cleric must not fire its extra attack before the configured third attempt."
            );
            cleric.onAttack(towerEntity, target, cleric.type().damage(), false);
            BuilderIntegrationGameTestSupport.require(
                    target.runtimeMonster().health() < beforeExtraAttack,
                    "The cleric must route its configured extra attack through the live entity damage pipeline."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Villager cleric attack integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }
}
