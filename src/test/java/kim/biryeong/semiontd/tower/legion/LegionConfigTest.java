package kim.biryeong.semiontd.tower.legion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LegionConfigTest {
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
    void abilityKeysAndIllusionProfileReadFromActiveRuntimeConfig() {
        TowerBalanceConfig config = TowerBalanceConfig.defaultConfig();
        TowerBalanceRuntime.apply(config);

        assertEquals(
                config.ability(LegionTowers.T1_PARROT_TOWER.id(), "maxAttackStacks", -1.0),
                LegionConfig.RUNTIME.integer(
                        LegionTowers.T1_PARROT_TOWER, LegionAbilityKey.MAX_ATTACK_STACKS
                ),
                0.0001
        );
        LegionIllusionProfile profile = LegionConfig.RUNTIME.illusionProfile(
                LegionTowers.T1_CHICKEN, LegionIllusionProfile.defaults()
        );
        assertEquals(
                config.ability(LegionTowers.T1_CHICKEN.id(), "cloneCount", -1.0),
                profile.cloneCount(),
                0.0001
        );
    }
}
