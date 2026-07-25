package kim.biryeong.semiontd.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.config.EconomyConfig;
import org.junit.jupiter.api.Test;

final class SemionGameTraitEconomyTest {
    @Test
    void performanceBonusUsesOnlyTheCurrentTeamIncomeAndEmeraldCap() {
        EconomyConfig config = EconomyConfig.defaultConfig();
        SemionPlayer redOne = player(config, "red-one", TeamId.RED, 100L);
        SemionPlayer redTwo = player(config, "red-two", TeamId.RED, 200L);
        SemionPlayer blue = player(config, "blue", TeamId.BLUE, 900L);

        assertEquals(300L, SemionGame.teamIncome(List.of(redOne, redTwo, blue), TeamId.RED));

        redOne.economy().addEmerald(500L, 75L);
        assertEquals(75L, redOne.economy().emerald());
    }

    private static SemionPlayer player(EconomyConfig config, String name, TeamId teamId, long income) {
        PlayerEconomy economy = new PlayerEconomy(config);
        economy.overrideStartingValues(0L, 0L, income, 0L);
        return new SemionPlayer(
                UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                name,
                teamId,
                1,
                economy
        );
    }
}
