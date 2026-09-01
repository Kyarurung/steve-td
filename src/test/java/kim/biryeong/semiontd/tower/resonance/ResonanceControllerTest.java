package kim.biryeong.semiontd.tower.resonance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResonanceControllerTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void applyDefaults() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @AfterEach
    void restoreDefaults() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void differentNearbyTowersRaiseResonanceAtOneThreeFiveLinks() {
        ResonanceTower focus = ResonanceTestFixture.tower(ResonanceTowers.FOCUS_CORE, 0, 0);
        List<ResonanceTower> links = ResonanceTestFixture.differentAspects();
        for (int count = 0; count <= 5; count++) {
            ResonanceController.refresh(ResonanceTestFixture.with(focus, links.subList(0, count)));
            assertEquals(count, focus.resonanceLinks());
            assertEquals(count >= 5 ? 3 : count >= 3 ? 2 : count >= 1 ? 1 : 0, focus.resonanceLevel());
        }
    }

    @Test
    void newWaveRemovesMissingProviderAuraAndDeadLinks() {
        ResonanceTower recipient = ResonanceTestFixture.tower(ResonanceTowers.FOCUS_CORE, 1, 0);
        ResonanceTower bloom = ResonanceTestFixture.tower(ResonanceTowers.AMPLIFY_CORE, 0, 0);
        List<kim.biryeong.semiontd.tower.Tower> towers = new ArrayList<>(ResonanceTestFixture.with(bloom, List.of(
                ResonanceTestFixture.tower(ResonanceTowers.FOCUS_CRYSTAL, -1, 0),
                ResonanceTestFixture.tower(ResonanceTowers.FOCUS_PRISM, 0, 1),
                ResonanceTestFixture.tower(ResonanceTowers.WAVE_CRYSTAL, 0, -1),
                ResonanceTestFixture.tower(ResonanceTowers.WAVE_PRISM, 1, 1),
                ResonanceTestFixture.tower(ResonanceTowers.FROST_CRYSTAL, -1, -1)
        )));
        towers.add(recipient);
        ResonanceController.refresh(towers);
        assertEquals(0.5, recipient.auraAttackSpeedBonus(), 0.0001);
        bloom.syncHealth(0.0);
        ResonanceController.refresh(towers);
        assertEquals(0.0, recipient.auraAttackSpeedBonus(), 0.0001);
    }

    @Test
    void duplicateAspectDoesNotLinkItsPeersButRaisesEveryDifferentAspectRecipient() {
        ResonanceTower firstFocus = ResonanceTestFixture.tower(ResonanceTowers.FOCUS_CORE, 0, 0);
        ResonanceTower wave = ResonanceTestFixture.tower(ResonanceTowers.WAVE_CRYSTAL, 1, 0);
        ResonanceTower frost = ResonanceTestFixture.tower(ResonanceTowers.FROST_CRYSTAL, 0, 1);
        List<kim.biryeong.semiontd.tower.Tower> towers = new ArrayList<>(List.of(firstFocus, wave, frost));
        ResonanceController.refresh(towers);
        assertEquals(2, firstFocus.resonanceLinks());
        assertEquals(2, wave.resonanceLinks());
        assertEquals(2, frost.resonanceLinks());

        ResonanceTower secondFocus = ResonanceTestFixture.tower(ResonanceTowers.FOCUS_PRISM, 1, 1);
        towers.add(secondFocus);
        ResonanceController.refresh(towers);

        assertEquals(2, firstFocus.resonanceLinks());
        assertEquals(2, secondFocus.resonanceLinks());
        assertEquals(3, wave.resonanceLinks());
        assertEquals(3, frost.resonanceLinks());
    }
}
