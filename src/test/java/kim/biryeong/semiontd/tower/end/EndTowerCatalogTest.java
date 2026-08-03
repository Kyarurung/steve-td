package kim.biryeong.semiontd.tower.end;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.job.EndTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.animal.AnimalTowers;
import kim.biryeong.semiontd.tower.description.TowerDescriptionRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EndTowerCatalogTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void resetCatalogs() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void defaultBalanceConfigIncludesEndTowersAndAbilities() {
        TowerBalanceConfig config = TowerBalanceConfig.defaultConfig();

        assertTrue(config.towers().containsKey(EndTowers.BASE_END_TOWER.id()));
        assertTrue(config.towers().containsKey(EndTowers.T3_END_CRYSTAL_TOWER.id()));
        assertTrue(config.towers().containsKey(EndTowers.T3_SHULKER_TOWER.id()));
        assertEquals(-1.0, config.ability(EndTower.CONFIG_ID, "hatchDelayTicks", -1.0), 0.0001);
        assertEquals(2000.0, config.ability(EndTower.CONFIG_ID, "dragonEvolution", -1.0), 0.0001);
        assertEquals(250.0, config.ability(EndTower.CONFIG_ID, "damageCap", -1.0), 0.0001);
        assertEquals(200.0, config.ability(EndTower.CONFIG_ID, "transferTicks", -1.0), 0.0001);
        assertEquals(30.0, config.ability(EndTower.CONFIG_ID, "transferHeal", -1.0), 0.0001);
        assertEquals(0.05, config.ability(EndTower.CONFIG_ID, "transferHealRatio", -1.0), 0.0001);
        assertEquals(0.50, config.ability(EndTower.CONFIG_ID, "roundHealthRatio", -1.0), 0.0001);
        assertEquals(0.75, config.ability(EndTower.CONFIG_ID, "roundDamageRatio", -1.0), 0.0001);
        assertEquals(0.04, config.ability(EndTower.CONFIG_ID, "permanentHealthRatio", -1.0), 0.0001);
        assertEquals(0.06, config.ability(EndTower.CONFIG_ID, "permanentDamageRatio", -1.0), 0.0001);
        assertEquals(1.0, config.ability(EndTower.CONFIG_ID, "transferAttackSpeedStacks", -1.0), 0.0001);
        assertEquals(1.0, config.ability(EndTower.CONFIG_ID, "transferAttackSpeedStep", -1.0), 0.0001);
        assertEquals(2.0, config.ability(EndTower.CONFIG_ID, "dragonRangeBonus", -1.0), 0.0001);
        assertEquals(0.20, config.ability(EndTower.CONFIG_ID, "dragonFinalDamage", -1.0), 0.0001);
        assertEquals(30.0, config.ability(EndTower.CONFIG_ID, "attackSpeedStacks", -1.0), 0.0001);
        assertEquals(1.0, config.ability(EndTower.CONFIG_ID, "attackSpeedStep", -1.0), 0.0001);
        assertEquals(10.0, config.ability(EndTower.CONFIG_ID, "attackSpeedCap", -1.0), 0.0001);
        assertEquals(5.0, config.ability(EndTower.CONFIG_ID, "attackSpeedMinimumTicks", -1.0), 0.0001);
        assertEquals(50.0, config.ability(EndTower.CONFIG_ID, "attackRangeStacks", -1.0), 0.0001);
        assertEquals(0.5, config.ability(EndTower.CONFIG_ID, "attackRangeStep", -1.0), 0.0001);
        assertEquals(3.0, config.ability(EndTower.CONFIG_ID, "attackRangeCap", -1.0), 0.0001);
        assertEquals(15.0, config.ability(EndTower.CONFIG_ID, "splash1", -1.0), 0.0001);
        assertEquals(60.0, config.ability(EndTower.CONFIG_ID, "splash2", -1.0), 0.0001);
        assertEquals(150.0, config.ability(EndTower.CONFIG_ID, "splash3", -1.0), 0.0001);
        assertEquals(300.0, config.ability(EndTower.CONFIG_ID, "splash4", -1.0), 0.0001);
        assertEquals(1.0, config.ability(EndTower.CONFIG_ID, "splashStep", -1.0), 0.0001);
        assertEquals(5.0, config.ability(EndTower.CONFIG_ID, "splashCap", -1.0), 0.0001);
        assertEquals(0.60, config.ability(EndTower.CONFIG_ID, "splashDamageRatio", -1.0), 0.0001);
        assertEquals(30.0, config.ability(EndTower.CONFIG_ID, "lifeStealStacks", -1.0), 0.0001);
        assertEquals(0.01, config.ability(EndTower.CONFIG_ID, "lifeStealStep", -1.0), 0.0001);
        assertEquals(0.10, config.ability(EndTower.CONFIG_ID, "lifeStealCap", -1.0), 0.0001);
        assertEquals(
                300.0,
                config.ability(EndTower.CONFIG_ID, "lifeStealStacks", -1.0) * 10.0,
                0.0001
        );
        assertEquals(
                config.ability(EndTower.CONFIG_ID, "lifeStealCap", -1.0),
                config.ability(EndTower.CONFIG_ID, "lifeStealStep", -1.0) * 10.0,
                0.0001
        );
        assertEquals(10.0, config.ability(EndTower.CONFIG_ID, "regenerationStacks", -1.0), 0.0001);
        assertEquals(1.0, config.ability(EndTower.CONFIG_ID, "regenerationStep", -1.0), 0.0001);
        assertEquals(30.0, config.ability(EndTower.CONFIG_ID, "regenerationCap", -1.0), 0.0001);
        assertEquals(20.0, config.ability(EndTower.CONFIG_ID, "regenerationTicks", -1.0), 0.0001);
        assertEquals(15.0, config.ability(EndTower.CONFIG_ID, "damageReductionStacks", -1.0), 0.0001);
        assertEquals(0.01, config.ability(EndTower.CONFIG_ID, "damageReductionStep", -1.0), 0.0001);
        assertEquals(0.20, config.ability(EndTower.CONFIG_ID, "damageReductionCap", -1.0), 0.0001);
        assertEquals(1.0, config.ability(EndTower.CONFIG_ID, "phantomScaleBase", -1.0), 0.0001);
        assertEquals(100.0, config.ability(EndTower.CONFIG_ID, "phantomScaleHealth", -1.0), 0.0001);
        assertEquals(0.2, config.ability(EndTower.CONFIG_ID, "phantomScaleStep", -1.0), 0.0001);
        assertEquals(5.0, config.ability(EndTower.CONFIG_ID, "phantomScaleCap", -1.0), 0.0001);
        assertEquals(0.10, config.ability(EndTowers.T1_SHULKER_TOWER.id(), "damageReduction", -1.0), 0.0001);
        assertEquals(0.30, config.ability(EndTowers.T2_SHULKER_TOWER.id(), "damageReduction", -1.0), 0.0001);
        assertEquals(0.50, config.ability(EndTowers.T3_SHULKER_TOWER.id(), "damageReduction", -1.0), 0.0001);
    }

    @Test
    void endJobAllowsEveryEndTowerOnly() {
        EndTowerJob job = new EndTowerJob();

        assertTrue(job.canUseTower(null, EndTowers.BASE_END_TOWER));
        assertTrue(job.canUseTower(null, EndTowers.T1_ENDERMITE_TOWER));
        assertTrue(job.canUseTower(null, EndTowers.T3_END_CRYSTAL_TOWER));
        assertTrue(job.canUseTower(null, EndTowers.T1_SHULKER_TOWER));
        assertTrue(job.canUseTower(null, EndTowers.T3_SHULKER_TOWER));
        assertFalse(job.canUseTower(null, AnimalTowers.T1_PIG_TOWER));
    }

    @Test
    void catalogRegistersDragonAndTwoUpgradePaths() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());

        assertEquals(50L, EndTowers.T1_ENDERMITE_TOWER.mineralCost());
        assertEquals(80L, EndTowers.T2_ENDERMAN_TOWER.mineralCost());
        assertEquals(130L, EndTowers.T3_END_CRYSTAL_TOWER.mineralCost());
        assertEquals(50L, EndTowers.T1_SHULKER_TOWER.mineralCost());
        assertEquals(80L, EndTowers.T2_SHULKER_TOWER.mineralCost());
        assertEquals(130L, EndTowers.T3_SHULKER_TOWER.mineralCost());

        assertStarter(EndTowers.BASE_END_TOWER.id(), "엔더 드래곤");
        assertStarter(EndTowers.T1_ENDERMITE_TOWER.id(), "엔더 마이트");
        assertStarter(EndTowers.T1_SHULKER_TOWER.id(), "셜커");
        assertUpgrade(EndTowers.T1_ENDERMITE_TOWER.id(), EndTowers.T2_ENDERMAN_TOWER.id(), "엔더맨", 80);
        assertUpgrade(EndTowers.T2_ENDERMAN_TOWER.id(), EndTowers.T3_END_CRYSTAL_TOWER.id(), "엔드 수정", 130);
        assertUpgrade(EndTowers.T1_SHULKER_TOWER.id(), EndTowers.T2_SHULKER_TOWER.id(), "견고한 셜커", 80);
        assertUpgrade(EndTowers.T2_SHULKER_TOWER.id(), EndTowers.T3_SHULKER_TOWER.id(), "완강한 셜커", 130);
    }

    @Test
    void shulkerLineUsesShulkerVisuals() {
        assertEquals("minecraft:shulker", EndTowers.T1_SHULKER_TOWER.visual().entityTypeId());
        assertEquals("minecraft:shulker", EndTowers.T2_SHULKER_TOWER.visual().entityTypeId());
        assertEquals("minecraft:shulker", EndTowers.T3_SHULKER_TOWER.visual().entityTypeId());
        assertFalse(EndTowers.T1_SHULKER_TOWER.visual().properties().containsKey("shulker_color"));
        assertEquals(DyeColor.PURPLE, EndTowers.T2_SHULKER_TOWER.visual().properties().get("shulker_color"));
        assertEquals(DyeColor.BLACK, EndTowers.T3_SHULKER_TOWER.visual().properties().get("shulker_color"));
    }

    @Test
    void shulkerDescriptionsShowTierDamageReduction() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());

        assertTrue(String.join("\n", TowerBalanceRuntime.resolve(EndTowers.T1_SHULKER_TOWER).description()).contains("10%"));
        assertTrue(String.join("\n", TowerBalanceRuntime.resolve(EndTowers.T2_SHULKER_TOWER).description()).contains("30%"));
        assertTrue(String.join("\n", TowerBalanceRuntime.resolve(EndTowers.T3_SHULKER_TOWER).description()).contains("50%"));
    }

    @Test
    void dragonDescriptionUsesConfiguredAbilityValues() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());

        assertEquals(10.0, EndTowers.BASE_END_TOWER.damage(), 0.0001);

        String description = String.join(
                "\n",
                TowerBalanceRuntime.resolve(EndTowers.BASE_END_TOWER).description()
        );
        String plainDescription = description.replaceAll("<[^>]+>", "");
        assertTrue(plainDescription.contains("알로 소환되며"));
        assertTrue(plainDescription.contains("라운드 시작 시 아기 드래곤"));
        assertTrue(plainDescription.contains("이상이면"));
        assertTrue(plainDescription.contains("아기 드래곤 크기는 최대 체력 100당 0.2씩 증가합니다."));
        assertTrue(plainDescription.contains("10초"));
        assertTrue(plainDescription.contains("전달 중인 셜커 타워의 최대 체력 5%만큼 매초 회복합니다."));
        assertTrue(description.contains("<#E66F6F>최대 체력 5%</#E66F6F>"));
        assertTrue(plainDescription.contains("피해량 상한(최종 피해 제외): 250"));
        assertTrue(plainDescription.contains("타워 공격력의 75%를 임시 획득"));
        assertTrue(plainDescription.contains("공격 범위: 엔드 수정 15, 60, 150, 300스택마다 +1 블록"));
        assertTrue(plainDescription.contains("엔드 수정 30스택마다 -1틱"));
        assertTrue(plainDescription.contains("사거리: 엔드 수정 50스택마다 +0.5 블록"));
        assertTrue(plainDescription.contains("타워 체력의 50%를 임시 획득"));
        assertTrue(plainDescription.contains("셜커 30스택마다 +1%"));
        assertTrue(plainDescription.contains("피해 감소: 셜커 15스택마다 +1%"));
        assertTrue(plainDescription.contains("셜커 10스택마다 +1 HP/s"));
        assertFalse(plainDescription.contains("(최대"));
        assertTrue(plainDescription.contains("엔더 드래곤: 최종 피해 +20% / 추가 사거리 +2 블록"));
        assertFalse(description.contains("{ability."));
        assertTrue(description.contains("<#B77DE8>엔더 드래곤</#B77DE8>:"));
        assertTrue(description.contains("<#D94343>공격력</#D94343>"));
        assertTrue(description.contains("<#E66F6F>체력</#E66F6F>"));
        assertTrue(description.contains("<#72A9E6>피해 감소</#72A9E6>"));
    }

    @Test
    void endJobDescriptionUsesConfiguredAbilityValues() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());

        String description = new EndTowerJob().description().stream()
                .map(Component::getString)
                .reduce("", (left, right) -> left + "\n" + right);

        assertTrue(description.contains("10초에 걸쳐"));
        assertTrue(description.contains("체력 50%, 공격력 75%"));
        assertTrue(description.contains("체력 4%, 공격력 6%"));
        assertTrue(description.contains("최대 250"));
    }

    @Test
    void everyEndFeederRegistersItsDescriptionTemplate() {
        assertDescription(EndTowers.T1_ENDERMITE_TOWER, "공격력이 높은 엔더마이트", "엔더 드래곤의 공격 능력");
        assertDescription(EndTowers.T2_ENDERMAN_TOWER, "공격력이 높은 엔더맨", "엔더 드래곤의 공격 능력");
        assertDescription(EndTowers.T3_END_CRYSTAL_TOWER, "공격력이 매우 높은 엔드 수정", "엔더 드래곤의 공격 능력");
        assertDescription(EndTowers.T1_SHULKER_TOWER, "체력이 높은 셜커", "엔더 드래곤의 내구력");
        assertDescription(EndTowers.T2_SHULKER_TOWER, "체력이 높은 견고한 셜커", "엔더 드래곤의 내구력");
        assertDescription(EndTowers.T3_SHULKER_TOWER, "체력이 매우 높은 완강한 셜커", "엔더 드래곤의 내구력");
    }

    @Test
    void upgradePricesComeFromBalanceConfig() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, Long> upgradeCosts = new LinkedHashMap<>(defaults.upgradeCosts());
        upgradeCosts.put(
                TowerBalanceConfig.upgradeKey(EndTowers.T1_ENDERMITE_TOWER.id(), EndTowers.T2_ENDERMAN_TOWER.id()),
                1L
        );
        TowerBalanceConfig custom = new TowerBalanceConfig(defaults.towers(), upgradeCosts, defaults.abilities());

        ProductionTowerCatalogs.reloadBuiltIns(custom);

        assertEquals(1L, ProductionTowerCatalog.upgrade(
                EndTowers.T1_ENDERMITE_TOWER,
                EndTowers.T2_ENDERMAN_TOWER.id()
        ).orElseThrow().mineralCost());
    }

    @Test
    void catalogCreatesEndRuntime() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());

        var entry = ProductionTowerCatalog.find(EndTowers.BASE_END_TOWER.id()).orElseThrow();
        var tower = entry.create(
                UUID.nameUUIDFromBytes("end-runtime".getBytes()),
                TeamId.RED,
                1,
                new GridPosition(0, 64, 0)
        );

        assertInstanceOf(EndTower.class, tower);
        assertEquals(0.0, tower.adjustAttackRange(tower.type().range()), 0.0001);
    }

    private static void assertStarter(String towerId, String displayName) {
        var entry = ProductionTowerCatalog.find(towerId).orElseThrow();
        assertTrue(entry.starter());
        assertEquals(displayName, entry.type().displayName());
    }

    private static void assertUpgrade(String fromTowerId, String upgradeId, String displayName, long cost) {
        var from = ProductionTowerCatalog.find(fromTowerId).orElseThrow().type();
        var upgrade = ProductionTowerCatalog.upgrade(from, upgradeId).orElseThrow();
        assertEquals(displayName, upgrade.displayName());
        assertEquals(cost, upgrade.mineralCost());
    }

    private static void assertDescription(
            kim.biryeong.semiontd.tower.TowerType towerType,
            String summary,
            String effect
    ) {
        String description = String.join("\n", TowerDescriptionRegistry.describe(towerType).orElseThrow());
        assertTrue(description.contains(summary));
        assertTrue(description.contains(effect));
    }
}
