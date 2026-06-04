package kim.biryeong.semiontd.tower.animal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BeeTowerCatalogTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void resetTowerBalance() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void defaultBalanceConfigIncludesBeeTowerStatsAbilitiesAndUpgradeCosts() {
        TowerBalanceConfig config = TowerBalanceConfig.defaultConfig();

        assertTrue(config.towers().containsKey(AnimalTowers.T1_BEE_TOWER.id()));
        assertTrue(config.towers().containsKey(AnimalTowers.T2_BEE_TOWER.id()));
        assertTrue(config.towers().containsKey(AnimalTowers.T3_BEE_TOWER.id()));
        assertEquals(3.0, config.ability(AnimalTowers.T1_BEE_TOWER.id(), "poisonDamagePerStack", -1.0), 0.0001);
        assertEquals(5.0, config.ability(AnimalTowers.T2_BEE_TOWER.id(), "maxPoisonStacks", -1.0), 0.0001);
        assertEquals(140.0, config.ability(AnimalTowers.T3_BEE_TOWER.id(), "poisonDurationTicks", -1.0), 0.0001);
        assertEquals(160, config.upgradeCost(AnimalTowers.T1_BEE_TOWER.id(), AnimalTowers.T2_BEE_TOWER.id(), -1));
        assertEquals(310, config.upgradeCost(AnimalTowers.T2_BEE_TOWER.id(), AnimalTowers.T3_BEE_TOWER.id(), -1));
    }

    @Test
    void beeDescriptionsRenderCurrentConfigValues() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        abilities.put(AnimalTowers.T1_BEE_TOWER.id(), Map.of(
                "poisonDamagePerStack", 4.5,
                "maxPoisonStacks", 6.0,
                "poisonDurationTicks", 100.0,
                "poisonTickIntervalTicks", 10.0
        ));
        TowerBalanceRuntime.apply(new TowerBalanceConfig(defaults.towers(), defaults.upgradeCosts(), abilities));

        var description = TowerBalanceRuntime.resolve(AnimalTowers.T1_BEE_TOWER).description();

        assertTrue(description.stream().anyMatch(line -> line.contains("4.5") && line.contains("스택당")));
        assertTrue(description.stream().anyMatch(line -> line.contains("최대 6스택")));
        assertTrue(description.stream().anyMatch(line -> line.contains("5초")));
        assertTrue(description.stream().anyMatch(line -> line.contains("0.5초마다")));
    }
}
