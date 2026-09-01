package kim.biryeong.semiontd.tower.ancientcity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
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
}
