package kim.biryeong.semiontd.tower.illager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class IllagerRaidRulesTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void resetBalance() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void configuredRaidBonusesScaleAndStopAtCaps() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
        IllagerRaidRules rules = new IllagerRaidRules(IllagerConfig.RUNTIME);

        assertEquals(0.08, rules.attackSpeedBonus(4), 0.0001);
        assertEquals(0.24, rules.damageBonus(4), 0.0001);
        assertEquals(0.20, rules.attackSpeedBonus(100), 0.0001);
        assertEquals(0.60, rules.damageBonus(100), 0.0001);
    }

    @Test
    void negativeTowerCountsDoNotCreateBonus() {
        assertEquals(0.0, IllagerRaidRules.cappedTowerBonus(-1, 0.02, 0.20), 0.0001);
    }

    @Test
    void killGaugeUsesIncomeAndMarkSources() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
        IllagerRaidRules rules = new IllagerRaidRules(IllagerConfig.RUNTIME);

        assertEquals(3, rules.killGauge(false, false));
        assertEquals(13, rules.killGauge(false, true));
        assertEquals(6, rules.killGauge(true, false));
        assertEquals(16, rules.killGauge(true, true));
    }
}
