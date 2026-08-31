package kim.biryeong.semiontd.gametest;

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
import kim.biryeong.semiontd.job.VillagerTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import kim.biryeong.semiontd.tower.villager.VillagerSplashTower;
import kim.biryeong.semiontd.tower.villager.VillagerTowers;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public final class VillagerBuilderIntegrationGameTest {
    @GameTest
    public void splashUpgradeKeepsSurvivalStateAndExtraAttackCadence(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderFamilyGameTestSupport.stableUuid("villager-splash-production-upgrade");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderFamilyGameTestSupport.startedGame(
                    context, owner, VillagerTowerJob.ID, "villager-splash-integration"
            );
            game.players().get(owner).economy().addMineral(10_000);
            PlayerLane lane = BuilderFamilyGameTestSupport.lane(game, owner);
            BlockPos position = BuilderFamilyGameTestSupport.primaryPosition(lane);

            BuilderFamilyGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, VillagerTowers.T1_SPLASH_TOWER.id())
                            == TowerPlacementResult.SUCCESS,
                    "Villager splash starter must place through ProductionTowerService."
            );
            BuilderFamilyGameTestSupport.require(
                    ProductionTowerService.upgradeTower(game, owner, position, VillagerTowers.T2_LIBRARIAN_TOWER.id())
                            == TowerUpgradeResult.SUCCESS,
                    "Villager splash starter must upgrade to its librarian runtime."
            );
            VillagerSplashTower librarian = (VillagerSplashTower) lane.towerAt(GridPosition.from(position));
            for (int round = 0; round < 2; round++) {
                game.teams().get(lane.teamId()).resetForRound();
            }
            BuilderFamilyGameTestSupport.require(
                    String.join("\n", librarian.runtimeDetailLines()).contains("생존 스택 2/6"),
                    "Librarian must own two survival stacks before upgrade."
            );

            BuilderFamilyGameTestSupport.require(
                    ProductionTowerService.upgradeTower(game, owner, position, VillagerTowers.T3_CLERIC_TOWER.id())
                            == TowerUpgradeResult.SUCCESS,
                    "Librarian must upgrade to cleric through the production graph."
            );
            VillagerSplashTower cleric = (VillagerSplashTower) lane.towerAt(GridPosition.from(position));
            BuilderFamilyGameTestSupport.require(
                    String.join("\n", cleric.runtimeDetailLines()).contains("생존 스택 2/6"),
                    "Cleric upgrade must retain the tower-scoped survival stacks."
            );
            double expectedDamage = cleric.type().damage() * 1.15;
            BuilderFamilyGameTestSupport.requireClose(
                    expectedDamage,
                    cleric.modifyAttackDamage(null, null, cleric.type().damage()),
                    "Retained survival stacks must use the cleric's current bonus value."
            );
            BuilderFamilyGameTestSupport.require(
                    cleric.adjustAttackInterval(cleric.type().attackIntervalTicks()) == 8,
                    "Retained survival stacks must accelerate the cleric's current attack interval."
            );

            SemionTowerEntity towerEntity = BuilderFamilyGameTestSupport.towerEntity(lane, cleric);
            SemionMonsterEntity target = BuilderFamilyGameTestSupport.spawnMonster(
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
            BuilderFamilyGameTestSupport.requireClose(
                    beforeExtraAttack,
                    target.runtimeMonster().health(),
                    "The cleric must not fire its extra attack before the configured third attempt."
            );
            cleric.onAttack(towerEntity, target, cleric.type().damage(), false);
            BuilderFamilyGameTestSupport.require(
                    target.runtimeMonster().health() < beforeExtraAttack,
                    "The cleric must route its configured extra attack through the live entity damage pipeline."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Villager builder integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }
}
