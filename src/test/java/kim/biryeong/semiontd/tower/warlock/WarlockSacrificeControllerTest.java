package kim.biryeong.semiontd.tower.warlock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WarlockSacrificeControllerTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void eligibilityRejectsDeadCoreForeignAndOutOfRangeTargets() {
        UUID owner = UUID.randomUUID();
        WarlockTower warlock = new WarlockTower(
                WarlockTowers.RANGED_WARLOCK_TOWER,
                owner,
                TeamId.RED,
                0,
                new GridPosition(0, 0, 0)
        );
        WarlockSacrificeTower valid = sacrifice(WarlockTowers.T1_RANGED_SLAVE, owner, new GridPosition(2, 0, 0));
        WarlockSacrificeTower foreign = sacrifice(
                WarlockTowers.T1_RANGED_SLAVE,
                UUID.randomUUID(),
                new GridPosition(1, 0, 0)
        );
        WarlockSacrificeTower distant = sacrifice(
                WarlockTowers.T1_RANGED_SLAVE,
                owner,
                new GridPosition(6, 0, 0)
        );
        WarlockTower otherCore = new WarlockTower(
                WarlockTowers.BASE_WARLOCK_TOWER,
                owner,
                TeamId.RED,
                0,
                new GridPosition(1, 0, 0)
        );

        assertTrue(WarlockSacrificeController.isEligibleTarget(warlock, valid, 5.0));
        assertFalse(WarlockSacrificeController.isEligibleTarget(warlock, warlock, 5.0));
        assertFalse(WarlockSacrificeController.isEligibleTarget(warlock, otherCore, 5.0));
        assertFalse(WarlockSacrificeController.isEligibleTarget(warlock, foreign, 5.0));
        assertFalse(WarlockSacrificeController.isEligibleTarget(warlock, distant, 5.0));

        valid.syncHealth(0.0);
        assertFalse(WarlockSacrificeController.isEligibleTarget(warlock, valid, 5.0));
        assertFalse(WarlockSacrificeController.isEligibleTarget(warlock, null, 5.0));
        assertFalse(WarlockSacrificeController.isEligibleTarget(null, valid, 5.0));
    }

    @Test
    void specializedPathsOnlyAcceptTheirOwnSacrificeLine() {
        UUID owner = UUID.randomUUID();
        WarlockTower ranged = warlock(WarlockTowers.RANGED_WARLOCK_TOWER, owner);
        WarlockTower melee = warlock(WarlockTowers.MELEE_WARLOCK_TOWER, owner);
        WarlockTower base = warlock(WarlockTowers.BASE_WARLOCK_TOWER, owner);
        WarlockSacrificeTower rangedPet = sacrifice(
                WarlockTowers.T1_RANGED_SLAVE,
                owner,
                new GridPosition(1, 0, 0)
        );
        WarlockSacrificeTower meleePet = sacrifice(
                WarlockTowers.T1_SLAVE,
                owner,
                new GridPosition(1, 0, 0)
        );

        assertTrue(WarlockSacrificeController.isEligibleTarget(ranged, rangedPet, 5.0));
        assertFalse(WarlockSacrificeController.isEligibleTarget(ranged, meleePet, 5.0));
        assertTrue(WarlockSacrificeController.isEligibleTarget(melee, meleePet, 5.0));
        assertFalse(WarlockSacrificeController.isEligibleTarget(melee, rangedPet, 5.0));
        assertTrue(WarlockSacrificeController.isEligibleTarget(base, rangedPet, 5.0));
        assertTrue(WarlockSacrificeController.isEligibleTarget(base, meleePet, 5.0));
    }

    @Test
    void rangedDamageReductionActivatesAtFifteenPercentAfterThreshold() {
        WarlockState state = new WarlockState();
        WarlockSacrificeController sacrifice = new WarlockSacrificeController(WarlockConfig.RUNTIME, state);
        WarlockTower ranged = new WarlockTower(
                WarlockTowers.RANGED_WARLOCK_TOWER,
                UUID.randomUUID(),
                TeamId.RED,
                0,
                new GridPosition(0, 0, 0)
        );

        for (int count = 0; count < 3; count++) {
            state.absorbForRound(0.0, 0.0, 0.0);
        }
        assertEquals(0.0, sacrifice.damageReduction(WarlockPath.RANGED), 0.0001);
        state.absorbForRound(0.0, 0.0, 0.0);
        assertEquals(0.15, sacrifice.damageReduction(WarlockPath.RANGED), 0.0001);
    }

    @Test
    void meleeDamageReductionGrowsEveryTenAbsorptionsAndCapsAtThirtyPercent() {
        WarlockState state = new WarlockState();
        WarlockSacrificeController sacrifice = new WarlockSacrificeController(WarlockConfig.RUNTIME, state);
        WarlockTower melee = new WarlockTower(
                WarlockTowers.MELEE_WARLOCK_TOWER,
                UUID.randomUUID(),
                TeamId.RED,
                0,
                new GridPosition(0, 0, 0)
        );

        for (int count = 0; count < 9; count++) {
            state.absorbForRound(0.0, 0.0, 0.0);
        }
        assertEquals(0.0, sacrifice.damageReduction(WarlockPath.MELEE), 0.0001);
        state.absorbForRound(0.0, 0.0, 0.0);
        assertEquals(0.025, sacrifice.damageReduction(WarlockPath.MELEE), 0.0001);
        for (int count = 10; count < 120; count++) {
            state.absorbForRound(0.0, 0.0, 0.0);
        }
        assertEquals(0.30, sacrifice.damageReduction(WarlockPath.MELEE), 0.0001);
        for (int count = 120; count < 140; count++) {
            state.absorbForRound(0.0, 0.0, 0.0);
        }
        assertEquals(0.30, sacrifice.damageReduction(WarlockPath.MELEE), 0.0001);
    }

    private static WarlockTower warlock(kim.biryeong.semiontd.tower.TowerType type, UUID owner) {
        return new WarlockTower(type, owner, TeamId.RED, 0, new GridPosition(0, 0, 0));
    }

    private static WarlockSacrificeTower sacrifice(
            kim.biryeong.semiontd.tower.TowerType type,
            UUID owner,
            GridPosition position
    ) {
        return new WarlockSacrificeTower(
                type,
                owner,
                TeamId.RED,
                0,
                position
        );
    }
}
