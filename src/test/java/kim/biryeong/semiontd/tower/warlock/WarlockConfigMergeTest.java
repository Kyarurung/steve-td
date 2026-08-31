package kim.biryeong.semiontd.tower.warlock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WarlockConfigMergeTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void resetBalance() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void damageScalingConfigAcceptsZeroAndBackfillsMissingValues() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, Map<String, Double>> zeroAbilities = new LinkedHashMap<>(defaults.abilities());
        Map<String, Double> zeroRanged = new LinkedHashMap<>(zeroAbilities.get(WarlockTowers.RANGED_WARLOCK_TOWER.id()));
        zeroRanged.put("damageThreshold", 0.0);
        zeroAbilities.put(WarlockTowers.RANGED_WARLOCK_TOWER.id(), zeroRanged);
        TowerBalanceConfig zero = new TowerBalanceConfig(defaults.towers(), defaults.upgradeCosts(), zeroAbilities);
        assertDoesNotThrow(() -> TowerBalanceRuntime.apply(zero));

        Map<String, Map<String, Double>> partialAbilities = new LinkedHashMap<>(defaults.abilities());
        removeScalingKeys(partialAbilities, WarlockTowers.RANGED_WARLOCK_TOWER.id());
        removeScalingKeys(partialAbilities, WarlockTowers.MELEE_WARLOCK_TOWER.id());
        TowerBalanceConfig merged = new TowerBalanceConfig(
                defaults.towers(),
                defaults.upgradeCosts(),
                partialAbilities
        ).withMissingDefaults(defaults);

        assertEquals(140.0, merged.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "damageThreshold", -1.0), 0.0001);
        assertEquals(20.0, merged.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "damageScale", -1.0), 0.0001);
        assertEquals(2000.0, merged.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "healthThreshold", -1.0), 0.0001);
        assertEquals(500.0, merged.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "healthScale", -1.0), 0.0001);
        assertEquals(200.0, merged.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "damageThreshold", -1.0), 0.0001);
        assertEquals(20.0, merged.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "damageScale", -1.0), 0.0001);
        assertEquals(3500.0, merged.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "healthThreshold", -1.0), 0.0001);
        assertEquals(500.0, merged.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "healthScale", -1.0), 0.0001);
        TowerBalanceRuntime.apply(merged);
        assertEquals(203.5611, WarlockConfig.RUNTIME.path(WarlockPath.RANGED).damageScaling().value(600.0), 0.0001);
    }

    @Test
    void configuredPassiveCapsRemainAuthoritative() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, Map<String, Double>> configuredAbilities = new LinkedHashMap<>(defaults.abilities());
        Map<String, Double> ranged = new LinkedHashMap<>(configuredAbilities.get(WarlockTowers.RANGED_WARLOCK_TOWER.id()));
        ranged.put("petHealthCap", 0.25);
        configuredAbilities.put(WarlockTowers.RANGED_WARLOCK_TOWER.id(), ranged);
        Map<String, Double> melee = new LinkedHashMap<>(configuredAbilities.get(WarlockTowers.MELEE_WARLOCK_TOWER.id()));
        melee.put("petDamageCap", 0.25);
        configuredAbilities.put(WarlockTowers.MELEE_WARLOCK_TOWER.id(), melee);

        TowerBalanceConfig merged = new TowerBalanceConfig(
                defaults.towers(),
                defaults.upgradeCosts(),
                configuredAbilities
        ).withMissingDefaults(defaults);

        assertEquals(0.25, merged.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "petHealthCap", -1.0), 0.0001);
        assertEquals(0.25, merged.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "petDamageCap", -1.0), 0.0001);
    }

    private static void removeScalingKeys(Map<String, Map<String, Double>> abilities, String towerId) {
        Map<String, Double> values = new LinkedHashMap<>(abilities.get(towerId));
        values.remove("damageThreshold");
        values.remove("damageScale");
        values.remove("healthThreshold");
        values.remove("healthScale");
        abilities.put(towerId, values);
    }
}
