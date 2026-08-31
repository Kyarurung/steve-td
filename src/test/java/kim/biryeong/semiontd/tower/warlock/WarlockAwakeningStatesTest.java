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

class WarlockAwakeningStatesTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void resetState() {
        WarlockAwakeningStates.clearAllForTesting();
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void awakeningUnlocksExactlyAtConfiguredKillThreshold() {
        UUID owner = UUID.randomUUID();

        for (int kill = 1; kill < 1400; kill++) {
            assertFalse(WarlockAwakeningStates.recordKill(owner));
        }
        WarlockAwakeningStates.Snapshot locked = WarlockAwakeningStates.snapshot(owner);
        assertEquals(1399L, locked.kills());
        assertEquals(1400L, locked.requiredKills());
        assertFalse(locked.unlocked());

        assertTrue(WarlockAwakeningStates.recordKill(owner));
        WarlockAwakeningStates.Snapshot unlocked = WarlockAwakeningStates.snapshot(owner);
        assertEquals(1400L, unlocked.kills());
        assertTrue(unlocked.unlocked());
        assertFalse(WarlockAwakeningStates.recordKill(owner));
    }

    @Test
    void clearRemovesProgressForTheNextMatch() {
        UUID owner = UUID.randomUUID();
        WarlockAwakeningStates.recordKill(owner);

        WarlockAwakeningStates.clear(owner);

        assertEquals(0L, WarlockAwakeningStates.snapshot(owner).kills());
        assertFalse(WarlockAwakeningStates.snapshot(owner).unlocked());
    }

    @Test
    void unlockedStateIsChangedByKillEventsAndDoesNotRelockAfterConfigReload() {
        UUID owner = UUID.randomUUID();
        applyAwakeningKills(2.0);

        assertFalse(WarlockAwakeningStates.recordKill(owner));
        applyAwakeningKills(1.0);
        assertFalse(WarlockAwakeningStates.snapshot(owner).unlocked());

        assertTrue(WarlockAwakeningStates.recordKill(owner));
        applyAwakeningKills(100.0);
        WarlockAwakeningStates.Snapshot snapshot = WarlockAwakeningStates.snapshot(owner);
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
