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
}
