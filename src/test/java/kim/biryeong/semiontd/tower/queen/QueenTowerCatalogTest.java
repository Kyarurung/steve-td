package kim.biryeong.semiontd.tower.queen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.config.AttackKind;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.job.JobRegistry;
import kim.biryeong.semiontd.job.QueenTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class QueenTowerCatalogTest {
    private static final UUID OWNER = UUID.nameUUIDFromBytes("queen-test".getBytes());

    @BeforeAll static void bootstrapMinecraft() {SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();}

    @AfterEach void cleanup() {
        QueenStates.clear(OWNER);
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void catalogRegistersOnlyQueenAndRandomCardAsStarters() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
        var entries = ProductionTowerCatalog.all().stream()
                .filter(entry -> QueenTowers.isQueenTower(entry.type())).toList();
        assertEquals(2, entries.size());
        assertEquals(2, entries.stream().filter(ProductionTowerCatalog.CatalogEntry::starter).count());
        assertTrue(JobRegistry.find(QueenTowerJob.ID).isPresent());
        assertInstanceOf(QueenTower.class, ProductionTowerCatalog.find(QueenTowers.QUEEN.id()).orElseThrow()
                .create(OWNER, TeamId.RED, 1, new GridPosition(0, 64, 0)));
        assertInstanceOf(QueenCardTower.class, ProductionTowerCatalog.find(QueenTowers.RANDOM_CARD_SOLDIER.id()).orElseThrow()
                .create(OWNER, TeamId.RED, 1, new GridPosition(1, 64, 0)));
        assertTrue(ProductionTowerCatalog.upgrades(QueenTowers.QUEEN).isEmpty());
        assertTrue(ProductionTowerCatalog.upgrades(QueenTowers.RANDOM_CARD_SOLDIER).isEmpty());
        String queenDescription = String.join(" ", ProductionTowerCatalog.find(QueenTowers.QUEEN.id())
                .orElseThrow().type().description());
        assertFalse(queenDescription.contains("{ability."));
        assertTrue(queenDescription.contains("약체화"));
        assertEquals("붉은 여왕", ProductionTowerCatalog.find(QueenTowers.QUEEN.id()).orElseThrow().type().displayName());
        String cardDescription = String.join(" ", ProductionTowerCatalog.find(QueenTowers.RANDOM_CARD_SOLDIER.id())
                .orElseThrow().type().description());
        assertTrue(cardDescription.contains("최대체력·공격력·크기"));
        assertTrue(cardDescription.contains("직접 처치하지 못"));
        assertEquals(55, QueenBalance.cardAggro(QueenCard.Suit.HEART));
        assertEquals(45, QueenBalance.cardAggro(QueenCard.Suit.DIAMOND));
        assertEquals(110, QueenBalance.cardAggro(QueenCard.Suit.CLUB));
        assertEquals(80, QueenBalance.cardAggro(QueenCard.Suit.SPADE));
        assertEquals("붉은 여왕 빌더", new QueenTowerJob().displayName().getString());
    }

    @Test
    void nextCardPreviewMatchesTheNextPlacedCard() {
        QueenStates.PlayerState state = QueenStates.state(OWNER);
        QueenCard preview = state.peekNextCard();

        assertEquals(preview, state.peekNextCard());
        assertEquals(preview, state.drawNextCard());
        assertTrue(state.peekNextCard() != null);
    }

    @Test
    void pokerRecognizesStandardHandsAceLowAndFiveOfAKind() {
        assertEquals(PokerHand.HIGH_CARD, hand("H2", "D4", "C6", "S8", "H10"));
        assertEquals(PokerHand.ONE_PAIR, hand("H2", "D2", "C6", "S8", "H10"));
        assertEquals(PokerHand.TWO_PAIR, hand("H2", "D2", "C6", "S6", "H10"));
        assertEquals(PokerHand.THREE_OF_A_KIND, hand("H2", "D2", "C2", "S8", "H10"));
        assertEquals(PokerHand.STRAIGHT, hand("HA", "D2", "C3", "S4", "H5"));
        assertEquals(PokerHand.FLUSH, hand("H2", "H4", "H6", "H8", "H10"));
        assertEquals(PokerHand.FULL_HOUSE, hand("H2", "D2", "C2", "S8", "H8"));
        assertEquals(PokerHand.FOUR_OF_A_KIND, hand("H2", "D2", "C2", "S2", "H8"));
        assertEquals(PokerHand.STRAIGHT_FLUSH, hand("H2", "H3", "H4", "H5", "H6"));
        assertEquals(PokerHand.ROYAL_FLUSH, hand("HA", "H10", "HJ", "HQ", "HK"));
        assertEquals(PokerHand.FIVE_OF_A_KIND, hand("H2", "H2", "H2", "H2", "H2"));
    }

    @Test
    void permanentMonsterScalePreservesHealthRatioAndStacksWithoutKilling() {
        Monster monster = new Monster("queen-scale", TeamId.RED, 1, Optional.empty(), Optional.empty(),
                1000, 0, 100, AttackKind.MELEE, "minecraft:zombie", 0);
        monster.syncHealth(500);
        monster.applyPermanentStatScale(0.8, 0.10);
        monster.applyPermanentStatScale(0.5, 0.10);
        assertEquals(400, monster.maxHealth(), 0.0001);
        assertEquals(200, monster.health(), 0.0001);
        assertEquals(40, monster.attackDamage(), 0.0001);
        assertEquals(0.4, monster.permanentStatScale(), 0.0001);
        assertEquals(0.4, monster.visualScale(), 0.0001);
        for (int i = 0; i < 200; i++) monster.applyPermanentStatScale(0.5, 0.10);
        assertTrue(monster.health() > 0.0);
        assertEquals(0.10, monster.visualScale(), 0.0001);
    }

    @Test
    void defaultsMergeAndRejectInvalidShrinkFactor() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        QueenTowers.all().forEach(type -> assertTrue(defaults.towers().containsKey(type.id())));
        assertEquals(0.98, defaults.ability(QueenBalance.GLOBAL_ID, "shrinkFactorPerPoint", -1), 0.0001);
        TowerBalanceConfig merged = new TowerBalanceConfig(Map.of(), Map.of(), Map.of()).withMissingDefaults(defaults);
        assertEquals(70, merged.towers().get(QueenTowers.QUEEN.id()).mineralCost());
        assertEquals(600, merged.abilityInt(QueenBalance.GLOBAL_ID, "giantChargeTicks", -1));
        assertEquals(50.0, merged.ability(QueenBalance.GLOBAL_ID,
                "giantInitialExecutionHealth", -1), 0.0001);
        assertEquals(4.0, merged.ability(QueenBalance.GLOBAL_ID,
                "giantContactRadius", -1), 0.0001);
        assertEquals(1.25, merged.ability(QueenBalance.GLOBAL_ID,
                "cardSplashRadius", -1), 0.0001);
        assertEquals(1, merged.abilityInt(QueenBalance.GLOBAL_ID,
                "cardSplashExtraTargets", -1));

        LinkedHashMap<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        LinkedHashMap<String, Double> invalid = new LinkedHashMap<>(abilities.get(QueenBalance.GLOBAL_ID));
        invalid.put("shrinkFactorPerPoint", 1.0);
        abilities.put(QueenBalance.GLOBAL_ID, invalid);
        TowerBalanceConfig broken = new TowerBalanceConfig(defaults.towers(), defaults.upgradeCosts(), abilities,
                defaults.illusionCloneQueue(), defaults.villagerAdv(), defaults.schemaVersion());
        assertThrows(IllegalArgumentException.class, broken::validateForRuntime);
    }

    private static PokerHand hand(String... specs) {
        return PokerHand.evaluate(java.util.Arrays.stream(specs).map(QueenTowerCatalogTest::card).toList());
    }

    private static QueenCard card(String spec) {
        QueenCard.Suit suit = switch (spec.charAt(0)) {
            case 'H' -> QueenCard.Suit.HEART;
            case 'D' -> QueenCard.Suit.DIAMOND;
            case 'C' -> QueenCard.Suit.CLUB;
            case 'S' -> QueenCard.Suit.SPADE;
            default -> throw new IllegalArgumentException(spec);
        };
        String rank = spec.substring(1);
        int value = switch (rank) {case "A" -> 1; case "J" -> 11; case "Q" -> 12; case "K" -> 13; default -> Integer.parseInt(rank);};
        return new QueenCard(suit, value);
    }
}
