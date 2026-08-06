package kim.biryeong.semiontd.tower.warlock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WarlockTowerBalanceTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void resetBalance() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void globalConfigDefinesRequestedCapsAndSplashGrowth() {
        TowerBalanceConfig config = TowerBalanceConfig.defaultConfig();

        TowerBalanceConfig.TowerStats baseStats = config.towers().get(WarlockTowers.BASE_WARLOCK_TOWER.id());
        assertEquals(80.0, baseStats.maxHealth(), 0.0001);
        assertEquals(4.0, baseStats.range(), 0.0001);
        assertEquals(5.0, baseStats.damage(), 0.0001);
        assertEquals(20, baseStats.attackIntervalTicks());
        assertEquals(30, baseStats.aggroPriority());
        TowerBalanceConfig.TowerStats rangedStats = config.towers().get(WarlockTowers.RANGED_WARLOCK_TOWER.id());
        assertEquals(100.0, rangedStats.maxHealth(), 0.0001);
        assertEquals(7.0, rangedStats.range(), 0.0001);
        assertEquals(8.0, rangedStats.damage(), 0.0001);
        assertEquals(20, rangedStats.attackIntervalTicks());
        assertEquals(20, rangedStats.aggroPriority());
        TowerBalanceConfig.TowerStats meleeStats = config.towers().get(WarlockTowers.MELEE_WARLOCK_TOWER.id());
        assertEquals(120.0, meleeStats.maxHealth(), 0.0001);
        assertEquals(3.0, meleeStats.range(), 0.0001);
        assertEquals(7.0, meleeStats.damage(), 0.0001);
        assertEquals(20, meleeStats.attackIntervalTicks());
        assertEquals(80, meleeStats.aggroPriority());

        assertEquals(350.0, config.ability(WarlockTower.CONFIG_ID, "damageCap", -1.0), 0.0001);
        assertEquals(0.085, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "lifeCap", -1.0), 0.0001);
        assertEquals(0.16, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "lifeCap", -1.0), 0.0001);
        assertEquals(-1.0, config.ability(WarlockTower.CONFIG_ID, "splashStep", -1.0), 0.0001);
        assertEquals(-1.0, config.ability(WarlockTower.CONFIG_ID, "splashCap", -1.0), 0.0001);
        assertEquals(0.1, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "splashStep", -1.0), 0.0001);
        assertEquals(8.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "splashCap", -1.0), 0.0001);
        assertEquals(25.0, config.ability(WarlockTower.CONFIG_ID, "sacrificeRadius", -1.0), 0.0001);
        assertEquals(5.0, config.ability(WarlockTower.CONFIG_ID, "minInterval", -1.0), 0.0001);
        assertEquals(6.0, config.ability(WarlockTowers.BASE_WARLOCK_TOWER.id(), "sacrificeRadius", -1.0), 0.0001);
        assertEquals(-1.0, config.ability(WarlockTower.CONFIG_ID, "attackRangeStep", -1.0), 0.0001);
        assertEquals(20.0, config.ability(WarlockTower.CONFIG_ID, "awakeningAbsorptions", -1.0), 0.0001);
        assertEquals(0.40, config.ability(WarlockTower.CONFIG_ID, "awakeningThreshold", -1.0), 0.0001);
        assertEquals(0.55, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "threshold", -1.0), 0.0001);
        assertEquals(0.40, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "roundStat", -1.0), 0.0001);
        assertEquals(15.0, config.ability(WarlockTower.CONFIG_ID, "speedCap", -1.0), 0.0001);
        assertEquals(-1.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "speedCap", -1.0), 0.0001);
        assertEquals(3.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "defenseThreshold", -1.0), 0.0001);
        assertEquals(0.50, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "splashDamage", -1.0), 0.0001);
        assertEquals(400.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "awakeningHeal", -1.0), 0.0001);
        assertEquals(40.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "awakeningRegeneration", -1.0), 0.0001);
        assertEquals(20.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "awakeningRegenerationTicks", -1.0), 0.0001);
        assertEquals(0.55, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "threshold", -1.0), 0.0001);
        assertEquals(0.60, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "roundStat", -1.0), 0.0001);
        assertEquals(1.0, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "speedStep", -1.0), 0.0001);
        assertEquals(75.0, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "awakeningDamage", -1.0), 0.0001);
        assertEquals(0.30, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "awakeningMoveSpeed", -1.0), 0.0001);
        assertEquals(0.25, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "splashStep", -1.0), 0.0001);
        assertEquals(2.0, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "splashCap", -1.0), 0.0001);
        assertEquals(0.75, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "splashDamage", -1.0), 0.0001);
    }

    @Test
    void combatCapsDamageAndSplashRadius() {
        WarlockCombat combat = new WarlockCombat(WarlockConfig.RUNTIME);

        assertEquals(350.0, combat.modifyOutgoingDamage(WarlockTowers.RANGED_WARLOCK_TOWER, 1_000.0), 0.0001);
        assertEquals(200.0, combat.modifyOutgoingDamage(WarlockTowers.RANGED_WARLOCK_TOWER, 200.0), 0.0001);
        assertEquals(1_000.0, combat.modifyOutgoingDamage(WarlockTowers.MELEE_WARLOCK_TOWER, 1_000.0), 0.0001);
        assertEquals(1_000.0, combat.modifyOutgoingDamage(WarlockTowers.BASE_WARLOCK_TOWER, 1_000.0), 0.0001);
        assertEquals(0.0, combat.modifyOutgoingDamage(WarlockTowers.RANGED_WARLOCK_TOWER, -1.0), 0.0001);
        assertEquals(0.1, combat.splashRadiusForCount(1), 0.0001);
        assertEquals(0.8, combat.splashRadiusForCount(8), 0.0001);
        assertEquals(6.4, combat.splashRadiusForCount(64), 0.0001);
        assertEquals(8.0, combat.splashRadiusForCount(100), 0.0001);
        assertEquals(0.25, combat.meleeSplashRadiusForCount(1), 0.0001);
        assertEquals(1.0, combat.meleeSplashRadiusForCount(4), 0.0001);
        assertEquals(1.5, combat.meleeSplashRadiusForCount(6), 0.0001);
        assertEquals(2.0, combat.meleeSplashRadiusForCount(8), 0.0001);
        assertEquals(2.0, combat.meleeSplashRadiusForCount(100), 0.0001);
        assertEquals(175.0, combat.resolvedSplashDamage(WarlockTowers.RANGED_WARLOCK_TOWER, 350.0), 0.0001);
        assertEquals(262.5, combat.resolvedSplashDamage(WarlockTowers.MELEE_WARLOCK_TOWER, 350.0), 0.0001);
    }

    @Test
    void rangedLifeStealGrowsEveryFiveAbsorptionsAndCapsAtPercent() {
        WarlockCombat combat = new WarlockCombat(WarlockConfig.RUNTIME);

        assertEquals(0.0, combat.lifeStealRatioForCount(WarlockTowers.RANGED_WARLOCK_TOWER, 4), 0.0001);
        assertEquals(0.005, combat.lifeStealRatioForCount(WarlockTowers.RANGED_WARLOCK_TOWER, 5), 0.0001);
        assertEquals(0.005, combat.lifeStealRatioForCount(WarlockTowers.RANGED_WARLOCK_TOWER, 9), 0.0001);
        assertEquals(0.01, combat.lifeStealRatioForCount(WarlockTowers.RANGED_WARLOCK_TOWER, 10), 0.0001);
        assertEquals(0.08, combat.lifeStealRatioForCount(WarlockTowers.RANGED_WARLOCK_TOWER, 80), 0.0001);
        assertEquals(0.085, combat.lifeStealRatioForCount(WarlockTowers.RANGED_WARLOCK_TOWER, 85), 0.0001);
        assertEquals(0.085, combat.lifeStealRatioForCount(WarlockTowers.RANGED_WARLOCK_TOWER, 90), 0.0001);
        assertEquals(0.085, combat.lifeStealRatioForCount(WarlockTowers.RANGED_WARLOCK_TOWER, 200), 0.0001);
    }

    @Test
    void meleeLifeStealUsesCurrentRoundAbsorptionsAndCapsAtPercent() {
        WarlockCombat combat = new WarlockCombat(WarlockConfig.RUNTIME);

        assertEquals(0.0, combat.lifeStealRatioForCounts(WarlockTowers.MELEE_WARLOCK_TOWER, 20, 0), 0.0001);
        assertEquals(0.03, combat.lifeStealRatioForCounts(WarlockTowers.MELEE_WARLOCK_TOWER, 20, 3), 0.0001);
        assertEquals(0.15, combat.lifeStealRatioForCounts(WarlockTowers.MELEE_WARLOCK_TOWER, 40, 15), 0.0001);
        assertEquals(0.16, combat.lifeStealRatioForCounts(WarlockTowers.MELEE_WARLOCK_TOWER, 40, 16), 0.0001);
        assertEquals(0.16, combat.lifeStealRatioForCounts(WarlockTowers.MELEE_WARLOCK_TOWER, 40, 17), 0.0001);
        assertEquals(0.16, combat.lifeStealRatioForCounts(WarlockTowers.MELEE_WARLOCK_TOWER, 40, 25), 0.0001);
        assertEquals(0.01, combat.lifeStealRatioForCounts(WarlockTowers.RANGED_WARLOCK_TOWER, 10, 3), 0.0001);
    }

    @Test
    void descriptionUsesAttackRangeTerminologyAndExactStep() {
        String description = String.join("\n", TowerBalanceRuntime.resolve(WarlockTowers.RANGED_WARLOCK_TOWER).description());
        assertTrue(description.contains("0.1블록"));
        assertTrue(description.contains("공격 범위"));
        assertTrue(description.contains("최대 8.5%"));
        assertTrue(description.contains("최대 8블록"));
        assertTrue(description.contains("50% 피해"));
        assertTrue(description.contains("체력이 55% 이하"));
        assertTrue(description.contains("체력과 공격력의 40%"));
        assertTrue(description.contains("20기 이상 흡수"));
        assertTrue(description.contains("생존한 타워가 이 코어뿐"));
        assertTrue(description.contains("체력이 40% 이하"));
        assertTrue(description.contains("체력을 400 회복"));
        assertTrue(description.contains("초당 체력을 40 회복"));
        assertFalse(description.contains("중첩 제한 없음"));
        assertFalse(description.contains("스플래시 범위"));
        String meleeDescription = String.join("\n", TowerBalanceRuntime.resolve(WarlockTowers.MELEE_WARLOCK_TOWER).description());
        assertTrue(meleeDescription.contains("체력이 55% 이하"));
        assertTrue(meleeDescription.contains("체력과 공격력의 60%"));
        assertTrue(meleeDescription.contains("공격 주기가 1틱 감소"));
        assertTrue(meleeDescription.contains("최소 5틱"));
        assertTrue(meleeDescription.contains("공격 범위가 0.25블록 증가"));
        assertTrue(meleeDescription.contains("최대 2블록"));
        assertTrue(meleeDescription.contains("75% 피해"));
        assertTrue(meleeDescription.contains("이번 라운드에 흡수한 타워마다 생명력 흡수"));
        assertTrue(meleeDescription.contains("20기 이상 흡수"));
        assertTrue(meleeDescription.contains("생존한 타워가 이 코어뿐"));
        assertTrue(meleeDescription.contains("체력이 40% 이하"));
        assertTrue(meleeDescription.contains("공격력이 75"));
        assertTrue(meleeDescription.contains("이동속도가 30% 증가"));
    }

    @Test
    void runtimeStatsShowAbsorptionAndAccumulatedCombatValues() {
        WarlockTower tower = new WarlockTower(
                TowerBalanceRuntime.resolve(WarlockTowers.RANGED_WARLOCK_TOWER),
                UUID.randomUUID(),
                TeamId.RED,
                0,
                new GridPosition(0, 0, 0)
        );
        String details = String.join("\n", tower.runtimeDetailLines()).replaceAll("<[^>]+>", "");
        assertEquals(7.0, tower.adjustAttackRange(7.0), 0.0001);
        assertEquals(350.0, tower.modifyResolvedOutgoingDamage(null, null, 500.0), 0.0001);
        assertEquals(350.0, tower.modifyAppliedDamage(null, null, 500.0), 0.0001);
        assertEquals(5, tower.minimumAttackIntervalTicks());
        assertTrue(details.contains("흡수한 타워: 0기"));
        assertTrue(details.contains("이번 라운드에 흡수한 타워: 0기"));
        assertTrue(details.contains("각성 상태: 미각성"));
        assertTrue(details.contains("피해·공격력 상한: 350"));
        assertTrue(details.contains("추가 공격력: 0.0"));
        assertTrue(details.contains("추가 체력: 0.0"));
        assertTrue(details.contains("공격 속도: -0틱 / -15틱"));
        assertTrue(details.contains("공격 범위: 0 블록 / 8 블록"));
        assertTrue(details.contains("재생: 0 / 40 HP/s"));
        assertTrue(details.contains("생명력 흡수: 0.0% / 8.5%"));
        assertTrue(details.contains("피해 감소: 0.0% / 10.0%"));
        assertFalse(details.contains("제한 없음"));
        assertFalse(details.contains("스플래시 범위:"));
        assertFalse(details.contains("최종 피해 제외"));
        assertFalse(details.contains("받는 피해 감소:"));
        WarlockTower melee = new WarlockTower(
                TowerBalanceRuntime.resolve(WarlockTowers.MELEE_WARLOCK_TOWER),
                UUID.randomUUID(),
                TeamId.RED,
                0,
                new GridPosition(0, 0, 0)
        );
        String meleeDetails = String.join("\n", melee.runtimeDetailLines()).replaceAll("<[^>]+>", "");
        assertEquals(5, melee.minimumAttackIntervalTicks());
        assertTrue(meleeDetails.contains("흡수한 타워: 0기"));
        assertTrue(meleeDetails.contains("이번 라운드에 흡수한 타워: 0기"));
        assertTrue(meleeDetails.contains("각성 상태: 미각성"));
        assertTrue(meleeDetails.contains("추가 공격력: 0.0"));
        assertTrue(meleeDetails.contains("추가 체력: 0.0"));
        assertTrue(meleeDetails.contains("공격 속도: -0틱 / -15틱"));
        assertTrue(meleeDetails.contains("공격 범위: 0 블록 / 2 블록"));
        assertTrue(meleeDetails.contains("생명력 흡수: 0.0% / 16.0%"));
        assertTrue(meleeDetails.contains("피해 감소: 0.0% / 25.0%"));
        assertFalse(meleeDetails.contains("피해·공격력 상한"));
        assertFalse(meleeDetails.contains("재생:"));
        assertFalse(meleeDetails.contains("제한 없음"));
        assertFalse(meleeDetails.contains("스플래시 범위:"));
        assertFalse(meleeDetails.contains("받는 피해 감소:"));
    }

    @Test
    void statsViewIncludesRequestedWarlockStats() {
        List<String> lines = WarlockStatsView.core(
                new WarlockStatsView.CoreStats(
                        12,
                        7,
                        true,
                        true,
                        new WarlockStatsView.CombatStats(
                                350.0,
                                42.5,
                                4,
                                15,
                                1.5,
                                8.0,
                                true
                        ),
                        new WarlockStatsView.DefenseStats(
                                75.0,
                                40.0,
                                40.0,
                                0.085,
                                0.085,
                                0.10,
                                0.10
                        )
                )
        );
        String details = String.join("\n", lines).replaceAll("<[^>]+>", "");
        assertTrue(details.contains("흡수한 타워: 12기"));
        assertTrue(details.contains("이번 라운드에 흡수한 타워: 7기"));
        assertTrue(details.contains("각성 상태: 각성"));
        assertTrue(details.contains("피해·공격력 상한: 350"));
        assertTrue(details.contains("추가 공격력: 42.5"));
        assertTrue(details.contains("추가 체력: 75.0"));
        assertTrue(details.contains("공격 속도: -4틱 / -15틱"));
        assertTrue(details.contains("공격 범위: 1.5 블록 / 8 블록"));
        assertTrue(details.contains("재생: 40 / 40 HP/s"));
        assertFalse(details.contains("제한 없음"));
        assertFalse(details.contains("스플래시 범위:"));
        assertTrue(details.contains("생명력 흡수: 8.5% / 8.5%"));
        assertTrue(details.contains("피해 감소: 10.0% / 10.0%"));
        assertFalse(details.contains("받는 피해 감소:"));
    }
}
