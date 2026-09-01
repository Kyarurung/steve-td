package kim.biryeong.semiontd.tower.nether;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NetherRulesTest {
    @Test
    void decayDamageUsesPerSecondRatioAndReduction() {
        assertEquals(1.0, NetherDecayController.decayDamage(100.0, 0.20, 0.0), 0.0001);
        assertEquals(0.5, NetherDecayController.decayDamage(100.0, 0.20, 0.50), 0.0001);
    }

    @Test
    void missingHealthBonusScalesAndStopsAtCap() {
        assertEquals(0.25, NetherCombat.cappedMissingHealthBonus(0.50, 0.50, 0.75), 0.0001);
        assertEquals(0.75, NetherCombat.cappedMissingHealthBonus(1.0, 1.0, 0.75), 0.0001);
    }
}
