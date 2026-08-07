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
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
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

class EndTowerTransferTest {
    private static final UUID OWNER = UUID.nameUUIDFromBytes("end-transfer-owner".getBytes());

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
    void exposedTowerListCannotBypassLaneMembership() {
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);

        assertThrows(UnsupportedOperationException.class, () -> lane.towers().add(tower(EndTowers.T1_ENDERMITE_TOWER, 1)));
        lane.addTower(dragon);
        assertEquals(List.of(dragon), lane.towers());
    }

    @Test
    void onlyFullyTransferredTowerIsCountedWhileStatsTransferGradually() {
        applyTransferDuration(4);
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower enderman = tower(EndTowers.T1_ENDERMITE_TOWER, 1);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.addTower(enderman);
        enderman.onWaveStarted(lane, 1);
        tick(dragon, lane, 3);
        assertEquals(0, dragon.endCrystalCount());
        assertTrue(lane.towers().contains(enderman));
        assertEquals(5.625, dragon.roundDamageBonus(), 0.0001);
        assertEquals(0.45, dragon.permanentDamageBonus(), 0.0001);
        assertEquals(6.075, dragon.damageBonus(), 0.0001);
        assertEquals(0.0, dragon.healthBonus(), 0.0001);
        assertEquals(0.75, enderman.transferProgress(), 0.0001);
        tick(dragon, lane, 1);
        assertEquals(1, dragon.endCrystalCount());
        assertTrue(lane.towers().contains(enderman));
        assertEquals(0.0, enderman.health(), 0.0001);
        assertEquals(0.0, enderman.transferProgress(), 0.0001);
        assertEquals(7.5, dragon.roundDamageBonus(), 0.0001);
        assertEquals(0.6, dragon.permanentDamageBonus(), 0.0001);
        assertEquals(8.1, dragon.damageBonus(), 0.0001);
        assertEquals(0.0, dragon.healthBonus(), 0.0001);
        assertEquals(1, dragon.roundCompletedTransferCount());
        assertEquals(19, dragon.adjustAttackInterval(20));
        String crystalHeavyDetails = plainRuntimeDetails(dragon);
        assertTrue(crystalHeavyDetails.contains("셜커 계열, 엔드 수정 계열 누적 수: 0 | 1"));
        assertTrue(crystalHeavyDetails.contains("공격 속도: -1틱 (30)"));
        assertTrue(crystalHeavyDetails.contains("영구 피해: +0.6"));
        tick(dragon, lane, 4);
        assertEquals(1, dragon.endCrystalCount());
        assertTrue(lane.towers().contains(enderman));
        assertEquals(0.0, enderman.health(), 0.0001);
        assertEquals(8.1, dragon.damageBonus(), 0.0001);
    }

    @Test
    void interruptedTransferRollsBackStatsAndDoesNotCountTower() {
        applyTransferDuration(4);
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower endCrystalLine = tower(EndTowers.T1_ENDERMITE_TOWER, 1);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.addTower(endCrystalLine);

        tick(dragon, lane, 2);
        lane.removeTower(endCrystalLine);
        tick(dragon, lane, 1);

        assertEquals(0, dragon.endCrystalCount());
        assertEquals(0.0, dragon.roundDamageBonus(), 0.0001);
        assertEquals(0.0, dragon.permanentDamageBonus(), 0.0001);
        assertEquals(0.0, endCrystalLine.transferProgress(), 0.0001);
    }

    @Test
    void interruptedTransferAlsoRollsBackDragonEvolutionState() {
        applyEndAbilities(Map.of(
                "transferTicks", 4.0,
                "dragonEvolution", 220.0,
                "roundHealthRatio", 0.50,
                "permanentHealthRatio", 0.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower shulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.addTower(shulker);

        tick(dragon, lane, 2);

        assertEquals(EndTowerState.DRAGON, dragon.state());
        assertEquals(225.0, dragon.currentMaxHealth(), 0.0001);

        lane.removeTower(shulker);
        dragon.tick(lane);

        assertEquals(EndTowerState.PHANTOM, dragon.state());
        assertEquals(200.0, dragon.currentMaxHealth(), 0.0001);
        assertEquals(0.0, dragon.finalDamageBonus(), 0.0001);
    }

    @Test
    void interruptedHealthTransferNeverHealsTheCoreForFree() {
        applyEndAbilities(Map.of(
                "transferTicks", 4.0,
                "roundHealthRatio", 0.50,
                "permanentHealthRatio", 0.0,
                "transferHealRatio", 0.0,
                "transferHeal", 0.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower firstShulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.syncHealth(100.0);
        lane.addTower(firstShulker);

        tick(dragon, lane, 2);

        assertEquals(225.0, dragon.currentMaxHealth(), 0.0001);
        assertEquals(100.0, dragon.health(), 0.0001);

        lane.removeTower(firstShulker);
        dragon.tick(lane);

        assertEquals(200.0, dragon.currentMaxHealth(), 0.0001);
        assertEquals(100.0, dragon.health(), 0.0001);

        EndTower secondShulker = tower(EndTowers.T1_SHULKER_TOWER, 2);
        lane.addTower(secondShulker);
        tick(dragon, lane, 2);
        lane.removeTower(secondShulker);
        dragon.tick(lane);

        assertEquals(200.0, dragon.currentMaxHealth(), 0.0001);
        assertEquals(100.0, dragon.health(), 0.0001);
    }

    @Test
    void typeRefreshPreservesHealthAfterTransferredMaxHealthIsRecalculated() {
        applyTransferDuration(1);
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower shulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.addTower(shulker);
        dragon.tick(lane);
        dragon.syncHealth(250.0);

        dragon.refreshType(dragon.type(), lane);

        assertEquals(254.0, dragon.currentMaxHealth(), 0.0001);
        assertEquals(250.0, dragon.health(), 0.0001);
    }

    @Test
    void typeRefreshRestartsAnActiveTransferWithTheNewBalanceSnapshot() {
        applyEndAbilities(Map.of(
                "transferTicks", 4.0,
                "roundHealthRatio", 0.50,
                "permanentHealthRatio", 0.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower shulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.addTower(dragon);
        lane.addTower(shulker);
        dragon.onWaveStarted(lane, 1);
        tick(dragon, lane, 2);

        assertEquals(0.50, shulker.transferProgress(), 0.0001);
        assertEquals(25.0, dragon.roundHealthBonus(), 0.0001);

        applyEndAbilities(Map.of(
                "transferTicks", 1.0,
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
        end.put("transferTicks", 0.0);
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
                () -> TowerBalanceRuntime.apply(endConfig(Map.of("transferTicks", 1.5)))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> TowerBalanceRuntime.apply(endConfig(Map.of(
                        "phantomScaleBase", 2.0,
                        "phantomScaleCap", 1.0
                )))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> TowerBalanceRuntime.apply(endConfig(Map.of("attackSpeedMinimumTicks", 16.0)))
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
        end.put("transferTicks", (double) Integer.MAX_VALUE + 1.0);
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
    void everyShulkerOrEndCrystalTransferReducesAttackIntervalForTheCurrentRoundOnly() {
        applyEndAbilities(Map.of(
                "transferTicks", 1.0,
                "roundHealthRatio", 0.0,
                "permanentHealthRatio", 0.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        dragon.syncHealth(10.0);
        lane.addTower(tower(EndTowers.T1_SHULKER_TOWER, 1));

        dragon.tick(lane);

        assertEquals(1, dragon.roundCompletedTransferCount());
        assertEquals(40.0, dragon.health(), 0.0001);
        assertEquals(19, dragon.adjustAttackInterval(20));

        lane.addTower(tower(EndTowers.T1_SHULKER_TOWER, 2));
        dragon.tick(lane);

        assertEquals(2, dragon.roundCompletedTransferCount());
        assertEquals(70.0, dragon.health(), 0.0001);
        assertEquals(18, dragon.adjustAttackInterval(20));

        lane.addTower(tower(EndTowers.T1_ENDERMITE_TOWER, 3));
        dragon.tick(lane);

        assertEquals(3, dragon.roundCompletedTransferCount());
        assertEquals(100.0, dragon.health(), 0.0001);
        assertEquals(17, dragon.adjustAttackInterval(20));

        dragon.resetRoundTransferBonuses(null);

        assertEquals(0, dragon.roundCompletedTransferCount());
        assertEquals(20, dragon.adjustAttackInterval(20));
    }

    @Test
    void shulkerTransfersFiftyPercentOfItsHealthForTheCurrentRound() {
        applyEndAbilities(Map.of(
                "transferTicks", 1.0,
                "attackSpeedStacks", 1.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower shulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        dragon.syncHealth(10.0);
        lane.addTower(shulker);

        dragon.tick(lane);

        assertEquals(50.0, dragon.roundHealthBonus(), 0.0001);
        assertEquals(4.0, dragon.permanentHealthBonus(), 0.0001);
        assertEquals(254.0, dragon.currentMaxHealth(), 0.0001);
        assertEquals(40.0, dragon.health(), 0.0001);
        assertEquals(0.0, shulker.health(), 0.0001);
        assertEquals(1, dragon.roundCompletedTransferCount());
        assertEquals(19, dragon.adjustAttackInterval(20));
    }

    @Test
    void shulkerStacksGrantCappedRegenerationThatHealsOncePerSecond() {
        applyEndAbilities(Map.of(
                "transferTicks", 1.0,
                "transferHeal", 0.0,
                "roundHealthRatio", 0.0,
                "permanentHealthRatio", 0.0,
                "regenerationStacks", 1.0,
                "regenerationStep", 2.0,
                "regenerationCap", 3.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.addTower(tower(EndTowers.T1_SHULKER_TOWER, 1));
        lane.addTower(tower(EndTowers.T1_SHULKER_TOWER, 2));

        dragon.tick(lane);

        assertEquals(3.0, dragon.regenerationPerSecond(), 0.0001);
        dragon.syncHealth(10.0);
        tick(dragon, lane, 18);
        assertEquals(10.0, dragon.health(), 0.0001);

        dragon.tick(lane);

        assertEquals(13.0, dragon.health(), 0.0001);
        assertTrue(plainRuntimeDetails(dragon).contains("재생: +3 HP/s (MAX)"));
    }

    @Test
    void activeEndCrystalLineTransfersDoNotGrantPeriodicHealing() {
        applyEndAbilities(Map.of(
                "transferTicks", 40.0,
                "transferHeal", 0.0,
                "regenerationTicks", 20.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        dragon.syncHealth(10.0);
        lane.addTower(tower(EndTowers.T1_ENDERMITE_TOWER, 1));
        lane.addTower(tower(EndTowers.T1_ENDERMITE_TOWER, 2));

        tick(dragon, lane, 19);
        assertEquals(10.0, dragon.health(), 0.0001);

        dragon.tick(lane);
        assertEquals(10.0, dragon.health(), 0.0001);

        tick(dragon, lane, 20);
        assertEquals(10.0, dragon.health(), 0.0001);
        assertEquals(2, dragon.roundCompletedTransferCount());
    }

    @Test
    void activeShulkerTransfersHealFivePercentOfTraitFreeBaseMaxHealthPerSecond() {
        applyEndAbilities(Map.ofEntries(
                Map.entry("transferTicks", 40.0),
                Map.entry("transferHeal", 0.0),
                Map.entry("roundHealthRatio", 0.0),
                Map.entry("permanentHealthRatio", 0.0),
                Map.entry("transferHealRatio", 0.05),
                Map.entry("regenerationTicks", 20.0)
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        dragon.syncHealth(10.0);
        lane.addTower(tower(EndTowers.T1_SHULKER_TOWER, 1));
        lane.addTower(tower(EndTowers.T2_SHULKER_TOWER, 2));
        lane.addTower(tower(EndTowers.T3_SHULKER_TOWER, 3));

        tick(dragon, lane, 19);
        assertEquals(10.0, dragon.health(), 0.0001);

        dragon.tick(lane);

        assertEquals(32.5, dragon.health(), 0.0001);
    }

    @Test
    void completedTransferDoesNotReceiveAnExtraPeriodicTransferHeal() {
        applyEndAbilities(Map.of(
                "transferTicks", 1.0,
                "transferHeal", 0.0,
                "regenerationTicks", 1.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.syncHealth(10.0);
        lane.addTower(tower(EndTowers.T1_ENDERMITE_TOWER, 1));

        dragon.tick(lane);

        assertEquals(10.0, dragon.health(), 0.0001);
        assertEquals(1, dragon.roundCompletedTransferCount());
    }

    @Test
    void copyingTheCoreRollsBackIncompleteTransferContributions() {
        applyEndAbilities(Map.of(
                "transferTicks", 4.0,
                "roundHealthRatio", 0.50,
                "permanentHealthRatio", 0.04
        ));
        PlayerLane lane = lane();
        EndTower original = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower source = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.addTower(original);
        lane.addTower(source);
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
    void coreReturnsToEggEachRoundAndPermanentHealthReturnsAfterHatching() {
        applyTransferDuration(1);
        PlayerLane lane = lane();
        EndTower core = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower shulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.addTower(core);
        core.onWaveStarted(lane, 1);
        core.tick(lane);
        lane.addTower(shulker);
        core.tick(lane);

        assertEquals(4.0, core.permanentHealthBonus(), 0.0001);
        assertEquals(254.0, core.currentMaxHealth(), 0.0001);

        core.resetForRound(null);

        assertEquals(EndTowerState.EGG, core.state());
        assertEquals(200.0, core.currentMaxHealth(), 0.0001);
        assertEquals(4.0, core.permanentHealthBonus(), 0.0001);
        assertEquals(0.0, core.splashRadius(), 0.0001);
        String eggDetails = plainRuntimeDetails(core);
        assertTrue(eggDetails.contains("셜커 계열, 엔드 수정 계열 누적 수: 1 | 0"));
        assertTrue(eggDetails.contains("피해량 상한: 300"));
        assertTrue(eggDetails.contains("영구 피해: +0.0"));
        assertTrue(eggDetails.contains("사거리: +5.0 블록 (50)"));
        assertTrue(eggDetails.contains("공격 속도: -0틱 (30)"));
        assertTrue(eggDetails.contains("공격 범위: +1 블록 (15)"));
        assertTrue(eggDetails.contains("영구 체력: +4.0"));
        assertTrue(eggDetails.contains("재생: +0 HP/s (10)"));
        assertTrue(eggDetails.contains("생명력 흡수: +0% (30)"));
        assertTrue(eggDetails.contains("피해 감소: +0% (15)"));
        assertFalse(eggDetails.contains("최종 피해: +"));
        String styledEggDetails = String.join("\n", core.runtimeDetailLines());
        assertTrue(styledEggDetails.contains("<#fc5454>\u2764 영구 체력</#fc5454>"));
        assertTrue(styledEggDetails.contains("<#20985d>➕ 재생</#20985d>"));
        assertTrue(styledEggDetails.contains("<#e32042>🩸 생명력 흡수</#e32042>"));
        assertTrue(styledEggDetails.contains("<#f3ba59>🛡 피해 감소</#f3ba59>"));

        core.onWaveStarted(null, 2);
        core.tick(null);

        assertEquals(EndTowerState.PHANTOM, core.state());
        assertEquals(204.0, core.currentMaxHealth(), 0.0001);
        assertEquals(4.0, core.permanentHealthBonus(), 0.0001);
    }

    @Test
    void completedLineCountsAccumulateThirtyPercentOfEverySourceStatForTheRound() {
        applyTransferDuration(1);
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        for (int index = 0; index < 20; index++) {
            lane.addTower(tower(EndTowers.T1_ENDERMITE_TOWER, index + 1));
            lane.addTower(tower(EndTowers.T1_SHULKER_TOWER, index + 21));
        }
        tick(dragon, lane, 1);

        assertEquals(20, dragon.endCrystalCount());
        assertEquals(20, dragon.shulkerCount());
        assertEquals(40, dragon.roundCompletedTransferCount());
        assertEquals(41, lane.towers().size());
        assertEquals(40, lane.towers().stream().filter(tower -> tower != dragon && tower.health() <= 0.0).count());
        assertEquals(1000.0, dragon.roundHealthBonus(), 0.0001);
        assertEquals(150.0, dragon.roundDamageBonus(), 0.0001);
        assertEquals(80.0, dragon.permanentHealthBonus(), 0.0001);
        assertEquals(12.0, dragon.permanentDamageBonus(), 0.0001);
        assertEquals(1080.0, dragon.healthBonus(), 0.0001);
        assertEquals(162.0, dragon.damageBonus(), 0.0001);
        assertEquals(1280.0, dragon.effectBaseMaxHealth(), 0.0001);
        assertEquals(86.0, dragon.modifyAttackDamage(null, null, 5.0), 0.0001);
        assertEquals(5.0, dragon.adjustAttackRange(5.0), 0.0001);
        assertEquals(2.0, dragon.splashRadius(), 0.0001);
        assertEquals(5, dragon.adjustAttackInterval(20));
        assertEquals(99.0, dragon.modifyIncomingDamage(null, null, 100.0), 0.0001);
        assertTrue(plainRuntimeDetails(dragon).contains("셜커 계열, 엔드 수정 계열 누적 수: 20 | 20"));
        assertTrue(plainRuntimeDetails(dragon).contains("생명력 흡수: +0% (30)"));

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
                Map.entry("transferTicks", 1.0),
                Map.entry("splash1", 1.0),
                Map.entry("splash2", 2.0),
                Map.entry("splashStep", 0.25),
                Map.entry("splashCap", 0.5),
                Map.entry("attackSpeedStacks", 1.0),
                Map.entry("attackSpeedCap", 2.0),
                Map.entry("attackRangeStacks", 1.0),
                Map.entry("attackRangeStep", 2.0),
                Map.entry("attackRangeCap", 5.0),
                Map.entry("lifeStealStacks", 1.0),
                Map.entry("lifeStealCap", 0.02),
                Map.entry("damageReductionStacks", 1.0),
                Map.entry("damageReductionStep", 0.02),
                Map.entry("damageReductionCap", 0.05)
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        for (int index = 0; index < 3; index++) {
            lane.addTower(tower(EndTowers.T1_ENDERMITE_TOWER, index + 1));
            lane.addTower(tower(EndTowers.T1_SHULKER_TOWER, index + 4));
        }

        dragon.tick(lane);

        assertEquals(3, dragon.endCrystalCount());
        assertEquals(3, dragon.shulkerCount());
        assertEquals(0.5, dragon.splashRadius(), 0.0001);
        assertEquals(5.0, dragon.attackRangeBonus(), 0.0001);
        assertEquals(10.0, dragon.adjustAttackRange(5.0), 0.0001);
        assertEquals(12, dragon.adjustAttackInterval(20));
        assertEquals(95.0, dragon.modifyIncomingDamage(null, null, 100.0), 0.0001);
        assertTrue(plainRuntimeDetails(dragon).contains("생명력 흡수: +2% (MAX)"));
    }

    @Test
    void damageCapRemainsConfiguredWithoutCappingDamage() {
        applyEndAbilities(Map.ofEntries(
                Map.entry("transferTicks", 1.0),
                Map.entry("roundDamageRatio", 1.0),
                Map.entry("permanentDamageRatio", 0.0),
                Map.entry("damageCap", 25.0)
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.addTower(tower(EndTowers.T3_END_CRYSTAL_TOWER, 1));
        dragon.tick(lane);
        assertEquals(20.0, dragon.roundDamageBonus(), 0.0001);
        assertEquals(30.0, dragon.previewHatchedAttackDamage(), 0.0001);
        assertEquals(30.0, dragon.modifyAttackDamage(null, null, 10.0), 0.0001);
        assertEquals(30.0, dragon.modifyResolvedAttackDamage(null, null, 30.0), 0.0001);
        assertEquals(20.0, dragon.modifyResolvedAttackDamage(null, null, 20.0), 0.0001);
        assertEquals(-10.0, dragon.modifyResolvedAttackDamage(null, null, -10.0), 0.0001);
        assertEquals(30.0, dragon.modifyResolvedOutgoingDamage(null, null, 30.0), 0.0001);
        assertEquals(20.0, dragon.modifyResolvedOutgoingDamage(null, null, 20.0), 0.0001);
        assertEquals(-10.0, dragon.modifyResolvedOutgoingDamage(null, null, -10.0), 0.0001);
        assertTrue(plainRuntimeDetails(dragon).contains("피해량 상한: 25"));
    }

    @Test
    void splashRatioUsesUncappedPrimaryDamage() {
        applyEndAbilities(Map.of(
                "damageCap", 25.0,
                "splashDamageRatio", 0.66
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        double resolvedPrimaryDamage = dragon.modifyResolvedOutgoingDamage(null, null, 1_000.0);
        assertEquals(1_000.0, resolvedPrimaryDamage, 0.0001);
        assertEquals(660.0, dragon.resolvedSplashDamage(resolvedPrimaryDamage), 0.0001);
        assertEquals(0.0, dragon.resolvedSplashDamage(Double.NaN), 0.0001);
    }

    @Test
    void extremeAttackIntervalConfigurationCannotOverflow() {
        applyEndAbilities(Map.ofEntries(
                Map.entry("transferTicks", 1.0),
                Map.entry("attackSpeedStacks", 1.0),
                Map.entry("attackSpeedStep", (double) Integer.MAX_VALUE),
                Map.entry("attackSpeedCap", (double) Integer.MAX_VALUE),
                Map.entry("transferAttackSpeedStacks", 1.0),
                Map.entry("transferAttackSpeedStep", (double) Integer.MAX_VALUE),
                Map.entry("attackSpeedMinimumTicks", 5.0)
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        lane.addTower(tower(EndTowers.T3_END_CRYSTAL_TOWER, 1));

        dragon.tick(lane);

        assertEquals(3, dragon.endCrystalCount());
        assertEquals(1, dragon.roundCompletedTransferCount());
        assertEquals(5, dragon.adjustAttackInterval(20));
        assertEquals(5, dragon.previewHatchedAttackIntervalTicks());
    }

    @Test
    void everyStackBasedStatReachesItsCapAtThreeHundredStacks() {
        applyEndAbilities(Map.ofEntries(
                Map.entry("transferTicks", 1.0),
                Map.entry("transferHeal", 0.0),
                Map.entry("roundHealthRatio", 0.0),
                Map.entry("roundDamageRatio", 0.0),
                Map.entry("permanentHealthRatio", 0.0),
                Map.entry("permanentDamageRatio", 0.0)
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);

        for (int index = 0; index < 99; index++) {
            lane.addTower(tower(EndTowers.T3_END_CRYSTAL_TOWER, index + 1));
            lane.addTower(tower(EndTowers.T3_SHULKER_TOWER, index + 101));
        }
        lane.addTower(tower(EndTowers.T2_ENDERMAN_TOWER, 201));
        lane.addTower(tower(EndTowers.T2_SHULKER_TOWER, 202));
        dragon.tick(lane);
        dragon.resetRoundTransferBonuses(null);

        assertEquals(299, dragon.endCrystalCount());
        assertEquals(299, dragon.shulkerCount());
        assertEquals(6, dragon.adjustAttackInterval(15));
        assertEquals(4.0, dragon.splashRadius(), 0.0001);
        assertEquals(7.5, dragon.adjustAttackRange(5.0), 0.0001);
        assertEquals(81.0, dragon.modifyIncomingDamage(null, null, 100.0), 0.0001);
        assertEquals(29.0, dragon.regenerationPerSecond(), 0.0001);
        assertTrue(plainRuntimeDetails(dragon).contains("생명력 흡수: +9% (300)"));

        lane.addTower(tower(EndTowers.T1_ENDERMITE_TOWER, 203));
        lane.addTower(tower(EndTowers.T1_SHULKER_TOWER, 204));
        dragon.tick(lane);
        dragon.resetRoundTransferBonuses(null);

        assertEquals(300, dragon.endCrystalCount());
        assertEquals(300, dragon.shulkerCount());
        assertEquals(5, dragon.adjustAttackInterval(15));
        assertEquals(5.0, dragon.splashRadius(), 0.0001);
        assertEquals(8.0, dragon.adjustAttackRange(5.0), 0.0001);
        assertEquals(80.0, dragon.modifyIncomingDamage(null, null, 100.0), 0.0001);
        assertEquals(30.0, dragon.regenerationPerSecond(), 0.0001);
        assertTrue(plainRuntimeDetails(dragon).contains("생명력 흡수: +10% (MAX)"));
    }

    @Test
    void hatchedDragonStartsWithOneSplashBlockAndGainsAnotherAtFifteenStacks() {
        applyEndAbilities(Map.of(
                "transferTicks", 1.0,
                "transferHeal", 0.0,
                "roundDamageRatio", 0.0,
                "permanentDamageRatio", 0.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        for (int index = 0; index < 14; index++) {
            lane.addTower(tower(EndTowers.T1_ENDERMITE_TOWER, index + 1));
        }

        dragon.tick(lane);

        assertEquals(14, dragon.endCrystalCount());
        assertEquals(1.0, dragon.splashRadius(), 0.0001);

        lane.addTower(tower(EndTowers.T1_ENDERMITE_TOWER, 15));
        dragon.tick(lane);

        assertEquals(15, dragon.endCrystalCount());
        assertEquals(2.0, dragon.splashRadius(), 0.0001);
    }

    @Test
    void completedTransfersUseRegisteredTowerTiersAsStackWeight() {
        applyTransferDuration(1);
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.addTower(tower(EndTowers.T2_ENDERMAN_TOWER, 1));
        lane.addTower(tower(EndTowers.T3_END_CRYSTAL_TOWER, 2));
        lane.addTower(tower(EndTowers.T2_SHULKER_TOWER, 3));
        lane.addTower(tower(EndTowers.T3_SHULKER_TOWER, 4));

        dragon.tick(lane);

        assertEquals(5, dragon.endCrystalCount());
        assertEquals(5, dragon.shulkerCount());
        assertEquals(4, dragon.roundCompletedTransferCount());
    }

    @Test
    void dragonEggAndHatchedPhantomAreStatesOfOneTowerType() {
        applyTransferDuration(1);
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
        assertTrue(tower.stopsBeforeFriendlyTowers());
        assertEquals(2.0, tower.entityAnchorYOffset(), 0.0001);
        assertEquals(EndTowers.BASE_END_TOWER, tower.type());
        assertEquals("minecraft:phantom", tower.visual().entityTypeId());
        assertTrue(tower.visual().blockbenchModel().isEmpty());
        assertEquals(0.0, tower.finalDamageBonus(), 0.0001);

        tower.syncMaxHealth(2000.0, true);
        tower.tick(null);

        assertEquals(EndTowerState.DRAGON, tower.state());
        assertFalse(tower.stopsBeforeFriendlyTowers());
        assertEquals(2.0, tower.entityAnchorYOffset(), 0.0001);
        assertEquals(0.20, tower.finalDamageBonus(), 0.0001);
        String dragonDetails = plainRuntimeDetails(tower);
        assertTrue(dragonDetails.contains("최종 피해: +20%"));
        assertTrue(dragonDetails.contains("추가 사거리: +2.0 블록"));

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
    void runtimeMaximumSplashUsesOnlyReachableThresholdSteps() {
        applyEndAbilities(Map.of("splashCap", 99.0));
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        dragon.onWaveStarted(null, 1);

        assertTrue(plainRuntimeDetails(dragon).contains("공격 범위: +1 블록 (15)"));
    }

    @Test
    void runtimeMaximumAttackSpeedIncludesRoundTransferReduction() {
        applyEndAbilities(Map.of("attackSpeedCap", 2.0, "transferAttackSpeedStep", 1.0));
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        dragon.onWaveStarted(null, 1);

        assertTrue(plainRuntimeDetails(dragon).contains("공격 속도: -0틱 (30)"));
    }

    @Test
    void activeTransferKeepsItsCompletionHealWhenBalanceReloads() {
        applyEndAbilities(Map.of("transferTicks", 4.0, "transferHeal", 30.0));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower feeder = tower(EndTowers.T1_ENDERMITE_TOWER, 1);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        dragon.syncHealth(10.0);
        lane.addTower(feeder);

        tick(dragon, lane, 2);
        applyEndAbilities(Map.of("transferHeal", 999.0));
        tick(dragon, lane, 2);

        assertEquals(40.0, dragon.health(), 0.0001);
    }

    @Test
    void activeTransferKeepsItsPeriodicHealingRatioWhenBalanceReloads() {
        applyEndAbilities(Map.of("transferTicks", 4.0, "regenerationTicks", 1.0, "transferHealRatio", 0.05));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower shulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        dragon.syncHealth(10.0);
        lane.addTower(shulker);

        dragon.tick(lane);
        assertEquals(15.0, dragon.health(), 0.0001);
        applyEndAbilities(Map.of("regenerationTicks", 1.0, "transferHealRatio", 1.0));
        dragon.tick(lane);

        assertEquals(20.0, dragon.health(), 0.0001);
    }

    @Test
    void deathNotificationSkipsTowerRemovedByAnEarlierCallback() {
        PlayerLane lane = lane();
        CallbackTower remover = new CallbackTower(EndTowers.T1_ENDERMITE_TOWER, 0);
        CallbackTower removed = new CallbackTower(EndTowers.T1_ENDERMITE_TOWER, 1);
        CallbackTower destroyed = new CallbackTower(EndTowers.T1_ENDERMITE_TOWER, 2);
        remover.removeOnNotification = removed;
        lane.addTower(remover);
        lane.addTower(removed);
        lane.addTower(destroyed);

        assertTrue(lane.killTower(destroyed));

        assertEquals(1, remover.notifications);
        assertEquals(0, removed.notifications);
        assertFalse(lane.towers().contains(removed));
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

    private static void applyTransferDuration(int durationTicks) {
        applyEndAbilities(Map.of(
                "transferTicks", (double) durationTicks
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

    private static final class CallbackTower extends Tower {
        private Tower removeOnNotification;
        private int notifications;

        private CallbackTower(TowerType type, int x) {
            super(type, OWNER, TeamId.BLUE, 1, new GridPosition(x, 64, 0));
        }

        @Override
        public void onNearbyTowerDeath(PlayerLane lane, Tower destroyedTower) {
            notifications++;
            if (removeOnNotification != null) {lane.removeTower(removeOnNotification);}
        }

        @Override
        protected boolean execute(PlayerLane lane) {
            return false;
        }
    }
}
