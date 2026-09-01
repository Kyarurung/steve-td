package kim.biryeong.semiontd.tower.resonance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import kim.biryeong.semiontd.game.GridPosition;
import org.junit.jupiter.api.Test;

class ResonanceRulesTest {
    @Test
    void levelUsesConfiguredThresholdsAndMaximumTier() {
        assertEquals(0, ResonanceRules.level(0, 3, 1, 3, 5));
        assertEquals(1, ResonanceRules.level(2, 3, 1, 3, 5));
        assertEquals(2, ResonanceRules.level(4, 3, 1, 3, 5));
        assertEquals(3, ResonanceRules.level(5, 3, 1, 3, 5));
        assertEquals(1, ResonanceRules.level(6, 1, 1, 3, 5));
    }

    @Test
    void combatMathClampsIntervalsAndBonuses() {
        assertEquals(10, ResonanceRules.adjustedAttackInterval(20, 1.0));
        assertEquals(1, ResonanceRules.adjustedAttackInterval(1, 100.0));
        assertEquals(180.0, ResonanceRules.adjustedDamage(100.0, 0.8));
        assertEquals(60.0, ResonanceRules.reducedDamage(100.0, 0.4));
    }

    @Test
    void distanceUsesChebyshevGridDistanceAndRejectsMissingPositions() {
        assertEquals(3, ResonanceRules.distance(new GridPosition(0, 0, 0), new GridPosition(3, 2, 1)));
        assertEquals(Integer.MAX_VALUE, ResonanceRules.distance(null, new GridPosition(0, 0, 0)));
    }
}
