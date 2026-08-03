package kim.biryeong.semiontd.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import kim.biryeong.semiontd.config.CombatSpeedConfig;
import org.junit.jupiter.api.Test;

final class SemionGameManagerCombatSpeedTest {
    @Test
    void accelerationRequiresAnEnabledNormalWaveWithoutSandboxOrOverload() {
        CombatSpeedConfig enabled = new CombatSpeedConfig(true, 40.0F, 25.0);

        assertEquals(40.0F, SemionGameManager.combatTickRateTarget(enabled, RoundPhase.LANE_WAVE, false, false));
        assertEquals(20.0F, SemionGameManager.combatTickRateTarget(enabled, RoundPhase.PREPARE_AND_SUMMON, false, false));
        assertEquals(20.0F, SemionGameManager.combatTickRateTarget(enabled, RoundPhase.LANE_WAVE, true, false));
        assertEquals(20.0F, SemionGameManager.combatTickRateTarget(enabled, RoundPhase.LANE_WAVE, false, true));
        assertEquals(20.0F, SemionGameManager.combatTickRateTarget(CombatSpeedConfig.defaultConfig(), RoundPhase.LANE_WAVE, false, false));
    }
}
