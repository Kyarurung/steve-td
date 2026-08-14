package kim.biryeong.semiontd.tower.engineer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.config.EconomyConfig;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.WaveConfig;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerEconomy;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.SemionPlayer;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.job.EngineerTowerJob;
import kim.biryeong.semiontd.job.JobContext;
import kim.biryeong.semiontd.job.JobRegistry;
import kim.biryeong.semiontd.map.GameArena;
import kim.biryeong.semiontd.map.LaneRegionLayout;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.map_templates.BlockBounds;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class EngineerTowerCatalogTest {
    private static final UUID OWNER = UUID.nameUUIDFromBytes("engineer-owner".getBytes());
    private static final GridPosition POSITION = new GridPosition(0, 64, 0);

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void resetCatalog() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void catalogRegistersEightStartersAndEveryUpgrade() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());

        var entries = ProductionTowerCatalog.all().stream()
                .filter(entry -> EngineerTowers.isEngineerTower(entry.type()))
                .toList();
        assertEquals(EngineerTowers.all().size(), entries.size());
        assertEquals(8, entries.stream().filter(ProductionTowerCatalog.CatalogEntry::starter).count());
        assertTrue(JobRegistry.find(EngineerTowerJob.ID).isPresent());
        assertEquals(15, ProductionTowerCatalog.find(EngineerTowers.REDSTONE_DUST.id())
                .orElseThrow().type().mineralCost());
        assertEquals(4, ProductionTowerCatalog.upgrades(EngineerTowers.REDSTONE_DUST).size());
        assertEquals(1, ProductionTowerCatalog.upgrades(EngineerTowers.repeater(Direction.NORTH)).size());
        for (EngineerTowers.PlateKind kind : EngineerTowers.PlateKind.values()) {
            assertEquals(kind == EngineerTowers.PlateKind.GOLD ? 0 : 1,
                    ProductionTowerCatalog.upgrades(EngineerTowers.plate(kind)).size());
        }
        for (EngineerTowers.TrapKind kind : EngineerTowers.TrapKind.values()) {
            assertEquals(1, ProductionTowerCatalog.upgrades(EngineerTowers.trap(kind, 1)).size());
            assertEquals(1, ProductionTowerCatalog.upgrades(EngineerTowers.trap(kind, 2)).size());
            assertEquals(0, ProductionTowerCatalog.upgrades(EngineerTowers.trap(kind, 3)).size());
        }
    }

    @Test
    void factoriesAndSlotWeightsMatchContract() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());

        assertInstanceOf(EngineerGolemTower.class, create(EngineerTowers.COPPER_GOLEM));
        assertInstanceOf(EngineerCircuitTower.class, create(EngineerTowers.REDSTONE_DUST));
        assertInstanceOf(EngineerCircuitTower.class, create(EngineerTowers.plate(EngineerTowers.PlateKind.GOLD)));
        assertInstanceOf(EngineerTrapTower.class, create(EngineerTowers.trap(EngineerTowers.TrapKind.TNT, 1)));
        assertEquals(0, create(EngineerTowers.COPPER_GOLEM).slotWeight());
        assertEquals(0, create(EngineerTowers.REDSTONE_DUST).slotWeight());
        assertEquals(0, create(EngineerTowers.repeater(Direction.WEST)).slotWeight());
        assertEquals(1, create(EngineerTowers.plate(EngineerTowers.PlateKind.WOOD)).slotWeight());
        assertEquals(1, create(EngineerTowers.trap(EngineerTowers.TrapKind.DOOR, 1)).slotWeight());
        assertFalse(create(EngineerTowers.trap(EngineerTowers.TrapKind.DOOR, 1)).participatesInFinalDefense());
    }

    @Test
    void priorityAndUpgradeChainRunFromWoodToGold() {
        assertEquals(4, EngineerTowers.PlateKind.GOLD.priority());
        assertEquals(3, EngineerTowers.PlateKind.IRON.priority());
        assertEquals(1, EngineerTowers.PlateKind.WOOD.priority());
        assertEquals(EngineerTowers.PlateKind.STONE, EngineerTowers.PlateKind.WOOD.next().orElseThrow());
        assertTrue(EngineerTowers.PlateKind.GOLD.next().isEmpty());
    }

    @Test
    void defaultsMergeAndDescriptionsResolve() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        assertTrue(EngineerTowers.all().stream().allMatch(type -> defaults.towers().containsKey(type.id())));
        assertEquals(60, defaults.abilityTicks(EngineerBalance.GLOBAL_ID, "activeTicks", -1));
        assertEquals(60, defaults.abilityTicks(EngineerBalance.GLOBAL_ID, "tntFuseTicks", -1));
        assertEquals(35, defaults.abilityInt(EngineerBalance.GLOBAL_ID, "maxRedstone", -1));
        assertEquals(4, defaults.abilityInt(EngineerBalance.GLOBAL_ID, "maxPlates", -1));
        assertEquals(4, defaults.abilityInt(EngineerBalance.GLOBAL_ID, "maxPistons", -1));
        assertEquals(0.20, defaults.ability(
                EngineerBalance.GLOBAL_ID, "dispenserDamagePerPlateBlock", -1), 0.0001);
        assertEquals(1.2, EngineerBalance.dispenserDamageMultiplier(1), 0.0001);
        assertEquals(2.0, EngineerBalance.dispenserDamageMultiplier(5), 0.0001);
        assertEquals(650.0, defaults.ability(
                EngineerTowers.trap(EngineerTowers.TrapKind.TNT, 3).id(), "damage", -1), 0.0001);
        ProductionTowerCatalogs.reloadBuiltIns(defaults);
        for (EngineerTowers.TrapKind kind : EngineerTowers.TrapKind.values()) {
            assertEquals(EngineerTowers.trap(kind, 1).displayName(), EngineerTowers.trap(kind, 2).displayName());
            assertEquals("강화", ProductionTowerCatalog.upgrades(EngineerTowers.trap(kind, 1)).getFirst().displayName());
        }

        TowerBalanceConfig partial = new TowerBalanceConfig(
                Map.of(), Map.of(), Map.of(EngineerBalance.GLOBAL_ID, Map.of("activeTicks", 80.0))
        );
        TowerBalanceConfig merged = partial.withMissingDefaults(defaults);
        assertEquals(80, merged.abilityTicks(EngineerBalance.GLOBAL_ID, "activeTicks", -1));
        assertEquals(100, merged.abilityTicks(EngineerBalance.GLOBAL_ID, "plateCooldownTicks", -1));

        for (var type : EngineerTowers.all()) {
            List<String> description = ProductionTowerCatalog.find(type.id()).orElseThrow().type().description();
            assertFalse(description.isEmpty());
            assertTrue(description.stream().noneMatch(line -> line.contains("{ability.")), type.id());
        }
    }

    @Test
    void invalidSlowRatioIsRejected() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        LinkedHashMap<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        String slime = EngineerTowers.trap(EngineerTowers.TrapKind.SLIME, 1).id();
        LinkedHashMap<String, Double> values = new LinkedHashMap<>(abilities.get(slime));
        values.put("slow", 1.2);
        abilities.put(slime, values);
        TowerBalanceConfig invalid = new TowerBalanceConfig(
                defaults.towers(), defaults.upgradeCosts(), abilities,
                defaults.illusionCloneQueue(), defaults.villagerAdv(), defaults.schemaVersion()
        );
        assertThrows(IllegalArgumentException.class, invalid::validateForRuntime);
    }

    @Test
    void jobLimitsOneGolemThirtyFiveRedstonePartsAndFourPlates() {
        EconomyConfig economy = EconomyConfig.defaultConfig();
        SemionGame game = new SemionGame(economy, WaveConfig.defaultConfig(), new GameArena(Map.of()));
        SemionPlayer player = new SemionPlayer(OWNER, "engineer", TeamId.RED, 1, new PlayerEconomy(economy));
        game.players().put(OWNER, player);
        game.teams().get(TeamId.RED).activate();
        PlayerLane lane = testLane();
        game.teams().get(TeamId.RED).laneGroup().addLane(lane);
        EngineerTowerJob job = new EngineerTowerJob();
        JobContext context = new JobContext(game, player);

        assertTrue(job.canUseTower(context, EngineerTowers.COPPER_GOLEM));
        lane.addTower(new TestTower(EngineerTowers.COPPER_GOLEM, 0));
        assertFalse(job.canUseTower(context, EngineerTowers.COPPER_GOLEM));
        for (int index = 0; index < EngineerBalance.MAX_REDSTONE - 1; index++) {
            lane.addTower(new TestTower(EngineerTowers.REDSTONE_DUST, index + 1));
        }
        lane.addTower(new TestTower(EngineerTowers.repeater(Direction.NORTH), EngineerBalance.MAX_REDSTONE));
        assertFalse(job.canUseTower(context, EngineerTowers.REDSTONE_DUST));
        assertTrue(job.canUseTower(context, EngineerTowers.repeater(Direction.EAST)));
        for (int index = 0; index < EngineerBalance.MAX_PLATES; index++) {
            assertTrue(job.canUseTower(context, EngineerTowers.plate(EngineerTowers.PlateKind.WOOD)));
            lane.addTower(new TestTower(EngineerTowers.plate(EngineerTowers.PlateKind.WOOD), 36 + index));
        }
        assertFalse(job.canUseTower(context, EngineerTowers.plate(EngineerTowers.PlateKind.WOOD)));
        assertTrue(job.canUseTower(context, EngineerTowers.plate(EngineerTowers.PlateKind.STONE)));
        for (int index = 0; index < EngineerBalance.MAX_PISTONS; index++) {
            assertTrue(job.canUseTower(context, EngineerTowers.trap(EngineerTowers.TrapKind.PISTON, 1)));
            lane.addTower(new TestTower(
                    EngineerTowers.trap(EngineerTowers.TrapKind.PISTON, 1),
                    50 + index
            ));
        }
        assertFalse(job.canUseTower(context, EngineerTowers.trap(EngineerTowers.TrapKind.PISTON, 1)));
        assertTrue(job.canUseTower(context, EngineerTowers.trap(EngineerTowers.TrapKind.PISTON, 2)),
                "The four-piston limit must not block upgrading an existing piston.");
    }

    @Test
    void redstoneBossBarShowsCurrentConfiguredLimit() {
        assertEquals("레드스톤 - 12/35", EngineerRedstoneBossBarService.title(12, 35).getString());
        assertEquals(12.0f / 35.0f, EngineerRedstoneBossBarService.progress(12, 35), 0.0001f);
        assertEquals(1.0f, EngineerRedstoneBossBarService.progress(99, 35), 0.0001f);
    }

    private static kim.biryeong.semiontd.tower.Tower create(kim.biryeong.semiontd.tower.TowerType type) {
        return ProductionTowerCatalog.find(type.id()).orElseThrow().create(OWNER, TeamId.RED, 1, POSITION);
    }

    private static PlayerLane testLane() {
        Vec3 spawn = new Vec3(0.5, 64.0, 0.5);
        LaneRegionLayout layout = new LaneRegionLayout(
                1,
                spawn,
                List.of(new Vec3(0.5, 64.0, 4.5)),
                new Vec3(0.5, 64.0, 10.5),
                BlockBounds.of(new BlockPos(0, 63, 0), new BlockPos(40, 66, 10)),
                List.of(new GridPosition(0, 63, 10))
        );
        return new PlayerLane(TeamId.RED, 1, OWNER, null, layout);
    }

    private static final class TestTower extends Tower {
        private TestTower(TowerType type, int x) {
            super(type, OWNER, TeamId.RED, 1, new GridPosition(x, 64, 1));
        }

        @Override
        protected boolean execute(PlayerLane lane) {
            return false;
        }
    }
}
