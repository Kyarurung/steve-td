package kim.biryeong.semiontd.tower.adversary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdversaryProgressStateTest {
    @Test
    void enhancedRivalKillsCountTwoWithoutChangingPastScore() {
        AdversaryProgressState state = new AdversaryProgressState();
        UUID rival = id("enhanced-breeze");

        state.registerRival(rival, RivalKind.BREEZE);
        state.recordRivalKill(rival, RivalKind.BREEZE, false);
        state.recordRivalKill(rival, RivalKind.BREEZE, true);

        assertEquals(4, state.score(RivalKind.BREEZE));
        assertEquals(4, state.contribution(rival));
    }

    @Test
    void evolutionConsumesSharedScoreAndFinalStageOnlyPaysTheRecipeDelta() {
        AdversaryProgressState state = new AdversaryProgressState();
        UUID fox = id("rapid-fox");
        state.registerFox(fox, FoxForm.BASE);
        state.reconcileRivals(contributions(Map.of(
                RivalKind.BREEZE, 50,
                RivalKind.POLAR_BEAR, 20
        )));

        assertTrue(state.canEvolve(fox, FoxForm.BASE, FoxForm.BREEZE));
        assertTrue(state.commitEvolution(fox, FoxForm.BASE, FoxForm.BREEZE));
        assertEquals(12, state.spentScore(RivalKind.BREEZE));
        assertEquals(38, state.availableScore(RivalKind.BREEZE));
        assertFalse(state.canEvolve(fox, FoxForm.BREEZE, FoxForm.SHIELD_BEARER));

        state.recordCompletedWave(fox, FoxForm.BREEZE);
        assertTrue(state.canEvolve(fox, FoxForm.BREEZE, FoxForm.SHIELD_BEARER));
        assertTrue(state.commitEvolution(fox, FoxForm.BREEZE, FoxForm.SHIELD_BEARER));
        assertEquals(30, state.spentScore(RivalKind.BREEZE));
        assertEquals(20, state.spentScore(RivalKind.POLAR_BEAR));
        assertEquals(Map.of(RivalKind.BREEZE, 18, RivalKind.POLAR_BEAR, 20),
                state.evolutionCost(FoxForm.BREEZE, FoxForm.SHIELD_BEARER));
    }

    @Test
    void routeClaimIsPerPlayerAndFoxSaleRefundsScoreAndReleasesIt() {
        AdversaryProgressState state = new AdversaryProgressState();
        UUID first = id("first-fox");
        UUID second = id("second-fox");
        state.registerFox(first, FoxForm.BASE);
        state.registerFox(second, FoxForm.BASE);
        state.reconcileRivals(contributions(Map.of(RivalKind.BREEZE, 24)));

        assertTrue(state.commitEvolution(first, FoxForm.BASE, FoxForm.BREEZE));
        assertFalse(state.canEvolve(second, FoxForm.BASE, FoxForm.BREEZE));
        assertEquals(first, state.routeOwner(FoxRoute.RAPID).orElseThrow());

        state.unregisterFox(first);

        assertEquals(0, state.spentScore(RivalKind.BREEZE));
        assertTrue(state.routeOwner(FoxRoute.RAPID).isEmpty());
        assertTrue(state.canEvolve(second, FoxForm.BASE, FoxForm.BREEZE));
    }

    @Test
    void demotionUsesNewestAffectedStepAndKeepsRouteAndFinalLocks() {
        AdversaryProgressState state = new AdversaryProgressState();
        UUID rapid = id("rapid");
        UUID control = id("control");
        state.registerFox(rapid, FoxForm.BASE);
        state.registerFox(control, FoxForm.BASE);
        state.reconcileRivals(contributions(Map.of(
                RivalKind.BREEZE, 50,
                RivalKind.PHANTOM, 14
        )));
        assertTrue(state.commitEvolution(rapid, FoxForm.BASE, FoxForm.BREEZE));
        state.recordCompletedWave(rapid, FoxForm.BREEZE);
        assertTrue(state.commitEvolution(rapid, FoxForm.BREEZE, FoxForm.GOLDEN_FANG));
        assertTrue(state.commitEvolution(control, FoxForm.BASE, FoxForm.BELL_KEEPER));

        List<AdversaryProgressState.FoxDemotion> demotions = state.reconcileRivals(List.of());

        assertEquals(List.of(control, rapid, rapid), demotions.stream()
                .map(AdversaryProgressState.FoxDemotion::foxId)
                .toList());
        var rapidProgress = state.foxProgress(rapid).orElseThrow();
        assertEquals(FoxForm.BASE, rapidProgress.currentForm());
        assertEquals(FoxRoute.RAPID, rapidProgress.lockedRoute().orElseThrow());
        assertEquals(FoxForm.GOLDEN_FANG, rapidProgress.lockedFinalForm().orElseThrow());

        state.reconcileRivals(contributions(Map.of(RivalKind.BREEZE, 50)));
        assertTrue(state.commitEvolution(rapid, FoxForm.BASE, FoxForm.BREEZE));
        assertTrue(state.canEvolve(rapid, FoxForm.BREEZE, FoxForm.GOLDEN_FANG));
        assertFalse(state.canEvolve(rapid, FoxForm.BREEZE, FoxForm.SHIELD_BEARER));
    }

    @Test
    void allFinalFoxesShareTheSameUnusedScorePool() {
        AdversaryProgressState state = new AdversaryProgressState();
        UUID fox = id("final-fox");
        state.registerFox(fox, FoxForm.BASE);
        state.reconcileRivals(contributions(Map.of(
                RivalKind.BREEZE, 60,
                RivalKind.CREEPER, 7,
                RivalKind.PHANTOM, 3
        )));
        assertTrue(state.commitEvolution(fox, FoxForm.BASE, FoxForm.BREEZE));
        state.recordCompletedWave(fox, FoxForm.BREEZE);
        assertTrue(state.commitEvolution(fox, FoxForm.BREEZE, FoxForm.GOLDEN_FANG));

        assertEquals(20, state.postEvolutionBonusScore());
    }

    @Test
    void allPublishedRecipesExactlyMatchTheApprovedRequirements() {
        assertRecipe(FoxForm.BREEZE, Map.of(RivalKind.BREEZE, 12));
        assertRecipe(FoxForm.BELL_KEEPER, Map.of(RivalKind.PHANTOM, 14));
        assertRecipe(FoxForm.TRACKER, Map.of(RivalKind.CREEPER, 16));
        assertRecipe(FoxForm.ECHO_FOX, Map.of(RivalKind.POLAR_BEAR, 18));
        assertRecipe(FoxForm.GOLDEN_FANG, Map.of(RivalKind.BREEZE, 50));
        assertRecipe(FoxForm.SHIELD_BEARER, Map.of(RivalKind.BREEZE, 30, RivalKind.POLAR_BEAR, 20));
        assertRecipe(FoxForm.BEACON_KEEPER, Map.of(RivalKind.PHANTOM, 50, RivalKind.POLAR_BEAR, 25));
        assertRecipe(FoxForm.OMINOUS_HEXER, Map.of(RivalKind.PHANTOM, 50, RivalKind.CREEPER, 30));
        assertRecipe(FoxForm.FIREWORK_PIERCER, Map.of(RivalKind.CREEPER, 60, RivalKind.BREEZE, 30));
        assertRecipe(FoxForm.BIG_GAME_TRACKER, Map.of(RivalKind.CREEPER, 60, RivalKind.POLAR_BEAR, 30));
        assertRecipe(FoxForm.MACE_EXECUTIONER, Map.of(RivalKind.POLAR_BEAR, 80, RivalKind.BREEZE, 40));
        assertRecipe(FoxForm.SCULK_CORE, Map.of(
                RivalKind.POLAR_BEAR, 100,
                RivalKind.PHANTOM, 50,
                RivalKind.CREEPER, 40
        ));
    }

    private static void assertRecipe(FoxForm form, Map<RivalKind, Integer> expected) {
        assertEquals(expected, form.recipe().orElseThrow().requirements());
    }

    private static List<RivalContribution> contributions(Map<RivalKind, Integer> scores) {
        EnumMap<RivalKind, Integer> ordered = new EnumMap<>(RivalKind.class);
        ordered.putAll(scores);
        List<RivalContribution> result = new ArrayList<>();
        ordered.forEach((kind, score) -> result.add(new RivalContribution(id(kind.name()), kind, score)));
        return result;
    }

    private static UUID id(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
