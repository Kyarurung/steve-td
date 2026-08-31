package kim.biryeong.semiontd.tower.legion;

import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.assertFamilyClosed;
import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.assertResolvedDescriptions;
import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.upgrade;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.job.LegionTowerJob;
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

final class LegionCatalogContractTest {
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
                LegionTowerJob.ID,
                LegionTowers.T1_PARROT_TOWER.id(),
                LegionTowers.all(),
                Map.ofEntries(
                        Map.entry(LegionTowers.T1_CHICKEN.id(), 1),
                        Map.entry(LegionTowers.T2_CHICKEN_TOWER.id(), 2),
                        Map.entry(LegionTowers.T2_DPS_CHICKEN_TOWER.id(), 2),
                        Map.entry(LegionTowers.T1_SLIME_TOWER.id(), 1),
                        Map.entry(LegionTowers.T2_SLIME_TOWER.id(), 2),
                        Map.entry(LegionTowers.T1_PENGUIN.id(), 1),
                        Map.entry(LegionTowers.T2_PENGUIN.id(), 2),
                        Map.entry(LegionTowers.T1_PARROT_TOWER.id(), 1),
                        Map.entry(LegionTowers.T2_PARROT_TOWER.id(), 2),
                        Map.entry(LegionTowers.T1_GOAT_TOWER.id(), 1),
                        Map.entry(LegionTowers.T2_STRONG_GOAT_TOWER.id(), 2),
                        Map.entry(LegionTowers.T3_EXTREME_GOAT_TOWER.id(), 3),
                        Map.entry(LegionTowers.ILLUSION_TOWER.id(), 1)
                ),
                upgrades(),
                LegionCatalogContractTest::runtimeType
        ), defaults);
    }

    @Test
    void partialConfigBackfillsTheWholeFamilyAndReloadsConfiguredAbility() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        TowerBalanceConfig partial = new TowerBalanceConfig(
                Map.of(),
                Map.of(),
                Map.of(LegionTowers.T1_PARROT_TOWER.id(), Map.of("maxAttackStacks", 7.0))
        );

        TowerBalanceConfig merged = partial.withMissingDefaults(defaults);
        LegionTowers.all().forEach(type -> assertTrue(merged.towers().containsKey(type.id()), type.id()));
        upgrades().forEach(edge -> assertTrue(merged.upgradeCosts().containsKey(edge.configKey()), edge.configKey()));
        assertEquals(7.0, merged.ability(LegionTowers.T1_PARROT_TOWER.id(), "maxAttackStacks", -1.0));
        assertTrue(merged.abilities().get(LegionTowers.T1_PARROT_TOWER.id()).containsKey("attackStackBonus"));

        ProductionTowerCatalogs.reloadBuiltIns(merged);
        assertEquals(7, LegionConfig.RUNTIME.integer(
                LegionTowers.T1_PARROT_TOWER, LegionAbilityKey.MAX_ATTACK_STACKS
        ));
        assertResolvedDescriptions(LegionTowers.all());
    }

    private static List<UpgradeExpectation> upgrades() {
        return List.of(
                upgrade(LegionTowers.T1_CHICKEN, LegionTowers.T2_CHICKEN_TOWER.id(), LegionTowers.T2_CHICKEN_TOWER),
                upgrade(LegionTowers.T1_CHICKEN, LegionTowers.T2_DPS_CHICKEN_TOWER.id(), LegionTowers.T2_DPS_CHICKEN_TOWER),
                upgrade(LegionTowers.T1_SLIME_TOWER, LegionTowers.T2_SLIME_TOWER.id(), LegionTowers.T2_SLIME_TOWER),
                upgrade(LegionTowers.T1_PENGUIN, LegionTowers.T2_PENGUIN.id(), LegionTowers.T2_PENGUIN),
                upgrade(LegionTowers.T1_PARROT_TOWER, LegionTowers.T2_PARROT_TOWER.id(), LegionTowers.T2_PARROT_TOWER),
                upgrade(LegionTowers.T1_GOAT_TOWER, LegionTowers.T2_STRONG_GOAT_TOWER.id(), LegionTowers.T2_STRONG_GOAT_TOWER),
                upgrade(LegionTowers.T2_STRONG_GOAT_TOWER, LegionTowers.T3_EXTREME_GOAT_TOWER.id(), LegionTowers.T3_EXTREME_GOAT_TOWER)
        );
    }

    private static Class<? extends Tower> runtimeType(TowerType type) {
        if (type.id().equals(LegionTowers.T1_SLIME_TOWER.id())
                || type.id().equals(LegionTowers.T2_SLIME_TOWER.id())) return LegionSlimeTower.class;
        if (type.id().equals(LegionTowers.T1_PARROT_TOWER.id())
                || type.id().equals(LegionTowers.T2_PARROT_TOWER.id())) return LegionParrotTower.class;
        if (type.id().equals(LegionTowers.T1_GOAT_TOWER.id())
                || type.id().equals(LegionTowers.T2_STRONG_GOAT_TOWER.id())
                || type.id().equals(LegionTowers.T3_EXTREME_GOAT_TOWER.id())) return LegionGoatTower.class;
        if (type.id().equals(LegionTowers.ILLUSION_TOWER.id())) return LegionGlobalIllusionTower.class;
        return LegionSplashIllusionTower.class;
    }
}
