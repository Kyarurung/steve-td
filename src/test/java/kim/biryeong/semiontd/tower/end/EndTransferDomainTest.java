package kim.biryeong.semiontd.tower.end;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kim.biryeong.semiontd.tower.TowerCategory;
import kim.biryeong.semiontd.tower.TowerType;
import org.junit.jupiter.api.Test;

class EndTransferDomainTest extends EndTestFixture {
    @Test
    void progressFactorySnapshotsTheRuleForEachTowerLine() {
        EndConfig.TransferRule rule = new EndConfig.TransferRule(
                40,
                30.0,
                0.05,
                0.50,
                0.04,
                0.66,
                0.04
        );

        EndTransferState.Progress shulker = EndTransferFactory.create(
                EndTowers.T2_SHULKER_TOWER,
                rule
        );
        assertEquals(40, shulker.durationTicks);
        assertEquals(EndTowers.T2_SHULKER_TOWER.maxHealth() * 0.50, shulker.roundHealthBonus, 0.0001);
        assertEquals(EndTowers.T2_SHULKER_TOWER.maxHealth() * 0.04, shulker.permanentHealthBonus, 0.0001);
        assertEquals(EndTowers.T2_SHULKER_TOWER.maxHealth() * 0.05, shulker.periodicHealingPerSecond, 0.0001);
        assertEquals(0.0, shulker.roundDamageBonus, 0.0001);

        EndTransferState.Progress crystal = EndTransferFactory.create(
                EndTowers.T3_END_CRYSTAL_TOWER,
                rule
        );
        assertEquals(EndTowers.T3_END_CRYSTAL_TOWER.damage() * 0.66, crystal.roundDamageBonus, 0.0001);
        assertEquals(EndTowers.T3_END_CRYSTAL_TOWER.damage() * 0.04, crystal.permanentDamageBonus, 0.0001);
        assertEquals(0.0, crystal.roundHealthBonus, 0.0001);
        assertEquals(30.0, crystal.completionHealing, 0.0001);
    }

    @Test
    void stackStateChangesImmutablyAndKeepsPermanentLineCountsOnRoundReset() {
        EndTransferStacks empty = EndTransferStacks.EMPTY;
        EndTransferStacks shulker = empty.recordCompletion(EndTowers.T2_SHULKER_TOWER);
        EndTransferStacks mixed = shulker.recordCompletion(EndTowers.T3_END_CRYSTAL_TOWER);

        assertEquals(new EndTransferStacks(0, 0, 0), empty);
        assertEquals(new EndTransferStacks(2, 0, 1), shulker);
        assertEquals(new EndTransferStacks(2, 3, 2), mixed);
        assertEquals(new EndTransferStacks(2, 3, 0), mixed.resetRound());
        assertEquals(0.10, mixed.shulkerBonus(new EndConfig.StackRule(1, 0.05, 0.10)), 0.0001);
        assertEquals(0.20, mixed.endCrystalBonus(new EndConfig.StackRule(1, 0.10, 0.20)), 0.0001);
        assertEquals(9L, mixed.attackIntervalReduction(
                new EndConfig.AttackSpeedRule(1, 2, 5, 1),
                new EndConfig.RoundAttackSpeedRule(1, 2)
        ));
    }

    @Test
    void stackStateRejectsUnknownTransferFamilies() {
        TowerType unknown = new TowerType(
                "unknown_end_transfer",
                "Unknown End Transfer",
                TowerCategory.SUPPORT,
                0,
                100.0,
                0.0,
                0.0,
                20,
                0
        );

        assertThrows(IllegalArgumentException.class, () -> EndTransferStacks.EMPTY.recordCompletion(unknown));
    }

    @Test
    void snapshotSeparatesRawAccumulationFromResolvedScaling() {
        EndTransferSnapshot snapshot = new EndTransferSnapshot(
                new EndTransferStacks(2, 3, 4),
                5.0,
                10.0,
                5.0,
                5.0
        );
        EndTransferStats stats = snapshot.resolve(
                new EndConfig.ScalingRule(10.0, 10.0),
                new EndConfig.ScalingRule(5.0, 5.0)
        );

        assertEquals(2, stats.shulkerCount());
        assertEquals(3, stats.endCrystalCount());
        assertEquals(4, stats.roundCompletedCount());
        assertEquals(10.0, stats.permanentHealthBonus(), 0.0001);
        assertEquals(10.0 + 10.0 * Math.log1p(0.5), stats.totalHealthBonus(), 0.0001);
        assertEquals(5.0, stats.permanentDamageBonus(), 0.0001);
        assertEquals(5.0 + 5.0 * Math.log1p(1.0), stats.totalDamageBonus(), 0.0001);
    }

    @Test
    void zeroScaleHardCapsEndTransferBonusesAtTheThreshold() {
        EndTransferSnapshot snapshot = new EndTransferSnapshot(
                EndTransferStacks.EMPTY,
                200.0,
                50.0,
                200.0,
                50.0
        );
        EndTransferStats stats = snapshot.resolve(
                new EndConfig.ScalingRule(100.0, 0.0),
                new EndConfig.ScalingRule(150.0, 0.0)
        );

        assertEquals(50.0, stats.permanentHealthBonus(), 0.0001);
        assertEquals(100.0, stats.totalHealthBonus(), 0.0001);
        assertEquals(50.0, stats.permanentDamageBonus(), 0.0001);
        assertEquals(150.0, stats.totalDamageBonus(), 0.0001);
    }

    @Test
    void zeroThresholdExplicitlyDisablesEndTransferScaling() {
        EndConfig.ScalingRule disabled = new EndConfig.ScalingRule(0.0, 25.0);
        EndConfig.ScalingRule hardCap = new EndConfig.ScalingRule(150.0, 0.0);

        assertEquals(true, disabled.disabled());
        assertEquals(false, disabled.hardCap());
        assertEquals(0.0, disabled.apply(200.0), 0.0001);
        assertEquals(false, hardCap.disabled());
        assertEquals(true, hardCap.hardCap());
        assertEquals(150.0, hardCap.apply(200.0), 0.0001);
    }
}
