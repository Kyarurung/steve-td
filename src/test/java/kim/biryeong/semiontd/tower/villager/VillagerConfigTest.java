package kim.biryeong.semiontd.tower.villager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VillagerConfigTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void restoreDefaults() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void abilityKeysReadFromActiveRuntimeConfig() {
        TowerBalanceConfig config = TowerBalanceConfig.defaultConfig();
        TowerBalanceRuntime.apply(config);

        assertEquals(
                config.ability(VillagerTowers.T2_LIBRARIAN_TOWER.id(), "splashRadius", -1.0),
                VillagerConfig.RUNTIME.value(
                        VillagerTowers.T2_LIBRARIAN_TOWER,
                        VillagerAbilityKey.SPLASH_RADIUS
                ),
                0.0001
        );
        assertEquals(
                config.ability(VillagerTowers.T3_CLERIC_TOWER.id(), "extraAttackEvery", -1.0),
                VillagerConfig.RUNTIME.integer(
                        VillagerTowers.T3_CLERIC_TOWER, VillagerAbilityKey.EXTRA_ATTACK_EVERY
                ),
                0.0001
        );
    }
}
