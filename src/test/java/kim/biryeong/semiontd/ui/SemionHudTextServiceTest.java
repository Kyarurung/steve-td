package kim.biryeong.semiontd.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.config.EconomyConfig;
import kim.biryeong.semiontd.config.WaveConfig;
import kim.biryeong.semiontd.game.PlayerEconomy;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.SemionPlayer;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.map.GameArena;
import org.junit.jupiter.api.Test;

final class SemionHudTextServiceTest {
    @Test
    void activePlayerEconomyActionbarMarkupKeepsExistingElements() {
        SemionGame game = new SemionGame(EconomyConfig.defaultConfig(), WaveConfig.defaultConfig(), new GameArena(Map.of()));
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        PlayerEconomy economy = new PlayerEconomy(EconomyConfig.defaultConfig());
        economy.overrideStartingValues(123, 45, 67, 8);
        SemionPlayer player = new SemionPlayer(playerId, "player", TeamId.RED, 1, economy);
        game.players().put(playerId, player);

        String actionbar = SemionHudTextService.actionbarMarkupFor(player, game);

        assertTrue(actionbar.contains("◆ 다이아 123"));
        assertTrue(actionbar.contains("⬢ 에메랄드 45"));
        assertTrue(actionbar.contains("+ 수입 67"));
        assertTrue(actionbar.contains("에메랄드/s 8"));
        assertTrue(actionbar.contains("▣ 타워"));
    }

    @Test
    void damageNumbersUseCompactSidebarUnits() {
        assertEquals("0", SemionHudTextService.formatDamage(0.0));
        assertEquals("999", SemionHudTextService.formatDamage(999.4));
        assertEquals("1.0K", SemionHudTextService.formatDamage(1_000.0));
        assertEquals("12.3M", SemionHudTextService.formatDamage(12_345_678.0));
        assertEquals("1.2B", SemionHudTextService.formatDamage(1_234_567_890.0));
    }

    @Test
    void damageSidebarViewTogglesInMemory() {
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000000102");
        SemionSidebarHudService service = new SemionSidebarHudService();

        assertTrue(service.toggleDamageView(playerId));
        assertTrue(service.damageViewEnabled(playerId));
        assertFalse(service.toggleDamageView(playerId));
        assertFalse(service.damageViewEnabled(playerId));
    }
}
