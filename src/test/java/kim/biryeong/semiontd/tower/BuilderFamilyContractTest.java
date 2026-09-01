package kim.biryeong.semiontd.tower;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
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

}
