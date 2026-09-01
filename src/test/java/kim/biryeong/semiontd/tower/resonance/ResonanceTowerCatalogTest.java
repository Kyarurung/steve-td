package kim.biryeong.semiontd.tower.resonance;

import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.assertFamilyClosed;
import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.assertResolvedDescriptions;
import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.upgrade;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.job.ResonanceTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.FamilyContract;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ResonanceTowerCatalogTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void restoreDefaults() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void catalogClosesAllTiersFactoriesUpgradeEdgesOwnerAndDescriptions() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        ProductionTowerCatalogs.reloadBuiltIns(defaults);
        assertFamilyClosed(new FamilyContract(
                ResonanceTowerJob.ID,
                ResonanceTowers.FOCUS_CRYSTAL.id(),
                ResonanceTowers.all(),
                Map.ofEntries(
                        Map.entry(ResonanceTowers.FOCUS_CRYSTAL.id(), 1),
                        Map.entry(ResonanceTowers.FOCUS_PRISM.id(), 2),
                        Map.entry(ResonanceTowers.FOCUS_CORE.id(), 3),
                        Map.entry(ResonanceTowers.WAVE_CRYSTAL.id(), 1),
                        Map.entry(ResonanceTowers.WAVE_PRISM.id(), 2),
                        Map.entry(ResonanceTowers.WAVE_CORE.id(), 3),
                        Map.entry(ResonanceTowers.FROST_CRYSTAL.id(), 1),
                        Map.entry(ResonanceTowers.FROST_PRISM.id(), 2),
                        Map.entry(ResonanceTowers.FROST_CORE.id(), 3),
                        Map.entry(ResonanceTowers.AMPLIFY_CRYSTAL.id(), 1),
                        Map.entry(ResonanceTowers.AMPLIFY_PRISM.id(), 2),
                        Map.entry(ResonanceTowers.AMPLIFY_CORE.id(), 3)
                ),
                java.util.List.of(
                        upgrade(ResonanceTowers.FOCUS_CRYSTAL, ResonanceTowers.FOCUS_PRISM.id(), ResonanceTowers.FOCUS_PRISM),
                        upgrade(ResonanceTowers.FOCUS_PRISM, ResonanceTowers.FOCUS_CORE.id(), ResonanceTowers.FOCUS_CORE),
                        upgrade(ResonanceTowers.WAVE_CRYSTAL, ResonanceTowers.WAVE_PRISM.id(), ResonanceTowers.WAVE_PRISM),
                        upgrade(ResonanceTowers.WAVE_PRISM, ResonanceTowers.WAVE_CORE.id(), ResonanceTowers.WAVE_CORE),
                        upgrade(ResonanceTowers.FROST_CRYSTAL, ResonanceTowers.FROST_PRISM.id(), ResonanceTowers.FROST_PRISM),
                        upgrade(ResonanceTowers.FROST_PRISM, ResonanceTowers.FROST_CORE.id(), ResonanceTowers.FROST_CORE),
                        upgrade(ResonanceTowers.AMPLIFY_CRYSTAL, ResonanceTowers.AMPLIFY_PRISM.id(), ResonanceTowers.AMPLIFY_PRISM),
                        upgrade(ResonanceTowers.AMPLIFY_PRISM, ResonanceTowers.AMPLIFY_CORE.id(), ResonanceTowers.AMPLIFY_CORE)
                ),
                ignored -> ResonanceTower.class
        ), defaults);
    }

    @Test
    void partialConfigBackfillsFamilyAndRuntimeDescriptionUsesConfiguredValue() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        TowerBalanceConfig partial = new TowerBalanceConfig(
                Map.of(),
                Map.of(),
                Map.of(ResonanceTowers.FOCUS_CRYSTAL.id(), Map.of("linkRange", 2.0))
        );
        TowerBalanceConfig merged = partial.withMissingDefaults(defaults);
        ResonanceTowers.all().forEach(type -> assertTrue(merged.towers().containsKey(type.id())));
        ProductionTowerCatalogs.reloadBuiltIns(merged);
        assertEquals(2.0, ResonanceConfig.RUNTIME.value(
                ResonanceTowers.FOCUS_CRYSTAL,
                ResonanceAbilityKey.LINK_RANGE
        ));
        assertResolvedDescriptions(ResonanceTowers.all());
    }

    @Test
    void invalidResonanceAbilityIsRejectedBeforeRuntimeReload() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        Map<String, Double> focus = new LinkedHashMap<>(abilities.get(ResonanceTowers.FOCUS_CRYSTAL.id()));
        focus.put(ResonanceAbilityKey.LINK_RANGE.key(), Double.NaN);
        abilities.put(ResonanceTowers.FOCUS_CRYSTAL.id(), focus);
        TowerBalanceConfig invalid = new TowerBalanceConfig(defaults.towers(), defaults.upgradeCosts(), abilities);
        assertThrows(IllegalArgumentException.class, invalid::validateForRuntime);
    }
}
