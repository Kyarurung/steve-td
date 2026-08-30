package kim.biryeong.semiontd.tower.warlock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WarlockStateTest {
    @Test
    void absorptionSeparatesPermanentAndRoundState() {
        WarlockState state = new WarlockState();

        state.recordSacrifice(gain(2.5, 1.0, 40.0, 8.0, 8.0));
        assertEquals(40.0, state.roundHealthBonus(), 0.0001);

        assertEquals(40.0, state.roundHealthBonus(), 0.0001);
        assertEquals(8.0, state.roundDamageBonus(), 0.0001);
        assertEquals(2.5, state.permanentHealthBonus(), 0.0001);
        assertEquals(1.0, state.permanentDamageBonus(), 0.0001);
        assertEquals(8.0, state.roundIntervalReduction(), 0.0001);
        assertEquals(1, state.totalSacrificeCount());
        assertEquals(1, state.roundSacrificeCount());

        state.resetRound();

        assertEquals(0.0, state.roundHealthBonus(), 0.0001);
        assertEquals(0.0, state.roundDamageBonus(), 0.0001);
        assertEquals(0.0, state.roundIntervalReduction(), 0.0001);
        assertEquals(0, state.roundSacrificeCount());
        assertEquals(1, state.totalSacrificeCount());
        assertEquals(2.5, state.permanentHealthBonus(), 0.0001);
        assertEquals(1.0, state.permanentDamageBonus(), 0.0001);
    }

    @Test
    void attackIntervalAbsorptionHonorsCapAndOnlyFasterTargets() {
        WarlockState state = new WarlockState();

        state.recordSacrifice(gain(0.0, 0.0, 0.0, 0.0, 10.0));
        state.recordSacrifice(gain(0.0, 0.0, 0.0, 0.0, 8.0));
        state.recordSacrifice(gain(0.0, 0.0, 0.0, 0.0, 0.0));

        assertEquals(15.0, state.roundIntervalReduction(), 0.0001);
    }

    @Test
    void copiedStateDoesNotShareFutureMutations() {
        WarlockState source = new WarlockState();
        source.recordSacrifice(gain(5.0, 0.5, 60.0, 12.0, 0.0));

        WarlockState copy = new WarlockState();
        copy.copyFrom(source);
        source.resetRound();

        assertEquals(60.0, copy.roundHealthBonus(), 0.0001);
        assertEquals(12.0, copy.roundDamageBonus(), 0.0001);
        assertEquals(5.0, copy.permanentHealthBonus(), 0.0001);
        assertEquals(0.5, copy.permanentDamageBonus(), 0.0001);
        assertEquals(1, copy.totalSacrificeCount());
        assertEquals(1, copy.roundSacrificeCount());
    }

    @Test
    void copiedBaseProgressionImmediatelyUsesSpecializedCountRules() {
        WarlockState base = new WarlockState();
        for (int sacrifice = 0; sacrifice < 5; sacrifice++) {
            base.recordSacrifice(gain(1.0, 1.0, 0.0, 0.0, 0.0));
        }
        WarlockState specialized = new WarlockState();
        specialized.copyFrom(base);
        WarlockProgressionSnapshot progression = WarlockProgressionSnapshot.from(specialized, null);

        assertEquals(5, progression.totalSacrificeCount());
        assertEquals(5, progression.roundSacrificeCount());
        assertEquals(5, progression.lifeStealSacrificeCount(WarlockPath.RANGED));
        assertEquals(5, progression.defenseSacrificeCount(WarlockPath.RANGED));
        assertEquals(5, progression.lifeStealSacrificeCount(WarlockPath.MELEE));
        assertEquals(5, progression.defenseSacrificeCount(WarlockPath.MELEE));
    }

    @Test
    void awakeningCanOccurOncePerRoundAndResetsWithRoundState() {
        WarlockState state = new WarlockState();

        assertTrue(state.awaken());
        assertFalse(state.awaken());
        assertTrue(state.awakenedThisRound());

        state.resetRound();

        assertFalse(state.awakenedThisRound());
        assertTrue(state.awaken());
    }

    @Test
    void awakeningRequiresUnlockLowHealthAndLastSurvivorTogether() {
        var rule = new WarlockRules.AwakeningRule(0.40, WarlockRules.AwakeningBonus.NONE);

        assertTrue(rule.canActivate(true, 0.40, true));
        assertFalse(rule.canActivate(false, 0.40, true));
        assertFalse(rule.canActivate(true, 0.41, true));
        assertFalse(rule.canActivate(true, 0.40, false));
        assertFalse(rule.canActivate(true, 0.0, true));
        assertFalse(rule.canActivate(true, Double.NaN, true));
    }

    private static WarlockSacrifice.Gain gain(
            double permanentHealth,
            double permanentDamage,
            double roundHealth,
            double roundDamage,
            double intervalReduction
    ) {
        return new WarlockSacrifice.Gain(
                permanentHealth,
                permanentDamage,
                roundHealth,
                roundDamage,
                intervalReduction,
                15.0
        );
    }
}
