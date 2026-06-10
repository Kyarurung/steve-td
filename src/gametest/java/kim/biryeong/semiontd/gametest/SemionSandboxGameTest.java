package kim.biryeong.semiontd.gametest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import kim.biryeong.semiontd.config.EconomyConfig;
import kim.biryeong.semiontd.config.MapConfig;
import kim.biryeong.semiontd.config.ProgressionConfig;
import kim.biryeong.semiontd.config.SummonConfig;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.WaveConfig;
import kim.biryeong.semiontd.game.AssignedParticipant;
import kim.biryeong.semiontd.game.MatchMode;
import kim.biryeong.semiontd.game.ParticipantSelectionPlan;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.SemionGameManager;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.game.TowerPlacementResult;
import kim.biryeong.semiontd.map.GameArena;
import kim.biryeong.semiontd.summon.SummonResult;
import kim.biryeong.semiontd.summon.SummonResultType;
import kim.biryeong.semiontd.tower.ProductionTowerService;
import kim.biryeong.semiontd.ui.SemionHotbarService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class SemionSandboxGameTest {
    @GameTest
    public void koreanSandboxAliasCommandTreeMirrorsSandboxSubcommands(GameTestHelper context) {
        var dispatcher = context.getLevel().getServer().getCommands().getDispatcher();
        var alias = dispatcher.getRoot().getChild("샌드박스");
        if (!assertTrue(context, alias != null, "Expected /샌드박스 alias to be registered.")) {
            return;
        }
        if (!assertTrue(context, alias.getCommand() != null, "Expected bare /샌드박스 to start sandbox like /semiontd sandbox.")) {
            return;
        }
        for (String child : List.of("start", "reset", "leave", "give", "money")) {
            if (!assertTrue(context, alias.getChild(child) != null, "Expected /샌드박스 " + child + " subcommand to be registered.")) {
                return;
            }
        }
        CommandSourceStack source = context.getLevel().getServer().createCommandSourceStack();
        for (String command : List.of(
                "샌드박스",
                "샌드박스 start",
                "샌드박스 reset",
                "샌드박스 leave",
                "샌드박스 give diamond 10",
                "샌드박스 give emerald 10",
                "샌드박스 give income 10",
                "샌드박스 money diamond 10",
                "샌드박스 money emerald 10",
                "샌드박스 money income 10"
        )) {
            var parsed = dispatcher.parse(command, source);
            if (!assertTrue(context, !parsed.getContext().getNodes().isEmpty() && !parsed.getReader().canRead(), "Expected command to parse completely: /" + command)) {
                return;
            }
        }
        context.succeed();
    }

    @GameTest
    public void sandboxSessionRunsSeparateFromActiveGame(GameTestHelper context) {
        SemionGameManager manager = new SemionGameManager();
        try {
            configureManager(manager);
            MinecraftServer server = context.getLevel().getServer();
            SemionGame activeGame = manager.createGame(server);
            UUID activeRedId = uuid("sandbox-active-red");
            UUID activeBlueId = uuid("sandbox-active-blue");
            ParticipantSelectionPlan activePlan = new ParticipantSelectionPlan(
                    MatchMode.TEST,
                    List.of(
                            new AssignedParticipant(activeRedId, "active-red", TeamId.RED, 1),
                            new AssignedParticipant(activeBlueId, "active-blue", TeamId.BLUE, 1)
                    ),
                    Set.of(),
                    2
            );
            if (!assertTrue(context, activeGame.start(server, activePlan), "Active match should start before sandbox.")) {
                return;
            }

            UUID sandboxOwnerId = uuid("sandbox-owner");
            GameArena sandboxArena = SyntheticArenaFactory.create(context.getLevel(), context.absolutePos(BlockPos.ZERO));
            SemionGameManager.SandboxStartResult startResult = manager.startSandbox(
                    server,
                    sandboxOwnerId,
                    "sandbox-owner",
                    sandboxArena
            );
            if (!assertEquals(context, SemionGameManager.SandboxStartResult.STARTED, startResult, "Non-participant should be able to start a sandbox.")) {
                return;
            }
            SemionGame sandboxGame = manager.sandboxGame(sandboxOwnerId).orElse(null);
            if (!assertTrue(context, sandboxGame != null, "Sandbox game should be registered for its owner.")) {
                return;
            }
            if (!assertTrue(context, sandboxGame != activeGame, "Sandbox game instance must be separate from the active match.")) {
                return;
            }
            if (!assertTrue(context, manager.activeGame().orElseThrow() == activeGame, "Starting sandbox must not replace the active match.")) {
                return;
            }
            if (!assertTrue(context, !activeGame.players().containsKey(sandboxOwnerId), "Sandbox owner must not be added to active match players.")) {
                return;
            }
            if (!assertTrue(context, sandboxGame.players().containsKey(sandboxOwnerId), "Sandbox owner should be a sandbox participant.")) {
                return;
            }
            if (!assertTrue(context, manager.playableGame(sandboxOwnerId).orElseThrow() == sandboxGame, "Sandbox owner commands should resolve to sandbox game.")) {
                return;
            }
            if (!assertTrue(context, manager.playableGame(activeRedId).orElseThrow() == activeGame, "Active participant commands should still resolve to active match.")) {
                return;
            }

            String starterTowerId = ProductionTowerService.availableTowers(sandboxGame, sandboxOwnerId).stream()
                    .findFirst()
                    .orElseThrow()
                    .type()
                    .id();
            PlayerLane sandboxLane = sandboxGame.playerLane(sandboxOwnerId).orElseThrow();
            BlockPos towerPos = sandboxLane.laneLayout().laneArea().min();
            TowerPlacementResult placement = ProductionTowerService.placeTower(sandboxGame, sandboxOwnerId, towerPos, starterTowerId);
            if (!assertEquals(context, TowerPlacementResult.SUCCESS, placement, "Sandbox tower placement should succeed.")) {
                return;
            }
            if (!assertEquals(context, 1, sandboxGame.towerCount(sandboxOwnerId), "Sandbox should count its placed tower.")) {
                return;
            }
            if (!assertEquals(context, 0, activeGame.towerCount(activeRedId), "Sandbox tower placement must not mutate the active match tower count.")) {
                return;
            }

            long activeIncomeBeforeSandboxSummon = activeGame.players().get(activeRedId).economy().income();
            SummonResult summon = sandboxGame.summonMonster(sandboxOwnerId, "chicken");
            if (!assertEquals(context, SummonResultType.SUCCESS, summon.type(), "Sandbox income summon should use the sandbox game only.")) {
                return;
            }
            if (!assertTrue(context, summon.targetTeam().filter(TeamId.RED::equals).isPresent(), "Sandbox income should route back to the sandbox owner's own team.")) {
                return;
            }
            if (!assertEquals(context, sandboxOwnerId, lane(sandboxGame, TeamId.RED, 1).ownerPlayer(), "Sandbox self-routed income should enter the owner's own lane.")) {
                return;
            }
            if (!assertEquals(context, activeIncomeBeforeSandboxSummon, activeGame.players().get(activeRedId).economy().income(), "Sandbox income summon must not change active match economy.")) {
                return;
            }

            if (!assertEquals(
                    context,
                    SemionGameManager.SandboxStartResult.PLAYER_IN_MATCH,
                    manager.startSandbox(server, activeRedId, "active-red", SyntheticArenaFactory.create(context.getLevel(), context.absolutePos(new BlockPos(60, 0, 0)))),
                    "Active match participants must not be able to start a sandbox."
            )) {
                return;
            }
            if (!assertTrue(context, manager.sandboxGame(activeRedId).isEmpty(), "Denied active participant sandbox should not be registered.")) {
                return;
            }

            if (!assertTrue(context, manager.stopSandbox(sandboxOwnerId), "Stopping sandbox should remove the session.")) {
                return;
            }
            if (!assertTrue(context, manager.sandboxGame(sandboxOwnerId).isEmpty(), "Stopped sandbox should not remain registered.")) {
                return;
            }
            if (!assertTrue(context, manager.activeGame().orElseThrow() == activeGame, "Stopping sandbox must not close active match.")) {
                return;
            }
            context.succeed();
        } catch (Exception exception) {
            context.fail(Component.literal("Sandbox isolation test failed: " + exception.getMessage()));
        } finally {
            manager.shutdown();
        }
    }

    @GameTest
    public void sandboxHotbarToolsOpenSandboxUi(GameTestHelper context) {
        SemionGameManager manager = new SemionGameManager();
        try {
            configureManager(manager);
            MinecraftServer server = context.getLevel().getServer();
            var player = context.makeMockServerPlayerInLevel();
            GameArena sandboxArena = SyntheticArenaFactory.create(context.getLevel(), context.absolutePos(BlockPos.ZERO));
            SemionGameManager.SandboxStartResult startResult = manager.startSandbox(
                    server,
                    player.getUUID(),
                    player.getGameProfile().getName(),
                    sandboxArena
            );
            if (!assertEquals(context, SemionGameManager.SandboxStartResult.STARTED, startResult, "Mock player should be able to start sandbox.")) {
                return;
            }
            if (!assertTrue(context, player.getInventory().getItem(0).is(Items.COMPASS), "Sandbox should grant tower compass.")) {
                return;
            }
            if (!assertTrue(context, player.getInventory().getItem(1).is(Items.ECHO_SHARD), "Sandbox should grant income summon item.")) {
                return;
            }

            player.setItemInHand(InteractionHand.MAIN_HAND, player.getInventory().getItem(0));
            InteractionResult towerResult = invokeHotbarUse(manager, player, player.level());
            if (!assertEquals(context, InteractionResult.SUCCESS, towerResult, "Tower compass should open the sandbox tower UI.")) {
                return;
            }

            player.setItemInHand(InteractionHand.MAIN_HAND, player.getInventory().getItem(1));
            InteractionResult summonResult = invokeHotbarUse(manager, player, player.level());
            if (!assertEquals(context, InteractionResult.SUCCESS, summonResult, "Income item should open the sandbox summon UI.")) {
                return;
            }
            context.succeed();
        } catch (Exception exception) {
            context.fail(Component.literal("Sandbox hotbar UI test failed: " + exception.getMessage()));
        } finally {
            manager.shutdown();
        }
    }

    @GameTest
    public void sandboxStartsWithNormalEconomyAndCanGrantCurrency(GameTestHelper context) {
        SemionGameManager manager = new SemionGameManager();
        try {
            configureManager(manager);
            MinecraftServer server = context.getLevel().getServer();
            UUID sandboxOwnerId = uuid("sandbox-economy-owner");
            GameArena sandboxArena = SyntheticArenaFactory.create(context.getLevel(), context.absolutePos(BlockPos.ZERO));
            SemionGameManager.SandboxStartResult startResult = manager.startSandbox(
                    server,
                    sandboxOwnerId,
                    "sandbox-economy-owner",
                    sandboxArena
            );
            if (!assertEquals(context, SemionGameManager.SandboxStartResult.STARTED, startResult, "Sandbox should start for economy test.")) {
                return;
            }
            SemionGame sandboxGame = manager.sandboxGame(sandboxOwnerId).orElseThrow();
            var economy = sandboxGame.players().get(sandboxOwnerId).economy();
            EconomyConfig defaultEconomy = EconomyConfig.defaultConfig();
            if (!assertEquals(context, defaultEconomy.startingDiamond(), economy.diamond(), "Sandbox should use normal starting diamond, not infinite resources.")) {
                return;
            }
            if (!assertEquals(context, defaultEconomy.startingEmerald(), economy.emerald(), "Sandbox should use normal starting emerald, not infinite resources.")) {
                return;
            }
            if (!assertEquals(context, defaultEconomy.startingIncome(), economy.income(), "Sandbox should use normal starting income.")) {
                return;
            }

            if (!assertTrue(context, manager.grantSandboxCurrency(sandboxOwnerId, SemionGameManager.SandboxCurrency.DIAMOND, 123), "Sandbox diamond grant command backend should succeed.")) {
                return;
            }
            if (!assertTrue(context, manager.grantSandboxCurrency(sandboxOwnerId, SemionGameManager.SandboxCurrency.EMERALD, 45), "Sandbox emerald grant command backend should succeed.")) {
                return;
            }
            if (!assertTrue(context, manager.grantSandboxCurrency(sandboxOwnerId, SemionGameManager.SandboxCurrency.INCOME, 6), "Sandbox income grant command backend should succeed.")) {
                return;
            }
            if (!assertEquals(context, defaultEconomy.startingDiamond() + 123, economy.diamond(), "Sandbox diamond grant should add to the sandbox owner only.")) {
                return;
            }
            if (!assertEquals(context, defaultEconomy.startingEmerald() + 45, economy.emerald(), "Sandbox emerald grant should add to the sandbox owner only.")) {
                return;
            }
            if (!assertEquals(context, defaultEconomy.startingIncome() + 6, economy.income(), "Sandbox income grant should add to the sandbox owner only.")) {
                return;
            }
            context.succeed();
        } catch (Exception exception) {
            context.fail(Component.literal("Sandbox economy test failed: " + exception.getMessage()));
        } finally {
            manager.shutdown();
        }
    }

    @GameTest
    public void sandboxEliminationDoesNotSpectateOwnerOrBroadcastMatchFlow(GameTestHelper context) {
        SemionGameManager manager = new SemionGameManager();
        try {
            configureManager(manager);
            MinecraftServer server = context.getLevel().getServer();
            var player = context.makeMockServerPlayerInLevel();
            GameArena sandboxArena = SyntheticArenaFactory.create(context.getLevel(), context.absolutePos(BlockPos.ZERO));
            SemionGameManager.SandboxStartResult startResult = manager.startSandbox(
                    server,
                    player.getUUID(),
                    player.getGameProfile().getName(),
                    sandboxArena
            );
            if (!assertEquals(context, SemionGameManager.SandboxStartResult.STARTED, startResult, "Sandbox should start for elimination test.")) {
                return;
            }
            SemionGame sandboxGame = manager.sandboxGame(player.getUUID()).orElseThrow();
            if (!assertTrue(context, player.getInventory().getItem(0).is(Items.COMPASS), "Sandbox owner should start with tower compass.")) {
                return;
            }

            if (!assertTrue(context, sandboxGame.killBoss(server, TeamId.RED), "Sandbox owner team boss should be killable for regression setup.")) {
                return;
            }
            if (!assertTrue(context, !sandboxGame.isMatchSpectator(player.getUUID()), "Sandbox owner elimination should not run real-match spectator conversion.")) {
                return;
            }
            if (!assertTrue(context, player.getInventory().getItem(0).is(Items.COMPASS), "Sandbox owner should keep sandbox tools after sandbox-only elimination.")) {
                return;
            }
            if (!assertTrue(context, manager.leaveSandbox(server, player), "Sandbox should still be leaveable after a sandbox-only elimination.")) {
                return;
            }
            if (!assertTrue(context, manager.sandboxGame(player.getUUID()).isEmpty(), "Leaving should remove sandbox after elimination.")) {
                return;
            }
            context.succeed();
        } catch (Exception exception) {
            context.fail(Component.literal("Sandbox elimination test failed: " + exception.getMessage()));
        } finally {
            manager.shutdown();
        }
    }

    @GameTest
    public void sandboxCurrencyGrantDoesNotAffectActiveMatch(GameTestHelper context) {
        SemionGameManager manager = new SemionGameManager();
        try {
            configureManager(manager);
            MinecraftServer server = context.getLevel().getServer();
            SemionGame activeGame = manager.createGame(server);
            UUID activeRedId = uuid("sandbox-currency-active-red");
            UUID activeBlueId = uuid("sandbox-currency-active-blue");
            if (!assertTrue(context, activeGame.start(server, new ParticipantSelectionPlan(
                    MatchMode.TEST,
                    List.of(
                            new AssignedParticipant(activeRedId, "active-red", TeamId.RED, 1),
                            new AssignedParticipant(activeBlueId, "active-blue", TeamId.BLUE, 1)
                    ),
                    Set.of(),
                    2
            )), "Active match should start for currency isolation test.")) {
                return;
            }
            long activeDiamondBefore = activeGame.players().get(activeRedId).economy().diamond();
            if (!assertTrue(context, !manager.grantSandboxCurrency(activeRedId, SemionGameManager.SandboxCurrency.DIAMOND, 999), "Active match player without sandbox should not receive sandbox currency.")) {
                return;
            }
            if (!assertEquals(context, activeDiamondBefore, activeGame.players().get(activeRedId).economy().diamond(), "Failed sandbox grant must not mutate active match economy.")) {
                return;
            }

            UUID sandboxOwnerId = uuid("sandbox-currency-owner");
            SemionGameManager.SandboxStartResult startResult = manager.startSandbox(
                    server,
                    sandboxOwnerId,
                    "sandbox-currency-owner",
                    SyntheticArenaFactory.create(context.getLevel(), context.absolutePos(new BlockPos(100, 0, 0)))
            );
            if (!assertEquals(context, SemionGameManager.SandboxStartResult.STARTED, startResult, "Non-participant sandbox should start beside active match.")) {
                return;
            }
            if (!assertTrue(context, manager.grantSandboxCurrency(sandboxOwnerId, SemionGameManager.SandboxCurrency.DIAMOND, 999), "Sandbox owner should receive sandbox currency.")) {
                return;
            }
            if (!assertEquals(context, activeDiamondBefore, activeGame.players().get(activeRedId).economy().diamond(), "Sandbox currency grant must not mutate active match economy.")) {
                return;
            }
            context.succeed();
        } catch (Exception exception) {
            context.fail(Component.literal("Sandbox currency isolation test failed: " + exception.getMessage()));
        } finally {
            manager.shutdown();
        }
    }

    @GameTest
    public void sandboxResetRestoresNormalEconomy(GameTestHelper context) {
        SemionGameManager manager = new SemionGameManager();
        try {
            configureManager(manager);
            MinecraftServer server = context.getLevel().getServer();
            UUID sandboxOwnerId = uuid("sandbox-reset-economy-owner");
            SemionGameManager.SandboxStartResult startResult = manager.startSandbox(
                    server,
                    sandboxOwnerId,
                    "sandbox-reset-economy-owner",
                    SyntheticArenaFactory.create(context.getLevel(), context.absolutePos(BlockPos.ZERO))
            );
            if (!assertEquals(context, SemionGameManager.SandboxStartResult.STARTED, startResult, "Sandbox should start before reset.")) {
                return;
            }
            if (!assertTrue(context, manager.grantSandboxCurrency(sandboxOwnerId, SemionGameManager.SandboxCurrency.DIAMOND, 999), "Sandbox grant before reset should succeed.")) {
                return;
            }
            SemionGameManager.SandboxStartResult resetResult = manager.startSandbox(
                    server,
                    sandboxOwnerId,
                    "sandbox-reset-economy-owner",
                    SyntheticArenaFactory.create(context.getLevel(), context.absolutePos(new BlockPos(200, 0, 0)))
            );
            if (!assertEquals(context, SemionGameManager.SandboxStartResult.REPLACED, resetResult, "Starting sandbox again should replace the old session.")) {
                return;
            }
            var economy = manager.sandboxGame(sandboxOwnerId).orElseThrow().players().get(sandboxOwnerId).economy();
            if (!assertEquals(context, EconomyConfig.defaultConfig().startingDiamond(), economy.diamond(), "Reset sandbox should restore normal starting diamond instead of carrying grants.")) {
                return;
            }
            if (!assertEquals(context, EconomyConfig.defaultConfig().startingEmerald(), economy.emerald(), "Reset sandbox should restore normal starting emerald.")) {
                return;
            }
            context.succeed();
        } catch (Exception exception) {
            context.fail(Component.literal("Sandbox reset economy test failed: " + exception.getMessage()));
        } finally {
            manager.shutdown();
        }
    }

    private static void configureManager(SemionGameManager manager) throws Exception {
        Path tempDir = Files.createTempDirectory("semion-sandbox-gametest");
        manager.configure(
                EconomyConfig.defaultConfig(),
                WaveConfig.defaultConfig(),
                MapConfig.defaultConfig(),
                ProgressionConfig.defaultConfig(),
                TowerBalanceConfig.defaultConfig(),
                SummonConfig.defaultConfig(),
                tempDir.resolve("progression.json")
        );
    }

    private static UUID uuid(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }

    private static InteractionResult invokeHotbarUse(SemionGameManager manager, Player player, Level world) throws Exception {
        java.lang.reflect.Method method = SemionHotbarService.class.getDeclaredMethod(
                "handleUse",
                SemionGameManager.class,
                Player.class,
                Level.class,
                InteractionHand.class
        );
        method.setAccessible(true);
        return (InteractionResult) method.invoke(null, manager, player, world, InteractionHand.MAIN_HAND);
    }

    private static PlayerLane lane(SemionGame game, TeamId teamId, int laneId) {
        return game.teams().get(teamId).laneGroup().lane(laneId).orElseThrow();
    }

    private static boolean assertTrue(GameTestHelper context, boolean condition, String message) {
        if (!condition) {
            context.fail(Component.literal(message));
            return false;
        }
        return true;
    }

    private static <T> boolean assertEquals(GameTestHelper context, T expected, T actual, String message) {
        if (!expected.equals(actual)) {
            context.fail(Component.literal(message + " expected=" + expected + " actual=" + actual));
            return false;
        }
        return true;
    }
}
