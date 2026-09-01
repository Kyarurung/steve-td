package kim.biryeong.semiontd.tower.villager;

import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.config.AttackKind;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import kim.biryeong.semiontd.tower.Tower;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public final class VillagerAdvProgressionLifecycleGameTest extends VillagerAdvGameTestSupport {
    @GameTest(maxTicks = 240)
    public void waveSnapshotAppliesAsynchronousExperienceToTheSameLiveTower(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("villager-adv-wave-experience");
        SemionGame game = startedGame(context, owner);
        PlayerLane lane = lane(game, owner);
        BlockPos position = BuilderIntegrationGameTestSupport.primaryPosition(lane);
        BuilderIntegrationGameTestSupport.require(
                ProductionTowerService.placeTower(game, owner, position, VillagerTowers.ADV_T1_SPLASH_TOWER.id())
                        == TowerPlacementResult.SUCCESS,
                "Villager ADV tower placement should succeed before wave progression."
        );
        GridPosition gridPosition = GridPosition.from(position);
        VillagerAdvProgressionController.onWaveStarted(game, 1);
        waitForExperience(context, game, lane, gridPosition, 0);
    }

    @GameTest
    public void laneLeakRemovesReputationAndPreventsWavePayout(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("villager-adv-lane-leak");
        SemionGame game = startedGame(context, owner);
        PlayerLane lane = lane(game, owner);
        VillagerAdvProgressionController.onWaveCleared(game, 1);
        requireClose(0.75, VillagerAdvStates.reputation(owner), "A defended first wave should grant reputation.");
        lane.resetForRound();
        VillagerAdvProgressionController.onWaveStarted(game, 2);
        Monster monster = new Monster(
                "villager_adv_leak_probe",
                TeamId.RED,
                1,
                Optional.empty(),
                Optional.empty(),
                10.0,
                0.0,
                1.0,
                AttackKind.MELEE,
                "minecraft:zombie",
                0L
        );
        lane.enqueueSummonedMonster(monster);
        lane.tick(context.getLevel().getServer(), null, game.players());
        monster.syncLaneProgress(1.0);
        lane.tick(context.getLevel().getServer(), null, game.players());
        requireClose(0.25, VillagerAdvStates.reputation(owner), "A lane leak should remove configured reputation.");
        VillagerAdvProgressionController.onWaveCleared(game, 2);
        requireClose(0.25, VillagerAdvStates.reputation(owner), "A leaked wave should not grant reputation payout.");
        context.succeed();
    }

    @GameTest
    public void matchClosePreventsStateLeakIntoSecondMatch(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("villager-adv-second-match");
        SemionGame first = startedGame(context, owner);
        VillagerAdvProgressionController.onWaveCleared(first, 1);
        requireClose(0.75, VillagerAdvStates.reputation(owner), "First match should create player reputation.");
        first.close();
        requireClose(0.0, VillagerAdvStates.reputation(owner), "Match close should clear keyed ADV state.");
        SemionGame second = startedGameWithoutClear(context, owner);
        requireClose(0.0, VillagerAdvStates.reputation(owner), "Second match should not inherit ADV state.");
        second.close();
        context.succeed();
    }

    @GameTest
    public void eliminationClearsPlayerStateThroughTheCentralLifecycle(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("villager-adv-elimination");
        SemionGame game = startedGame(context, owner);
        VillagerAdvProgressionController.onWaveCleared(game, 1);
        requireClose(0.75, VillagerAdvStates.reputation(owner), "Wave clear should create reputation before elimination.");
        BuilderIntegrationGameTestSupport.require(
                game.killBoss(context.getLevel().getServer(), TeamId.RED),
                "Boss death should eliminate the ADV player's team."
        );
        requireClose(0.0, VillagerAdvStates.reputation(owner), "Elimination should clear keyed ADV state.");
        context.succeed();
    }

    private static void waitForExperience(
            GameTestHelper context,
            SemionGame game,
            PlayerLane lane,
            GridPosition position,
            int waitedTicks
    ) {
        context.runAfterDelay(1, () -> {
            VillagerAdvProgressionController.applyPending(game);
            Tower tower = lane.towerAt(position);
            if (tower != null && Math.abs(VillagerAdvStates.experience(tower) - 5.5) <= 0.01) {
                context.succeed();
                return;
            }
            if (waitedTicks >= 180) {
                double experience = tower == null ? 0.0 : VillagerAdvStates.experience(tower);
                context.fail(Component.literal("Expected 5.5 ADV experience, got " + experience + '.'));
                return;
            }
            waitForExperience(context, game, lane, position, waitedTicks + 1);
        });
    }
}
