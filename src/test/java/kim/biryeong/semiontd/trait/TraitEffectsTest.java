package kim.biryeong.semiontd.trait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kim.biryeong.semiontd.config.AttackKind;
import kim.biryeong.semiontd.config.EconomyConfig;
import kim.biryeong.semiontd.config.TraitBalanceConfig;
import kim.biryeong.semiontd.config.TraitBalanceRuntime;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.test.tower.TestTower;
import kim.biryeong.semiontd.test.tower.TestTowerTypes;
import kim.biryeong.semiontd.tower.warlock.WarlockTowers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

final class TraitEffectsTest {
    @AfterEach
    void resetTraitBalance() {
        TraitBalanceRuntime.apply(TraitBalanceConfig.defaultConfig());
    }

    @Test
    void builtInTraitsExposeAllPlannedChoicesAndSlotValues() {
        BuiltInTraits.register();

        Set<?> registeredIds = TraitRegistry.all().stream()
                .map(SemionTrait::id)
                .collect(Collectors.toSet());
        assertTrue(registeredIds.containsAll(Set.of(
                BuiltInTraits.NONE_ID,
                BuiltInTraits.MOBILIZATION_GRANT_ID,
                BuiltInTraits.CLEAN_LANE_BONUS_ID,
                BuiltInTraits.RAPID_DEPLOYMENT_ID,
                BuiltInTraits.BERSERK_SUMMONS_ID,
                BuiltInTraits.INTERCEPTION_DOCTRINE_ID,
                BuiltInTraits.OPENING_SALVO_ID,
                BuiltInTraits.WAVEBREAKER_DOCTRINE_ID,
                BuiltInTraits.FORTITUDE_ID,
                BuiltInTraits.DOUBLE_EDGED_SWORD_ID,
                BuiltInTraits.STRENGTH_IN_NUMBERS_ID,
                BuiltInTraits.DIVERSITY_ID,
                BuiltInTraits.SUPPLY_DEPOT_ID,
                BuiltInTraits.TRANSCENDENCE_ID
        )));

        SemionTrait goldSpoon = TraitRegistry.find(BuiltInTraits.MOBILIZATION_GRANT_ID).orElseThrow();
        assertEquals("시작 다이아 +150", goldSpoon.effectSummary(TraitSlot.PRIMARY).getString());
        assertEquals("시작 다이아 +75", goldSpoon.effectSummary(TraitSlot.SECONDARY).getString());
    }

    @Test
    void economicTraitsUseFullPrimaryAndHalfSecondaryPower() {
        TraitLoadout primaryGold = primary(BuiltInTraits.MOBILIZATION_GRANT_ID);
        TraitLoadout secondaryGold = secondary(BuiltInTraits.MOBILIZATION_GRANT_ID);
        TraitLoadout primaryClean = primary(BuiltInTraits.CLEAN_LANE_BONUS_ID);
        TraitLoadout secondaryClean = secondary(BuiltInTraits.CLEAN_LANE_BONUS_ID);

        assertEquals(150L, TraitEffects.startingMineralBonus(primaryGold));
        assertEquals(75L, TraitEffects.startingMineralBonus(secondaryGold));
        assertEquals(150L, TraitEffects.cleanLaneBonus(primaryClean, 1_000L));
        assertEquals(75L, TraitEffects.cleanLaneBonus(secondaryClean, 1_000L));
        assertEquals(0L, TraitEffects.cleanLaneBonus(primaryClean, -1L));
        assertEquals(4, TraitEffects.towerLimitBonus(primary(BuiltInTraits.SUPPLY_DEPOT_ID)));
        assertEquals(2, TraitEffects.towerLimitBonus(secondary(BuiltInTraits.SUPPLY_DEPOT_ID)));

        EconomyConfig.TowerLimitConfig towerLimit = new EconomyConfig.TowerLimitConfig(5, 5, 5, 3, 23);
        assertEquals(9, towerLimit.limitForRound(1)
                + TraitEffects.towerLimitBonus(primary(BuiltInTraits.SUPPLY_DEPOT_ID)));
        assertEquals(7, towerLimit.limitForRound(1)
                + TraitEffects.towerLimitBonus(secondary(BuiltInTraits.SUPPLY_DEPOT_ID)));
        assertEquals(27, towerLimit.limitForRound(100)
                + TraitEffects.towerLimitBonus(primary(BuiltInTraits.SUPPLY_DEPOT_ID)));
        assertEquals(25, towerLimit.limitForRound(100)
                + TraitEffects.towerLimitBonus(secondary(BuiltInTraits.SUPPLY_DEPOT_ID)));
    }

    @Test
    void attackAndSaleTraitsUsePlannedValues() {
        TraitLoadout primaryBerserk = primary(BuiltInTraits.BERSERK_SUMMONS_ID);
        TraitLoadout secondaryBerserk = secondary(BuiltInTraits.BERSERK_SUMMONS_ID);

        assertEquals(1.20, TraitEffects.incomeAttackDamageMultiplier(primaryBerserk), 0.000_001);
        assertEquals(1.10, TraitEffects.incomeAttackSpeedMultiplier(primaryBerserk), 0.000_001);
        assertEquals(1.10, TraitEffects.incomeAttackDamageMultiplier(secondaryBerserk), 0.000_001);
        assertEquals(1.05, TraitEffects.incomeAttackSpeedMultiplier(secondaryBerserk), 0.000_001);

        assertEquals(0.90, TraitEffects.sellRefundRate(primary(BuiltInTraits.RAPID_DEPLOYMENT_ID), true), 0.000_001);
        assertEquals(0.70, TraitEffects.sellRefundRate(secondary(BuiltInTraits.RAPID_DEPLOYMENT_ID), true), 0.000_001);
        assertEquals(1.00, TraitEffects.sellRefundRate(primary(BuiltInTraits.RAPID_DEPLOYMENT_ID), false), 0.000_001);
        assertEquals(0.15, TraitEffects.openingAttackSpeedBonus(primary(BuiltInTraits.OPENING_SALVO_ID)), 0.000_001);
        assertEquals(0.075, TraitEffects.openingAttackSpeedBonus(secondary(BuiltInTraits.OPENING_SALVO_ID)), 0.000_001);
        assertEquals(400, TraitEffects.transcendenceActivationDelayTicks());
        assertEquals(0.30, TraitEffects.transcendenceDamageBonus(primary(BuiltInTraits.TRANSCENDENCE_ID)), 0.000_001);
        assertEquals(0.15, TraitEffects.transcendenceDamageBonus(secondary(BuiltInTraits.TRANSCENDENCE_ID)), 0.000_001);
    }

    @Test
    void configuredTraitValuesDriveRuntimeAndUiSummary() {
        TraitBalanceRuntime.apply(new TraitBalanceConfig(Map.of(
                "opening_salvo", Map.of(
                        "attackSpeedBonus", 0.25,
                        "durationSeconds", 12.0
                )
        )));
        BuiltInTraits.register();

        assertEquals(0.25, TraitEffects.openingAttackSpeedBonus(primary(BuiltInTraits.OPENING_SALVO_ID)), 0.000_001);
        assertEquals(240, TraitEffects.openingAttackSpeedDurationTicks());
        SemionTrait speedrunner = TraitRegistry.find(BuiltInTraits.OPENING_SALVO_ID).orElseThrow();
        assertEquals("12초간 공속 +25%", speedrunner.effectSummary(TraitSlot.PRIMARY).getString());
        assertEquals("12초간 공속 +12.5%", speedrunner.effectSummary(TraitSlot.SECONDARY).getString());
    }

    @Test
    void configuredSupplyAndTranscendenceValuesDriveRuntimeAndUiSummary() {
        TraitBalanceRuntime.apply(new TraitBalanceConfig(Map.of(
                "supply_depot", Map.of("towerLimitBonus", 6.0),
                "transcendence", Map.of(
                        "activationDelaySeconds", 10.0,
                        "damageBonus", 0.40
                )
        )));
        BuiltInTraits.register();

        assertEquals(6, TraitEffects.towerLimitBonus(primary(BuiltInTraits.SUPPLY_DEPOT_ID)));
        assertEquals(3, TraitEffects.towerLimitBonus(secondary(BuiltInTraits.SUPPLY_DEPOT_ID)));
        assertEquals(200, TraitEffects.transcendenceActivationDelayTicks());
        assertEquals(0.40, TraitEffects.transcendenceDamageBonus(primary(BuiltInTraits.TRANSCENDENCE_ID)), 0.000_001);
        assertEquals(0.20, TraitEffects.transcendenceDamageBonus(secondary(BuiltInTraits.TRANSCENDENCE_ID)), 0.000_001);
        assertEquals(
                "최대 타워 수 +3",
                TraitRegistry.find(BuiltInTraits.SUPPLY_DEPOT_ID).orElseThrow()
                        .effectSummary(TraitSlot.SECONDARY).getString()
        );
        assertEquals(
                "10초 후 피해 +40%",
                TraitRegistry.find(BuiltInTraits.TRANSCENDENCE_ID).orElseThrow()
                        .effectSummary(TraitSlot.PRIMARY).getString()
        );
    }

    @Test
    void targetAndDoubleEdgedBonusesDistinguishIncomeFromWaveMonsters() {
        Monster incomeMonster = monster(Optional.of(TeamId.BLUE));
        Monster waveMonster = monster(Optional.empty());

        assertEquals(0.15,
                TraitEffects.targetDamageBonus(primary(BuiltInTraits.INTERCEPTION_DOCTRINE_ID), incomeMonster),
                0.000_001);
        assertEquals(0.0,
                TraitEffects.targetDamageBonus(primary(BuiltInTraits.INTERCEPTION_DOCTRINE_ID), waveMonster),
                0.000_001);
        assertEquals(0.15,
                TraitEffects.targetDamageBonus(primary(BuiltInTraits.WAVEBREAKER_DOCTRINE_ID), waveMonster),
                0.000_001);
        assertEquals(0.25,
                TraitEffects.doubleEdgedIncomingDamageBonus(primary(BuiltInTraits.DOUBLE_EDGED_SWORD_ID)),
                0.000_001);
        assertEquals(0.125,
                TraitEffects.doubleEdgedIncomingDamageBonus(secondary(BuiltInTraits.DOUBLE_EDGED_SWORD_ID)),
                0.000_001);
        assertEquals(0.25,
                TraitEffects.doubleEdgedOutgoingDamageBonus(primary(BuiltInTraits.DOUBLE_EDGED_SWORD_ID)),
                0.000_001);
    }

    @Test
    void fortitudeUsesTheWarlockCoreException() {
        UUID owner = UUID.nameUUIDFromBytes("fortitude-owner".getBytes());
        GridPosition position = new GridPosition(0, 0, 0);
        TestTower normal = new TestTower(TestTowerTypes.TEST_DIRECT, owner, TeamId.RED, 1, position);
        TestTower warlockCore = new TestTower(WarlockTowers.BASE_WARLOCK_TOWER, owner, TeamId.RED, 1, position);
        TraitLoadout fortitude = primary(BuiltInTraits.FORTITUDE_ID);

        assertEquals(0.20, TraitEffects.towerMaxHealthBonus(fortitude, normal), 0.000_001);
        assertEquals(0.10, TraitEffects.towerMaxHealthBonus(fortitude, warlockCore), 0.000_001);
    }

    @Test
    void incomeMonsterRuntimeAppliesDamageAndConservativeAttackSpeed() {
        Monster monster = monster(Optional.of(TeamId.BLUE));

        monster.applyAttackModifiers(1.20, 1.10);

        assertEquals(12.0, monster.attackDamage(), 0.000_001);
        assertEquals(12, monster.attackIntervalTicks());
    }

    private static TraitLoadout primary(net.minecraft.resources.ResourceLocation traitId) {
        return new TraitLoadout(traitId, BuiltInTraits.NONE_ID);
    }

    private static TraitLoadout secondary(net.minecraft.resources.ResourceLocation traitId) {
        return new TraitLoadout(BuiltInTraits.NONE_ID, traitId);
    }

    private static Monster monster(Optional<TeamId> senderTeam) {
        return new Monster(
                "trait-test",
                TeamId.RED,
                1,
                Optional.of(UUID.nameUUIDFromBytes("trait-test-owner".getBytes())),
                senderTeam,
                100.0,
                0.0,
                10.0,
                AttackKind.MELEE,
                "minecraft:zombie",
                0L
        );
    }
}
