package kim.biryeong.semiontd.tower.warlock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WarlockBalanceDefaultsTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void resetBalance() {
        WarlockAwakeningProgress.clearAllForTesting();
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
        assertEquals(70, config.upgradeCost(
                WarlockTowers.T1_SLAVE.id(),
                WarlockTowers.T2_SLAVE.id(),
                -1
        ));
        assertEquals(150, config.upgradeCost(
                WarlockTowers.T2_SLAVE.id(),
                WarlockTowers.T3_SLAVE.id(),
                -1
        ));
        assertEquals(80, config.upgradeCost(
                WarlockTowers.T1_RANGED_SLAVE.id(),
                WarlockTowers.T2_RANGED_SLAVE.id(),
                -1
        ));
        assertEquals(160, config.upgradeCost(
                WarlockTowers.T2_RANGED_SLAVE.id(),
                WarlockTowers.T3_RANGED_SLAVE.id(),
                -1
        ));

        assertEquals(140.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "damageThreshold", -1.0), 0.0001);
        assertEquals(20.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "damageScale", -1.0), 0.0001);
        assertEquals(2000.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "healthThreshold", -1.0), 0.0001);
        assertEquals(500.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "healthScale", -1.0), 0.0001);
        assertEquals(200.0, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "damageThreshold", -1.0), 0.0001);
        assertEquals(20.0, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "damageScale", -1.0), 0.0001);
        assertEquals(3500.0, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "healthThreshold", -1.0), 0.0001);
        assertEquals(500.0, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "healthScale", -1.0), 0.0001);
        assertEquals(0.07, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "lifeCap", -1.0), 0.0001);
        assertEquals(0.30, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "incomeDebuffResistance", -1.0), 0.0001);
        assertEquals(10.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "lifeEvery", -1.0), 0.0001);
        assertEquals(0.13, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "lifeCap", -1.0), 0.0001);
        assertEquals(0.40, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "incomeDebuffResistance", -1.0), 0.0001);
        assertEquals(0.30, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "defenseCap", -1.0), 0.0001);
        assertEquals(10.0, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "defenseEvery", -1.0), 0.0001);
        assertEquals(0.1, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "splashStep", -1.0), 0.0001);
        assertEquals(2.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "splashEvery", -1.0), 0.0001);
        assertEquals(8.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "splashCap", -1.0), 0.0001);
        assertEquals(25.0, config.ability(WarlockTower.CONFIG_ID, "sacrificeRadius", -1.0), 0.0001);
        assertEquals(30.0, config.ability(WarlockTower.CONFIG_ID, "absorptionHeal", -1.0), 0.0001);
        assertEquals(5.0, config.ability(WarlockTower.CONFIG_ID, "minInterval", -1.0), 0.0001);
        assertEquals(6.0, config.ability(WarlockTowers.BASE_WARLOCK_TOWER.id(), "sacrificeRadius", -1.0), 0.0001);
        assertEquals(0.40, config.ability(WarlockTower.CONFIG_ID, "awakeningThreshold", -1.0), 0.0001);
        assertEquals(1400.0, config.ability(WarlockTower.CONFIG_ID, "awakeningKills", -1.0), 0.0001);
        assertEquals(0.65, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "threshold", -1.0), 0.0001);
        assertEquals(0.50, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "roundStat", -1.0), 0.0001);
        assertEquals(15.0, config.ability(WarlockTower.CONFIG_ID, "speedCap", -1.0), 0.0001);
        assertEquals(3.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "defenseThreshold", -1.0), 0.0001);
        assertEquals(0.15, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "defense", -1.0), 0.0001);
        assertEquals(0.50, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "splashDamage", -1.0), 0.0001);
        assertEquals(0.20, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "petHealthCap", -1.0), 0.0001);
        assertEquals(0.04, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "petHealth", -1.0), 0.0001);
        assertEquals(0.50, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "petDamageCap", -1.0), 0.0001);
        assertEquals(0.10, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "petDamage", -1.0), 0.0001);
        assertEquals(800.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "awakeningHeal", -1.0), 0.0001);
        assertEquals(40.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "awakeningRegeneration", -1.0), 0.0001);
        assertEquals(20.0, config.ability(WarlockTowers.RANGED_WARLOCK_TOWER.id(), "awakeningRegenerationTicks", -1.0), 0.0001);
        assertEquals(0.65, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "threshold", -1.0), 0.0001);
        assertEquals(0.60, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "roundStat", -1.0), 0.0001);
        assertEquals(1.0, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "speedStep", -1.0), 0.0001);
        assertEquals(75.0, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "awakeningDamage", -1.0), 0.0001);
        assertEquals(0.30, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "awakeningMoveSpeed", -1.0), 0.0001);
        assertEquals(0.25, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "splashStep", -1.0), 0.0001);
        assertEquals(2.0, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "splashCap", -1.0), 0.0001);
        assertEquals(0.75, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "splashDamage", -1.0), 0.0001);
        assertEquals(0.50, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "petHealthCap", -1.0), 0.0001);
        assertEquals(0.10, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "petHealth", -1.0), 0.0001);
        assertEquals(0.20, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "petDamageCap", -1.0), 0.0001);
        assertEquals(0.04, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "petDamage", -1.0), 0.0001);
        assertEquals(800.0, config.ability(WarlockTowers.MELEE_WARLOCK_TOWER.id(), "awakeningHeal", -1.0), 0.0001);
        assertEquals(List.of(
                "sacrificeRadius", "absorptionHeal", "minInterval", "speedCap", "awakeningKills", "awakeningThreshold"
        ), List.copyOf(config.abilities().get(WarlockTowers.CONFIG_ID).keySet()));
        assertEquals(List.of(
                "sacrificeRadius", "permanentHealth", "permanentDamage"
        ), List.copyOf(config.abilities().get(WarlockTowers.BASE_WARLOCK_TOWER.id()).keySet()));
        assertEquals(List.of(
                "threshold", "roundStat", "permanentHealth", "healthThreshold", "healthScale", "permanentDamage",
                "damageThreshold", "damageScale", "lifeEvery", "lifeStep", "lifeCap", "incomeDebuffResistance",
                "splashEvery", "splashStep", "splashCap", "splashDamage", "defenseThreshold", "defense", "petHealth", "petHealthCap",
                "petDamage", "petDamageCap", "awakeningHeal", "awakeningRegeneration", "awakeningRegenerationTicks"
        ), List.copyOf(config.abilities().get(WarlockTowers.RANGED_WARLOCK_TOWER.id()).keySet()));
        assertEquals(List.of(
                "threshold", "roundStat", "permanentHealth", "healthThreshold", "healthScale", "permanentDamage",
                "damageThreshold", "damageScale", "lifeStep", "lifeCap", "incomeDebuffResistance", "speedStep",
                "splashStep", "splashCap", "splashDamage", "defenseEvery", "defenseStep", "defenseCap", "petHealth",
                "petHealthCap", "petDamage", "petDamageCap", "awakeningHeal", "awakeningDamage", "awakeningMoveSpeed"
        ), List.copyOf(config.abilities().get(WarlockTowers.MELEE_WARLOCK_TOWER.id()).keySet()));
    }

}
