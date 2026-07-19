package kim.biryeong.semiontd.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

final class TimedEffectSetTest {
    private static final ResourceLocation SOURCE = ResourceLocation.fromNamespaceAndPath("semion-td", "persistent-test");

    @Test
    void persistentEffectsReplaceAndRemoveWithoutTickingDown() {
        TimedEffectSet effects = new TimedEffectSet();

        assertTrue(effects.setPersistent(TimedEffectType.TOWER_TRAIT_DAMAGE_BONUS, SOURCE, 0.10));
        effects.apply(TimedEffectType.TOWER_TRAIT_DAMAGE_BONUS, 0.05, 2);
        effects.tick();
        effects.tick();

        assertTrue(effects.hasPersistent(TimedEffectType.TOWER_TRAIT_DAMAGE_BONUS, SOURCE));
        assertEquals(0.10, effects.magnitude(TimedEffectType.TOWER_TRAIT_DAMAGE_BONUS), 0.000_001);
        assertTrue(effects.setPersistent(TimedEffectType.TOWER_TRAIT_DAMAGE_BONUS, SOURCE, 0.20));
        assertEquals(0.20, effects.magnitude(TimedEffectType.TOWER_TRAIT_DAMAGE_BONUS), 0.000_001);
        assertTrue(effects.setPersistent(TimedEffectType.TOWER_TRAIT_DAMAGE_BONUS, SOURCE, 0.0));
        assertFalse(effects.hasPersistent(TimedEffectType.TOWER_TRAIT_DAMAGE_BONUS));
        assertEquals(0.0, effects.magnitude(TimedEffectType.TOWER_TRAIT_DAMAGE_BONUS), 0.000_001);
    }
}
