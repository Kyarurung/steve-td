package kim.biryeong.semiontd.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.config.ProgressionConfig;
import kim.biryeong.semiontd.progression.ProgressionService.CosmeticUpdateResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class CosmeticProgressionTest {
    @TempDir
    Path tempDir;

    @Test
    void purchaseAndSelectionPersistAcrossServiceReload() {
        Path path = tempDir.resolve("profiles.json");
        UUID playerId = UUID.randomUUID();
        SemionProgressionStore seedStore = new SemionProgressionStore(path);
        seedStore.putProfile(playerId, SemionPlayerProfile.fresh("Player").recordMatch("Player", true, 100));

        ProgressionService service = new ProgressionService(ProgressionConfig.defaultConfig(), path);
        assertEquals(CosmeticUpdateResult.SUCCESS, service.purchaseCosmetic(playerId, "Player", "crown", 40));
        assertEquals(CosmeticUpdateResult.ALREADY_OWNED, service.purchaseCosmetic(playerId, "Player", "crown", 40));
        assertEquals(CosmeticUpdateResult.INSUFFICIENT_FUNDS, service.purchaseCosmetic(playerId, "Player", "halo", 61));
        assertEquals(CosmeticUpdateResult.SUCCESS, service.purchaseCosmetic(playerId, "Player", "rabbit", 20));
        assertEquals(CosmeticUpdateResult.SUCCESS,
                service.selectCosmetics(playerId, "Player", List.of("crown", "rabbit")));

        ProgressionService reloaded = new ProgressionService(ProgressionConfig.defaultConfig(), path);
        SemionPlayerProfile profile = reloaded.profile(null, playerId, "Player");
        assertEquals(40, profile.cosmeticCurrency());
        assertEquals("crown", profile.selectedCosmeticId());
        assertEquals(List.of("crown", "rabbit"), profile.selectedCosmeticIds());
        assertEquals(List.of("crown", "rabbit"), profile.ownedCosmeticIds());
    }

    @Test
    void failedPersistRollsBackCosmeticMutation() throws Exception {
        Path path = Files.createDirectory(tempDir.resolve("profiles.json"));
        UUID playerId = UUID.randomUUID();
        SemionProgressionStore store = new SemionProgressionStore(path);
        SemionPlayerProfile funded = SemionPlayerProfile.fresh("Player").recordMatch("Player", true, 100);
        store.putProfile(playerId, funded);

        boolean saved = store.putProfilePersisted(playerId, funded.purchaseCosmetic("Player", "crown", 40));

        assertFalse(saved);
        assertEquals(funded, store.getOrCreateProfile(playerId, "Player"));
        assertTrue(Files.exists(tempDir.resolve("progression-fallback.log")));
        assertFalse(Files.readString(tempDir.resolve("progression-fallback.log")).contains("crown"));
    }

    @Test
    void cosmeticCurrencyGrantPersistsAndRejectsOverflow() {
        Path path = tempDir.resolve("profiles.json");
        UUID playerId = UUID.randomUUID();
        ProgressionService service = new ProgressionService(ProgressionConfig.defaultConfig(), path);

        assertEquals(125L, service.grantCosmeticCurrency(playerId, "Player", 125).orElseThrow().cosmeticCurrency());
        assertTrue(service.grantCosmeticCurrency(playerId, "Player", Long.MAX_VALUE).isEmpty());

        ProgressionService reloaded = new ProgressionService(ProgressionConfig.defaultConfig(), path);
        assertEquals(125L, reloaded.profile(null, playerId, "Player").cosmeticCurrency());
    }
}
