package kim.biryeong.semiontd.tower;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.entity.tower.vfx.BuilderPalette;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.job.JobRegistry;
import kim.biryeong.semiontd.job.SemionJob;
import kim.biryeong.semiontd.tower.undead.UndeadTowers;
import kim.biryeong.semiontd.tower.villager.VillagerTowers;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class BuilderFamilyContractTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    @AfterEach
    void reloadCatalog() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void officialBuilderSetContainsFourteenContracts() {
        assertEquals(14, JobRegistry.officialBuilders().size());
    }

    @TestFactory
    Stream<DynamicTest> everyOfficialBuilderSatisfiesFamilyContract() {
        return JobRegistry.officialBuilders().stream()
                .map(job -> DynamicTest.dynamicTest(job.id().toString(), () -> verifyContract(job)));
    }

    @Test
    void villagerBaseDescriptionReloadsFromRuntimeAbilityConfig() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        Map<String, Double> allay = new LinkedHashMap<>(abilities.get(VillagerTowers.T1_ALLAY_TOWER.id()));
        allay.put("healAmount", 123.0);
        abilities.put(VillagerTowers.T1_ALLAY_TOWER.id(), allay);

        ProductionTowerCatalogs.reloadBuiltIns(new TowerBalanceConfig(
                defaults.towers(),
                defaults.upgradeCosts(),
                abilities,
                defaults.illusionCloneQueue(),
                defaults.villagerAdv(),
                defaults.schemaVersion()
        ));

        String description = String.join(" ", ProductionTowerCatalog.find(VillagerTowers.T1_ALLAY_TOWER.id())
                .orElseThrow().type().description());
        assertTrue(description.contains("123"));
    }

    @Test
    void targetAbilityRejectsInvalidNegativeValue() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        Map<String, Double> zombie = new LinkedHashMap<>(abilities.get(UndeadTowers.T1_ZOMBIE_TOWER.id()));
        zombie.put("lifeStealRatio", -0.1);
        abilities.put(UndeadTowers.T1_ZOMBIE_TOWER.id(), zombie);

        TowerBalanceConfig invalid = new TowerBalanceConfig(
                defaults.towers(),
                defaults.upgradeCosts(),
                abilities,
                defaults.illusionCloneQueue(),
                defaults.villagerAdv(),
                defaults.schemaVersion()
        );
        assertThrows(IllegalArgumentException.class, invalid::validateForRuntime);
    }

    private static void verifyContract(SemionJob job) {
        List<ProductionTowerCatalog.CatalogEntry> entries = ProductionTowerCatalog.all().stream()
                .filter(entry -> job.includesTowerInCatalog(entry.type()))
                .toList();
        assertFalse(entries.isEmpty(), "Official builder has no catalog towers: " + job.id());
        assertTrue(entries.stream().anyMatch(ProductionTowerCatalog.CatalogEntry::starter),
                "Official builder has no starter tower: " + job.id());

        UUID owner = UUID.nameUUIDFromBytes(("official-contract:" + job.id()).getBytes());
        GridPosition original = new GridPosition(1, 64, 2);
        GridPosition current = new GridPosition(4, 65, 6);
        for (ProductionTowerCatalog.CatalogEntry entry : entries) {
            TowerType type = entry.type();
            List<SemionJob> owners = JobRegistry.all().stream()
                    .filter(candidate -> candidate.includesTowerInCatalog(type))
                    .toList();
            assertEquals(List.of(job), owners, "Tower must have one catalog owner: " + type.id());
            assertFalse(type.description().isEmpty(), "Tower description is missing: " + type.id());
            assertTrue(type.description().stream().noneMatch(BuilderFamilyContractTest::hasPlaceholder),
                    "Tower description has an unresolved placeholder: " + type.id());
            assertNotEquals(BuilderPalette.DEFAULT, TowerVfxService.paletteFor(type),
                    "Official tower uses the fallback VFX palette: " + type.id());

            Tower runtime = entry.create(owner, TeamId.RED, 1, original, current);
            assertEquals(type.id(), runtime.type().id(), "Factory changed tower type: " + type.id());
            assertEquals(original, runtime.originalPosition(), "Factory lost original position: " + type.id());
            assertEquals(current, runtime.position(), "Factory lost current position: " + type.id());

            for (TowerUpgradeOption upgrade : ProductionTowerCatalog.upgrades(type)) {
                TowerType target = ProductionTowerCatalog.find(upgrade.targetType().id()).orElseThrow().type();
                assertTrue(job.includesTowerInCatalog(target),
                        "Upgrade crosses builder ownership: " + type.id() + " -> " + target.id());
                assertTrue(upgrade.mineralCost() >= 0,
                        "Upgrade has a negative runtime cost: " + type.id() + " -> " + target.id());
            }
        }
    }

    private static boolean hasPlaceholder(String line) {
        return line.contains("{ability.") || line.contains("{stat.");
    }
}
