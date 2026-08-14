package kim.biryeong.semiontd.tower.futureagency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.job.FutureAgencyTowerJob;
import kim.biryeong.semiontd.job.JobRegistry;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class FutureAgencyTowerCatalogTest {
    private static final UUID OWNER = UUID.nameUUIDFromBytes("future-agency-owner".getBytes());

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void restoreDefaults() {
        FutureAgencyStates.clear(OWNER);
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void catalogRegistersEighteenTowersAndFourInternalStarters() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());

        var entries = ProductionTowerCatalog.all().stream()
                .filter(entry -> FutureAgencyTowers.isFutureAgencyTower(entry.type()))
                .toList();
        assertEquals(18, entries.size());
        assertEquals(4, entries.stream().filter(ProductionTowerCatalog.CatalogEntry::starter).count());
        assertTrue(JobRegistry.find(FutureAgencyTowerJob.ID).isPresent());
        assertInstanceOf(FutureAgencyLeaderTower.class, create(FutureAgencyTowers.ESCAPEE));
        assertInstanceOf(FutureAgencyAgentTower.class,
                create(FutureAgencyTowers.agent(FutureAgencyRole.COMBAT, 5)));

        assertEquals(0, upgradeCost(FutureAgencyTowers.ESCAPEE, FutureAgencyLeaderTower.RECONSTRUCT));
        assertEquals(800, upgradeCost(FutureAgencyTowers.REBUILDER,
                FutureAgencyLeaderTower.PROMOTE_COMMANDER));
        assertEquals(1500, upgradeCost(FutureAgencyTowers.COMMANDER,
                FutureAgencyLeaderTower.SAVE_WORLD));
        assertEquals(1500, upgradeCost(FutureAgencyTowers.REBUILDER,
                FutureAgencyLeaderTower.SAVE_WORLD));
        assertEquals(100, upgradeCost(FutureAgencyTowers.agent(FutureAgencyRole.COMBAT, 5),
                FutureAgencyTowers.agent(FutureAgencyRole.COMBAT, 4).id()));
        assertEquals(700, upgradeCost(FutureAgencyTowers.agent(FutureAgencyRole.PROTECTION, 2),
                FutureAgencyTowers.agent(FutureAgencyRole.PROTECTION, 1).id()));
        assertEquals(200, new FutureAgencyTowerJob().modifyStartingMineral(null, 200));
        assertEquals(150, ProductionTowerCatalog.find(FutureAgencyTowers.ESCAPEE.id()).orElseThrow()
                .type().mineralCost());
    }

    @Test
    void policyOffersAreStableDistinctAndOnlyOneCanBeChosenPerRound() {
        FutureAgencyStates.PlayerState state = FutureAgencyStates.state(OWNER);
        state.reconstruct();
        state.openRound(7);
        var firstOffers = state.offers();

        assertEquals(30, FutureAgencyPolicy.values().length);
        assertEquals(3, firstOffers.size());
        assertEquals(3, new HashSet<>(firstOffers).size());
        state.openRound(7);
        assertEquals(firstOffers, state.offers());

        FutureAgencyPolicy chosen = firstOffers.getFirst();
        assertTrue(state.choose(chosen));
        assertEquals(1, state.stacks(chosen));
        assertEquals(1, state.policySelections());
        assertTrue(state.offers().isEmpty());
        assertFalse(state.choose(firstOffers.get(1)));

        state.openRound(8);
        assertEquals(3, state.offers().size());
    }

    @Test
    void reconstructionCommanderAndWorldSaveArePermanentStateTransitions() {
        FutureAgencyStates.PlayerState state = FutureAgencyStates.state(OWNER);
        assertFalse(state.reconstructed());
        state.reconstruct();
        assertTrue(state.reconstructed());

        for (int round = 1; state.policySelections() < 10; round++) {
            state.openRound(round);
            assertTrue(state.choose(state.offers().getFirst()));
            if (state.policySelections() == 5) state.promoteCommander();
        }
        assertTrue(state.commander());
        state.saveWorld();
        assertTrue(state.worldSaved());
        state.openRound(99);
        assertTrue(state.offers().isEmpty());
    }

    @Test
    void defaultsMergeDescriptionsAndRejectInvalidCap() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        FutureAgencyTowers.all().forEach(type -> assertTrue(defaults.towers().containsKey(type.id())));
        assertEquals(0.80, defaults.ability(FutureAgencyBalance.GLOBAL_ID,
                "damageReductionCap", -1), 0.0001);
        assertEquals(0.08, defaults.ability(FutureAgencyBalance.GLOBAL_ID,
                "policy.agency_tactics", -1), 0.0001);

        TowerBalanceConfig merged = new TowerBalanceConfig(Map.of(), Map.of(), Map.of())
                .withMissingDefaults(defaults);
        assertEquals(150, merged.towers().get(FutureAgencyTowers.ESCAPEE.id()).mineralCost());
        assertEquals(1500, merged.upgradeCost(FutureAgencyTowers.COMMANDER.id(),
                FutureAgencyLeaderTower.SAVE_WORLD, -1));

        ProductionTowerCatalogs.reloadBuiltIns(defaults);
        FutureAgencyTowers.all().forEach(type -> assertTrue(
                ProductionTowerCatalog.find(type.id()).orElseThrow().type().description().stream()
                        .noneMatch(line -> line.contains("{ability.") || line.contains("{stat.")), type.id()));

        LinkedHashMap<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        LinkedHashMap<String, Double> invalidGlobal = new LinkedHashMap<>(
                abilities.get(FutureAgencyBalance.GLOBAL_ID));
        invalidGlobal.put("damageReductionCap", 1.1);
        abilities.put(FutureAgencyBalance.GLOBAL_ID, invalidGlobal);
        TowerBalanceConfig invalid = new TowerBalanceConfig(
                defaults.towers(), defaults.upgradeCosts(), abilities,
                defaults.illusionCloneQueue(), defaults.villagerAdv(), defaults.schemaVersion());
        assertThrows(IllegalArgumentException.class, invalid::validateForRuntime);
    }

    @Test
    void leaderCannotBeSoldAndSaveTooltipUsesRequestedPhrase() {
        FutureAgencyLeaderTower leader = (FutureAgencyLeaderTower) create(FutureAgencyTowers.REBUILDER);
        FutureAgencyStates.state(OWNER).reconstruct();
        var save = ProductionTowerCatalog.upgrade(
                ProductionTowerCatalog.find(FutureAgencyTowers.REBUILDER.id()).orElseThrow().type(),
                FutureAgencyLeaderTower.SAVE_WORLD).orElseThrow();
        assertFalse(leader.canBeSold());
        assertTrue(leader.showsUnavailableUpgrade(null, save));
        assertFalse(leader.meetsUpgradeRequirements(null, save));
        assertTrue(leader.upgradeTooltipLines(save).stream().anyMatch(line -> line.contains("이번에야말로")));
        assertTrue(leader.upgradeTooltipLines(save).stream().anyMatch(line -> line.contains("기관 최고 지휘자")));
        assertTrue(leader.upgradeTooltipLines(save).stream().anyMatch(line -> line.contains("정책 0/10")));
        assertTrue(leader.runtimeDetailLines().stream().noneMatch(line -> line.contains("정책 0/10")));
        assertEquals(20.0, FutureAgencyTowers.REBUILDER.damage(), 0.0001);
        assertEquals(7.0, FutureAgencyTowers.REBUILDER.range(), 0.0001);
        assertEquals(45.0, FutureAgencyTowers.COMMANDER.damage(), 0.0001);
    }

    @Test
    void leaderDialogOrdersSaveBeforePoliciesAndAgentStatsAllowNullTarget() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
        var upgrades = ProductionTowerCatalog.upgrades(
                ProductionTowerCatalog.find(FutureAgencyTowers.REBUILDER.id()).orElseThrow().type());
        assertEquals(FutureAgencyLeaderTower.SAVE_WORLD, upgrades.getFirst().id());
        assertTrue(FutureAgencyPolicy.fromUpgradeId(upgrades.get(1).id()).isPresent());

        FutureAgencyAgentTower agent = (FutureAgencyAgentTower) create(
                FutureAgencyTowers.agent(FutureAgencyRole.COMBAT, 5));
        assertEquals(agent.type().damage(), agent.modifyAttackDamage(null, null, agent.type().damage()), 0.0001);
    }

    private static long upgradeCost(kim.biryeong.semiontd.tower.TowerType type, String id) {
        return ProductionTowerCatalog.upgrade(
                ProductionTowerCatalog.find(type.id()).orElseThrow().type(), id).orElseThrow().mineralCost();
    }

    private static kim.biryeong.semiontd.tower.Tower create(
            kim.biryeong.semiontd.tower.TowerType type) {
        return ProductionTowerCatalog.find(type.id()).orElseThrow()
                .create(OWNER, TeamId.RED, 1, new GridPosition(0, 64, 0));
    }
}
