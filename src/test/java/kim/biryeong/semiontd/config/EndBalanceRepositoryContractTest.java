package kim.biryeong.semiontd.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import kim.biryeong.semiontd.tower.end.EndTower;
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
        Set<String> externalKeys = end.keySet();
        Set<String> codeKeys = TowerBalanceConfig.defaultConfig().abilities().get(EndTower.CONFIG_ID).keySet().stream().collect(Collectors.toUnmodifiableSet());

        assertNotNull(root.get("schemaVersion"), "tower_balance.json must contain schemaVersion.");
        assertEquals(TowerBalanceConfig.CURRENT_SCHEMA_VERSION, root.get("schemaVersion").getAsInt());
        assertEquals(codeKeys, externalKeys);
        assertEquals(30.0, end.get("absorptionHealAmount").getAsDouble(), 0.0001);
        assertEquals(0.05, end.get("shulkerTransferHealingMaxHealthRatio").getAsDouble(), 0.0001);
        assertEquals(0.15, end.get("dragonFinalDamageBonus").getAsDouble(), 0.0001);
        assertEquals(50.0, end.get("endCrystalAttackRangeEvery").getAsDouble(), 0.0001);
        assertEquals(20.0, end.get("shulkerLifeStealEvery").getAsDouble(), 0.0001);
        assertEquals(0.01, end.get("lifeStealPerStep").getAsDouble(), 0.0001);
        assertEquals(0.15, end.get("lifeStealCap").getAsDouble(), 0.0001);
        assertEquals(15.0, end.get("shulkerReductionEvery").getAsDouble(), 0.0001);
        assertEquals(0.01, end.get("damageReductionPerStep").getAsDouble(), 0.0001);
    }
}
