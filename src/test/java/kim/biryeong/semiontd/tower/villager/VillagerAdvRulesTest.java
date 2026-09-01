package kim.biryeong.semiontd.tower.villager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import kim.biryeong.semiontd.config.TowerBalanceConfig;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VillagerAdvRulesTest {
    private TowerBalanceConfig.VillagerAdvConfig config;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void loadConfig() {
        config = TowerBalanceConfig.defaultConfig().villagerAdv();
    }

    @Test
    void experienceAndReputationClampToConfiguredBounds() {
        assertEquals(config.resolvedExperienceMax(),
                VillagerAdvRules.nextExperience(config.resolvedExperienceMax(), 3, config));
        assertEquals(0.0, VillagerAdvRules.nextReputation(0.25, -1.0, config));
        assertEquals(config.resolvedReputationMax(),
                VillagerAdvRules.nextReputation(config.resolvedReputationMax(), 1.0, config));
    }

    @Test
    void bonusesAndUpgradeRequirementHonorExactBoundaries() {
        assertEquals(0.3, VillagerAdvRules.buff(3.0, 1.0, 0.1, 0.3), 1.0E-9);
        assertTrue(VillagerAdvRules.meetsUpgradeRequirement(9.9999995, 10.0));
        assertFalse(VillagerAdvRules.meetsUpgradeRequirement(9.9, 10.0));
    }
}
