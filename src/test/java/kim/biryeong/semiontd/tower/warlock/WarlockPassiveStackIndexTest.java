package kim.biryeong.semiontd.tower.warlock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import org.junit.jupiter.api.Test;

final class WarlockPassiveStackIndexTest {
    private static final UUID OWNER = UUID.nameUUIDFromBytes("warlock-passive-owner".getBytes());

    @Test
    void countsOnlyLivingOwnedSacrificesForEachPath() {
        WarlockTower ranged = core(WarlockTowers.RANGED_WARLOCK_TOWER, OWNER);
        WarlockTower melee = core(WarlockTowers.MELEE_WARLOCK_TOWER, OWNER);
        WarlockTower base = core(WarlockTowers.BASE_WARLOCK_TOWER, OWNER);
        WarlockSacrificeTower rangedFirst = sacrifice(WarlockTowers.T1_RANGED_SLAVE, OWNER);
        WarlockSacrificeTower rangedSecond = sacrifice(WarlockTowers.T3_RANGED_SLAVE, OWNER);
        WarlockSacrificeTower meleeFirst = sacrifice(WarlockTowers.T1_SLAVE, OWNER);
        WarlockSacrificeTower deadMelee = sacrifice(WarlockTowers.T2_SLAVE, OWNER);
        WarlockSacrificeTower foreignRanged = sacrifice(WarlockTowers.T2_RANGED_SLAVE, UUID.randomUUID());
        deadMelee.syncHealth(0.0);

        WarlockPassiveStackIndex index = WarlockPassiveStackIndex.capture(List.of(
                ranged, melee, base, rangedFirst, rangedSecond, meleeFirst, deadMelee, foreignRanged
        ));

        assertEquals(2, index.count(ranged));
        assertEquals(1, index.count(melee));
        assertEquals(0, index.count(base));
    }

    @Test
    void capturedCountsDoNotDependOnLaterRosterMutation() {
        WarlockTower ranged = core(WarlockTowers.RANGED_WARLOCK_TOWER, OWNER);
        WarlockSacrificeTower sacrifice = sacrifice(WarlockTowers.T1_RANGED_SLAVE, OWNER);
        WarlockPassiveStackIndex index = WarlockPassiveStackIndex.capture(List.of(ranged, sacrifice));
        sacrifice.syncHealth(0.0);
        assertEquals(1, index.count(ranged));
        assertEquals(0, WarlockPassiveStackIndex.capture(List.of(ranged, sacrifice)).count(ranged));
    }

    private static WarlockTower core(kim.biryeong.semiontd.tower.TowerType type, UUID owner) {
        return new WarlockTower(type, owner, TeamId.RED, 1, new GridPosition(0, 64, 0));
    }

    private static WarlockSacrificeTower sacrifice(kim.biryeong.semiontd.tower.TowerType type, UUID owner) {
        return new WarlockSacrificeTower(type, owner, TeamId.RED, 1, new GridPosition(1, 64, 0));
    }
}
