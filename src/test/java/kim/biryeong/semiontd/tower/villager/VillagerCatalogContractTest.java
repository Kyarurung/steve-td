package kim.biryeong.semiontd.tower.villager;

import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.assertFamilyClosed;
import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.assertResolvedDescriptions;
import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.upgrade;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.job.VillagerTowerJob;
import kim.biryeong.semiontd.tower.ProductionTower;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.FamilyContract;
import kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.UpgradeExpectation;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class VillagerCatalogContractTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void restoreDefaults() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void catalogClosesEveryBaseTowerTierFactoryUpgradeOwnerAndDescription() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        ProductionTowerCatalogs.reloadBuiltIns(defaults);
        assertFamilyClosed(new FamilyContract(
                VillagerTowerJob.ID,
                VillagerTowers.T1_ALLAY_TOWER.id(),
                VillagerTowers.baseTowers(),
                Map.ofEntries(
                        Map.entry(VillagerTowers.T1_SPLASH_TOWER.id(), 1),
                        Map.entry(VillagerTowers.T2_LIBRARIAN_TOWER.id(), 2),
                        Map.entry(VillagerTowers.T3_CLERIC_TOWER.id(), 3),
                        Map.entry(VillagerTowers.T1_GOLEM_TOWER.id(), 1),
                        Map.entry(VillagerTowers.T2_GOLEM_TOWER.id(), 2),
                        Map.entry(VillagerTowers.T3_GOLEM_TOWER.id(), 3),
                        Map.entry(VillagerTowers.T1_ALLAY_TOWER.id(), 1),
                        Map.entry(VillagerTowers.T2_ALLAY_TOWER.id(), 2),
                        Map.entry(VillagerTowers.T2_WEAPON_SMITH_TOWER.id(), 2),
                        Map.entry(VillagerTowers.T3_ARMORER_TOWER.id(), 3),
                        Map.entry(VillagerTowers.T3_WEAPON_SMITH_TOWER.id(), 3),
                        Map.entry(VillagerTowers.T1_CAT_TOWER.id(), 1),
                        Map.entry(VillagerTowers.T2_ANTI_TANKER_CAT_TOWER.id(), 2),
                        Map.entry(VillagerTowers.T2_LANE_CLEAR_CAT_TOWER.id(), 2),
                        Map.entry(VillagerTowers.T3_ANTI_TANKER_CAT_TOWER.id(), 3),
                        Map.entry(VillagerTowers.T3_LANE_CLEAR_CAT_TOWER.id(), 3)
                ),
                upgrades(),
                VillagerCatalogContractTest::runtimeType
        ), defaults);
    }

    @Test
    void partialConfigBackfillsTheWholeFamilyAndReloadsConfiguredAbility() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        TowerBalanceConfig partial = new TowerBalanceConfig(
                Map.of(),
                Map.of(),
                Map.of(VillagerTowers.T1_ALLAY_TOWER.id(), Map.of("healAmount", 123.0))
        );

        TowerBalanceConfig merged = partial.withMissingDefaults(defaults);
        VillagerTowers.baseTowers().forEach(type -> assertTrue(merged.towers().containsKey(type.id()), type.id()));
        upgrades().forEach(edge -> assertTrue(merged.upgradeCosts().containsKey(edge.configKey()), edge.configKey()));
        assertEquals(123.0, merged.ability(VillagerTowers.T1_ALLAY_TOWER.id(), "healAmount", -1.0));
        assertTrue(merged.abilities().get(VillagerTowers.T1_ALLAY_TOWER.id()).containsKey("radius"));

        ProductionTowerCatalogs.reloadBuiltIns(merged);
        assertEquals(123.0, VillagerConfig.RUNTIME.value(
                VillagerTowers.T1_ALLAY_TOWER, VillagerAbilityKey.HEAL_AMOUNT
        ));
        assertResolvedDescriptions(VillagerTowers.baseTowers());
    }

    private static List<UpgradeExpectation> upgrades() {
        return List.of(
                upgrade(VillagerTowers.T1_SPLASH_TOWER, "villager_splash_t2", VillagerTowers.T2_LIBRARIAN_TOWER),
                upgrade(VillagerTowers.T2_LIBRARIAN_TOWER, "villager_splash_t3", VillagerTowers.T3_CLERIC_TOWER),
                upgrade(VillagerTowers.T1_GOLEM_TOWER, "t2_golem_tower", VillagerTowers.T2_GOLEM_TOWER),
                upgrade(VillagerTowers.T2_GOLEM_TOWER, "t3_golem_tower", VillagerTowers.T3_GOLEM_TOWER),
                upgrade(VillagerTowers.T1_ALLAY_TOWER, "t2_allay_tower", VillagerTowers.T2_ALLAY_TOWER),
                upgrade(VillagerTowers.T1_ALLAY_TOWER, "t2_weapon_smith_tower", VillagerTowers.T2_WEAPON_SMITH_TOWER),
                upgrade(VillagerTowers.T2_ALLAY_TOWER, "t3_armorer_tower", VillagerTowers.T3_ARMORER_TOWER),
                upgrade(VillagerTowers.T2_WEAPON_SMITH_TOWER, "t3_weapon_smith_tower", VillagerTowers.T3_WEAPON_SMITH_TOWER),
                upgrade(VillagerTowers.T1_CAT_TOWER, "t2_anti_tanker_cat_tower", VillagerTowers.T2_ANTI_TANKER_CAT_TOWER),
                upgrade(VillagerTowers.T1_CAT_TOWER, "t2_lane_clear_cat_tower", VillagerTowers.T2_LANE_CLEAR_CAT_TOWER),
                upgrade(VillagerTowers.T2_ANTI_TANKER_CAT_TOWER, "t3_anti_tanker_cat_tower", VillagerTowers.T3_ANTI_TANKER_CAT_TOWER),
                upgrade(VillagerTowers.T2_LANE_CLEAR_CAT_TOWER, "t3_lane_clear_cat_tower", VillagerTowers.T3_LANE_CLEAR_CAT_TOWER)
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
