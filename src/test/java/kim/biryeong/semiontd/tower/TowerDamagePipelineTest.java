package kim.biryeong.semiontd.tower;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TowerDamagePipelineTest {
    @Test
    void traitDamageStaysPreCapButTowerFinalDamageAppliesAfterTheCap() {
        double baseDamage = 250.0;
        double damageWithPrimaryDoubleEdgedSword = baseDamage * 1.25;

        double outgoingDamage = Tower.applyOutgoingDamageStages(damageWithPrimaryDoubleEdgedSword, damage -> Math.min(250.0, damage), damage -> damage * 1.10);

        assertEquals(275.0, outgoingDamage, 0.0001);
    }
}
