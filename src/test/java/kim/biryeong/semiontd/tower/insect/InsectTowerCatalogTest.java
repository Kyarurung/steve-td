package kim.biryeong.semiontd.tower.insect;

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
import kim.biryeong.semiontd.job.InsectTowerJob;
import kim.biryeong.semiontd.job.JobRegistry;
import kim.biryeong.semiontd.map.LaneRegionLayout;
import kim.biryeong.semiontd.tower.ProductionTower;
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

final class InsectTowerCatalogTest {
    private static final UUID OWNER = UUID.nameUUIDFromBytes("insect-owner".getBytes());

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
    void catalogRegistersFourStartersAndSixUpgradeEdges() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());

        var entries = ProductionTowerCatalog.all().stream()
                .filter(entry -> InsectTowers.isInsectTower(entry.type()))
                .toList();
        assertEquals(10, entries.size());
        assertEquals(4, entries.stream().filter(ProductionTowerCatalog.CatalogEntry::starter).count());
        assertTrue(JobRegistry.find(InsectTowerJob.ID).isPresent());
        assertInstanceOf(InsectUnitTower.class, create(InsectTowers.SILVERFISH));
        assertInstanceOf(InsectSpawnerTower.class, create(InsectTowers.SPAWNER));
        assertEquals(75, ProductionTowerCatalog.upgrades(InsectTowers.SILVERFISH).getFirst().mineralCost());
        assertEquals(140, ProductionTowerCatalog.upgrades(InsectTowers.ENDERMITE).getFirst().mineralCost());
        assertEquals(90, ProductionTowerCatalog.upgrades(InsectTowers.CAVE_SPIDER).getFirst().mineralCost());
        assertEquals(160, ProductionTowerCatalog.upgrades(InsectTowers.SPIDER).getFirst().mineralCost());
        assertEquals(90, ProductionTowerCatalog.upgrades(InsectTowers.BEE).getFirst().mineralCost());
        assertEquals(170, ProductionTowerCatalog.upgrades(InsectTowers.ENHANCED_BEE).getFirst().mineralCost());
        assertTrue(ProductionTowerCatalog.upgrades(InsectTowers.SPAWNER).isEmpty());
        assertEquals(4.0, ProductionTowerCatalog.find(InsectTowers.CAVE_SPIDER.id()).orElseThrow().type().damage());
        assertEquals(160.0, ProductionTowerCatalog.find(InsectTowers.CAVE_SPIDER.id()).orElseThrow().type().maxHealth());
        assertEquals(8.0, ProductionTowerCatalog.find(InsectTowers.SPIDER.id()).orElseThrow().type().damage());
        assertEquals(320.0, ProductionTowerCatalog.find(InsectTowers.SPIDER.id()).orElseThrow().type().maxHealth());
        assertEquals(15.0, ProductionTowerCatalog.find(InsectTowers.ENHANCED_SPIDER.id()).orElseThrow().type().damage());
        assertEquals(620.0, ProductionTowerCatalog.find(InsectTowers.ENHANCED_SPIDER.id()).orElseThrow().type().maxHealth());
        assertEquals(8.0, ProductionTowerCatalog.find(InsectTowers.BEE.id()).orElseThrow().type().damage());
        assertEquals(13.0, ProductionTowerCatalog.find(InsectTowers.ENHANCED_BEE.id()).orElseThrow().type().damage());
        assertEquals(24.0, ProductionTowerCatalog.find(InsectTowers.QUEEN_BEE.id()).orElseThrow().type().damage());
    }

    @Test
    void firstWaveBoostsTierOneAndBlocksEarlyUpgrade() {
        InsectUnitTower unit = unit(InsectTowers.SILVERFISH, 0);
        unit.recordPlacementEconomy(30, 1);
        unit.onPlaced(null);
        assertFalse(unit.waveStartedAfterPlacement());
        assertTrue(unit.freshPowerActive());
        assertFalse(unit.freshPowerPending());
        assertEquals(225.0, unit.currentMaxHealth(), 0.0001);
        assertEquals(20.0, unit.modifyAttackDamage(null, null, 8.0), 0.0001);
        assertEquals(1.2, unit.visual().scale(), 0.0001);
        assertFalse(unit.meetsUpgradeRequirements(null, null));

        unit.markWaveStarted(1);
        unit.onWaveStarted(null, 1);
        assertTrue(unit.freshPowerActive());
        assertFalse(unit.freshPowerPending());
        assertEquals(225.0, unit.currentMaxHealth(), 0.0001);
        assertEquals(20.0, unit.modifyAttackDamage(null, null, 8.0), 0.0001);
        assertTrue(unit.meetsUpgradeRequirements(null, null));

        unit.resetForRound(null);
        assertFalse(unit.freshPowerActive());
        assertEquals(90.0, unit.currentMaxHealth(), 0.0001);
        assertEquals(8.0, unit.modifyAttackDamage(null, null, 8.0), 0.0001);
        assertEquals(1.0, unit.visual().scale(), 0.0001);
    }

    @Test
    void revivalUsesThreeThenFiveSecondsAndCancelsWhenSpawnerDies() {
        PlayerLane lane = testLane();
        ProductionTower spawner = new ProductionTower(
                InsectTowers.SPAWNER, OWNER, TeamId.RED, 1, position(0));
        InsectUnitTower unit = unit(InsectTowers.SILVERFISH, 2);
        lane.addTower(spawner);
        lane.addTower(unit);
        unit.recordPlacementEconomy(30, 1);
        unit.markWaveStarted(1);
        unit.onWaveStarted(lane, 1);

        unit.syncHealth(0.0);
        assertFalse(unit.isDestroyed(lane));
        assertEquals(1, unit.deathsThisRound());
        assertEquals(60, unit.reviveTicksRemaining());
        for (int tick = 0; tick < 60; tick++) {
            unit.tick(lane);
        }
        assertEquals(unit.currentMaxHealth(), unit.health(), 0.0001);

        unit.syncHealth(0.0);
        assertFalse(unit.isDestroyed(lane));
        assertEquals(100, unit.reviveTicksRemaining());
        spawner.syncHealth(0.0);
        assertTrue(unit.isDestroyed(lane));
    }

    @Test
    void spiderReductionMultipliesWithDeathVulnerability() {
        PlayerLane lane = testLane();
        ProductionTower spawner = new ProductionTower(
                InsectTowers.SPAWNER, OWNER, TeamId.RED, 1, position(0));
        InsectUnitTower spider = unit(InsectTowers.CAVE_SPIDER, 2);
        lane.addTower(spawner);
        lane.addTower(spider);
        spider.markWaveStarted(1);
        spider.onWaveStarted(lane, 1);
        assertEquals(90.0, spider.modifyIncomingDamage(null, null, 100.0), 0.0001);

        spider.syncHealth(0.0);
        assertFalse(spider.isDestroyed(lane));
        assertEquals(103.5, spider.modifyIncomingDamage(null, null, 100.0), 0.0001);
        spider.resetForRound(lane);
        assertEquals(90.0, spider.modifyIncomingDamage(null, null, 100.0), 0.0001);
    }

    @Test
    void outsideSpawnerRangeMakesDeathPermanent() {
        PlayerLane lane = testLane();
        lane.addTower(new ProductionTower(InsectTowers.SPAWNER, OWNER, TeamId.RED, 1, position(0)));
        InsectUnitTower unit = unit(InsectTowers.BEE, 8);
        lane.addTower(unit);
        unit.markWaveStarted(1);
        unit.onWaveStarted(lane, 1);
        unit.syncHealth(0.0);
        assertTrue(unit.isDestroyed(lane));
    }

    @Test
    void defaultsMergeDescriptionsAndRejectInvalidReduction() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        assertTrue(InsectTowers.all().stream().allMatch(type -> defaults.towers().containsKey(type.id())));
        assertEquals(2.5, defaults.ability(InsectBalance.GLOBAL_ID, "freshPowerMultiplier", -1), 0.0001);
        assertEquals(1.2, defaults.ability(InsectBalance.GLOBAL_ID, "freshPowerScale", -1), 0.0001);
        assertEquals(60, defaults.abilityTicks(InsectBalance.GLOBAL_ID, "reviveBaseTicks", -1));
        assertEquals(0.15, defaults.ability(InsectBalance.GLOBAL_ID, "deathDamageTakenPerStack", -1), 0.0001);
        assertEquals(6.0, defaults.ability(InsectTowers.SPAWNER.id(), "reviveRadius", -1), 0.0001);
        assertEquals(20, defaults.towers().get(InsectTowers.SPAWNER.id()).aggroPriority());

        ProductionTowerCatalogs.reloadBuiltIns(defaults);
        InsectTowers.all().forEach(type -> assertTrue(
                ProductionTowerCatalog.find(type.id()).orElseThrow().type().description().stream()
                        .noneMatch(line -> line.contains("{ability.")), type.id()));

        LinkedHashMap<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        abilities.put(InsectTowers.CAVE_SPIDER.id(), Map.of("damageReduction", 1.0));
        TowerBalanceConfig invalid = new TowerBalanceConfig(
                defaults.towers(), defaults.upgradeCosts(), abilities,
                defaults.illusionCloneQueue(), defaults.villagerAdv(), defaults.schemaVersion());
        assertThrows(IllegalArgumentException.class, invalid::validateForRuntime);
    }

    private static InsectUnitTower unit(kim.biryeong.semiontd.tower.TowerType type, int x) {
        return new InsectUnitTower(type, OWNER, TeamId.RED, 1, position(x), position(x));
    }

    private static kim.biryeong.semiontd.tower.Tower create(kim.biryeong.semiontd.tower.TowerType type) {
        return ProductionTowerCatalog.find(type.id()).orElseThrow()
                .create(OWNER, TeamId.RED, 1, position(0));
    }

    private static GridPosition position(int x) {
        return new GridPosition(x, 64, 0);
    }

    private static PlayerLane testLane() {
        LaneRegionLayout layout = new LaneRegionLayout(
                1,
                new Vec3(0.5, 64, 0.5),
                List.of(new Vec3(5.5, 64, 0.5)),
                new Vec3(10.5, 64, 0.5),
                BlockBounds.of(new BlockPos(0, 63, 0), new BlockPos(20, 66, 4)),
                List.of(new GridPosition(10, 63, 0))
        );
        return new PlayerLane(TeamId.RED, 1, OWNER, null, layout);
    }
}
