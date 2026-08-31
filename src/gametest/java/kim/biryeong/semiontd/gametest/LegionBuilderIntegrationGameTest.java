package kim.biryeong.semiontd.gametest;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import kim.biryeong.semiontd.config.MapConfig;
import kim.biryeong.semiontd.config.ProgressionConfig;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.config.WaveConfig;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.AssignedParticipant;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.MatchMode;
import kim.biryeong.semiontd.game.ParticipantSelectionPlan;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.SemionGameManager;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.game.TowerUpgradeResult;
import kim.biryeong.semiontd.job.LegionTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import kim.biryeong.semiontd.tower.legion.IllusionCloneSpawnQueue;
import kim.biryeong.semiontd.tower.legion.LegionGlobalIllusionTower;
import kim.biryeong.semiontd.tower.legion.LegionParrotTower;
import kim.biryeong.semiontd.tower.legion.LegionTowers;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public final class LegionBuilderIntegrationGameTest {
    @GameTest
    public void parrotStacksResetForRoundAndDoNotTransferOnUpgrade(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderFamilyGameTestSupport.stableUuid("legion-parrot-production-lifecycle");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderFamilyGameTestSupport.startedGame(
                    context, owner, LegionTowerJob.ID, "legion-parrot-integration"
            );
            game.players().get(owner).economy().addMineral(10_000);
            PlayerLane lane = BuilderFamilyGameTestSupport.lane(game, owner);
            BlockPos position = BuilderFamilyGameTestSupport.primaryPosition(lane);
            BuilderFamilyGameTestSupport.require(
                    ProductionTowerService.placeTower(game, owner, position, LegionTowers.T1_PARROT_TOWER.id())
                            == TowerPlacementResult.SUCCESS,
                    "Legion parrot must place through ProductionTowerService."
            );
            LegionParrotTower parrot = (LegionParrotTower) lane.towerAt(GridPosition.from(position));
            SemionTowerEntity towerEntity = BuilderFamilyGameTestSupport.towerEntity(lane, parrot);
            SemionMonsterEntity target = BuilderFamilyGameTestSupport.spawnMonster(
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
            BuilderFamilyGameTestSupport.require(maximumStacks > 0,
                    "T1 parrot maximum stacks must be positive: " + maximumStacks);
            BuilderFamilyGameTestSupport.require(stackBonus > 0.0,
                    "T1 parrot stack bonus must be positive: " + stackBonus);
            for (int attack = 0; attack < maximumStacks + 2; attack++) {
                parrot.onAttack(towerEntity, target, parrot.type().damage(), false);
            }
            BuilderFamilyGameTestSupport.require(parrot.attackStacks() == maximumStacks,
                    "Parrot attack stacks must stop at the configured maximum; actual="
                            + parrot.attackStacks() + ", maximum=" + maximumStacks);
            BuilderFamilyGameTestSupport.require(
                    String.join("\n", parrot.runtimeDetailLines()).contains(
                            "공격 스택 " + maximumStacks + "/" + maximumStacks
                    ),
                    "Parrot runtime details must expose the capped live stack state."
            );
            double maximumMultiplier = 1.0 + maximumStacks * stackBonus;
            BuilderFamilyGameTestSupport.requireClose(
                    parrot.type().damage() * maximumMultiplier,
                    parrot.modifyAttackDamage(towerEntity, target, parrot.type().damage()),
                    "T1 parrot damage must use all configured live attack stacks."
            );
            BuilderFamilyGameTestSupport.require(
                    parrot.adjustAttackInterval(parrot.type().attackIntervalTicks())
                            == (int) Math.ceil(parrot.type().attackIntervalTicks() / maximumMultiplier),
                    "T1 parrot attack interval must use the same live multiplier as damage."
            );

            game.teams().get(lane.teamId()).resetForRound();
            BuilderFamilyGameTestSupport.require(parrot.attackStacks() == 0,
                    "Parrot attack stacks must reset at the round boundary.");
            for (int attack = 0; attack < 3; attack++) {
                parrot.onAttack(towerEntity, target, parrot.type().damage(), false);
            }
            BuilderFamilyGameTestSupport.require(parrot.attackStacks() == 3,
                    "Parrot must rebuild its current-round stack state before upgrade.");

            BuilderFamilyGameTestSupport.require(
                    ProductionTowerService.upgradeTower(game, owner, position, LegionTowers.T2_PARROT_TOWER.id())
                            == TowerUpgradeResult.SUCCESS,
                    "Parrot must upgrade through the Legion production graph."
            );
            LegionParrotTower upgraded = (LegionParrotTower) lane.towerAt(GridPosition.from(position));
            BuilderFamilyGameTestSupport.require(upgraded.attackStacks() == 0,
                    "Current-round parrot attack stacks must not transfer to the upgraded tower instance.");
            BuilderFamilyGameTestSupport.require(
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

    @GameTest
    public void managerResetClearsPendingIllusionsBeforeSecondMatch(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        MinecraftServer server = context.getLevel().getServer();
        SemionGameManager manager = new SemionGameManager();
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            IllusionCloneSpawnQueue.clear();
            Path storePath = Files.createTempDirectory("legion-illusion-reset").resolve("profiles.json");
            manager.configure(
                    kim.biryeong.semiontd.config.EconomyConfig.defaultConfig(),
                    new WaveConfig(List.of(), 20, null),
                    MapConfig.defaultConfig(),
                    ProgressionConfig.defaultConfig(),
                    storePath
            );
            UUID red = BuilderFamilyGameTestSupport.stableUuid("legion-reset-red");
            UUID blue = BuilderFamilyGameTestSupport.stableUuid("legion-reset-blue");
            SemionGame firstGame = manager.createGame(server);
            BuilderFamilyGameTestSupport.require(firstGame.start(server, plan(red, blue, "first")),
                    "The first manager-owned match must start.");
            PlayerLane firstLane = firstGame.playerLane(red).orElseThrow();
            addPendingIllusions(firstLane, red);
            BuilderFamilyGameTestSupport.require(pendingCloneCount() > 0,
                    "The first match must leave pending illusion work before manager reset.");

            BuilderFamilyGameTestSupport.require(manager.resetToLobby(server),
                    "Manager reset must close the first active match.");
            BuilderFamilyGameTestSupport.require(pendingCloneCount() == 0,
                    "Manager reset must clear every pending illusion from the previous match.");
            BuilderFamilyGameTestSupport.require(queueTick() == 0,
                    "Manager reset must reset the global illusion scheduler tick."
            );

            SemionGame secondGame = manager.createGame(server);
            BuilderFamilyGameTestSupport.require(secondGame.start(server, plan(red, blue, "second")),
                    "A second match with the reused players must start after reset.");
            PlayerLane secondLane = secondGame.playerLane(red).orElseThrow();
            int observationTicks = TowerBalanceRuntime.illusionCloneSpawnSpreadTicks() + 5;
            for (int tick = 0; tick < observationTicks; tick++) {
                IllusionCloneSpawnQueue.tick();
            }
            BuilderFamilyGameTestSupport.require(pendingCloneCount() == 0,
                    "The second match must not inherit pending illusion work from the first match.");
            BuilderFamilyGameTestSupport.require(countIllusionClones(secondLane) == 0,
                    "The second match must not receive a stale illusion clone from the first match.");
            context.succeed();
        } catch (Exception | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Legion illusion lifecycle failed: " + failure.getMessage()));
        } finally {
            manager.shutdown();
            IllusionCloneSpawnQueue.clear();
            TowerBalanceRuntime.apply(defaults);
        }
    }

    private static ParticipantSelectionPlan plan(UUID red, UUID blue, String suffix) {
        return new ParticipantSelectionPlan(
                MatchMode.NORMAL,
                List.of(
                        new AssignedParticipant(red, "legion-reset-red-" + suffix, TeamId.RED, 1),
                        new AssignedParticipant(blue, "legion-reset-blue-" + suffix, TeamId.BLUE, 1)
                ),
                Set.of(),
                2
        );
    }

    private static void addPendingIllusions(PlayerLane lane, UUID owner) {
        GridPosition sourcePosition = GridPosition.from(
                BlockPos.containing(lane.laneLayout().positionAt(0.35))
        );
        GridPosition illusionPosition = new GridPosition(
                sourcePosition.x() + 1,
                sourcePosition.y(),
                sourcePosition.z()
        );
        LegionParrotTower source = new LegionParrotTower(
                TowerBalanceRuntime.resolve(LegionTowers.T1_PARROT_TOWER),
                owner,
                lane.teamId(),
                lane.laneId(),
                sourcePosition
        );
        LegionGlobalIllusionTower illusion = new LegionGlobalIllusionTower(
                TowerBalanceRuntime.resolve(LegionTowers.ILLUSION_TOWER),
                owner,
                lane.teamId(),
                lane.laneId(),
                illusionPosition
        );
        lane.addTower(source);
        lane.addTower(illusion);
        illusion.onDeath(lane);
    }

    private static int pendingCloneCount() throws ReflectiveOperationException {
        Field field = IllusionCloneSpawnQueue.class.getDeclaredField("PENDING_CLONE_SPAWNS");
        field.setAccessible(true);
        NavigableMap<?, ?> pending = (NavigableMap<?, ?>) field.get(null);
        int count = 0;
        for (Object value : pending.values()) {
            count += ((Queue<?>) value).size();
        }
        return count;
    }

    private static int queueTick() throws ReflectiveOperationException {
        Field field = IllusionCloneSpawnQueue.class.getDeclaredField("currentTick");
        field.setAccessible(true);
        return field.getInt(null);
    }

    private static long countIllusionClones(PlayerLane lane) {
        long count = 0;
        for (var entity : lane.arenaWorld().getAllEntities()) {
            if (entity instanceof SemionTowerEntity towerEntity && towerEntity.isIllusionClone()) {
                count++;
            }
        }
        return count;
    }
}
