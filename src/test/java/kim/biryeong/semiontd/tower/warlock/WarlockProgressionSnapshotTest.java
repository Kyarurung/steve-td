package kim.biryeong.semiontd.tower.warlock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WarlockProgressionSnapshotTest {
    private static final WarlockAwakeningProgress.Snapshot LOCKED =
            new WarlockAwakeningProgress.Snapshot(20, 1250, false);

    @Test
    void everyDerivedStatUsesItsConfiguredTotalOrRoundCount() {
        var progression = new WarlockProgressionSnapshot(20, 3, LOCKED);

        assertEquals(20, progression.lifeStealSacrificeCount(WarlockPath.BASE));
        assertEquals(20, progression.lifeStealSacrificeCount(WarlockPath.RANGED));
        assertEquals(3, progression.lifeStealSacrificeCount(WarlockPath.MELEE));

        assertEquals(20, progression.defenseSacrificeCount(WarlockPath.BASE));
        assertEquals(3, progression.defenseSacrificeCount(WarlockPath.RANGED));
        assertEquals(20, progression.defenseSacrificeCount(WarlockPath.MELEE));

        assertEquals(3, progression.splashSacrificeCount(WarlockPath.BASE));
        assertEquals(20, progression.splashSacrificeCount(WarlockPath.RANGED));
        assertEquals(3, progression.splashSacrificeCount(WarlockPath.MELEE));
    }

    @Test
    void snapshotNormalizesInvalidCountsAndKeepsAwakeningProgress() {
        var progression = new WarlockProgressionSnapshot(-10, -2, LOCKED);

        assertEquals(0, progression.totalSacrificeCount());
        assertEquals(0, progression.roundSacrificeCount());
        assertEquals(LOCKED, progression.awakening());
    }

    @Test
    void calculationRulesClampStacksAndAttackSpeedAtTheirCaps() {
        var stacks = new WarlockRules.StackRule(10, 0.005, 0.07);
        var combat = new WarlockRules.CombatRule(5, 15, 0.5);

        assertEquals(0.0, stacks.value(9), 0.0001);
        assertEquals(0.005, stacks.value(10), 0.0001);
        assertEquals(0.07, stacks.value(999), 0.0001);
        assertEquals(4, combat.meleeIntervalReduction(9));
        assertEquals(15, combat.meleeIntervalReduction(999));
    }
}
