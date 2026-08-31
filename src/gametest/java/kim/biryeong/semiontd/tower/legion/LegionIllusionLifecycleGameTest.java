package kim.biryeong.semiontd.tower.legion;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import kim.biryeong.semiontd.config.EconomyConfig;
import kim.biryeong.semiontd.config.MapConfig;
import kim.biryeong.semiontd.config.ProgressionConfig;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.config.WaveConfig;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.AssignedParticipant;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.MatchMode;
import kim.biryeong.semiontd.game.ParticipantSelectionPlan;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.SemionGameManager;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public final class LegionIllusionLifecycleGameTest {
    @GameTest
    public void managerResetClearsPendingIllusionsBeforeSecondMatch(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        MinecraftServer server = context.getLevel().getServer();
        SemionGameManager manager = new SemionGameManager();
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            LegionIllusionSpawnQueue.clear();
            Path storePath = Files.createTempDirectory("legion-illusion-reset").resolve("profiles.json");
            manager.configure(
                    EconomyConfig.defaultConfig(),
                    new WaveConfig(List.of(), 20, null),
                    MapConfig.defaultConfig(),
                    ProgressionConfig.defaultConfig(),
                    storePath
            );
            UUID red = BuilderIntegrationGameTestSupport.stableUuid("legion-reset-red");
            UUID blue = BuilderIntegrationGameTestSupport.stableUuid("legion-reset-blue");
            SemionGame firstGame = manager.createGame(server);
            BuilderIntegrationGameTestSupport.require(firstGame.start(server, plan(red, blue, "first")),
                    "The first manager-owned match must start.");
            PlayerLane firstLane = firstGame.playerLane(red).orElseThrow();
            addPendingIllusions(firstLane, red);
            BuilderIntegrationGameTestSupport.require(pendingCloneCount() > 0,
                    "The first match must leave pending illusion work before manager reset.");

            BuilderIntegrationGameTestSupport.require(manager.resetToLobby(server),
                    "Manager reset must close the first active match.");
            BuilderIntegrationGameTestSupport.require(pendingCloneCount() == 0,
                    "Manager reset must clear every pending illusion from the previous match.");
            BuilderIntegrationGameTestSupport.require(queueTick() == 0,
                    "Manager reset must reset the global illusion scheduler tick."
            );

            SemionGame secondGame = manager.createGame(server);
            BuilderIntegrationGameTestSupport.require(secondGame.start(server, plan(red, blue, "second")),
                    "A second match with the reused players must start after reset.");
            PlayerLane secondLane = secondGame.playerLane(red).orElseThrow();
            int observationTicks = TowerBalanceRuntime.illusionCloneSpawnSpreadTicks() + 5;
            for (int tick = 0; tick < observationTicks; tick++) {
                LegionIllusionSpawnQueue.tick();
            }
            BuilderIntegrationGameTestSupport.require(pendingCloneCount() == 0,
                    "The second match must not inherit pending illusion work from the first match.");
            BuilderIntegrationGameTestSupport.require(countIllusionClones(secondLane) == 0,
                    "The second match must not receive a stale illusion clone from the first match.");
            context.succeed();
        } catch (Exception | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Legion illusion lifecycle failed: " + failure.getMessage()));
        } finally {
            manager.shutdown();
            LegionIllusionSpawnQueue.clear();
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
        Field field = LegionIllusionSpawnQueue.class.getDeclaredField("PENDING_CLONE_SPAWNS");
        field.setAccessible(true);
        NavigableMap<?, ?> pending = (NavigableMap<?, ?>) field.get(null);
        int count = 0;
        for (Object value : pending.values()) {
            count += ((Queue<?>) value).size();
        }
        return count;
    }

    private static int queueTick() throws ReflectiveOperationException {
        Field field = LegionIllusionSpawnQueue.class.getDeclaredField("currentTick");
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
