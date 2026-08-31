package kim.biryeong.semiontd.tower.undead;

import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.assertFamilyClosed;
import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.assertResolvedDescriptions;
import static kim.biryeong.semiontd.tower.TowerIntegrationSliceAssertions.upgrade;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.job.UndeadTowerJob;
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

final class UndeadCatalogContractTest {
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
                UndeadTowerJob.ID,
                UndeadTowers.T1_ZOMBIE_TOWER.id(),
                UndeadTowers.all(),
                Map.of(
                        UndeadTowers.T1_ZOMBIE_TOWER.id(), 1,
                        UndeadTowers.T2_ZOMBIE_TOWER.id(), 2,
                        UndeadTowers.T3_ZOMBIE_TOWER.id(), 3,
                        UndeadTowers.T1_SKELETON_TOWER.id(), 1,
                        UndeadTowers.T2_RANGED_SKELETON_TOWER.id(), 2,
                        UndeadTowers.T2_MELEE_TOWER.id(), 2,
                        UndeadTowers.T3_RANGED_SKELETON_TOWER.id(), 3,
                        UndeadTowers.T3_MELEE_TOWER.id(), 3,
                        UndeadTowers.T1_UNDEAD_ANIMAL_TOWER.id(), 1,
                        UndeadTowers.T2_UNDEAD_ANIMAL_TOWER.id(), 2
                ),
                upgrades(),
                UndeadCatalogContractTest::runtimeType
        ), defaults);
    }

    @Test
    void partialConfigBackfillsTheWholeFamilyAndReloadsConfiguredAbility() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        TowerBalanceConfig partial = new TowerBalanceConfig(
                Map.of(),
                Map.of(),
                Map.of(UndeadTowers.T1_ZOMBIE_TOWER.id(), Map.of("lifeStealRatio", 0.42))
        );

        TowerBalanceConfig merged = partial.withMissingDefaults(defaults);
        UndeadTowers.all().forEach(type -> assertTrue(merged.towers().containsKey(type.id()), type.id()));
        upgrades().forEach(edge -> assertTrue(merged.upgradeCosts().containsKey(edge.configKey()), edge.configKey()));
        assertEquals(0.42, merged.ability(UndeadTowers.T1_ZOMBIE_TOWER.id(), "lifeStealRatio", -1.0));
        assertTrue(merged.abilities().get(UndeadTowers.T1_ZOMBIE_TOWER.id()).containsKey("killDamageBoost"));

        ProductionTowerCatalogs.reloadBuiltIns(merged);
        assertEquals(0.42, UndeadConfig.RUNTIME.value(
                UndeadTowers.T1_ZOMBIE_TOWER, UndeadAbilityKey.LIFE_STEAL_RATIO
        ));
        assertResolvedDescriptions(UndeadTowers.all());
    }

    private static List<UpgradeExpectation> upgrades() {
        return List.of(
                upgrade(UndeadTowers.T1_ZOMBIE_TOWER, "t2_zombie_tower", UndeadTowers.T2_ZOMBIE_TOWER),
                upgrade(UndeadTowers.T2_ZOMBIE_TOWER, "t3_zombie_tower", UndeadTowers.T3_ZOMBIE_TOWER),
                upgrade(UndeadTowers.T1_SKELETON_TOWER, "t2_ranged_skeleton_tower", UndeadTowers.T2_RANGED_SKELETON_TOWER),
                upgrade(UndeadTowers.T1_SKELETON_TOWER, "t2_melee_tower", UndeadTowers.T2_MELEE_TOWER),
                upgrade(UndeadTowers.T2_RANGED_SKELETON_TOWER, "t3_ranged_skeleton_tower", UndeadTowers.T3_RANGED_SKELETON_TOWER),
                upgrade(UndeadTowers.T2_MELEE_TOWER, "t3_melee_tower", UndeadTowers.T3_MELEE_TOWER),
                upgrade(UndeadTowers.T1_UNDEAD_ANIMAL_TOWER, "t2_undead_animal_tower", UndeadTowers.T2_UNDEAD_ANIMAL_TOWER)
        );
    }

    private static Class<? extends Tower> runtimeType(TowerType type) {
        if (type.id().equals(UndeadTowers.T1_ZOMBIE_TOWER.id())) return UndeadZombieTower.class;
        if (type.id().equals(UndeadTowers.T2_ZOMBIE_TOWER.id())) return UndeadHuskTower.class;
        if (type.id().equals(UndeadTowers.T3_ZOMBIE_TOWER.id())) return UndeadDrownedTower.class;
        if (type.id().equals(UndeadTowers.T2_RANGED_SKELETON_TOWER.id())
                || type.id().equals(UndeadTowers.T3_RANGED_SKELETON_TOWER.id())) {
            return UndeadRangedSkeletonTower.class;
        }
        if (type.id().equals(UndeadTowers.T2_MELEE_TOWER.id())
                || type.id().equals(UndeadTowers.T3_MELEE_TOWER.id())) {
            return UndeadMeleeSkeletonTower.class;
        }
        if (type.id().equals(UndeadTowers.T1_UNDEAD_ANIMAL_TOWER.id())
                || type.id().equals(UndeadTowers.T2_UNDEAD_ANIMAL_TOWER.id())) {
            return UndeadAnimalTower.class;
        }
        return ProductionTower.class;
    }
}
