package kim.biryeong.semiontd.tower.warlock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.job.WarlockTowerJob;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WarlockDescriptionTest {
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
    void descriptionsExposeAwakeningAndPreviouslyHiddenAbilities() {
        List<String> rangedDescriptionLines = TowerBalanceRuntime.resolve(WarlockTowers.RANGED_WARLOCK_TOWER).description();
        String rangedMarkup = String.join("\n", rangedDescriptionLines);
        String description = rangedMarkup.replaceAll("<[^>]+>", "");
        assertTrue(description.contains("체력 55% 이하이면"));
        assertTrue(description.contains("흡수 시 최대 체력 증가분에 체력 30을 더해 회복"));
        assertTrue(description.contains("공격 우선순위가 가장 낮은 타워를 흡수합니다."));
        assertTrue(description.contains("흡수한 타워 체력과 피해의 50%"));
        assertTrue(description.contains("체력 +2.5%"));
        assertTrue(description.contains("피해 +5%"));
        assertTrue(description.contains("생존 중인 개구리 계열마다 체력 +4%, 피해 +10%"));
        assertTrue(description.contains("최대 체력 +20%, 피해 +50%까지 증가"));
        assertTrue(description.contains("최소 공격 간격은 5틱"));
        assertTrue(description.contains("누적 흡수 10기마다 생명력 흡수 +0.5%"));
        assertTrue(description.contains("최대 7%"));
        assertTrue(description.contains("누적 흡수 2기마다 스플래시 범위 +0.1블록"));
        assertTrue(description.contains("본 피해의 50%"));
        assertTrue(description.contains("이번 라운드 흡수가 3기를 초과하면 받는 피해 15% 감소"));
        assertTrue(description.contains("인컴 디버프 저항 30%"));
        assertTrue(description.contains("누적 1400킬에 각성을 해금"));
        assertTrue(description.contains("체력 40% 이하"));
        assertTrue(description.contains("체력 600을 회복하고 재생 +40 HP/s"));
        assertEquals(13, rangedDescriptionLines.size());
        assertEquals("능력치는 높아질수록 증가 효율이 감소합니다.",
                rangedDescriptionLines.getLast().replaceAll("<[^>]+>", ""));
        assertFalse(rangedMarkup.contains("{ability."));

        List<String> meleeDescriptionLines = TowerBalanceRuntime.resolve(WarlockTowers.MELEE_WARLOCK_TOWER).description();
        String meleeMarkup = String.join("\n", meleeDescriptionLines);
        String meleeDescription = meleeMarkup.replaceAll("<[^>]+>", "");
        assertTrue(meleeDescription.contains("체력 55% 이하이면"));
        assertTrue(meleeDescription.contains("흡수 시 최대 체력 증가분에 체력 30을 더해 회복"));
        assertTrue(meleeDescription.contains("공격 우선순위가 가장 높은 타워를 흡수합니다."));
        assertTrue(meleeDescription.contains("흡수한 타워 체력과 피해의 60%"));
        assertTrue(meleeDescription.contains("체력 +5%"));
        assertTrue(meleeDescription.contains("피해 +2.5%"));
        assertTrue(meleeDescription.contains("생존 중인 양 계열마다 체력 +10%, 피해 +4%"));
        assertTrue(meleeDescription.contains("최대 체력 +50%, 피해 +20%까지 증가"));
        assertTrue(meleeDescription.contains("공격 간격이 1틱 감소"));
        assertTrue(meleeDescription.contains("스플래시 범위 +0.25블록"));
        assertTrue(meleeDescription.contains("생명력 흡수 +1%"));
        assertTrue(meleeDescription.contains("최대 13%"));
        assertTrue(meleeDescription.contains("누적 흡수 10기마다 받는 피해 2.5% 감소"));
        assertTrue(meleeDescription.contains("인컴 디버프 저항 40%"));
        assertTrue(meleeDescription.contains("누적 1400킬에 각성을 해금"));
        assertTrue(meleeDescription.contains("체력 600을 회복하고 피해 +75, 이동 속도 +30%"));
        assertTrue(meleeMarkup.contains("<#F1E7D4>이동 속도 +30%</#F1E7D4>"));
        assertEquals(12, meleeDescriptionLines.size());
        assertEquals("능력치는 높아질수록 증가 효율이 감소합니다.",
                meleeDescriptionLines.getLast().replaceAll("<[^>]+>", ""));
        assertFalse(meleeMarkup.contains("{ability."));

        List<String> baseDescriptionLines = TowerBalanceRuntime.resolve(WarlockTowers.BASE_WARLOCK_TOWER).description();
        String baseMarkup = String.join("\n", baseDescriptionLines);
        String baseDescription = baseMarkup.replaceAll("<[^>]+>", "");
        assertTrue(baseDescription.contains("치명적인 피해를 입으면 주위 6블록 내 아군 중 공격 우선순위가 가장 낮은 타워를 흡수"));
        assertTrue(baseDescription.contains("최대 체력 증가분에 체력 30을 더해 회복"));
        assertTrue(baseDescription.contains("체력 2.5%, 피해 5%를 영구 누적"));
        assertTrue(baseDescription.contains("선택 후에는 변경할 수 없습니다"));
        assertTrue(baseDescription.contains("핵심 타워는 1기만 설치"));
        assertFalse(baseMarkup.contains("{ability."));
    }

    @Test
    void sacrificeDescriptionsStateTheDeathDebuffDirectly() {
        String meleeTierTwo = plainDescription(WarlockTowers.T2_SLAVE);
        String meleeTierThree = plainDescription(WarlockTowers.T3_SLAVE);
        String rangedTierTwo = plainDescription(WarlockTowers.T2_RANGED_SLAVE);
        String rangedTierThree = plainDescription(WarlockTowers.T3_RANGED_SLAVE);

        assertTrue(meleeTierTwo.contains("사망 시 주위 20블록 내 적이 받는 피해를 10% 증가시킵니다."));
        assertTrue(meleeTierThree.contains("사망 시 주위 20블록 내 적이 받는 피해를 10% 증가시킵니다."));
        assertTrue(rangedTierTwo.contains("사망 시 주위 20블록 내 적의 공격 속도를 10% 감소시킵니다."));
        assertTrue(rangedTierThree.contains("사망 시 주위 20블록 내 적의 공격 속도를 10% 감소시킵니다."));
    }

    @Test
    void jobDescriptionCommunicatesTheCompleteBuilderFantasy() {
        String description = new WarlockTowerJob().description().stream()
                .map(component -> component.getString())
                .collect(Collectors.joining("\n"));

        assertTrue(description.contains("희생으로 성장해"));
        assertTrue(description.contains("원거리·근거리 중 선택"));
        assertTrue(description.contains("1400킬 후 최후 생존·저체력에서 각성"));
        assertTrue(new WarlockTowerJob().description().stream()
                .allMatch(component -> component.getString().length() <= 31));

        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        Map<String, Double> global = new LinkedHashMap<>(abilities.get(WarlockTowers.CONFIG_ID));
        global.put("awakeningKills", 42.0);
        abilities.put(WarlockTowers.CONFIG_ID, global);
        TowerBalanceRuntime.apply(new TowerBalanceConfig(defaults.towers(), defaults.upgradeCosts(), abilities));

        String configuredDescription = new WarlockTowerJob().description().stream()
                .map(component -> component.getString())
                .collect(Collectors.joining("\n"));
        assertTrue(configuredDescription.contains("42킬 후 최후 생존·저체력에서 각성"));
    }

    private static String plainDescription(TowerType type) {
        return String.join("\n", TowerBalanceRuntime.resolve(type).description()).replaceAll("<[^>]+>", "");
    }
}
