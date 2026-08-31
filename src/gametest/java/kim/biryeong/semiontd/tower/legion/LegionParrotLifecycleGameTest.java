package kim.biryeong.semiontd.tower.legion;

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
import kim.biryeong.semiontd.job.LegionTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public final class LegionParrotLifecycleGameTest {
    @GameTest
    public void parrotStacksResetForRoundAndDoNotTransferOnUpgrade(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("legion-parrot-production-lifecycle");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(
                    context, owner, LegionTowerJob.ID, "legion-parrot-integration"
            );
            game.players().get(owner).economy().addMineral(10_000);
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, LegionTowers.T1_PARROT_TOWER.id())
                            == TowerPlacementResult.SUCCESS,
                    "Legion parrot must place through ProductionTowerService."
            );
            LegionParrotTower parrot = (LegionParrotTower) lane.towerAt(GridPosition.from(position));
            SemionTowerEntity towerEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, parrot);
            SemionMonsterEntity target = BuilderIntegrationGameTestSupport.spawnMonster(
                    context,
                    lane,
                    "legion-parrot-stack-target",
                    lane.laneId(),
                    1_000.0,
                    towerEntity.getX() + 1.0,
                    towerEntity.getY(),
                    towerEntity.getZ()
            );
            int maximumStacks = TowerBalanceRuntime.abilityInt(
                    parrot.type().id(), "maxAttackStacks", 0
            );
            double stackBonus = TowerBalanceRuntime.ability(
                    parrot.type().id(), "attackStackBonus", 0.0
            );
            BuilderIntegrationGameTestSupport.require(maximumStacks > 0,
                    "T1 parrot maximum stacks must be positive: " + maximumStacks);
            BuilderIntegrationGameTestSupport.require(stackBonus > 0.0,
                    "T1 parrot stack bonus must be positive: " + stackBonus);
            for (int attack = 0; attack < maximumStacks + 2; attack++) {
                parrot.onAttack(towerEntity, target, parrot.type().damage(), false);
            }
            BuilderIntegrationGameTestSupport.require(parrot.attackStacks() == maximumStacks,
                    "Parrot attack stacks must stop at the configured maximum; actual="
                            + parrot.attackStacks() + ", maximum=" + maximumStacks);
            BuilderIntegrationGameTestSupport.require(
                    String.join("\n", parrot.runtimeDetailLines()).contains(
                            "공격 스택 " + maximumStacks + "/" + maximumStacks
                    ),
                    "Parrot runtime details must expose the capped live stack state."
            );
            double maximumMultiplier = 1.0 + maximumStacks * stackBonus;
            BuilderIntegrationGameTestSupport.requireClose(
                    parrot.type().damage() * maximumMultiplier,
                    parrot.modifyAttackDamage(towerEntity, target, parrot.type().damage()),
                    "T1 parrot damage must use all configured live attack stacks."
            );
            BuilderIntegrationGameTestSupport.require(
                    parrot.adjustAttackInterval(parrot.type().attackIntervalTicks())
                            == (int) Math.ceil(parrot.type().attackIntervalTicks() / maximumMultiplier),
                    "T1 parrot attack interval must use the same live multiplier as damage."
            );

            game.teams().get(lane.teamId()).resetForRound();
            BuilderIntegrationGameTestSupport.require(parrot.attackStacks() == 0,
                    "Parrot attack stacks must reset at the round boundary.");
            for (int attack = 0; attack < 3; attack++) {
                parrot.onAttack(towerEntity, target, parrot.type().damage(), false);
            }
            BuilderIntegrationGameTestSupport.require(parrot.attackStacks() == 3,
                    "Parrot must rebuild its current-round stack state before upgrade.");

            BuilderIntegrationGameTestSupport.require(
                    ProductionTowerService.upgradeTower(game, owner, position, LegionTowers.T2_PARROT_TOWER.id())
                            == TowerUpgradeResult.SUCCESS,
                    "Parrot must upgrade through the Legion production graph."
            );
            LegionParrotTower upgraded = (LegionParrotTower) lane.towerAt(GridPosition.from(position));
            BuilderIntegrationGameTestSupport.require(upgraded.attackStacks() == 0,
                    "Current-round parrot attack stacks must not transfer to the upgraded tower instance.");
            BuilderIntegrationGameTestSupport.require(
                    String.join("\n", upgraded.runtimeDetailLines()).contains(
                            "공격 스택 0/" + TowerBalanceRuntime.abilityInt(
                                    upgraded.type().id(), "maxAttackStacks", 0
                            )
                    ),
                    "The upgraded parrot detail line must reflect its fresh current-round state."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Legion parrot integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }
}
