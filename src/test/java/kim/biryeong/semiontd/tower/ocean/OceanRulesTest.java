package kim.biryeong.semiontd.tower.ocean;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class OceanRulesTest {
    private static final double EPSILON = 0.0001;

    @Test
    void waterDamageUsesLinearRootBelowSoftCapAndLogarithmicExcessAboveIt() {
        assertEquals(0.0, OceanRules.waterRoot(-10.0, 1_000.0, 250.0), EPSILON);
        assertEquals(Math.sqrt(4.0), OceanRules.waterRoot(1_000.0, 1_000.0, 250.0), EPSILON);
        double effective = 1_000.0 + 1_000.0 * Math.log1p(9.0);
        assertEquals(Math.sqrt(effective / 250.0), OceanRules.waterRoot(10_000.0, 1_000.0, 250.0), EPSILON);
    }

    @Test
    void stackedSupplyClampsInvalidInputsAndPreservesGeometricDecay() {
        assertEquals(1.0, OceanRules.stackedSupplyMultiplier(0, 0.60), EPSILON);
        assertEquals(1.0 / 6.0, OceanRules.stackedSupplyMultiplier(6, 0.0), EPSILON);
        assertEquals(1.0, OceanRules.stackedSupplyMultiplier(6, 1.0), EPSILON);
        assertEquals(2.38336 / 6.0, OceanRules.stackedSupplyMultiplier(6, 0.60), EPSILON);
    }

    @Test
    void supplyEfficiencyIsFullAtSoftCapAndStopsAtThreshold() {
        assertEquals(1.0, OceanRules.supplyEfficiency(999.0, 1_000.0, 2_500.0), EPSILON);
        assertEquals(1.0, OceanRules.supplyEfficiency(1_000.0, 1_000.0, 2_500.0), EPSILON);
        assertEquals(0.5, OceanRules.supplyEfficiency(1_750.0, 1_000.0, 2_500.0), EPSILON);
        assertEquals(0.0, OceanRules.supplyEfficiency(2_500.0, 1_000.0, 2_500.0), EPSILON);
        assertEquals(0.0, OceanRules.supplyEfficiency(3_000.0, 1_000.0, 2_500.0), EPSILON);
    }
}
