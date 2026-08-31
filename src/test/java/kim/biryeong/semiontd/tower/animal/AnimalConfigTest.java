package kim.biryeong.semiontd.tower.animal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AnimalConfigTest {
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
                config.ability(AnimalTowers.T1_PIG_TOWER.id(), "maxStacks", -1.0),
                AnimalConfig.RUNTIME.integer(AnimalTowers.T1_PIG_TOWER, AnimalAbilityKey.MAX_STACKS),
                0.0001
        );
        assertEquals(
                config.ability(AnimalTowers.T4_FOX_LEADER_TOWER.id(), "leaderAuraRadius", -1.0),
                AnimalConfig.RUNTIME.value(
                        AnimalTowers.T4_FOX_LEADER_TOWER, AnimalAbilityKey.LEADER_AURA_RADIUS
                ),
                0.0001
        );
    }
}
