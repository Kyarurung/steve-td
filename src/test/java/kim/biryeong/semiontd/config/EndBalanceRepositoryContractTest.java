package kim.biryeong.semiontd.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import kim.biryeong.semiontd.tower.end.EndTower;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EndBalanceRepositoryContractTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void siblingBalanceRepositoryMatchesTheEndAbilityContract() throws Exception {
        Path balancePath = Path.of("")
                .toAbsolutePath()
                .getParent()
                .resolve("semiontd-balance")
                .resolve("tower_balance.json");
        Assumptions.assumeTrue(
                Files.isRegularFile(balancePath),
                "Sibling semiontd-balance repository is not available."
        );

        JsonObject root = JsonParser.parseString(Files.readString(balancePath))
                .getAsJsonObject();
        JsonObject end = root.getAsJsonObject("abilities")
                .getAsJsonObject(EndTower.CONFIG_ID);
        Set<String> externalKeys = end.keySet();
        Set<String> codeKeys = TowerBalanceConfig.defaultConfig()
                .abilities()
                .get(EndTower.CONFIG_ID)
                .keySet()
                .stream()
                .collect(Collectors.toUnmodifiableSet());

        assertEquals(TowerBalanceConfig.CURRENT_SCHEMA_VERSION, root.get("schemaVersion").getAsInt());
        assertEquals(codeKeys, externalKeys);
        assertEquals(15.0, end.get("shulkerReductionEvery").getAsDouble(), 0.0001);
        assertEquals(0.01, end.get("damageReductionPerStep").getAsDouble(), 0.0001);
    }
}
