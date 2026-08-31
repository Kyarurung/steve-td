package kim.biryeong.semiontd.tower.warlock;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WarlockConfigValidationTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void discreteWarlockConfigRejectsFractionalAndOverflowValues() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();

        for (double invalid : List.of(1349.5, (double) Integer.MAX_VALUE + 1.0)) {
            Map<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
            Map<String, Double> global = new LinkedHashMap<>(abilities.get(WarlockTowers.CONFIG_ID));
            global.put("awakeningKills", invalid);
            abilities.put(WarlockTowers.CONFIG_ID, global);
            TowerBalanceConfig invalidConfig = new TowerBalanceConfig(
                    defaults.towers(),
                    defaults.upgradeCosts(),
                    abilities
            );

            assertThrows(IllegalArgumentException.class, invalidConfig::validateForRuntime);
        }

        Map<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        Map<String, Double> ranged = new LinkedHashMap<>(abilities.get(WarlockTowers.RANGED_WARLOCK_TOWER.id()));
        ranged.put("splashEvery", 1.5);
        abilities.put(WarlockTowers.RANGED_WARLOCK_TOWER.id(), ranged);
        TowerBalanceConfig fractionalSplashPeriod = new TowerBalanceConfig(
                defaults.towers(),
                defaults.upgradeCosts(),
                abilities
        );
        assertThrows(IllegalArgumentException.class, fractionalSplashPeriod::validateForRuntime);
    }
}
