package kim.biryeong.semiontd.tower.end;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.visual.BlockDisplayVisual;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.map.LaneRegionLayout;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.nucleoid.map_templates.BlockBounds;

class EndTowerAbsorptionTest {
    private static final UUID OWNER = UUID.nameUUIDFromBytes("end-absorption-owner".getBytes());

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void reloadCatalogs() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
    }

    @AfterEach
    void resetBalance() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void onlyFullyAbsorbedTowerIsCountedWhileStatsTransferGradually() {
        applyAbsorptionDuration(4);
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower enderman = tower(EndTowers.T1_ENDERMITE_TOWER, 1);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.towers().add(enderman);
        assertTrue(plainRuntimeDetails(enderman).contains("엔더 드래곤에게 힘 전달 대기 중"));
        assertFalse(plainRuntimeDetails(enderman).contains("힘 전달 진행률"));
        enderman.onWaveStarted(lane, 1);
        assertTrue(plainRuntimeDetails(enderman).contains("힘 전달 진행률 0.0%"));
        assertFalse(plainRuntimeDetails(enderman).contains("엔더 드래곤에게 힘 전달 대기 중"));

        tick(dragon, lane, 3);

        assertEquals(0, dragon.absorbedEndCrystalCount());
        assertTrue(lane.towers().contains(enderman));
        assertEquals(4.5, dragon.roundDamageBonus(), 0.0001);
        assertEquals(0.45, dragon.permanentDamageBonus(), 0.0001);
        assertEquals(4.95, dragon.absorbedDamageBonus(), 0.0001);
        assertEquals(0.0, dragon.absorbedHealthBonus(), 0.0001);
        assertEquals(0.75, enderman.transferProgress(), 0.0001);
        assertTrue(plainRuntimeDetails(enderman).contains("힘 전달 진행률 75.0%"));

        tick(dragon, lane, 1);

        assertEquals(1, dragon.absorbedEndCrystalCount());
        assertTrue(lane.towers().contains(enderman));
        assertEquals(0.0, enderman.health(), 0.0001);
        assertEquals(0.0, enderman.transferProgress(), 0.0001);
        assertEquals(6.0, dragon.roundDamageBonus(), 0.0001);
        assertEquals(0.6, dragon.permanentDamageBonus(), 0.0001);
        assertEquals(6.6, dragon.absorbedDamageBonus(), 0.0001);
        assertEquals(0.0, dragon.absorbedHealthBonus(), 0.0001);
        assertEquals(1, dragon.roundCompletedTransferCount());
        assertEquals(19, dragon.adjustAttackInterval(20));
        String crystalHeavyDetails = plainRuntimeDetails(dragon);
        assertTrue(crystalHeavyDetails.contains("엔드 수정, 셜커 스택: 1 / 0"));
        assertTrue(crystalHeavyDetails.contains("공격 속도: -1틱 / -10틱"));
        assertTrue(String.join("\n", dragon.runtimeDetailLines()).contains("<#D94343>추가 공격력: 0.6</#D94343>"));

        tick(dragon, lane, 4);

        assertEquals(1, dragon.absorbedEndCrystalCount());
        assertTrue(lane.towers().contains(enderman));
        assertEquals(0.0, enderman.health(), 0.0001);
        assertEquals(6.6, dragon.absorbedDamageBonus(), 0.0001);
    }

    @Test
    void interruptedTransferRollsBackStatsAndDoesNotCountTower() {
        applyAbsorptionDuration(4);
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower endCrystalLine = tower(EndTowers.T1_ENDERMITE_TOWER, 1);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.towers().add(endCrystalLine);

        tick(dragon, lane, 2);
        lane.towers().remove(endCrystalLine);
        tick(dragon, lane, 1);

        assertEquals(0, dragon.absorbedEndCrystalCount());
        assertEquals(0.0, dragon.roundDamageBonus(), 0.0001);
        assertEquals(0.0, dragon.permanentDamageBonus(), 0.0001);
        assertEquals(0.0, endCrystalLine.transferProgress(), 0.0001);
    }

    @Test
    void interruptedTransferAlsoRollsBackDragonEvolutionState() {
        applyEndAbilities(Map.of(
                "absorptionDurationTicks", 4.0,
                "dragonEvolutionMaxHealth", 220.0,
                "roundHealthRatio", 0.50,
                "permanentHealthRatio", 0.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower shulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.towers().add(shulker);

        tick(dragon, lane, 2);

        assertEquals(EndTowerState.DRAGON, dragon.state());
        assertEquals(225.0, dragon.currentMaxHealth(), 0.0001);

        lane.towers().remove(shulker);
        dragon.tick(lane);

        assertEquals(EndTowerState.PHANTOM, dragon.state());
        assertEquals(200.0, dragon.currentMaxHealth(), 0.0001);
        assertEquals(0.0, dragon.finalDamageBonus(), 0.0001);
    }

    @Test
    void interruptedHealthTransferNeverHealsTheCoreForFree() {
        applyEndAbilities(Map.of(
                "absorptionDurationTicks", 4.0,
                "roundHealthRatio", 0.50,
                "permanentHealthRatio", 0.0,
                "transferHealingPerTower", 0.0,
                "absorptionHealAmount", 0.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower firstShulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.syncHealth(100.0);
        lane.towers().add(firstShulker);

        tick(dragon, lane, 2);

        assertEquals(225.0, dragon.currentMaxHealth(), 0.0001);
        assertEquals(100.0, dragon.health(), 0.0001);

        lane.towers().remove(firstShulker);
        dragon.tick(lane);

        assertEquals(200.0, dragon.currentMaxHealth(), 0.0001);
        assertEquals(100.0, dragon.health(), 0.0001);

        EndTower secondShulker = tower(EndTowers.T1_SHULKER_TOWER, 2);
        lane.towers().add(secondShulker);
        tick(dragon, lane, 2);
        lane.towers().remove(secondShulker);
        dragon.tick(lane);

        assertEquals(200.0, dragon.currentMaxHealth(), 0.0001);
        assertEquals(100.0, dragon.health(), 0.0001);
    }

    @Test
    void typeRefreshPreservesHealthAfterAbsorbedMaxHealthIsRecalculated() {
        applyAbsorptionDuration(1);
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower shulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.towers().add(shulker);
        dragon.tick(lane);
        dragon.syncHealth(250.0);

        dragon.refreshType(dragon.type(), lane);

        assertEquals(254.0, dragon.currentMaxHealth(), 0.0001);
        assertEquals(250.0, dragon.health(), 0.0001);
    }

    @Test
    void typeRefreshRestartsAnActiveTransferWithTheNewBalanceSnapshot() {
        applyEndAbilities(Map.of(
                "absorptionDurationTicks", 4.0,
                "roundHealthRatio", 0.50,
                "permanentHealthRatio", 0.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower shulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.towers().add(dragon);
        lane.towers().add(shulker);
        dragon.onWaveStarted(lane, 1);
        tick(dragon, lane, 2);

        assertEquals(0.50, shulker.transferProgress(), 0.0001);
        assertEquals(25.0, dragon.roundHealthBonus(), 0.0001);

        applyEndAbilities(Map.of(
                "absorptionDurationTicks", 1.0,
                "roundHealthRatio", 0.20,
                "permanentHealthRatio", 0.0
        ));
        dragon.refreshType(dragon.type(), lane);

        assertEquals(0.0, shulker.transferProgress(), 0.0001);
        assertEquals(0.0, dragon.roundHealthBonus(), 0.0001);

        dragon.tick(lane);

        assertEquals(20.0, dragon.roundHealthBonus(), 0.0001);
        assertEquals(0.0, shulker.health(), 0.0001);
    }

    @Test
    void invalidEndBalanceIsRejectedBeforeItBecomesRuntimeState() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        Map<String, Double> end = new LinkedHashMap<>(abilities.get(EndTower.CONFIG_ID));
        end.put("absorptionDurationTicks", 0.0);
        abilities.put(EndTower.CONFIG_ID, end);
        TowerBalanceConfig invalid = new TowerBalanceConfig(
                defaults.towers(),
                defaults.upgradeCosts(),
                abilities
        );

        assertThrows(IllegalArgumentException.class, () -> TowerBalanceRuntime.apply(invalid));
    }

    @Test
    void malformedEndRatiosIntegerSettingsAndCrossFieldRangesAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TowerBalanceRuntime.apply(endConfig(Map.of("splashDamageRatio", 1.01)))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> TowerBalanceRuntime.apply(endConfig(Map.of("absorptionDurationTicks", 1.5)))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> TowerBalanceRuntime.apply(endConfig(Map.of(
                        "phantomBaseScale", 2.0,
                        "phantomScaleCap", 1.0
                )))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> TowerBalanceRuntime.apply(endConfig(Map.of("minimumAttackIntervalTicks", 16.0)))
        );
    }

    @Test
    void nonFiniteTowerStatsAndOversizedEndIntegersAreRejected() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, TowerBalanceConfig.TowerStats> towers =
                new LinkedHashMap<>(defaults.towers());
        TowerBalanceConfig.TowerStats base =
                towers.get(EndTowers.BASE_END_TOWER.id());
        towers.put(
                EndTowers.BASE_END_TOWER.id(),
                new TowerBalanceConfig.TowerStats(
                        base.mineralCost(),
                        Double.NaN,
                        base.range(),
                        base.damage(),
                        base.attackIntervalTicks(),
                        base.aggroPriority()
                )
        );
        TowerBalanceConfig invalidStats = new TowerBalanceConfig(
                towers,
                defaults.upgradeCosts(),
                defaults.abilities()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> TowerBalanceRuntime.apply(invalidStats)
        );

        Map<String, Map<String, Double>> abilities =
                new LinkedHashMap<>(defaults.abilities());
        Map<String, Double> end =
                new LinkedHashMap<>(abilities.get(EndTower.CONFIG_ID));
        end.put("absorptionDurationTicks", (double) Integer.MAX_VALUE + 1.0);
        abilities.put(EndTower.CONFIG_ID, end);
        TowerBalanceConfig oversizedInteger = new TowerBalanceConfig(
                defaults.towers(),
                defaults.upgradeCosts(),
                abilities
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> TowerBalanceRuntime.apply(oversizedInteger)
        );
    }

    @Test
    void everyShulkerOrEndCrystalAbsorptionReducesAttackIntervalForTheCurrentRoundOnly() {
        applyEndAbilities(Map.of(
                "absorptionDurationTicks", 1.0,
                "roundHealthRatio", 0.0,
                "permanentHealthRatio", 0.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        dragon.syncHealth(10.0);
        lane.towers().add(tower(EndTowers.T1_SHULKER_TOWER, 1));

        dragon.tick(lane);

        assertEquals(1, dragon.roundCompletedTransferCount());
        assertEquals(35.0, dragon.health(), 0.0001);
        assertEquals(19, dragon.adjustAttackInterval(20));

        lane.towers().add(tower(EndTowers.T1_SHULKER_TOWER, 2));
        dragon.tick(lane);

        assertEquals(2, dragon.roundCompletedTransferCount());
        assertEquals(60.0, dragon.health(), 0.0001);
        assertEquals(18, dragon.adjustAttackInterval(20));

        lane.towers().add(tower(EndTowers.T1_ENDERMITE_TOWER, 3));
        dragon.tick(lane);

        assertEquals(3, dragon.roundCompletedTransferCount());
        assertEquals(85.0, dragon.health(), 0.0001);
        assertEquals(17, dragon.adjustAttackInterval(20));

        dragon.resetRoundTransferBonuses(null);

        assertEquals(0, dragon.roundCompletedTransferCount());
        assertEquals(20, dragon.adjustAttackInterval(20));
    }

    @Test
    void shulkerTransfersFiftyPercentOfItsHealthForTheCurrentRound() {
        applyEndAbilities(Map.of(
                "absorptionDurationTicks", 1.0,
                "endCrystalAttackIntervalEvery", 1.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower shulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        dragon.syncHealth(10.0);
        lane.towers().add(shulker);

        dragon.tick(lane);

        assertEquals(50.0, dragon.roundHealthBonus(), 0.0001);
        assertEquals(4.0, dragon.permanentHealthBonus(), 0.0001);
        assertEquals(254.0, dragon.currentMaxHealth(), 0.0001);
        assertEquals(35.0, dragon.health(), 0.0001);
        assertEquals(0.0, shulker.health(), 0.0001);
        assertEquals(1, dragon.roundCompletedTransferCount());
        assertEquals(19, dragon.adjustAttackInterval(20));
    }

    @Test
    void shulkerStacksGrantCappedRegenerationThatHealsOncePerSecond() {
        applyEndAbilities(Map.of(
                "absorptionDurationTicks", 1.0,
                "absorptionHealAmount", 0.0,
                "roundHealthRatio", 0.0,
                "permanentHealthRatio", 0.0,
                "shulkerRegenerationEvery", 1.0,
                "regenerationPerStep", 2.0,
                "regenerationCap", 3.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.towers().add(tower(EndTowers.T1_SHULKER_TOWER, 1));
        lane.towers().add(tower(EndTowers.T1_SHULKER_TOWER, 2));

        dragon.tick(lane);

        assertEquals(3.0, dragon.regenerationPerSecond(), 0.0001);
        dragon.syncHealth(10.0);
        tick(dragon, lane, 18);
        assertEquals(10.0, dragon.health(), 0.0001);

        dragon.tick(lane);

        assertEquals(13.0, dragon.health(), 0.0001);
        assertTrue(plainRuntimeDetails(dragon).contains("재생: 3 / 3/초"));
    }

    @Test
    void activeTransfersHealOncePerSecondForEachTransferringTower() {
        applyEndAbilities(Map.of(
                "absorptionDurationTicks", 40.0,
                "absorptionHealAmount", 0.0,
                "transferHealingPerTower", 1.0,
                "transferHealingIntervalTicks", 20.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        dragon.syncHealth(10.0);
        lane.towers().add(tower(EndTowers.T1_ENDERMITE_TOWER, 1));
        lane.towers().add(tower(EndTowers.T1_ENDERMITE_TOWER, 2));

        tick(dragon, lane, 19);
        assertEquals(10.0, dragon.health(), 0.0001);

        dragon.tick(lane);
        assertEquals(12.0, dragon.health(), 0.0001);

        tick(dragon, lane, 20);
        assertEquals(12.0, dragon.health(), 0.0001);
        assertEquals(2, dragon.roundCompletedTransferCount());
    }

    @Test
    void completedTransferDoesNotReceiveAnExtraPeriodicTransferHeal() {
        applyEndAbilities(Map.of(
                "absorptionDurationTicks", 1.0,
                "absorptionHealAmount", 0.0,
                "transferHealingPerTower", 10.0,
                "transferHealingIntervalTicks", 1.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.syncHealth(10.0);
        lane.towers().add(tower(EndTowers.T1_ENDERMITE_TOWER, 1));

        dragon.tick(lane);

        assertEquals(10.0, dragon.health(), 0.0001);
        assertEquals(1, dragon.roundCompletedTransferCount());
    }

    @Test
    void copyingTheCoreRollsBackIncompleteTransferContributions() {
        applyEndAbilities(Map.of(
                "absorptionDurationTicks", 4.0,
                "roundHealthRatio", 0.50,
                "permanentHealthRatio", 0.04
        ));
        PlayerLane lane = lane();
        EndTower original = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower source = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.towers().add(original);
        lane.towers().add(source);
        original.onWaveStarted(lane, 1);
        tick(original, lane, 2);

        assertEquals(25.0, original.roundHealthBonus(), 0.0001);
        assertEquals(2.0, original.permanentHealthBonus(), 0.0001);

        EndTower replacement = tower(EndTowers.BASE_END_TOWER, 2);
        replacement.copyFrom(original, 0);

        assertEquals(0.0, original.roundHealthBonus(), 0.0001);
        assertEquals(0.0, original.permanentHealthBonus(), 0.0001);
        assertEquals(0.0, replacement.roundHealthBonus(), 0.0001);
        assertEquals(0.0, replacement.permanentHealthBonus(), 0.0001);
        assertEquals(0.0, source.transferProgress(), 0.0001);
    }

    @Test
    void upgradingAnAbsorptionTargetKeepsWaveStateButRestartsItsProgress() {
        applyAbsorptionDuration(4);
        PlayerLane lane = lane();
        EndTower core = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower source = tower(EndTowers.T1_ENDERMITE_TOWER, 1);
        lane.towers().add(core);
        lane.towers().add(source);
        core.onWaveStarted(lane, 1);
        source.onWaveStarted(lane, 1);
        tick(core, lane, 2);

        assertEquals(0.5, source.transferProgress(), 0.0001);

        EndTower upgraded = tower(EndTowers.T2_ENDERMAN_TOWER, 1);
        upgraded.copyFrom(source, 0);

        assertEquals(0.0, upgraded.transferProgress(), 0.0001);
        assertTrue(upgraded.runtimeDetailLines().getFirst().contains("%"));
    }

    @Test
    void coreReturnsToEggEachRoundAndPermanentHealthReturnsAfterHatching() {
        applyAbsorptionDuration(1);
        PlayerLane lane = lane();
        EndTower core = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower shulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.towers().add(core);
        core.onWaveStarted(lane, 1);
        core.tick(lane);
        lane.towers().add(shulker);
        core.tick(lane);

        assertEquals(4.0, core.permanentHealthBonus(), 0.0001);
        assertEquals(254.0, core.currentMaxHealth(), 0.0001);

        core.resetForRound(null);

        assertEquals(EndTowerState.EGG, core.state());
        assertEquals(200.0, core.currentMaxHealth(), 0.0001);
        assertEquals(4.0, core.permanentHealthBonus(), 0.0001);
        String eggDetails = plainRuntimeDetails(core);
        assertTrue(eggDetails.contains("엔더 드래곤 능력치"));
        assertTrue(eggDetails.contains("엔드 수정, 셜커 스택: 0 / 1"));
        assertTrue(eggDetails.contains("추가 공격력: 0.0"));
        assertTrue(eggDetails.contains("사거리: 5.0블록 / 8.0블록"));
        assertTrue(eggDetails.contains("공격 속도: -0틱 / -10틱"));
        assertTrue(eggDetails.contains("공격 범위: 0블록 / 4블록"));
        assertTrue(eggDetails.contains("추가 체력: 4.0"));
        assertTrue(eggDetails.contains("재생: 0 / 10/초"));
        assertTrue(eggDetails.contains("생명력 흡수: 0% / 10%"));
        assertTrue(eggDetails.contains("피해 감소: 0% / 20%"));
        assertFalse(eggDetails.contains("최종 피해"));
        assertFalse(eggDetails.contains("저항"));
        String styledEggDetails = String.join("\n", core.runtimeDetailLines());
        assertTrue(styledEggDetails.contains("<#B77DE8>엔더 드래곤</#B77DE8><white> 능력치</white>"));
        assertTrue(styledEggDetails.contains("<#D9B94F>공격 범위: 0블록 / 4블록</#D9B94F>"));
        assertTrue(styledEggDetails.contains("<#E66F6F>추가 체력: 4.0</#E66F6F>"));
        assertTrue(styledEggDetails.contains("<#72A9E6>피해 감소: 0% / 20%</#72A9E6>"));
        assertTrue(styledEggDetails.contains("<#79C97B>재생: 0 / 10/초</#79C97B>"));

        core.onWaveStarted(null, 2);
        core.tick(null);

        assertEquals(EndTowerState.PHANTOM, core.state());
        assertEquals(204.0, core.currentMaxHealth(), 0.0001);
        assertEquals(4.0, core.permanentHealthBonus(), 0.0001);
    }

    @Test
    void completedLineCountsAccumulateThirtyPercentOfEverySourceStatForTheRound() {
        applyAbsorptionDuration(1);
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        for (int index = 0; index < 20; index++) {
            lane.towers().add(tower(EndTowers.T1_ENDERMITE_TOWER, index + 1));
            lane.towers().add(tower(EndTowers.T1_SHULKER_TOWER, index + 21));
        }
        tick(dragon, lane, 1);

        assertEquals(20, dragon.absorbedEndCrystalCount());
        assertEquals(20, dragon.absorbedShulkerCount());
        assertEquals(40, dragon.roundCompletedTransferCount());
        assertEquals(41, lane.towers().size());
        assertEquals(40, lane.towers().stream().filter(tower -> tower != dragon && tower.health() <= 0.0).count());
        assertEquals(1000.0, dragon.roundHealthBonus(), 0.0001);
        assertEquals(120.0, dragon.roundDamageBonus(), 0.0001);
        assertEquals(80.0, dragon.permanentHealthBonus(), 0.0001);
        assertEquals(12.0, dragon.permanentDamageBonus(), 0.0001);
        assertEquals(1080.0, dragon.absorbedHealthBonus(), 0.0001);
        assertEquals(132.0, dragon.absorbedDamageBonus(), 0.0001);
        assertEquals(1280.0, dragon.effectBaseMaxHealth(), 0.0001);
        assertEquals(71.0, dragon.modifyAttackDamage(null, null, 5.0), 0.0001);
        assertEquals(5.0, dragon.adjustAttackRange(5.0), 0.0001);
        assertEquals(1.0, dragon.splashRadius(), 0.0001);
        assertEquals(5, dragon.adjustAttackInterval(20));
        assertEquals(99.0, dragon.modifyIncomingDamage(null, null, 100.0), 0.0001);
        assertTrue(plainRuntimeDetails(dragon).contains("엔드 수정, 셜커 스택: 20 / 20"));
        assertTrue(plainRuntimeDetails(dragon).contains("생명력 흡수: 0%"));

        dragon.resetRoundTransferBonuses(null);

        assertEquals(0.0, dragon.roundHealthBonus(), 0.0001);
        assertEquals(0.0, dragon.roundDamageBonus(), 0.0001);
        assertEquals(80.0, dragon.permanentHealthBonus(), 0.0001);
        assertEquals(12.0, dragon.permanentDamageBonus(), 0.0001);
        assertEquals(280.0, dragon.effectBaseMaxHealth(), 0.0001);
        assertEquals(11.0, dragon.modifyAttackDamage(null, null, 5.0), 0.0001);
        assertEquals(280.0, dragon.previewHatchedMaxHealth(), 0.0001);
        assertEquals(22.0, dragon.previewHatchedAttackDamage(), 0.0001);
        assertEquals(15, dragon.previewHatchedAttackIntervalTicks());
        assertEquals(0, dragon.roundCompletedTransferCount());
        assertEquals(15, dragon.adjustAttackInterval(15));
    }

    @Test
    void cumulativeLineBonusesUseTheirRequestedFamiliesAndRespectEveryCap() {
        applyEndAbilities(Map.ofEntries(
                Map.entry("absorptionDurationTicks", 1.0),
                Map.entry("endCrystalSplashThreshold1", 1.0),
                Map.entry("endCrystalSplashThreshold2", 2.0),
                Map.entry("splashRadiusPerThreshold", 0.25),
                Map.entry("splashRadiusCap", 0.5),
                Map.entry("endCrystalAttackIntervalEvery", 1.0),
                Map.entry("maxAttackIntervalReductionTicks", 2.0),
                Map.entry("endCrystalAttackRangeEvery", 1.0),
                Map.entry("attackRangePerStep", 2.0),
                Map.entry("attackRangeCap", 5.0),
                Map.entry("shulkerLifeStealEvery", 1.0),
                Map.entry("lifeStealCap", 0.02),
                Map.entry("shulkerReductionEvery", 1.0),
                Map.entry("damageReductionPerStep", 0.02),
                Map.entry("damageReductionCap", 0.05)
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        for (int index = 0; index < 3; index++) {
            lane.towers().add(tower(EndTowers.T1_ENDERMITE_TOWER, index + 1));
            lane.towers().add(tower(EndTowers.T1_SHULKER_TOWER, index + 4));
        }

        dragon.tick(lane);

        assertEquals(3, dragon.absorbedEndCrystalCount());
        assertEquals(3, dragon.absorbedShulkerCount());
        assertEquals(0.5, dragon.splashRadius(), 0.0001);
        assertEquals(5.0, dragon.attackRangeBonus(), 0.0001);
        assertEquals(10.0, dragon.adjustAttackRange(5.0), 0.0001);
        assertEquals(12, dragon.adjustAttackInterval(20));
        assertEquals(95.0, dragon.modifyIncomingDamage(null, null, 100.0), 0.0001);
        assertTrue(plainRuntimeDetails(dragon).contains("생명력 흡수: 2%"));
    }

    @Test
    void attackDamageCapComesFromEndGlobalConfig() {
        applyEndAbilities(Map.ofEntries(
                Map.entry("absorptionDurationTicks", 1.0),
                Map.entry("roundDamageRatio", 1.0),
                Map.entry("permanentDamageRatio", 0.0),
                Map.entry("attackDamageCap", 25.0),
                Map.entry("dragonDamageBonus", 0.0)
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.towers().add(tower(EndTowers.T3_END_CRYSTAL_TOWER, 1));
        dragon.tick(lane);

        assertEquals(20.0, dragon.roundDamageBonus(), 0.0001);
        assertEquals(30.0, dragon.previewHatchedAttackDamage(), 0.0001);
        assertEquals(30.0, dragon.modifyAttackDamage(null, null, 10.0), 0.0001);
        assertEquals(25.0, dragon.modifyOutgoingDamage(null, null, 30.0), 0.0001);
        assertEquals(20.0, dragon.modifyOutgoingDamage(null, null, 20.0), 0.0001);
        assertEquals(0.0, dragon.modifyOutgoingDamage(null, null, -10.0), 0.0001);
        assertTrue(plainRuntimeDetails(dragon).contains("피해량 상한: 25.0"));
    }

    @Test
    void splashRatioUsesDamageAfterThePrimaryAttackCap() {
        applyEndAbilities(Map.of(
                "attackDamageCap", 25.0,
                "splashDamageRatio", 0.60
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);

        double resolvedPrimaryDamage = dragon.modifyOutgoingDamage(null, null, 1_000.0);

        assertEquals(25.0, resolvedPrimaryDamage, 0.0001);
        assertEquals(15.0, dragon.resolvedSplashDamage(resolvedPrimaryDamage), 0.0001);
        assertEquals(0.0, dragon.resolvedSplashDamage(Double.NaN), 0.0001);
    }

    @Test
    void extremeAttackIntervalConfigurationCannotOverflow() {
        applyEndAbilities(Map.ofEntries(
                Map.entry("absorptionDurationTicks", 1.0),
                Map.entry("endCrystalAttackIntervalEvery", 1.0),
                Map.entry("attackIntervalReductionPerStep", (double) Integer.MAX_VALUE),
                Map.entry("maxAttackIntervalReductionTicks", (double) Integer.MAX_VALUE),
                Map.entry("roundAbsorptionAttackIntervalEvery", 1.0),
                Map.entry("roundAbsorptionAttackIntervalReductionTicks", (double) Integer.MAX_VALUE),
                Map.entry("minimumAttackIntervalTicks", 5.0)
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        lane.towers().add(tower(EndTowers.T3_END_CRYSTAL_TOWER, 1));

        dragon.tick(lane);

        assertEquals(3, dragon.absorbedEndCrystalCount());
        assertEquals(1, dragon.roundCompletedTransferCount());
        assertEquals(5, dragon.adjustAttackInterval(20));
        assertEquals(5, dragon.previewHatchedAttackIntervalTicks());
    }

    @Test
    void everyStackBasedStatReachesItsCapAtThreeHundredStacks() {
        applyEndAbilities(Map.ofEntries(
                Map.entry("absorptionDurationTicks", 1.0),
                Map.entry("absorptionHealAmount", 0.0),
                Map.entry("roundHealthRatio", 0.0),
                Map.entry("roundDamageRatio", 0.0),
                Map.entry("permanentHealthRatio", 0.0),
                Map.entry("permanentDamageRatio", 0.0)
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);

        for (int index = 0; index < 99; index++) {
            lane.towers().add(tower(EndTowers.T3_END_CRYSTAL_TOWER, index + 1));
            lane.towers().add(tower(EndTowers.T3_SHULKER_TOWER, index + 101));
        }
        lane.towers().add(tower(EndTowers.T2_ENDERMAN_TOWER, 201));
        lane.towers().add(tower(EndTowers.T2_SHULKER_TOWER, 202));
        dragon.tick(lane);
        dragon.resetRoundTransferBonuses(null);

        assertEquals(299, dragon.absorbedEndCrystalCount());
        assertEquals(299, dragon.absorbedShulkerCount());
        assertEquals(6, dragon.adjustAttackInterval(15));
        assertEquals(3.0, dragon.splashRadius(), 0.0001);
        assertEquals(7.0, dragon.adjustAttackRange(5.0), 0.0001);
        assertEquals(81.0, dragon.modifyIncomingDamage(null, null, 100.0), 0.0001);
        assertEquals(9.0, dragon.regenerationPerSecond(), 0.0001);
        assertTrue(plainRuntimeDetails(dragon).contains("생명력 흡수: 9%"));

        lane.towers().add(tower(EndTowers.T1_ENDERMITE_TOWER, 203));
        lane.towers().add(tower(EndTowers.T1_SHULKER_TOWER, 204));
        dragon.tick(lane);
        dragon.resetRoundTransferBonuses(null);

        assertEquals(300, dragon.absorbedEndCrystalCount());
        assertEquals(300, dragon.absorbedShulkerCount());
        assertEquals(5, dragon.adjustAttackInterval(15));
        assertEquals(4.0, dragon.splashRadius(), 0.0001);
        assertEquals(7.5, dragon.adjustAttackRange(5.0), 0.0001);
        assertEquals(80.0, dragon.modifyIncomingDamage(null, null, 100.0), 0.0001);
        assertEquals(10.0, dragon.regenerationPerSecond(), 0.0001);
        assertTrue(plainRuntimeDetails(dragon).contains("생명력 흡수: 10%"));
    }

    @Test
    void splashRadiusUnlocksItsFirstBlockAtFifteenStacks() {
        applyEndAbilities(Map.of(
                "absorptionDurationTicks", 1.0,
                "absorptionHealAmount", 0.0,
                "roundDamageRatio", 0.0,
                "permanentDamageRatio", 0.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        for (int index = 0; index < 14; index++) {
            lane.towers().add(tower(EndTowers.T1_ENDERMITE_TOWER, index + 1));
        }

        dragon.tick(lane);

        assertEquals(14, dragon.absorbedEndCrystalCount());
        assertEquals(0.0, dragon.splashRadius(), 0.0001);

        lane.towers().add(tower(EndTowers.T1_ENDERMITE_TOWER, 15));
        dragon.tick(lane);

        assertEquals(15, dragon.absorbedEndCrystalCount());
        assertEquals(1.0, dragon.splashRadius(), 0.0001);
    }

    @Test
    void completedTransfersUseRegisteredTowerTiersAsStackWeight() {
        applyAbsorptionDuration(1);
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.towers().add(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.towers().add(tower(EndTowers.T2_ENDERMAN_TOWER, 1));
        lane.towers().add(tower(EndTowers.T3_END_CRYSTAL_TOWER, 2));
        lane.towers().add(tower(EndTowers.T2_SHULKER_TOWER, 3));
        lane.towers().add(tower(EndTowers.T3_SHULKER_TOWER, 4));

        dragon.tick(lane);

        assertEquals(5, dragon.absorbedEndCrystalCount());
        assertEquals(5, dragon.absorbedShulkerCount());
        assertEquals(4, dragon.roundCompletedTransferCount());
    }

    @Test
    void dragonEggAndHatchedPhantomAreStatesOfOneTowerType() {
        applyAbsorptionDuration(1);
        EndTower tower = tower(EndTowers.BASE_END_TOWER, 0);

        assertEquals(EndTowerState.EGG, tower.state());
        assertEquals(1.0, tower.entityAnchorYOffset(), 0.0001);
        assertTrue(BlockDisplayVisual.matches(tower.visual()));
        assertEquals(
                Blocks.DRAGON_EGG.defaultBlockState(),
                BlockDisplayVisual.blockState(tower.visual())
        );

        tower.onWaveStarted(null, 1);
        tower.tick(null);

        assertEquals(EndTowerState.PHANTOM, tower.state());
        assertEquals(2.0, tower.entityAnchorYOffset(), 0.0001);
        assertEquals(EndTowers.BASE_END_TOWER, tower.type());
        assertEquals("minecraft:phantom", tower.visual().entityTypeId());
        assertTrue(tower.visual().blockbenchModel().isEmpty());
        assertEquals(0.0, tower.finalDamageBonus(), 0.0001);
        assertEquals(0.0, tower.incomeDebuffResistance(), 0.0001);

        tower.syncMaxHealth(2000.0, true);
        tower.tick(null);

        assertEquals(EndTowerState.DRAGON, tower.state());
        assertEquals(2.0, tower.entityAnchorYOffset(), 0.0001);
        assertEquals(0.10, tower.finalDamageBonus(), 0.0001);
        assertEquals(0.10, tower.incomeDebuffResistance(), 0.0001);
        assertTrue(plainRuntimeDetails(tower).contains("최종 피해: +10%"));
        assertTrue(plainRuntimeDetails(tower).contains("저항: +10%"));

        tower.resetForRound(null);

        assertEquals(EndTowerState.EGG, tower.state());
        assertEquals(1.0, tower.entityAnchorYOffset(), 0.0001);
        assertTrue(BlockDisplayVisual.matches(tower.visual()));
        assertEquals(200.0, tower.currentMaxHealth(), 0.0001);
    }

    private static String plainRuntimeDetails(EndTower tower) {
        return String.join("\n", tower.runtimeDetailLines()).replaceAll("<[^>]+>", "");
    }

    @Test
    void shulkerTiersReduceIncomingDamageByConfiguredAmount() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());

        assertEquals(90.0, tower(EndTowers.T1_SHULKER_TOWER, 0)
                .modifyIncomingDamage(null, null, 100.0), 0.0001);
        assertEquals(70.0, tower(EndTowers.T2_SHULKER_TOWER, 0)
                .modifyIncomingDamage(null, null, 100.0), 0.0001);
        assertEquals(50.0, tower(EndTowers.T3_SHULKER_TOWER, 0)
                .modifyIncomingDamage(null, null, 100.0), 0.0001);
    }

    private static void applyAbsorptionDuration(int durationTicks) {
        applyEndAbilities(Map.of(
                "absorptionDurationTicks", (double) durationTicks
        ));
    }

    private static void applyEndAbilities(Map<String, Double> overrides) {
        TowerBalanceRuntime.apply(endConfig(overrides));
    }

    private static TowerBalanceConfig endConfig(Map<String, Double> overrides) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        Map<String, Double> end = new LinkedHashMap<>(abilities.get(EndTower.CONFIG_ID));
        end.putAll(overrides);
        abilities.put(EndTower.CONFIG_ID, end);
        return new TowerBalanceConfig(defaults.towers(), defaults.upgradeCosts(), abilities);
    }

    private static EndTower tower(kim.biryeong.semiontd.tower.TowerType type, int x) {
        return new EndTower(type, OWNER, TeamId.BLUE, 1, new GridPosition(x, 64, 0));
    }

    private static void tick(EndTower dragon, PlayerLane lane, int ticks) {
        for (int index = 0; index < ticks; index++) {
            dragon.tick(lane);
        }
    }

    private static PlayerLane lane() {
        LaneRegionLayout layout = new LaneRegionLayout(
                1,
                new Vec3(0.5, 64.0, 0.5),
                List.of(new Vec3(0.5, 64.0, 2.5)),
                new Vec3(0.5, 64.0, 10.5),
                BlockBounds.of(new BlockPos(0, 63, 0), new BlockPos(64, 66, 10)),
                List.of(new GridPosition(0, 63, 10))
        );
        return new PlayerLane(TeamId.BLUE, 1, OWNER, null, layout);
    }
}
