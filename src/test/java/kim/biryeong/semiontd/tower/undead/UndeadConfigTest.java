package kim.biryeong.semiontd.tower.undead;

import static org.junit.jupiter.api.Assertions.assertEquals;

import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UndeadConfigTest {
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
                config.ability(UndeadTowers.T1_ZOMBIE_TOWER.id(), "lifeStealRatio", -1.0),
                UndeadConfig.RUNTIME.value(UndeadTowers.T1_ZOMBIE_TOWER, UndeadAbilityKey.LIFE_STEAL_RATIO),
                0.0001
        );
        assertEquals(
                config.ability(UndeadTowers.T2_MELEE_TOWER.id(), "stackCap", -1.0),
                UndeadConfig.RUNTIME.integer(
                        UndeadTowers.T2_MELEE_TOWER, UndeadAbilityKey.STACK_CAP
                ),
                0.0001
        );
    }
}
