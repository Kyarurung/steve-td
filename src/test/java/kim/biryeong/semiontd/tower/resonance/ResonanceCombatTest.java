package kim.biryeong.semiontd.tower.resonance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResonanceCombatTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void applyDefaults() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void focusAndBloomUseResolvedRuntimeBonuses() {
        ResonanceTower focus = ResonanceTestFixture.tower(ResonanceTowers.FOCUS_CORE, 0, 0);
        ResonanceController.refresh(ResonanceTestFixture.with(focus, ResonanceTestFixture.differentAspects()));
        assertEquals(180.0, focus.modifyAttackDamage(null, null, 100.0), 0.0001);
        assertEquals(10, focus.adjustAttackInterval(20));

        ResonanceTower bloom = ResonanceTestFixture.tower(ResonanceTowers.AMPLIFY_CORE, 0, 0);
        ResonanceController.refresh(ResonanceTestFixture.with(bloom, List.of(
                ResonanceTestFixture.tower(ResonanceTowers.FOCUS_CRYSTAL, 1, 0),
                ResonanceTestFixture.tower(ResonanceTowers.FOCUS_PRISM, -1, 0),
                ResonanceTestFixture.tower(ResonanceTowers.WAVE_CRYSTAL, 0, 1),
                ResonanceTestFixture.tower(ResonanceTowers.WAVE_PRISM, 0, -1),
                ResonanceTestFixture.tower(ResonanceTowers.FROST_CRYSTAL, 1, 1)
        )));
        assertEquals(60.0, bloom.modifyIncomingDamage(null, null, 100.0), 0.0001);
    }

    @Test
    void upgradeCopiesWaveSnapshotAndPulseCharge() {
        ResonanceTower previous = ResonanceTestFixture.tower(ResonanceTowers.FOCUS_PRISM, 0, 0);
        ResonanceController.refresh(ResonanceTestFixture.with(previous, ResonanceTestFixture.differentAspects()));
        previous.chargeReady(3);
        ResonanceTower upgraded = ResonanceTestFixture.tower(ResonanceTowers.FOCUS_CORE, 0, 0);
        upgraded.copyFrom(previous, 180);
        assertEquals(previous.resonanceSnapshot(), upgraded.resonanceSnapshot());
    }
}
