package kim.biryeong.semiontd.tower.warlock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WarlockPathTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void restoreDefaults() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void everyCoreAndSacrificeTowerResolvesToOnePath() {
        assertEquals(WarlockPath.BASE, WarlockPath.fromCore(WarlockTowers.BASE_WARLOCK_TOWER));
        assertEquals(WarlockPath.RANGED, WarlockPath.fromCore(WarlockTowers.RANGED_WARLOCK_TOWER));
        assertEquals(WarlockPath.MELEE, WarlockPath.fromCore(WarlockTowers.MELEE_WARLOCK_TOWER));
        assertEquals(WarlockPath.RANGED, WarlockPath.fromTower(WarlockTowers.T1_RANGED_SLAVE));
        assertEquals(WarlockPath.RANGED, WarlockPath.fromTower(WarlockTowers.T3_RANGED_SLAVE));
        assertEquals(WarlockPath.MELEE, WarlockPath.fromTower(WarlockTowers.T1_SLAVE));
        assertEquals(WarlockPath.MELEE, WarlockPath.fromTower(WarlockTowers.T3_SLAVE));
        assertThrows(IllegalArgumentException.class, () -> WarlockPath.fromCore(null));
    }

    @Test
    void semanticRulesExposeNormalizedPathBehavior() {
        WarlockConfig config = WarlockConfig.RUNTIME;
        WarlockRules.PathRule base = config.path(WarlockPath.BASE);
        WarlockRules.PathRule ranged = config.path(WarlockPath.RANGED);
        WarlockRules.PathRule melee = config.path(WarlockPath.MELEE);

        assertEquals(600.0, base.healthScaling().value(600.0), 0.0001);
        assertEquals(2000.0, ranged.healthScaling().threshold(), 0.0001);
        assertEquals(180.0, melee.damageScaling().threshold(), 0.0001);
        assertEquals(0.07, ranged.lifeSteal().maximum(), 0.0001);
        assertEquals(0.13, melee.lifeSteal().maximum(), 0.0001);
        assertEquals(4, ranged.defense().sacrificesPerStep());
        assertEquals(10, melee.defense().sacrificesPerStep());
        assertEquals(5, config.combat().minimumIntervalTicks());
        assertEquals(1400, config.requiredAwakeningKills());
    }

    @Test
    void basePathUsesNeutralOptionalRules() {
        WarlockRules.PathRule base = WarlockConfig.RUNTIME.path(WarlockPath.BASE);

        assertEquals(0.0, base.lifeSteal().value(100), 0.0001);
        assertEquals(0.0, base.splash().radius(100), 0.0001);
        assertEquals(0.0, base.defense().value(100), 0.0001);
        assertEquals(0.0, base.passive().healthBonus(100), 0.0001);
        assertEquals(0.0, base.passive().damageBonus(100), 0.0001);
        assertEquals(0.0, base.incomeDebuffResistance(), 0.0001);
        assertEquals(WarlockRules.AwakeningBonus.NONE, base.awakeningBonus());
    }

    @Test
    void sacrificeDeathEffectsResolveWithoutDirectRuntimeLookups() {
        WarlockRules.DeathEffectRule melee = WarlockConfig.RUNTIME.deathEffect(WarlockTowers.T2_SLAVE);
        WarlockRules.DeathEffectRule ranged = WarlockConfig.RUNTIME.deathEffect(WarlockTowers.T2_RANGED_SLAVE);

        assertEquals(WarlockPath.MELEE, melee.path());
        assertEquals(WarlockPath.RANGED, ranged.path());
        assertEquals(20.0, melee.radius(), 0.0001);
        assertEquals(0.10, melee.magnitude(), 0.0001);
        assertEquals(0.10, ranged.magnitude(), 0.0001);
        assertTrue(melee.durationTicks() > 0);
        assertTrue(ranged.durationTicks() > 0);
    }
}
