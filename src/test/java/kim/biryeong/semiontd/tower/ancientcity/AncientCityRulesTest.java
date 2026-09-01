package kim.biryeong.semiontd.tower.ancientcity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class AncientCityRulesTest {
    private static final double EPSILON = 0.0001;

    @Test
    void resonanceClampsCountsThresholdsAndCap() {
        assertEquals(0.0, AncientCityRules.resonanceBonus(-1, 256, 224, 2.25), EPSILON);
        assertEquals(0.5625, AncientCityRules.resonanceBonus(56, 256, 224, 2.25), EPSILON);
        assertEquals(2.25, AncientCityRules.resonanceBonus(224, 256, 224, 2.25), EPSILON);
        assertEquals(2.25, AncientCityRules.resonanceBonus(999, 256, 224, 2.25), EPSILON);
        assertEquals(2.25, AncientCityRules.resonanceBonus(256, 256, 999, 2.25), EPSILON);
    }

    @Test
    void combinedAndIncomeMagicDamageClampInvalidMultipliers() {
        assertEquals(0.0, AncientCityRules.combinedMagicBonus(-1.0, 2.55), EPSILON);
        assertEquals(2.55, AncientCityRules.combinedMagicBonus(9.0, 2.55), EPSILON);
        assertEquals(10.0, AncientCityRules.incomeAdjustedMagicDamage(10.0, false, 1.75), EPSILON);
        assertEquals(17.5, AncientCityRules.incomeAdjustedMagicDamage(10.0, true, 1.75), EPSILON);
        assertEquals(0.0, AncientCityRules.incomeAdjustedMagicDamage(10.0, true, -1.0), EPSILON);
    }
}
