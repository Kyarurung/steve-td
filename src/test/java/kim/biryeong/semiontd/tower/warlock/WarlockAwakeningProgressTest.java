package kim.biryeong.semiontd.tower.warlock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WarlockAwakeningProgressTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void resetState() {
        WarlockAwakeningProgress.clearAllForTesting();
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void awakeningUnlocksExactlyAtConfiguredKillThreshold() {
        UUID owner = UUID.randomUUID();

        for (int kill = 1; kill < 1250; kill++) {
            assertFalse(WarlockAwakeningProgress.recordKill(owner));
        }
        WarlockAwakeningProgress.Snapshot locked = WarlockAwakeningProgress.snapshot(owner);
        assertEquals(1249L, locked.kills());
        assertEquals(1250L, locked.requiredKills());
        assertFalse(locked.unlocked());

        assertTrue(WarlockAwakeningProgress.recordKill(owner));
        WarlockAwakeningProgress.Snapshot unlocked = WarlockAwakeningProgress.snapshot(owner);
        assertEquals(1250L, unlocked.kills());
        assertTrue(unlocked.unlocked());
        assertFalse(WarlockAwakeningProgress.recordKill(owner));
    }

    @Test
    void clearRemovesProgressForTheNextMatch() {
        UUID owner = UUID.randomUUID();
        WarlockAwakeningProgress.recordKill(owner);

        WarlockAwakeningProgress.clear(owner);

        assertEquals(0L, WarlockAwakeningProgress.snapshot(owner).kills());
        assertFalse(WarlockAwakeningProgress.snapshot(owner).unlocked());
    }

    @Test
    void unlockedStateIsChangedByKillEventsAndDoesNotRelockAfterConfigReload() {
        UUID owner = UUID.randomUUID();
        applyAwakeningKills(2.0);

        assertFalse(WarlockAwakeningProgress.recordKill(owner));
        applyAwakeningKills(1.0);
        assertFalse(WarlockAwakeningProgress.snapshot(owner).unlocked());

        assertTrue(WarlockAwakeningProgress.recordKill(owner));
        applyAwakeningKills(100.0);
        WarlockAwakeningProgress.Snapshot snapshot = WarlockAwakeningProgress.snapshot(owner);
        assertEquals(2L, snapshot.kills());
        assertEquals(100L, snapshot.requiredKills());
        assertTrue(snapshot.unlocked());
    }

    private static void applyAwakeningKills(double requiredKills) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        Map<String, Double> warlock = new LinkedHashMap<>(abilities.get(WarlockTowers.CONFIG_ID));
        warlock.put("awakeningKills", requiredKills);
        abilities.put(WarlockTowers.CONFIG_ID, warlock);
        TowerBalanceRuntime.apply(new TowerBalanceConfig(defaults.towers(), defaults.upgradeCosts(), abilities));
    }
}
