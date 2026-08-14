package kim.biryeong.semiontd.tower.mage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.job.JobRegistry;
import kim.biryeong.semiontd.job.MageTowerJob;
import kim.biryeong.semiontd.map.LaneRegionLayout;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.nucleoid.map_templates.BlockBounds;

final class MageTowerCatalogTest {
    private static final UUID OWNER = UUID.nameUUIDFromBytes("mage-owner".getBytes());

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void reset() {
        MageStates.clear(OWNER);
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void catalogExposesThreeStartersAndTemporaryZeroCostChoices() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());

        var entries = ProductionTowerCatalog.all().stream()
                .filter(entry -> MageTowers.isMageTower(entry.type()))
                .toList();
        assertEquals(MageTowers.all().size(), entries.size());
        assertEquals(3, entries.stream().filter(ProductionTowerCatalog.CatalogEntry::starter).count());
        assertTrue(JobRegistry.find(MageTowerJob.ID).isPresent());
        assertEquals(MageSpell.values().length, ProductionTowerCatalog.upgrades(MageTowers.WIZARD).size());
        assertEquals(MageTowers.predictionTypes().size(), ProductionTowerCatalog.upgrades(MageTowers.PROPHET).size());
        assertTrue(ProductionTowerCatalog.upgrades(MageTowers.WIZARD).stream()
                .allMatch(option -> option.mineralCost() == 0));
        assertTrue(ProductionTowerCatalog.upgrades(MageTowers.PROPHET).stream()
                .allMatch(option -> option.mineralCost() == 0));
        assertFalse(ProductionTowerCatalog.hasUpgrades(MageTowers.MAGIC_CORE));
    }

    @Test
    void factoriesCreateDedicatedRuntimeTypes() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
        GridPosition position = new GridPosition(0, 64, 0);

        assertInstanceOf(MageWizardTower.class, ProductionTowerCatalog.find(MageTowers.WIZARD.id())
                .orElseThrow().create(OWNER, TeamId.RED, 1, position));
        assertInstanceOf(MageProphetTower.class, ProductionTowerCatalog.find(MageTowers.PROPHET.id())
                .orElseThrow().create(OWNER, TeamId.RED, 1, position));
        assertInstanceOf(MageCoreTower.class, ProductionTowerCatalog.find(MageTowers.MAGIC_CORE.id())
                .orElseThrow().create(OWNER, TeamId.RED, 1, position));
    }

    @Test
    void manaStartsOnceCapsAtOneThousandAndCoreBreakPreservesIt() {
        MageStates.PlayerState state = MageStates.state(OWNER);
        state.grantStartingMana();
        state.grantStartingMana();
        assertEquals(30, state.mana());

        state.addMana(5_000);
        assertEquals(1_000, state.mana());
        MageCoreTower core = new MageCoreTower(
                MageTowers.MAGIC_CORE, OWNER, TeamId.RED, 1, new GridPosition(0, 64, 0)
        );
        core.onDeath(null);
        assertEquals(800, state.mana());
        state.clearMana();
        state.grantStartingMana();
        assertEquals(0, state.mana(), "Reinstalling the core must not grant starting mana twice.");
    }

    @Test
    void spellSelectionDoesNotSpendManaBeforeTheSpellActuallyCasts() {
        MageStates.PlayerState state = MageStates.state(OWNER);
        state.grantStartingMana();
        MageWizardTower support = new MageWizardTower(
                MageTowers.spellType(MageSpell.MAGIC_AMPLIFICATION), OWNER, TeamId.RED, 1,
                new GridPosition(0, 64, 0)
        );
        support.onPlaced(null);
        support.onWaveStarted(null, 1);
        assertEquals(30, state.mana());
        assertFalse(support.spellUsed());
        assertEquals(10, support.naturalManaProduction());

        MageStates.clear(OWNER);
        state = MageStates.state(OWNER);
        state.grantStartingMana();
        MageWizardTower unusedAttack = new MageWizardTower(
                MageTowers.spellType(MageSpell.MANA_MISSILE), OWNER, TeamId.RED, 1,
                new GridPosition(0, 64, 0)
        );
        unusedAttack.onPlaced(null);
        unusedAttack.onWaveStarted(null, 1);
        assertEquals(30, state.mana());
        assertFalse(unusedAttack.spellUsed());
        assertEquals(10, unusedAttack.naturalManaProduction());
    }

    @Test
    void livingCoreProducesSeventyManaAtRoundEnd() {
        MageStates.PlayerState state = MageStates.state(OWNER);
        PlayerLane lane = testLane();
        MageCoreTower core = new MageCoreTower(
                MageTowers.MAGIC_CORE, OWNER, TeamId.RED, 1,
                new GridPosition(0, 64, 0), new GridPosition(0, 64, 0)
        );
        lane.addTower(core);
        assertEquals(30, state.mana());

        MageTowerLifecycle.finishRound(lane, OWNER);

        assertEquals(100, state.mana());
    }

    @Test
    void defaultsAndDescriptionsPublishEveryMageValue() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        assertTrue(MageTowers.all().stream().allMatch(type -> defaults.towers().containsKey(type.id())));
        assertEquals(1_000.0, defaults.ability(MageBalance.GLOBAL_ID, "manaCapacity", -1), 0.0001);
        assertEquals(70.0, defaults.ability(MageBalance.GLOBAL_ID, "coreMana", -1), 0.0001);
        assertEquals(0.20, defaults.ability(MageBalance.GLOBAL_ID, "coreBreakManaLossRatio", -1), 0.0001);
        assertEquals(400.0, defaults.ability(MageBalance.GLOBAL_ID, "dimensional_collapseManaCost", -1), 0.0001);
        assertEquals(0.99, defaults.ability(MageBalance.GLOBAL_ID, "rangedBarrierReduction", -1), 0.0001);
        assertEquals(1.0, defaults.ability(MageBalance.GLOBAL_ID, "amplificationBonus", -1), 0.0001);
        assertEquals(1.0, defaults.ability(MageBalance.GLOBAL_ID, "manaDamageBonusAtCapacity", -1), 0.0001);
        assertEquals(10.0, defaults.ability(MageBalance.GLOBAL_ID, "missileDamage", -1), 0.0001);
        assertEquals(30.0, defaults.ability(MageBalance.GLOBAL_ID, "windCutterDamage", -1), 0.0001);
        assertEquals(120.0, defaults.ability(MageBalance.GLOBAL_ID, "manaBombDamage", -1), 0.0001);
        assertEquals(90.0, defaults.ability(MageBalance.GLOBAL_ID, "chainDamage1", -1), 0.0001);
        assertEquals(70.0, defaults.ability(MageBalance.GLOBAL_ID, "frostWaveDamage", -1), 0.0001);
        assertEquals(450.0, defaults.ability(MageBalance.GLOBAL_ID, "collapseDamage", -1), 0.0001);

        ProductionTowerCatalogs.reloadBuiltIns(defaults);
        for (var type : MageTowers.all()) {
            List<String> description = ProductionTowerCatalog.find(type.id()).orElseThrow().type().description();
            assertFalse(description.isEmpty());
            assertTrue(description.stream().noneMatch(line -> line.contains("{ability.")), type.id());
        }
    }

    @Test
    void missingDefaultsMergeKeepsOverridesAndValidationRejectsBadRatios() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        TowerBalanceConfig partial = new TowerBalanceConfig(
                Map.of(), Map.of(), Map.of(MageBalance.GLOBAL_ID, Map.of("manaCapacity", 777.0))
        );
        TowerBalanceConfig merged = partial.withMissingDefaults(defaults);
        assertEquals(777.0, merged.ability(MageBalance.GLOBAL_ID, "manaCapacity", -1), 0.0001);
        assertEquals(105.0, merged.ability(MageBalance.GLOBAL_ID, "prophecyReward", -1), 0.0001);

        LinkedHashMap<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        LinkedHashMap<String, Double> mage = new LinkedHashMap<>(abilities.get(MageBalance.GLOBAL_ID));
        mage.put("rangedBarrierReduction", 1.25);
        abilities.put(MageBalance.GLOBAL_ID, mage);
        TowerBalanceConfig invalid = new TowerBalanceConfig(
                defaults.towers(), defaults.upgradeCosts(), abilities,
                defaults.illusionCloneQueue(), defaults.villagerAdv(), defaults.schemaVersion()
        );
        assertThrows(IllegalArgumentException.class, invalid::validateForRuntime);
    }

    @Test
    void manaBossBarShowsCurrentConfiguredCapacity() {
        assertEquals("마나 - 30/1000", MageManaBossBarService.title(30, 1_000).getString());
        assertEquals(0.03f, MageManaBossBarService.progress(30, 1_000), 0.0001f);
        assertEquals(1.0f, MageManaBossBarService.progress(2_000, 1_000), 0.0001f);
    }

    private static PlayerLane testLane() {
        LaneRegionLayout layout = new LaneRegionLayout(
                1,
                new Vec3(0.5, 64.0, 0.5),
                List.of(new Vec3(5.5, 64.0, 0.5)),
                new Vec3(10.5, 64.0, 0.5),
                BlockBounds.of(new BlockPos(-4, 60, -4), new BlockPos(14, 70, 4)),
                List.of(new GridPosition(8, 64, 0))
        );
        return new PlayerLane(TeamId.RED, 1, OWNER, null, layout);
    }
}
