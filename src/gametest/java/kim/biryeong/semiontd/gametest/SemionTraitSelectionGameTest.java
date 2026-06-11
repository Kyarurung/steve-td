package kim.biryeong.semiontd.gametest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import kim.biryeong.semiontd.config.EconomyConfig;
import kim.biryeong.semiontd.config.WaveConfig;
import kim.biryeong.semiontd.game.AssignedParticipant;
import kim.biryeong.semiontd.game.MatchMode;
import kim.biryeong.semiontd.game.ParticipantSelectionPlan;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.SemionGameManager;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.trait.BuiltInTraits;
import kim.biryeong.semiontd.trait.TraitLoadout;
import kim.biryeong.semiontd.trait.TraitSelectionConfig;
import kim.biryeong.semiontd.trait.TraitSelectionSession;
import kim.biryeong.semiontd.trait.TraitSlot;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public final class SemionTraitSelectionGameTest {
    @GameTest
    public void traitSelectionDelaysMatchStartUntilTimeout(GameTestHelper context) {
        MinecraftServer server = context.getLevel().getServer();
        UUID redId = playerId("trait-timeout-red");
        UUID blueId = playerId("trait-timeout-blue");
        SemionGame game = syntheticGame(context, EconomyConfig.defaultConfig());
        SemionGameManager manager = managerWithActiveGame(game);
        ParticipantSelectionPlan plan = twoPlayerPlan(redId, "trait-timeout-red", blueId, "trait-timeout-blue");

        if (!assertEquals(context, SemionGameManager.StartCountdownResult.SCHEDULED, manager.scheduleStart(server, plan), "Manager should schedule trait selection.")) {
            return;
        }
        if (!assertTrue(context, manager.traitSelectionActive(), "Trait selection should be active immediately after scheduling.")) {
            return;
        }
        if (!assertTrue(context, !game.rosterLocked(), "Trait selection should not lock the roster before timeout.")) {
            return;
        }

        for (int i = 0; i < TraitSelectionConfig.DEFAULT_DURATION_TICKS; i++) {
            manager.tick(server);
        }
        if (!assertTrue(context, !manager.traitSelectionActive(), "Trait selection should finish at timeout.")) {
            return;
        }
        if (!assertTrue(context, manager.startCountdownActive(), "Start countdown should begin after trait selection timeout.")) {
            return;
        }
        if (!assertTrue(context, !game.rosterLocked(), "Countdown should still delay the actual game start.")) {
            return;
        }
        for (int i = 0; i < SemionGameManager.START_COUNTDOWN_TICKS; i++) {
            manager.tick(server);
        }
        if (!assertTrue(context, game.rosterLocked(), "Game should start after trait timeout plus countdown.")) {
            return;
        }
        if (!assertEquals(context, TraitLoadout.none(), game.players().get(blueId).traitLoadout(), "Missing trait slots should default to none.")) {
            return;
        }
        context.succeed();
    }

    @GameTest
    public void traitSelectionStartsCountdownEarlyWhenAllActivePlayersComplete(GameTestHelper context) {
        MinecraftServer server = context.getLevel().getServer();
        UUID redId = playerId("trait-complete-red");
        UUID blueId = playerId("trait-complete-blue");
        SemionGame game = syntheticGame(context, EconomyConfig.defaultConfig());
        SemionGameManager manager = managerWithActiveGame(game);
        ParticipantSelectionPlan plan = new ParticipantSelectionPlan(
                MatchMode.NORMAL,
                List.of(
                        new AssignedParticipant(redId, "trait-complete-red", TeamId.RED, 1),
                        new AssignedParticipant(blueId, "trait-complete-blue", TeamId.BLUE, 1)
                ),
                Set.of(playerId("trait-complete-spectator")),
                2
        );

        if (!assertEquals(context, SemionGameManager.StartCountdownResult.SCHEDULED, manager.scheduleStart(server, plan), "Manager should schedule trait selection.")) {
            return;
        }
        completeNone(manager, server, redId);
        if (!assertTrue(context, manager.traitSelectionActive(), "One active participant missing should keep selection active.")) {
            return;
        }
        completeNone(manager, server, blueId);
        if (!assertTrue(context, !manager.traitSelectionActive(), "All active participants complete should finish trait selection.")) {
            return;
        }
        if (!assertTrue(context, manager.startCountdownActive(), "Start countdown should begin early when active participants complete.")) {
            return;
        }
        for (int i = 0; i < SemionGameManager.START_COUNTDOWN_TICKS; i++) {
            manager.tick(server);
        }
        if (!assertTrue(context, game.rosterLocked(), "Game should start after early trait completion countdown.")) {
            return;
        }
        context.succeed();
    }

    @GameTest
    public void primaryAndSecondaryTraitScaleStartingEconomy(GameTestHelper context) {
        MinecraftServer server = context.getLevel().getServer();
        UUID redId = playerId("trait-economy-primary");
        UUID blueId = playerId("trait-economy-secondary");
        EconomyConfig economy = EconomyConfig.defaultConfig();
        SemionGame game = syntheticGame(context, economy);
        SemionGameManager manager = managerWithActiveGame(game);
        ParticipantSelectionPlan plan = twoPlayerPlan(redId, "trait-economy-primary", blueId, "trait-economy-secondary");

        if (!assertEquals(context, SemionGameManager.StartCountdownResult.SCHEDULED, manager.scheduleStart(server, plan), "Manager should schedule trait selection.")) {
            return;
        }
        if (!assertEquals(context, TraitSelectionSession.SelectionResult.SELECTED, manager.selectTrait(server, redId, TraitSlot.PRIMARY, BuiltInTraits.STARTER_MINERAL_TRAINING_ID), "Primary trait selection should succeed.")) {
            return;
        }
        if (!assertEquals(context, TraitSelectionSession.SelectionResult.SELECTED, manager.selectTrait(server, redId, TraitSlot.SECONDARY, BuiltInTraits.NONE_ID), "Secondary none selection should succeed.")) {
            return;
        }
        if (!assertEquals(context, TraitSelectionSession.SelectionResult.SELECTED, manager.selectTrait(server, blueId, TraitSlot.PRIMARY, BuiltInTraits.NONE_ID), "Primary none selection should succeed.")) {
            return;
        }
        if (!assertEquals(context, TraitSelectionSession.SelectionResult.SELECTED, manager.selectTrait(server, blueId, TraitSlot.SECONDARY, BuiltInTraits.STARTER_MINERAL_TRAINING_ID), "Secondary trait selection should succeed.")) {
            return;
        }
        if (!assertEquals(context, BuiltInTraits.STARTER_MINERAL_TRAINING_ID, manager.traitLoadoutOrDefault(redId).primaryTraitId(), "Completed selection should remain visible during start countdown.")) {
            return;
        }
        if (!assertEquals(context, BuiltInTraits.STARTER_MINERAL_TRAINING_ID, manager.traitLoadoutOrDefault(blueId).secondaryTraitId(), "Completed secondary selection should remain visible during start countdown.")) {
            return;
        }
        for (int i = 0; i < SemionGameManager.START_COUNTDOWN_TICKS; i++) {
            manager.tick(server);
        }

        long baseDiamond = economy.startingDiamond();
        if (!assertEquals(context, baseDiamond + 100L, game.players().get(redId).economy().diamond(), "Primary trait should apply 100% starting diamond delta.")) {
            return;
        }
        if (!assertEquals(context, baseDiamond + 50L, game.players().get(blueId).economy().diamond(), "Secondary trait should apply 50% starting diamond delta.")) {
            return;
        }
        context.succeed();
    }

    @GameTest
    public void traitSelectionRejectsDuplicateNonNoneTraits(GameTestHelper context) {
        MinecraftServer server = context.getLevel().getServer();
        UUID redId = playerId("trait-duplicate-red");
        UUID blueId = playerId("trait-duplicate-blue");
        SemionGame game = syntheticGame(context, EconomyConfig.defaultConfig());
        SemionGameManager manager = managerWithActiveGame(game);
        if (!assertEquals(context, SemionGameManager.StartCountdownResult.SCHEDULED, manager.scheduleStart(server, twoPlayerPlan(redId, "trait-duplicate-red", blueId, "trait-duplicate-blue")), "Manager should schedule trait selection.")) {
            return;
        }
        if (!assertEquals(context, TraitSelectionSession.SelectionResult.SELECTED, manager.selectTrait(server, redId, TraitSlot.PRIMARY, BuiltInTraits.STARTER_MINERAL_TRAINING_ID), "Primary trait selection should succeed.")) {
            return;
        }
        if (!assertEquals(context, TraitSelectionSession.SelectionResult.DUPLICATE_TRAIT, manager.selectTrait(server, redId, TraitSlot.SECONDARY, BuiltInTraits.STARTER_MINERAL_TRAINING_ID), "Duplicate non-none trait should be rejected.")) {
            return;
        }
        context.succeed();
    }

    @GameTest
    public void traitCommandTreeRegistersKoreanAlias(GameTestHelper context) {
        var dispatcher = context.getLevel().getServer().getCommands().getDispatcher();
        if (!assertTrue(context, dispatcher.getRoot().getChild("특성") != null, "Expected /특성 alias to be registered.")) {
            return;
        }
        var source = context.getLevel().getServer().createCommandSourceStack();
        for (String command : List.of(
                "특성",
                "semiontd trait",
                "semiontd trait current",
                "semiontd trait list",
                "semiontd trait ui primary",
                "semiontd trait ui secondary",
                "특성 ui primary",
                "semiontd trait select primary none",
                "semiontd trait select secondary none"
        )) {
            var parsed = dispatcher.parse(command, source);
            if (!assertTrue(context, !parsed.getContext().getNodes().isEmpty() && !parsed.getReader().canRead(), "Expected command to parse completely: /" + command)) {
                return;
            }
        }
        context.succeed();
    }

    private static SemionGame syntheticGame(GameTestHelper context, EconomyConfig economyConfig) {
        return new SemionGame(economyConfig, WaveConfig.defaultConfig(), SyntheticArenaFactory.create(
                context.getLevel(),
                context.absolutePos(BlockPos.ZERO)
        ));
    }

    private static SemionGameManager managerWithActiveGame(SemionGame game) {
        SemionGameManager manager = new SemionGameManager();
        try {
            var field = SemionGameManager.class.getDeclaredField("activeGame");
            field.setAccessible(true);
            field.set(manager, game);
            return manager;
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Failed to set active game for trait selection test.", exception);
        }
    }

    private static ParticipantSelectionPlan twoPlayerPlan(UUID redId, String redName, UUID blueId, String blueName) {
        return new ParticipantSelectionPlan(
                MatchMode.NORMAL,
                List.of(
                        new AssignedParticipant(redId, redName, TeamId.RED, 1),
                        new AssignedParticipant(blueId, blueName, TeamId.BLUE, 1)
                ),
                Set.of(),
                2
        );
    }

    private static void completeNone(SemionGameManager manager, MinecraftServer server, UUID playerId) {
        manager.selectTrait(server, playerId, TraitSlot.PRIMARY, BuiltInTraits.NONE_ID);
        manager.selectTrait(server, playerId, TraitSlot.SECONDARY, BuiltInTraits.NONE_ID);
    }

    private static UUID playerId(String seed) {
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean assertTrue(GameTestHelper context, boolean condition, String message) {
        if (!condition) {
            context.fail(Component.literal(message));
            return false;
        }
        return true;
    }

    private static boolean assertEquals(GameTestHelper context, Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            context.fail(Component.literal(message + " expected=" + expected + " actual=" + actual));
            return false;
        }
        return true;
    }
}
