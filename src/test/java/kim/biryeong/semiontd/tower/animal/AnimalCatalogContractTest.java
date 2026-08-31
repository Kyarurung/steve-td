package kim.biryeong.semiontd.tower.animal;

import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.assertFamilyClosed;
import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.assertResolvedDescriptions;
import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.upgrade;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.job.AnimalTowerJob;
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

final class AnimalCatalogContractTest {
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
    void catalogClosesEveryTowerTierFactoryUpgradeOwnerAndDescription() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        ProductionTowerCatalogs.reloadBuiltIns(defaults);
        assertFamilyClosed(new FamilyContract(
                AnimalTowerJob.ID,
                AnimalTowers.T1_PIG_TOWER.id(),
                AnimalTowers.all(),
                Map.ofEntries(
                        Map.entry(AnimalTowers.T1_PIG_TOWER.id(), 1),
                        Map.entry(AnimalTowers.T2_PIG_TOWER.id(), 2),
                        Map.entry(AnimalTowers.T3_PIG_TOWER.id(), 3),
                        Map.entry(AnimalTowers.T4_PIG_LEADER_TOWER.id(), 4),
                        Map.entry(AnimalTowers.T1_WOLF_TOWER.id(), 1),
                        Map.entry(AnimalTowers.T2_WOLF_DPS_TOWER.id(), 2),
                        Map.entry(AnimalTowers.T3_WOLF_DPS_TOWER.id(), 3),
                        Map.entry(AnimalTowers.T4_WOLF_LEADER_TOWER.id(), 4),
                        Map.entry(AnimalTowers.T1_RABBIT_TOWER.id(), 1),
                        Map.entry(AnimalTowers.T2_RABBIT_TOWER.id(), 2),
                        Map.entry(AnimalTowers.T3_RABBIT_TOWER.id(), 3),
                        Map.entry(AnimalTowers.T4_RABBIT_LEADER_TOWER.id(), 4),
                        Map.entry(AnimalTowers.T1_FOX_TOWER.id(), 1),
                        Map.entry(AnimalTowers.T2_FOX_TOWER.id(), 2),
                        Map.entry(AnimalTowers.T3_FOX_TOWER.id(), 3),
                        Map.entry(AnimalTowers.T4_FOX_LEADER_TOWER.id(), 4)
                ),
                upgrades(),
                AnimalCatalogContractTest::runtimeType
        ), defaults);
    }

    @Test
    void partialConfigBackfillsTheWholeFamilyAndReloadsConfiguredAbility() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        TowerBalanceConfig partial = new TowerBalanceConfig(
                Map.of(),
                Map.of(),
                Map.of(AnimalTowers.T1_PIG_TOWER.id(), Map.of("maxStacks", 3.0))
        );

        TowerBalanceConfig merged = partial.withMissingDefaults(defaults);
        AnimalTowers.all().forEach(type -> assertTrue(merged.towers().containsKey(type.id()), type.id()));
        upgrades().forEach(edge -> assertTrue(merged.upgradeCosts().containsKey(edge.configKey()), edge.configKey()));
        assertEquals(3.0, merged.ability(AnimalTowers.T1_PIG_TOWER.id(), "maxStacks", -1.0));
        assertTrue(merged.abilities().get(AnimalTowers.T1_PIG_TOWER.id()).containsKey("healthPerStack"));

        ProductionTowerCatalogs.reloadBuiltIns(merged);
        assertEquals(3, AnimalConfig.RUNTIME.integer(
                AnimalTowers.T1_PIG_TOWER, AnimalAbilityKey.MAX_STACKS
        ));
        assertResolvedDescriptions(AnimalTowers.all());
    }

    private static List<UpgradeExpectation> upgrades() {
        return List.of(
                upgrade(AnimalTowers.T1_PIG_TOWER, "t2_pig_tower", AnimalTowers.T2_PIG_TOWER),
                upgrade(AnimalTowers.T2_PIG_TOWER, "t3_pig_tower", AnimalTowers.T3_PIG_TOWER),
                upgrade(AnimalTowers.T3_PIG_TOWER, "t4_pig_leader_tower", AnimalTowers.T4_PIG_LEADER_TOWER),
                upgrade(AnimalTowers.T1_WOLF_TOWER, "t2_wolf_dps_tower", AnimalTowers.T2_WOLF_DPS_TOWER),
                upgrade(AnimalTowers.T2_WOLF_DPS_TOWER, "t3_wolf_dps_tower", AnimalTowers.T3_WOLF_DPS_TOWER),
                upgrade(AnimalTowers.T3_WOLF_DPS_TOWER, "t4_wolf_leader_tower", AnimalTowers.T4_WOLF_LEADER_TOWER),
                upgrade(AnimalTowers.T1_RABBIT_TOWER, "t2_rabbit_tower", AnimalTowers.T2_RABBIT_TOWER),
                upgrade(AnimalTowers.T2_RABBIT_TOWER, "t3_rabbit_tower", AnimalTowers.T3_RABBIT_TOWER),
                upgrade(AnimalTowers.T3_RABBIT_TOWER, "t4_rabbit_leader_tower", AnimalTowers.T4_RABBIT_LEADER_TOWER),
                upgrade(AnimalTowers.T1_FOX_TOWER, "t2_fox_tower", AnimalTowers.T2_FOX_TOWER),
                upgrade(AnimalTowers.T2_FOX_TOWER, "t3_fox_tower", AnimalTowers.T3_FOX_TOWER),
                upgrade(AnimalTowers.T3_FOX_TOWER, "t4_fox_leader_tower", AnimalTowers.T4_FOX_LEADER_TOWER)
        );
    }

    private static Class<? extends Tower> runtimeType(TowerType type) {
        if (List.of(
                AnimalTowers.T1_PIG_TOWER, AnimalTowers.T2_PIG_TOWER,
                AnimalTowers.T3_PIG_TOWER, AnimalTowers.T4_PIG_LEADER_TOWER
        ).stream().anyMatch(expected -> expected.id().equals(type.id()))) return AnimalPigTower.class;
        if (List.of(
                AnimalTowers.T1_WOLF_TOWER, AnimalTowers.T2_WOLF_DPS_TOWER,
                AnimalTowers.T3_WOLF_DPS_TOWER, AnimalTowers.T4_WOLF_LEADER_TOWER
        ).stream().anyMatch(expected -> expected.id().equals(type.id()))) return AnimalWolfTower.class;
        if (List.of(
                AnimalTowers.T1_RABBIT_TOWER, AnimalTowers.T2_RABBIT_TOWER,
                AnimalTowers.T3_RABBIT_TOWER, AnimalTowers.T4_RABBIT_LEADER_TOWER
        ).stream().anyMatch(expected -> expected.id().equals(type.id()))) return AnimalRabbitTower.class;
        return AnimalFoxTower.class;
    }
}
