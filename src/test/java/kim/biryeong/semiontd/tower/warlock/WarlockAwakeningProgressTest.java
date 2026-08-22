package kim.biryeong.semiontd.tower.warlock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WarlockAwakeningTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void resetState() {
        WarlockAwakening.clearAllForTesting();
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void awakeningUnlocksExactlyAtConfiguredKillThreshold() {
        UUID owner = UUID.randomUUID();

        for (int kill = 1; kill < 1250; kill++) {
            assertFalse(WarlockAwakening.recordKill(owner));
        }
        WarlockAwakening.Snapshot locked = WarlockAwakening.snapshot(owner);
        assertEquals(1249L, locked.kills());
        assertEquals(1250L, locked.requiredKills());
        assertFalse(locked.unlocked());

        assertTrue(WarlockAwakening.recordKill(owner));
        WarlockAwakening.Snapshot unlocked = WarlockAwakening.snapshot(owner);
        assertEquals(1250L, unlocked.kills());
        assertTrue(unlocked.unlocked());
        assertFalse(WarlockAwakening.recordKill(owner));
    }

    @Test
    void clearRemovesProgressForTheNextMatch() {
        UUID owner = UUID.randomUUID();
        WarlockAwakening.recordKill(owner);

        WarlockAwakening.clear(owner);

        assertEquals(0L, WarlockAwakening.snapshot(owner).kills());
        assertFalse(WarlockAwakening.snapshot(owner).unlocked());
    }
}
