package kim.biryeong.semiontd.tower.ancientcity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import kim.biryeong.semiontd.game.GridPosition;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

final class AncientCityTerritoryStateTest {
    private final UUID owner = UUID.nameUUIDFromBytes("ancient-state-owner".getBytes());

    @AfterEach
    void clearState() {
        AncientCityTerritoryStates.clearAllForTesting();
    }

    @Test
    void roundTransitionResetsDeathSpreadCounterExactlyOnce() {
        AncientCityTerritoryState state = AncientCityTerritoryStates.state(owner);
        state.beginRound(1);
        state.recordDeathSpread();
        state.recordDeathSpread();
        state.beginRound(1);
        assertEquals(2, state.deathSpreadsThisRound());
        state.beginRound(2);
        assertEquals(0, state.deathSpreadsThisRound());
    }

    @Test
    void matchLifecycleClearsPlayerStateAndPreventsSecondMatchLeak() {
        AncientCityTerritoryState firstMatch = AncientCityTerritoryStates.state(owner);
        firstMatch.recordDeathSpread();
        AncientCityTerritoryController.onMatchClosed(owner);
        assertEquals(0, AncientCityTerritoryStates.territoryCount(owner));
        AncientCityTerritoryState secondMatch = AncientCityTerritoryStates.state(owner);
        assertTrue(firstMatch != secondMatch);
        assertEquals(0, secondMatch.deathSpreadsThisRound());
    }

    @Test
    void membershipIndexTracksMainAndFinalDefenseColumnsRegardlessOfHeight() {
        AncientCityTerritoryState state = AncientCityTerritoryStates.state(owner);
        assertTrue(state.add(new BlockPos(4, 63, -7), true));
        assertTrue(state.add(new BlockPos(-2, 70, 9), false));
        assertTrue(state.contains(new GridPosition(4, 120, -7)));
        assertTrue(state.contains(new GridPosition(-2, -20, 9)));
        assertFalse(state.contains(new GridPosition(4, 63, -6)));
        assertEquals(1, state.territory().size());
        assertEquals(1, state.finalDefenseTerritory().size());
    }

    @Test
    void duplicatePositionDoesNotDivergeTerritoryAndMembershipIndex() {
        AncientCityTerritoryState state = AncientCityTerritoryStates.state(owner);
        BlockPos position = new BlockPos(3, 64, 5);
        assertTrue(state.add(position, true));
        assertFalse(state.add(position, true));
        assertEquals(1, state.territory().size());
        assertTrue(state.contains(GridPosition.from(position.above(10))));
    }

    @Test
    void territoryViewsCannotBypassMembershipIndex() {
        AncientCityTerritoryState state = AncientCityTerritoryStates.state(owner);
        assertThrows(UnsupportedOperationException.class,
                () -> state.territory().add(new BlockPos(1, 64, 1)));
        assertThrows(UnsupportedOperationException.class,
                () -> state.finalDefenseTerritory().add(new BlockPos(2, 64, 2)));
    }
}
