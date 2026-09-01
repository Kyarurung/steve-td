package kim.biryeong.semiontd.tower.villager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VillagerAdvProgressionControllerTest {
    private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000123");

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void clearState() {
        VillagerAdvStates.clear(OWNER);
    }

    @Test
    void experienceSnapshotCalculationIsPureAndKeepsSnapshotIdentity() {
        VillagerAdvExperienceSnapshot snapshot = new VillagerAdvExperienceSnapshot(OWNER, null, null, 2, 7.0);
        List<VillagerAdvExperienceResult> results = VillagerAdvProgressionController.calculateExperienceGains(
                List.of(snapshot),
                TowerBalanceConfig.defaultConfig().villagerAdv()
        );
        assertEquals(1, results.size());
        assertEquals(OWNER, results.getFirst().ownerPlayer());
        assertEquals(VillagerAdvRules.nextExperience(7.0, 2, TowerBalanceConfig.defaultConfig().villagerAdv()),
                results.getFirst().nextExperience());
    }

    @Test
    void keyedStateClearsReputationAndPendingResultsTogether() {
        TowerBalanceConfig.VillagerAdvConfig config = TowerBalanceConfig.defaultConfig().villagerAdv();
        VillagerAdvStates.addReputation(OWNER, 10.0, config);
        VillagerAdvStates.enqueue(List.of(new VillagerAdvExperienceResult(OWNER, null, null, 5.5)));
        VillagerAdvProgressionController.onEliminated(OWNER);
        assertEquals(0.0, VillagerAdvStates.reputation(OWNER));
        assertEquals(List.of(), VillagerAdvStates.drain(OWNER));
    }

    @Test
    void matchLifecycleCallbacksResetTheSamePlayerKey() {
        TowerBalanceConfig.VillagerAdvConfig config = TowerBalanceConfig.defaultConfig().villagerAdv();
        VillagerAdvStates.addReputation(OWNER, 10.0, config);
        VillagerAdvProgressionController.onMatchStarted(OWNER);
        assertEquals(0.0, VillagerAdvStates.reputation(OWNER));
        VillagerAdvStates.addReputation(OWNER, 10.0, config);
        VillagerAdvProgressionController.onMatchClosed(OWNER);
        assertEquals(0.0, VillagerAdvStates.reputation(OWNER));
    }
}
