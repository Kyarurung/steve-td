package kim.biryeong.semiontd.tower;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.job.AnimalTowerJob;
import kim.biryeong.semiontd.job.JobRegistry;
import kim.biryeong.semiontd.job.LegionTowerJob;
import kim.biryeong.semiontd.job.SemionJob;
import kim.biryeong.semiontd.job.UndeadTowerJob;
import kim.biryeong.semiontd.job.VillagerAdvTowerJob;
import kim.biryeong.semiontd.job.VillagerTowerJob;
import kim.biryeong.semiontd.tower.animal.AnimalTowers;
import kim.biryeong.semiontd.tower.legion.LegionTowers;
import kim.biryeong.semiontd.tower.undead.UndeadTowers;
import kim.biryeong.semiontd.tower.villager.VillagerTowers;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BuilderFamilyContractTest {
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
    void towerDefinitionsAreTheSingleCatalogOwnershipSource() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());

        assertOwned(VillagerTowerJob.ID.toString(), VillagerTowers.baseTowers());
        assertOwned(VillagerAdvTowerJob.ID.toString(), VillagerTowers.advTowers());
        assertOwned(UndeadTowerJob.ID.toString(), UndeadTowers.all());
        assertOwned(AnimalTowerJob.ID.toString(), AnimalTowers.all());
        assertOwned(LegionTowerJob.ID.toString(), LegionTowers.all());

        targetTowers().forEach(type -> {
            List<SemionJob> owners = JobRegistry.all().stream()
                    .filter(job -> job.includesTowerInCatalog(type))
                    .toList();
            assertEquals(1, owners.size(), "Tower must have exactly one catalog owner: " + type.id());
            assertTrue(ProductionTowerCatalog.find(type.id()).isPresent(), "Missing catalog entry: " + type.id());
        });
    }

    @Test
    void everyResolvedTargetDescriptionHasNoPlaceholder() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
        targetTowers().forEach(type -> {
            TowerType resolved = ProductionTowerCatalog.find(type.id()).orElseThrow().type();
            assertFalse(resolved.description().isEmpty(), "Missing description: " + type.id());
            assertTrue(resolved.description().stream().noneMatch(line -> line.contains("{ability.") || line.contains("{stat.")),
                    "Unresolved placeholder: " + type.id());
        });
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

    private static void assertOwned(String jobId, List<TowerType> expected) {
        SemionJob job = JobRegistry.all().stream()
                .filter(candidate -> candidate.id().toString().equals(jobId))
                .findFirst()
                .orElseThrow();
        Set<String> actual = ProductionTowerCatalog.all().stream()
                .map(ProductionTowerCatalog.CatalogEntry::type)
                .filter(job::includesTowerInCatalog)
                .map(TowerType::id)
                .collect(Collectors.toSet());
        assertEquals(expected.stream().map(TowerType::id).collect(Collectors.toSet()), actual);
    }

    private static Stream<TowerType> targetTowers() {
        return Stream.of(
                VillagerTowers.all(),
                UndeadTowers.all(),
                AnimalTowers.all(),
                LegionTowers.all()
        ).flatMap(List::stream);
    }
}
