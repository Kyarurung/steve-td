package kim.biryeong.semiontd.tower;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DamageScalingTest {
    @Test
    void logarithmicBonusPreservesTheLinearRangeWithoutAHardCap() {
        assertEquals(0.0, DamageScaling.logarithmicBonus(-1.0, 120.0), 0.0001);
        assertEquals(119.0, DamageScaling.logarithmicBonus(119.0, 120.0), 0.0001);
        assertEquals(120.0, DamageScaling.logarithmicBonus(120.0, 120.0), 0.0001);
        assertEquals(239.1902, DamageScaling.logarithmicBonus(324.0, 120.0), 0.0001);
        assertEquals(271.9486, DamageScaling.logarithmicBonus(300.0, 180.0), 0.0001);
        assertEquals(396.7151, DamageScaling.logarithmicBonus(600.0, 180.0), 0.0001);
        assertEquals(488.6637, DamageScaling.logarithmicBonus(1_000.0, 180.0), 0.0001);
    }

    @Test
    void logarithmicBonusSupportsAnIndependentThresholdAndScale() {
        assertEquals(150.0, DamageScaling.logarithmicBonus(150.0, 150.0, 25.0), 0.0001);
        assertEquals(201.8607, DamageScaling.logarithmicBonus(324.0, 150.0, 25.0), 0.0001);
        assertEquals(223.6109, DamageScaling.logarithmicBonus(600.0, 150.0, 25.0), 0.0001);
        assertEquals(3972.9551, DamageScaling.logarithmicBonus(6_000.0, 3_000.0, 500.0), 0.0001);
        assertEquals(4354.0251, DamageScaling.logarithmicBonus(10_000.0, 3_000.0, 500.0), 0.0001);
    }
}
