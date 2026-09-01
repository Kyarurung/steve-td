package kim.biryeong.semiontd.tower.villager;

import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.assertResolvedDescriptions;
import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.upgrade;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.job.VillagerAdvTowerJob;
import kim.biryeong.semiontd.tower.ProductionTower;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.UpgradeExpectation;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VillagerAdvCatalogContractTest {
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
    void advCatalogOwnsAllTiersFactoriesUpgradeEdgesAndDescriptions() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        ProductionTowerCatalogs.reloadBuiltIns(defaults);
        Map<String, Integer> tiers = Map.ofEntries(
                Map.entry(VillagerTowers.ADV_T1_SPLASH_TOWER.id(), 1),
                Map.entry(VillagerTowers.ADV_T2_LIBRARIAN_TOWER.id(), 2),
                Map.entry(VillagerTowers.ADV_T3_CLERIC_TOWER.id(), 3),
                Map.entry(VillagerTowers.ADV_T1_GOLEM_TOWER.id(), 1),
                Map.entry(VillagerTowers.ADV_T2_GOLEM_TOWER.id(), 2),
                Map.entry(VillagerTowers.ADV_T3_GOLEM_TOWER.id(), 3),
                Map.entry(VillagerTowers.ADV_T1_ALLAY_TOWER.id(), 1),
                Map.entry(VillagerTowers.ADV_T2_ALLAY_TOWER.id(), 2),
                Map.entry(VillagerTowers.ADV_T2_WEAPON_SMITH_TOWER.id(), 2),
                Map.entry(VillagerTowers.ADV_T3_ARMORER_TOWER.id(), 3),
                Map.entry(VillagerTowers.ADV_T3_WEAPON_SMITH_TOWER.id(), 3),
                Map.entry(VillagerTowers.ADV_T1_CAT_TOWER.id(), 1),
                Map.entry(VillagerTowers.ADV_T2_ANTI_TANKER_CAT_TOWER.id(), 2),
                Map.entry(VillagerTowers.ADV_T2_LANE_CLEAR_CAT_TOWER.id(), 2),
                Map.entry(VillagerTowers.ADV_T3_ANTI_TANKER_CAT_TOWER.id(), 3),
                Map.entry(VillagerTowers.ADV_T3_LANE_CLEAR_CAT_TOWER.id(), 3)
        );
        VillagerAdvTowerJob job = new VillagerAdvTowerJob();
        for (TowerType type : VillagerTowers.advTowers()) {
            ProductionTowerCatalog.CatalogEntry entry = ProductionTowerCatalog.find(type.id()).orElseThrow();
            assertEquals(tiers.get(type.id()), entry.tier(), type.id());
            assertEquals(entry.tier() == 1, entry.starter(), type.id());
            assertTrue(job.includesTowerInCatalog(type), type.id());
            Tower tower = entry.create(
                    UUID.fromString("00000000-0000-0000-0000-000000000123"),
                    TeamId.RED,
                    1,
                    new GridPosition(0, 64, 0)
            );
            assertEquals(runtimeType(type), tower.getClass(), type.id());
        }
        for (UpgradeExpectation edge : upgrades()) {
            TowerType from = ProductionTowerCatalog.find(edge.fromTowerId()).orElseThrow().type();
            var option = ProductionTowerCatalog.upgrade(from, edge.upgradeId()).orElseThrow();
            assertEquals(edge.toTowerId(), option.targetType().id(), edge.configKey());
            assertEquals(defaults.upgradeCost(edge.fromTowerId(), edge.upgradeId(), -1), option.mineralCost(), edge.configKey());
        }
        assertResolvedDescriptions(VillagerTowers.advTowers());
    }

    @Test
    void baseAndAdvCatalogRegistrationHaveSeparateOwnership() {
        ProductionTowerCatalog.clear();
        VillagerTowerCatalogs.register();
        assertTrue(ProductionTowerCatalog.find(VillagerTowers.T1_SPLASH_TOWER.id()).isPresent());
        assertFalse(ProductionTowerCatalog.find(VillagerTowers.ADV_T1_SPLASH_TOWER.id()).isPresent());
        VillagerAdvTowerCatalogs.register();
        assertTrue(ProductionTowerCatalog.find(VillagerTowers.ADV_T1_SPLASH_TOWER.id()).isPresent());
    }

    @Test
    void partialConfigBackfillsAdvValuesAndPreservesConfiguredBuff() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        TowerBalanceConfig partial = new TowerBalanceConfig(Map.of(), Map.of(), Map.of());
        TowerBalanceConfig merged = partial.withMissingDefaults(defaults);
        VillagerTowers.advTowers().forEach(type -> assertTrue(merged.towers().containsKey(type.id())));
        assertEquals(partial.villagerAdv().resolvedExperienceMax(), merged.villagerAdv().resolvedExperienceMax());
        assertEquals(
                partial.villagerAdv().buff(VillagerTowers.ADV_T1_SPLASH_TOWER.id(), "rangedDamagePerExperience"),
                merged.villagerAdv().buff(VillagerTowers.ADV_T1_SPLASH_TOWER.id(), "rangedDamagePerExperience")
        );
        ProductionTowerCatalogs.reloadBuiltIns(merged);
        assertResolvedDescriptions(VillagerTowers.advTowers());
    }

    @Test
    void invalidAdvAbilityIsRejectedBeforeRuntimeReload() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        Map<String, Double> ranged = new LinkedHashMap<>(
                abilities.getOrDefault(VillagerTowers.ADV_T1_SPLASH_TOWER.id(), Map.of())
        );
        ranged.put("rangedDamagePerExperience", -0.1);
        abilities.put(VillagerTowers.ADV_T1_SPLASH_TOWER.id(), ranged);
        TowerBalanceConfig invalid = new TowerBalanceConfig(defaults.towers(), defaults.upgradeCosts(), abilities);
        assertThrows(IllegalArgumentException.class, invalid::validateForRuntime);
    }

    private static List<kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.UpgradeExpectation> upgrades() {
        return List.of(
                upgrade(VillagerTowers.ADV_T1_SPLASH_TOWER, "villager_splash_t2", VillagerTowers.ADV_T2_LIBRARIAN_TOWER),
                upgrade(VillagerTowers.ADV_T2_LIBRARIAN_TOWER, "villager_splash_t3", VillagerTowers.ADV_T3_CLERIC_TOWER),
                upgrade(VillagerTowers.ADV_T1_GOLEM_TOWER, "t2_golem_tower", VillagerTowers.ADV_T2_GOLEM_TOWER),
                upgrade(VillagerTowers.ADV_T2_GOLEM_TOWER, "t3_golem_tower", VillagerTowers.ADV_T3_GOLEM_TOWER),
                upgrade(VillagerTowers.ADV_T1_ALLAY_TOWER, "t2_allay_tower", VillagerTowers.ADV_T2_ALLAY_TOWER),
                upgrade(VillagerTowers.ADV_T1_ALLAY_TOWER, "t2_weapon_smith_tower", VillagerTowers.ADV_T2_WEAPON_SMITH_TOWER),
                upgrade(VillagerTowers.ADV_T2_ALLAY_TOWER, "t3_armorer_tower", VillagerTowers.ADV_T3_ARMORER_TOWER),
                upgrade(VillagerTowers.ADV_T2_WEAPON_SMITH_TOWER, "t3_weapon_smith_tower", VillagerTowers.ADV_T3_WEAPON_SMITH_TOWER),
                upgrade(VillagerTowers.ADV_T1_CAT_TOWER, "t2_anti_tanker_cat_tower", VillagerTowers.ADV_T2_ANTI_TANKER_CAT_TOWER),
                upgrade(VillagerTowers.ADV_T1_CAT_TOWER, "t2_lane_clear_cat_tower", VillagerTowers.ADV_T2_LANE_CLEAR_CAT_TOWER),
                upgrade(VillagerTowers.ADV_T2_ANTI_TANKER_CAT_TOWER, "t3_anti_tanker_cat_tower", VillagerTowers.ADV_T3_ANTI_TANKER_CAT_TOWER),
                upgrade(VillagerTowers.ADV_T2_LANE_CLEAR_CAT_TOWER, "t3_lane_clear_cat_tower", VillagerTowers.ADV_T3_LANE_CLEAR_CAT_TOWER)
        );
    }

    private static Class<? extends Tower> runtimeType(TowerType type) {
        if (VillagerTowers.matches(type, VillagerTowers.T2_LIBRARIAN_TOWER)
                || VillagerTowers.matches(type, VillagerTowers.T3_CLERIC_TOWER)) {
            return VillagerSplashTower.class;
        }
        if (VillagerTowers.matches(type, VillagerTowers.T2_GOLEM_TOWER)
                || VillagerTowers.matches(type, VillagerTowers.T3_GOLEM_TOWER)) {
            return VillagerThornTower.class;
        }
        if (List.of(
                VillagerTowers.T1_ALLAY_TOWER,
                VillagerTowers.T2_ALLAY_TOWER,
                VillagerTowers.T2_WEAPON_SMITH_TOWER,
                VillagerTowers.T3_ARMORER_TOWER,
                VillagerTowers.T3_WEAPON_SMITH_TOWER
        ).stream().anyMatch(expected -> VillagerTowers.matches(type, expected))) {
            return VillagerAllayTower.class;
        }
        if (VillagerTowers.matches(type, VillagerTowers.T2_ANTI_TANKER_CAT_TOWER)
                || VillagerTowers.matches(type, VillagerTowers.T3_ANTI_TANKER_CAT_TOWER)) {
            return VillagerAntiTankerCatTower.class;
        }
        if (VillagerTowers.matches(type, VillagerTowers.T2_LANE_CLEAR_CAT_TOWER)
                || VillagerTowers.matches(type, VillagerTowers.T3_LANE_CLEAR_CAT_TOWER)) {
            return VillagerLaneClearCatTower.class;
        }
        return ProductionTower.class;
    }
}
