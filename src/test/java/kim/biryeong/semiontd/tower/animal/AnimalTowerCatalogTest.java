package kim.biryeong.semiontd.tower.animal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.job.AnimalTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.villager.VillagerTowers;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AnimalTowerCatalogTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void leaderUpgradesExistOnlyAfterTierThreeAndBelongToAnimalJob() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());

        assertLeaderUpgrade(AnimalTowers.T3_PIG_TOWER, AnimalTowers.T4_PIG_LEADER_TOWER);
        assertLeaderUpgrade(AnimalTowers.T3_WOLF_DPS_TOWER, AnimalTowers.T4_WOLF_LEADER_TOWER);
        assertLeaderUpgrade(AnimalTowers.T3_RABBIT_TOWER, AnimalTowers.T4_RABBIT_LEADER_TOWER);
        assertLeaderUpgrade(AnimalTowers.T3_FOX_TOWER, AnimalTowers.T4_FOX_LEADER_TOWER);
        assertTrue(ProductionTowerCatalog.upgrades(AnimalTowers.T2_PIG_TOWER).stream()
                .noneMatch(option -> option.targetType().id().equals(AnimalTowers.T4_PIG_LEADER_TOWER.id())));

        AnimalTowerJob job = new AnimalTowerJob();
        assertTrue(job.canUseTower(null, AnimalTowers.T4_PIG_LEADER_TOWER));
        assertTrue(job.includesTowerInCatalog(AnimalTowers.T4_WOLF_LEADER_TOWER));
        assertTrue(job.includesTowerInCatalog(AnimalTowers.T4_RABBIT_LEADER_TOWER));
        assertTrue(job.includesTowerInCatalog(AnimalTowers.T4_FOX_LEADER_TOWER));
        assertFalse(job.includesTowerInCatalog(VillagerTowers.T1_SPLASH_TOWER));
    }

    private static void assertLeaderUpgrade(TowerType tierThree, TowerType leader) {
        assertEquals(4, ProductionTowerCatalog.find(leader.id()).orElseThrow().tier());
        assertEquals(
                leader.id(),
                ProductionTowerCatalog.upgrade(tierThree, leader.id()).orElseThrow().targetType().id()
        );
    }
}
