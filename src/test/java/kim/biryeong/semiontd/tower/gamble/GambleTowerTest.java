package kim.biryeong.semiontd.tower.gamble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.job.GambleTowerJob;
import kim.biryeong.semiontd.job.JobRegistry;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.TowerUpgradeOption;
import kim.biryeong.semiontd.tower.illager.IllagerTowers;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class GambleTowerTest {
    private static final double EPSILON = 0.0001;
    private static final UUID OWNER = UUID.nameUUIDFromBytes("gamble-test".getBytes(StandardCharsets.UTF_8));

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void reloadCatalog() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void allThirtySixTwoDiceOutcomesUseTheNonlinearScoreAndDoubleRule() {
        double[] bySum = {0, 0, -80, -60, -40, -28, 20, 40, 50, 60, 75, 90, 100};
        double abilityAdjustedTotal = 0.0;
        for (int first = 1; first <= 6; first++) {
            for (int second = 1; second <= 6; second++) {
                double expected = bySum[first + second] * (first == second ? 2.0 : 1.0);
                assertEquals(expected, GambleRolls.defaultTwoDiceDelta(first, second), EPSILON,
                        first + "+" + second);
                assertEquals(expected, GambleRolls.twoDiceDelta(first, second), EPSILON,
                        "configured " + first + "+" + second);
                abilityAdjustedTotal += expected > 0.0 ? expected * 0.75 : expected;
            }
        }
        assertEquals(28.5555555556, GambleRolls.defaultExpectedTwoDiceDelta(), EPSILON);
        assertEquals(28.5555555556, GambleRolls.expectedTwoDiceDelta(), EPSILON);
        assertEquals(17.5833333333, abilityAdjustedTotal / 36.0, EPSILON);
        assertEquals(-160.0, GambleRolls.defaultTwoDiceDelta(1, 1), EPSILON);
        assertEquals(200.0, GambleRolls.defaultTwoDiceDelta(6, 6), EPSILON);
    }

    @Test
    void oddEvenAndFixedStatConversionNeverUseMultiplicativePercentages() {
        assertEquals(40.0, GambleRolls.oddEvenDelta(GambleBet.ODD, 3), EPSILON);
        assertEquals(-28.0, GambleRolls.oddEvenDelta(GambleBet.ODD, 4), EPSILON);
        assertEquals(40.0, GambleRolls.oddEvenDelta(GambleBet.EVEN, 4), EPSILON);
        assertEquals(-28.0, GambleRolls.oddEvenDelta(GambleBet.EVEN, 3), EPSILON);
        assertThrows(IllegalArgumentException.class,
                () -> GambleRolls.oddEvenDelta(GambleBet.TWO_DICE, 3));
        assertEquals(1.0, (40.0 * 0.75 - 28.0) / 2.0, EPSILON,
                "Odd/even stat expectation must remain positive while abilities can replace wins.");

        assertEquals(200.0, GambleBalance.statDelta(GambleStat.MAX_HEALTH, 40), EPSILON);
        assertEquals(20.0, GambleBalance.statDelta(GambleStat.DAMAGE, 40), EPSILON);
        assertEquals(2.0, GambleBalance.statDelta(GambleStat.RANGE, 40), EPSILON);
        assertEquals(1.0, GambleBalance.statDelta(GambleStat.SPLASH_RADIUS, 40), EPSILON);
        assertEquals(50.0, GambleBalance.statDelta(GambleStat.MAX_HEALTH, 10), EPSILON);
        assertEquals(5.0, GambleBalance.statDelta(GambleStat.DAMAGE, 10), EPSILON);
        assertEquals(0.5, GambleBalance.statDelta(GambleStat.RANGE, 10), EPSILON);
        assertEquals(0.25, GambleBalance.statDelta(GambleStat.SPLASH_RADIUS, 10), EPSILON);
    }

    @Test
    void stateStacksFixedDeltasHasNoUpperCapAndFloorsEachStatAtTwentyPercent() {
        GambleState state = GambleState.EMPTY
                .recordStat(GambleStat.MAX_HEALTH, 40, 100, "hp")
                .recordStat(GambleStat.DAMAGE, 4, 8, "damage")
                .recordStat(GambleStat.RANGE, 1, 6, "range")
                .recordStat(GambleStat.SPLASH_RADIUS, 1, 1.5, "splash");
        assertEquals(140, state.resolvedValue(GambleStat.MAX_HEALTH, 100), EPSILON);
        assertEquals(12, state.resolvedValue(GambleStat.DAMAGE, 8), EPSILON);
        assertEquals(7, state.resolvedValue(GambleStat.RANGE, 6), EPSILON);
        assertEquals(2.5, state.resolvedValue(GambleStat.SPLASH_RADIUS, 1.5), EPSILON);

        state = state.recordStat(GambleStat.DAMAGE, 10_000, 8, "uncapped");
        assertEquals(10_012, state.resolvedValue(GambleStat.DAMAGE, 8), EPSILON);
        state = state.recordStat(GambleStat.DAMAGE, -20_000, 8, "floor");
        assertEquals(1.6, state.resolvedValue(GambleStat.DAMAGE, 8), EPSILON);
        assertEquals(-6.4, state.damageDelta(), EPSILON);
        state = state.recordStat(GambleStat.SPLASH_RADIUS, -100, 1.5, "splash floor");
        assertEquals(0.3, state.resolvedValue(GambleStat.SPLASH_RADIUS, 1.5), EPSILON);
        assertEquals(-1.2, state.splashRadiusDelta(), EPSILON);
    }

    @Test
    void insuranceAndAbilityRewardRulesAreDeterministicAndUnique() {
        GambleState insured = GambleState.EMPTY.recordAbility(GambleAbility.LOSS_INSURANCE, "insured");
        assertEquals(-8.0, GambleRewards.insuredDelta(insured, -10.0), EPSILON);
        assertEquals(10.0, GambleRewards.insuredDelta(insured, 10.0), EPSILON);
        assertEquals(-10.0, GambleRewards.insuredDelta(GambleState.EMPTY, -10.0), EPSILON);

        assertTrue(GambleRewards.awardsAbility(GambleState.EMPTY, 2.0, 0.249999));
        assertFalse(GambleRewards.awardsAbility(GambleState.EMPTY, 2.0, 0.25));
        assertFalse(GambleRewards.awardsAbility(GambleState.EMPTY, -5.0, 0.0));
        GambleState all = new GambleState(0, 0, 0, 0,
                EnumSet.allOf(GambleAbility.class), 3, "all");
        assertFalse(GambleRewards.awardsAbility(all, 40.0, 0.0));
        assertEquals(3, GambleRewards.missingAbilities(GambleState.EMPTY).size());
        GambleState one = GambleState.EMPTY.recordAbility(GambleAbility.LUCKY_STRIKE, "one");
        assertEquals(2, GambleRewards.missingAbilities(one).size());
        assertEquals(GambleAbility.LOSS_INSURANCE, GambleRewards.chooseMissing(one, 0));
        assertEquals(GambleAbility.SPREAD_BET, GambleRewards.chooseMissing(one, 1));
        assertEquals(GambleStat.SPLASH_RADIUS, GambleRewards.chooseStat(3));
    }

    @Test
    void luckyStrikeIsExactlyOneFaceOutOfSix() {
        int successes = 0;
        for (int die = 1; die <= 6; die++) {
            if (GambleRolls.luckyStrike(die)) successes++;
        }
        assertEquals(1, successes);
        assertTrue(GambleRolls.luckyStrike(6));
        assertFalse(GambleRolls.luckyStrike(5));
        assertEquals(200.0, GambleRolls.luckyStrikeDamage(100.0, 6, 2.0), EPSILON);
        assertEquals(100.0, GambleRolls.luckyStrikeDamage(100.0, 5, 2.0), EPSILON);
    }

    @Test
    void catalogUsesThreeStartersHundredCostEdgesAndCreativeClassification() {
        List<ProductionTowerCatalog.CatalogEntry> entries = ProductionTowerCatalog.all().stream()
                .filter(entry -> GambleTowers.isGambleTower(entry.type())).toList();
        assertEquals(7, entries.size());
        assertEquals(3, entries.stream().filter(ProductionTowerCatalog.CatalogEntry::starter).count());
        assertTrue(entries.stream().filter(ProductionTowerCatalog.CatalogEntry::starter)
                .map(entry -> entry.type().id()).toList().containsAll(List.of(
                        GambleTowers.DICE_T1.id(), GambleTowers.GAMBLER.id(), GambleTowers.SPECTATOR_T1.id())));
        GambleSupportTower dice = assertInstanceOf(GambleSupportTower.class,
                ProductionTowerCatalog.find(GambleTowers.DICE_T1.id()).orElseThrow()
                        .create(OWNER, TeamId.RED, 1, new GridPosition(0, 64, 0)));
        GambleSupportTower spectator = assertInstanceOf(GambleSupportTower.class,
                ProductionTowerCatalog.find(GambleTowers.SPECTATOR_T1.id()).orElseThrow()
                        .create(OWNER, TeamId.RED, 1, new GridPosition(0, 64, 1)));
        assertEquals(0.0, dice.adjustAttackRange(dice.type().range()), EPSILON);
        assertEquals(0.0, spectator.adjustAttackRange(spectator.type().range()), EPSILON);
        assertEquals(5.0, dice.type().range(), EPSILON);
        GamblerTower gambler = assertInstanceOf(GamblerTower.class,
                ProductionTowerCatalog.find(GambleTowers.GAMBLER.id()).orElseThrow()
                        .create(OWNER, TeamId.RED, 1, new GridPosition(1, 64, 0)));
        assertTrue(gambler.type().maxHealth() >= IllagerTowers.T1_PILLAGER.maxHealth());
        assertTrue(gambler.type().range() >= IllagerTowers.T1_PILLAGER.range());
        assertTrue(gambler.type().damage() * IllagerTowers.T1_PILLAGER.attackIntervalTicks()
                > IllagerTowers.T1_PILLAGER.damage() * gambler.type().attackIntervalTicks());
        for (GambleBet bet : GambleBet.values()) {
            TowerUpgradeOption option = ProductionTowerCatalog.upgrade(GambleTowers.GAMBLER, bet.upgradeId())
                    .orElseThrow();
            assertEquals(100, option.mineralCost());
            assertFalse(gambler.upgradeCostAddsToSaleValue(option));
        }
        assertEquals(100, ProductionTowerCatalog.upgrade(GambleTowers.DICE_T1, GambleTowers.DICE_T2.id())
                .orElseThrow().mineralCost());
        assertTrue(JobRegistry.creativeBuilders().stream().anyMatch(job -> job.id().equals(GambleTowerJob.ID)));
        assertFalse(JobRegistry.officialBuilders().stream().anyMatch(job -> job.id().equals(GambleTowerJob.ID)));
        assertEquals("semion-td:gamble_towers", new GambleTowerJob().id().toString());
    }

    @Test
    void immutableStateCopiesAcrossSelfUpgradeReplacement() {
        GamblerTower original = new GamblerTower(GambleTowers.GAMBLER, OWNER, TeamId.RED, 1,
                new GridPosition(0, 64, 0), new GridPosition(0, 64, 0));
        GambleState state = GambleState.EMPTY
                .recordStat(GambleStat.MAX_HEALTH, 40, 100, "hp")
                .recordAbility(GambleAbility.LUCKY_STRIKE, "lucky");
        original.setData(GamblerTower.STATE, state);
        original.syncMaxHealth(state.resolvedValue(GambleStat.MAX_HEALTH, 100), false);
        original.syncHealth(70);
        GamblerTower replacement = new GamblerTower(GambleTowers.GAMBLER, OWNER, TeamId.RED, 1,
                original.originalPosition(), original.position());
        replacement.copyFrom(original, 0);
        assertEquals(state, replacement.state());
        assertEquals(0, replacement.paidMineralCost() - original.paidMineralCost());
    }

    @Test
    void supportFaceTypesAndTierConfigurationMatchTheDesign() {
        assertEquals(1, GambleBalance.minimumRoll(GambleTowers.DICE_T3));
        assertEquals(1, GambleBalance.minimumRoll(GambleTowers.SPECTATOR_T1));
        assertEquals(2, GambleBalance.minimumRoll(GambleTowers.SPECTATOR_T2));
        assertEquals(3, GambleBalance.minimumRoll(GambleTowers.SPECTATOR_T3));
        assertEquals(1.5, GambleBalance.positiveMultiplier(GambleTowers.SPECTATOR_T3), EPSILON);
        assertEquals(0.30, GambleBalance.supportMagnitude(1, 99), EPSILON);
        assertEquals(0.15, GambleBalance.supportMagnitude(2, 99), EPSILON);
        assertEquals(0.075, GambleBalance.supportMagnitude(3, 1.5), EPSILON);
        assertEquals(0.375, GambleBalance.supportMagnitude(6, 1.5), EPSILON);
        assertEquals(1.5, GambleBalance.baseSplashRadius(), EPSILON);
        assertEquals(0.60, GambleBalance.splashDamageRatio(), EPSILON);
    }

    @Test
    void defaultsMergeMissingGambleValuesAndRejectInvalidOnes() throws Exception {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        GambleTowers.all().forEach(type -> assertTrue(defaults.towers().containsKey(type.id())));
        assertEquals(100, defaults.upgradeCost(GambleTowers.GAMBLER.id(), GambleBet.ODD.upgradeId(), -1));
        TowerBalanceConfig partial = new TowerBalanceConfig(Map.of(), Map.of(), Map.of(
                GambleBalance.GLOBAL_ID, Map.of("damagePerScore", 0.2))).withMissingDefaults(defaults);
        assertEquals(0.2, partial.ability(GambleBalance.GLOBAL_ID, "damagePerScore", -1), EPSILON);
        assertEquals(5.0, partial.ability(GambleBalance.GLOBAL_ID, "maxHealthPerScore", -1), EPSILON);
        assertEquals(110, partial.towers().get(GambleTowers.GAMBLER.id()).maxHealth(), EPSILON);

        assertInvalidAbility(defaults, GambleBalance.GLOBAL_ID, "abilityRewardChance", 1.1);
        assertInvalidAbility(defaults, GambleBalance.GLOBAL_ID, "splashDamageRatio", 1.1);
        assertInvalidAbility(defaults, GambleBalance.GLOBAL_ID, "spreadEveryAttacks", 4.5);
        assertInvalidAbility(defaults, GambleBalance.GLOBAL_ID, "twoDiceLoss2", 1_000.0);
        assertInvalidAbility(defaults, GambleTowers.SPECTATOR_T3.id(), "minimumRoll", 7.0);

        try (var input = GambleTowerTest.class.getResourceAsStream(
                "/semiontd/balance-defaults/tower_balance.json")) {
            var root = JsonParser.parseReader(new InputStreamReader(
                    java.util.Objects.requireNonNull(input), StandardCharsets.UTF_8)).getAsJsonObject();
            assertTrue(root.getAsJsonObject("towers").has(GambleTowers.GAMBLER.id()));
            assertTrue(root.getAsJsonObject("abilities").has(GambleBalance.GLOBAL_ID));
        }
    }

    private static void assertInvalidAbility(
            TowerBalanceConfig defaults, String configId, String key, double value
    ) {
        LinkedHashMap<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        LinkedHashMap<String, Double> invalid = new LinkedHashMap<>(abilities.get(configId));
        invalid.put(key, value);
        abilities.put(configId, invalid);
        TowerBalanceConfig broken = new TowerBalanceConfig(defaults.towers(), defaults.upgradeCosts(), abilities,
                defaults.illusionCloneQueue(), defaults.villagerAdv(), defaults.schemaVersion());
        assertThrows(IllegalArgumentException.class, broken::validateForRuntime);
    }
}
