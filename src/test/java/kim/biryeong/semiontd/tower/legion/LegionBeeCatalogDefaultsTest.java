package kim.biryeong.semiontd.tower.legion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import kim.biryeong.semiontd.config.TowerBalanceConfig;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LegionBeeCatalogDefaultsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void beeAbilitiesAndUpgradeEdgesRemainInTheFamilyDefaults() {
        TowerBalanceConfig config = TowerBalanceConfig.defaultConfig();

        assertEquals(3.0, config.ability(
                LegionTowers.T1_BEE_TOWER.id(), "poisonDamagePerStack", -1.0
        ), 0.0001);
        assertEquals(5.0, config.ability(
                LegionTowers.T2_BEE_TOWER.id(), "maxPoisonStacks", -1.0
        ), 0.0001);
        assertEquals(140.0, config.ability(
                LegionTowers.T3_BEE_TOWER.id(), "poisonDurationTicks", -1.0
        ), 0.0001);
        assertEquals(160, config.upgradeCost(
                LegionTowers.T1_BEE_TOWER.id(), LegionTowers.T2_BEE_TOWER.id(), -1
        ));
        assertEquals(310, config.upgradeCost(
                LegionTowers.T2_BEE_TOWER.id(), LegionTowers.T3_BEE_TOWER.id(), -1
        ));
    }
}
