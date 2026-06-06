package kim.biryeong.semiontd.tower.resonance;

import static kim.biryeong.semiontd.tower.catalog.ProductionTowerDefinitions.tower;

import java.util.List;
import kim.biryeong.semiontd.entity.visual.EntityVisual;
import kim.biryeong.semiontd.entity.visual.MoobloomVisual;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.description.TowerDescriptionRegistry;

public final class ResonanceTowers {
    private static final String LINK_DESCRIPTION = "<gray>{ability.linkRange:integer}칸 안에 다른 종의 무블룸을 모으면 공명합니다.</gray>";

    public static final TowerType FOCUS_CRYSTAL = tower(
            "resonance_focus_crystal",
            "민들레 집중 무블룸",
            95,
            55,
            6,
            10,
            22,
            5,
            moobloom("dandelion"),
            List.of(
                    "<gray>역할: 초반 단일 딜러</gray>",
                    "<gray>첫 타워로 적합합니다. 저렴한 비용으로 초반 라인의 킬 속도를 확보합니다.</gray>",
                    "<green>해금: 공명 1단계. 다른 종 2기를 붙이면 공격속도 보너스로 초반 누수를 줄입니다.</green>"
            )
    );

    public static final TowerType FOCUS_PRISM = tower(
            "resonance_focus_prism",
            "해바라기 집속 무블룸",
            180,
            75,
            6.5,
            18,
            20,
            8,
            moobloom("sunflower"),
            List.of(
                    "<gray>역할: 중반 단일 딜러</gray>",
                    "<gray>체력이 높은 몬스터와 선두 몬스터를 빠르게 처리합니다.</gray>",
                    "<green>해금: 공명 2단계. 다른 종 4기를 붙이면 단일 피해 보너스가 열립니다.</green>"
            )
    );

    public static final TowerType FOCUS_CORE = tower(
            "resonance_focus_core",
            "주황 튤립 초점 무블룸",
            320,
            95,
            7,
            27,
            18,
            12,
            moobloom("orange_tulip"),
            List.of(
                    "<gray>역할: 후반 단일 딜러</gray>",
                    "<gray>높은 체력의 적을 오래 추적해 보스와 선두 몬스터를 처리합니다.</gray>",
                    "<green>해금: 공명 3단계. 다른 종 6기를 붙이면 주 대상 추가 타격이 열립니다.</green>"
            )
    );

    public static final TowerType WAVE_CRYSTAL = tower(
            "resonance_wave_crystal",
            "수레국화 파동 무블룸",
            85,
            45,
            5,
            7,
            24,
            10,
            moobloom("cornflower"),
            List.of(
                    "<gray>역할: 저비용 보조 딜러</gray>",
                    "<gray>첫 단일 딜러 옆에 세우기 좋은 두 번째 타워입니다.</gray>",
                    "<green>해금: 공명 1단계. 다른 종 2기를 붙이면 공격속도 보너스로 초반 소형 몬스터 처리에 보탬이 됩니다.</green>"
            )
    );

    public static final TowerType WAVE_PRISM = tower(
            "resonance_wave_prism",
            "파란 난초 진동 무블룸",
            160,
            60,
            5.5,
            13,
            22,
            12,
            moobloom("blue_orchid"),
            List.of(
                    "<gray>역할: 중반 보조 딜러</gray>",
                    "<gray>몬스터가 뭉치는 구간에서 꾸준한 피해를 넣습니다.</gray>",
                    "<green>해금: 공명 2단계. 다른 종 4기를 붙이면 매 공격 스플래시가 열립니다.</green>"
            )
    );

    public static final TowerType WAVE_CORE = tower(
            "resonance_wave_core",
            "푸른 들꽃 파동 무블룸",
            300,
            80,
            6,
            22,
            20,
            15,
            moobloom("azure_bluet"),
            List.of(
                    "<gray>역할: 후반 범위 보조</gray>",
                    "<gray>소형 몬스터 무리와 체력이 남은 적을 정리합니다.</gray>",
                    "<green>해금: 공명 3단계. 다른 종 6기를 붙이면 넓은 파동 폭발이 열립니다.</green>"
            )
    );

    public static final TowerType FROST_CRYSTAL = tower(
            "resonance_frost_crystal",
            "은방울꽃 결빙 무블룸",
            80,
            50,
            6,
            6,
            26,
            0,
            moobloom("lily_of_the_valley"),
            List.of(
                    "<gray>역할: 저비용 둔화 준비</gray>",
                    "<gray>피해는 낮지만 사거리가 길어 배치 부담이 적습니다.</gray>",
                    "<green>해금: 공명 1단계. 다른 종 2기를 붙이면 공격 대상 둔화가 열립니다.</green>"
            )
    );

    public static final TowerType FROST_PRISM = tower(
            "resonance_frost_prism",
            "하얀 튤립 서리 무블룸",
            150,
            75,
            6.5,
            10,
            24,
            2,
            moobloom("white_tulip"),
            List.of(
                    "<gray>역할: 중반 둔화 보조</gray>",
                    "<gray>긴 사거리로 딜러 주변 빈칸을 메우기 쉽습니다.</gray>",
                    "<green>해금: 공명 2단계. 다른 종 4기를 붙이면 둔화 강화와 둔화 대상 추가 피해가 열립니다.</green>"
            )
    );

    public static final TowerType FROST_CORE = tower(
            "resonance_frost_core",
            "데이지 빙결 무블룸",
            280,
            105,
            7,
            16,
            22,
            5,
            moobloom("oxeye_daisy"),
            List.of(
                    "<gray>역할: 후반 둔화 보조</gray>",
                    "<gray>긴 사거리로 선두와 후속 몬스터를 함께 견제합니다.</gray>",
                    "<green>해금: 공명 3단계. 다른 종 6기를 붙이면 범위 둔화 파동이 열립니다.</green>"
            )
    );

    public static final TowerType AMPLIFY_CRYSTAL = tower(
            "resonance_amplify_crystal",
            "알리움 만개 무블룸",
            110,
            65,
            5,
            5,
            28,
            20,
            moobloom("allium"),
            List.of(
                    "<gray>역할: 연결용 전방 타워</gray>",
                    "<gray>체력과 어그로가 높아 앞줄에 세우기 좋습니다.</gray>",
                    "<green>해금: 공명 1단계. 다른 종 2기를 붙이면 받는 피해 감소가 열립니다.</green>"
            )
    );

    public static final TowerType AMPLIFY_PRISM = tower(
            "resonance_amplify_prism",
            "라일락 정원 무블룸",
            200,
            100,
            5.5,
            11,
            25,
            25,
            moobloom("lilac"),
            List.of(
                    "<gray>역할: 중반 연결 탱커</gray>",
                    "<gray>전선을 버티면서 주변 딜러의 공명 단계를 유지합니다.</gray>",
                    "<green>해금: 공명 2단계. 다른 종 4기를 붙이면 주변 무블룸 공격속도 보조가 열립니다.</green>"
            )
    );

    public static final TowerType AMPLIFY_CORE = tower(
            "resonance_amplify_core",
            "작약 공명 무블룸",
            350,
            140,
            6,
            20,
            22,
            30,
            moobloom("peony"),
            List.of(
                    "<gray>역할: 후반 연결 탱커</gray>",
                    "<gray>높은 체력으로 후반 라인의 압박을 받아냅니다.</gray>",
                    "<green>해금: 공명 3단계. 다른 종 6기를 붙이면 주변 무블룸 회복과 보호가 열립니다.</green>"
            )
    );

    private ResonanceTowers() {
    }

    public static ResonanceAspect aspectOf(TowerType type) {
        if (type == null) {
            return null;
        }
        String id = type.id();
        if (FOCUS_CRYSTAL.id().equals(id) || FOCUS_PRISM.id().equals(id) || FOCUS_CORE.id().equals(id)) {
            return ResonanceAspect.FOCUS;
        }
        if (WAVE_CRYSTAL.id().equals(id) || WAVE_PRISM.id().equals(id) || WAVE_CORE.id().equals(id)) {
            return ResonanceAspect.WAVE;
        }
        if (FROST_CRYSTAL.id().equals(id) || FROST_PRISM.id().equals(id) || FROST_CORE.id().equals(id)) {
            return ResonanceAspect.FROST;
        }
        if (AMPLIFY_CRYSTAL.id().equals(id) || AMPLIFY_PRISM.id().equals(id) || AMPLIFY_CORE.id().equals(id)) {
            return ResonanceAspect.AMPLIFY;
        }
        return null;
    }

    public static boolean isResonanceTower(TowerType type) {
        return aspectOf(type) != null;
    }

    static {
        registerDescription(FOCUS_CRYSTAL);
        registerDescription(FOCUS_PRISM);
        registerDescription(FOCUS_CORE);
        registerDescription(WAVE_CRYSTAL);
        registerDescription(WAVE_PRISM);
        registerDescription(WAVE_CORE);
        registerDescription(FROST_CRYSTAL);
        registerDescription(FROST_PRISM);
        registerDescription(FROST_CORE);
        registerDescription(AMPLIFY_CRYSTAL);
        registerDescription(AMPLIFY_PRISM);
        registerDescription(AMPLIFY_CORE);
    }

    private static void registerDescription(TowerType type) {
        java.util.ArrayList<String> template = new java.util.ArrayList<>(type.description());
        template.add(LINK_DESCRIPTION);
        template.add("<green>조건: 다른 종 {ability.level1RequiredLinks:integer}/{ability.level2RequiredLinks:integer}/{ability.level3RequiredLinks:integer}기 이상에서 1/2/3단계 공명합니다.</green>");
        template.add("<green>현재 해금: 공명 {ability.maxResonanceLevel:integer}단계까지 사용합니다.</green>");
        switch (aspectOf(type)) {
            case FOCUS -> addFocusDescription(template, type);
            case WAVE -> addWaveDescription(template, type);
            case FROST -> addFrostDescription(template, type);
            case AMPLIFY -> addBloomDescription(template, type);
        }
        TowerDescriptionRegistry.registerTemplate(type, template);
    }

    private static void addFocusDescription(java.util.List<String> template, TowerType type) {
        template.add("<green>공명 1단계: 공격속도 +{ability.focusLevel1AttackSpeedBonus:percent}</green>");
        if (unlockedResonanceLevel(type) >= 2) {
            template.add("<green>공명 2단계: 공격속도 +{ability.focusLevel2AttackSpeedBonus:percent}, 피해 +{ability.focusLevel2DamageBonus:percent}</green>");
        }
        if (unlockedResonanceLevel(type) >= 3) {
            template.add("<green>공명 3단계: 공격속도 +{ability.focusLevel3AttackSpeedBonus:percent}, 피해 +{ability.focusLevel3DamageBonus:percent}</green>");
            template.add("<green>3단계부터 {ability.focusStrikeEveryAttacks:integer}번째 공격마다 주 대상에게 {ability.focusStrikeDamageRatio:percent} 추가 피해를 줍니다.</green>");
        }
    }

    private static void addWaveDescription(java.util.List<String> template, TowerType type) {
        template.add("<green>공명 1단계: 공격속도 +{ability.waveLevel1AttackSpeedBonus:percent}</green>");
        if (unlockedResonanceLevel(type) >= 2) {
            template.add("<green>공명 2단계: 매 공격마다 {ability.waveLevel2SplashRadius:blocks} 범위에 {ability.waveLevel2SplashDamageRatio:percent} 스플래시 피해를 줍니다.</green>");
        }
        if (unlockedResonanceLevel(type) >= 3) {
            template.add("<green>공명 3단계: 스플래시가 {ability.waveLevel3SplashRadius:blocks}, {ability.waveLevel3SplashDamageRatio:percent}로 강화됩니다.</green>");
            template.add("<green>3단계부터 {ability.wavePulseEveryAttacks:integer}번째 공격마다 {ability.wavePulseRadius:blocks} 범위에 {ability.wavePulseDamageRatio:percent} 파동 피해를 줍니다.</green>");
        }
    }

    private static void addFrostDescription(java.util.List<String> template, TowerType type) {
        template.add("<green>공명 1단계: 공격 대상에게 {ability.frostLevel1SlowTicks:seconds} 동안 이동속도 -{ability.frostLevel1SlowMagnitude:percent}를 줍니다.</green>");
        if (unlockedResonanceLevel(type) >= 2) {
            template.add("<green>공명 2단계: 둔화가 {ability.frostLevel2SlowTicks:seconds}, -{ability.frostLevel2SlowMagnitude:percent}로 강화되고 둔화 대상 피해가 +{ability.frostLevel2DamageVsSlowedBonus:percent} 증가합니다.</green>");
        }
        if (unlockedResonanceLevel(type) >= 3) {
            template.add("<green>공명 3단계: 둔화가 {ability.frostLevel3SlowTicks:seconds}, -{ability.frostLevel3SlowMagnitude:percent}로 강화되고 둔화 대상 피해가 +{ability.frostLevel3DamageVsSlowedBonus:percent} 증가합니다.</green>");
            template.add("<green>3단계부터 {ability.frostPulseEveryAttacks:integer}번째 공격마다 {ability.frostPulseRadius:blocks} 범위에 {ability.frostPulseDamageRatio:percent} 피해와 {ability.frostPulseSlowTicks:seconds} 둔화를 줍니다.</green>");
        }
    }

    private static void addBloomDescription(java.util.List<String> template, TowerType type) {
        template.add("<green>공명 1단계: 받는 피해 -{ability.bloomLevel1DamageReduction:percent}</green>");
        if (unlockedResonanceLevel(type) >= 2) {
            template.add("<green>공명 2단계: 받는 피해 -{ability.bloomLevel2DamageReduction:percent}, {ability.bloomAuraRange:blocks} 안 무블룸 공격속도 +{ability.bloomLevel2AuraAttackSpeedBonus:percent}</green>");
        }
        if (unlockedResonanceLevel(type) >= 3) {
            template.add("<green>공명 3단계: 받는 피해 -{ability.bloomLevel3DamageReduction:percent}, {ability.bloomAuraRange:blocks} 안 무블룸 공격속도 +{ability.bloomLevel3AuraAttackSpeedBonus:percent}</green>");
            template.add("<green>3단계부터 {ability.bloomProtectEveryAttacks:integer}번째 공격마다 {ability.bloomProtectRadius:blocks} 안 무블룸을 공격력의 {ability.bloomProtectHealRatio:percent}만큼 회복하고 {ability.bloomProtectTicks:seconds} 동안 받는 피해 -{ability.bloomProtectDamageReduction:percent}를 줍니다.</green>");
        }
    }

    private static int unlockedResonanceLevel(TowerType type) {
        String id = type.id();
        if (FOCUS_CRYSTAL.id().equals(id) || WAVE_CRYSTAL.id().equals(id) || FROST_CRYSTAL.id().equals(id) || AMPLIFY_CRYSTAL.id().equals(id)) {
            return 1;
        }
        if (FOCUS_PRISM.id().equals(id) || WAVE_PRISM.id().equals(id) || FROST_PRISM.id().equals(id) || AMPLIFY_PRISM.id().equals(id)) {
            return 2;
        }
        return 3;
    }

    private static EntityVisual moobloom(String variant) {
        return MoobloomVisual.builder().variant(variant).build();
    }
}
