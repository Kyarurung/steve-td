package kim.biryeong.semiontd.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import kim.biryeong.semiontd.tower.end.EndTower;
import kim.biryeong.semiontd.tower.warlock.WarlockTowers;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "SEMIONTD_BALANCE_REPOSITORY", matches = ".+")
class EndBalanceRepositoryContractTest {
    private static final String BALANCE_REPOSITORY_ENV = "SEMIONTD_BALANCE_REPOSITORY";

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void siblingBalanceRepositoryMatchesTheEndAbilityContract() throws Exception {
        String repositoryPath = System.getenv(BALANCE_REPOSITORY_ENV);
        assertNotNull(repositoryPath, BALANCE_REPOSITORY_ENV + " must point to the semiontd-balance repository.");
        Path balancePath = Path.of(repositoryPath).toAbsolutePath().resolve("tower_balance.json");
        assertTrue(Files.isRegularFile(balancePath), "tower_balance.json is not available: " + balancePath);

        JsonObject root = JsonParser.parseString(Files.readString(balancePath)).getAsJsonObject();
        JsonObject abilities = root.getAsJsonObject("abilities");
        assertNotNull(abilities, "tower_balance.json must contain an abilities object.");
        JsonObject end = abilities.getAsJsonObject(EndTower.CONFIG_ID);
        assertNotNull(end, "tower_balance.json must contain abilities." + EndTower.CONFIG_ID + ".");
        List<String> externalKeys = List.copyOf(end.keySet());
        List<String> codeKeys = List.copyOf(
                TowerBalanceConfig.defaultConfig().abilities().get(EndTower.CONFIG_ID).keySet()
        );

        assertNotNull(root.get("schemaVersion"), "tower_balance.json must contain schemaVersion.");
        assertEquals(TowerBalanceConfig.CURRENT_SCHEMA_VERSION, root.get("schemaVersion").getAsInt());
        assertEquals(codeKeys, externalKeys, "End ability keys and their display order must match.");
        assertEquals(120.0, end.get("damageSoftCap").getAsDouble(), 0.0001);
        assertFalse(end.has("damageCap"));
        assertEquals(30.0, end.get("transferHeal").getAsDouble(), 0.0001);
        assertEquals(0.05, end.get("transferHealRatio").getAsDouble(), 0.0001);
        assertEquals(0.75, end.get("roundDamageRatio").getAsDouble(), 0.0001);
        assertEquals(0.20, end.get("dragonFinalDamage").getAsDouble(), 0.0001);
        assertEquals(50.0, end.get("attackRangeStacks").getAsDouble(), 0.0001);
        assertEquals(30.0, end.get("lifeStealStacks").getAsDouble(), 0.0001);
        assertEquals(0.01, end.get("lifeStealStep").getAsDouble(), 0.0001);
        assertEquals(0.10, end.get("lifeStealCap").getAsDouble(), 0.0001);
        assertEquals(10.0, end.get("regenerationStacks").getAsDouble(), 0.0001);
        assertEquals(30.0, end.get("regenerationCap").getAsDouble(), 0.0001);
        assertEquals(15.0, end.get("damageReductionStacks").getAsDouble(), 0.0001);
        assertEquals(0.01, end.get("damageReductionStep").getAsDouble(), 0.0001);

        JsonObject warlock = abilities.getAsJsonObject(WarlockTowers.CONFIG_ID);
        assertNotNull(warlock, "tower_balance.json must contain abilities." + WarlockTowers.CONFIG_ID + ".");
        assertEquals(180.0, warlock.get("damageSoftCap").getAsDouble(), 0.0001);
        assertFalse(warlock.has("damageCap"));
    }

    @Test
    void siblingBalanceRepositoryContainsOnlyKnownTowerAbilityKeys() throws Exception {
        Path balancePath = Path.of(System.getenv(BALANCE_REPOSITORY_ENV))
                .toAbsolutePath()
                .resolve("tower_balance.json");
        JsonObject root = JsonParser.parseString(Files.readString(balancePath)).getAsJsonObject();
        Map<String, Map<String, Double>> defaults = TowerBalanceConfig.defaultConfig().abilities();

        for (Map.Entry<String, com.google.gson.JsonElement> entry
                : root.getAsJsonObject("abilities").entrySet()) {
            assertTrue(defaults.containsKey(entry.getKey()), "Unknown ability owner: " + entry.getKey());
            for (String key : entry.getValue().getAsJsonObject().keySet()) {
                assertTrue(defaults.get(entry.getKey()).containsKey(key),
                        "Unknown ability key: " + entry.getKey() + "." + key);
            }
        }
    }
}
