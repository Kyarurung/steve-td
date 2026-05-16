package kim.biryeong.semiontd.tower;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ProductionTowerCatalog {
    private static final TowerType VILLAGER_TANKER_T3 = tower(
            "villager_armor_tower_villager_tanker_t3", "개쌉탱커", 395, 375.0, 7.5, 7.0, 12, 75,
            "minecraft:iron_golem",
            List.of("주민 탱커 최종 업그레이드입니다.", "높은 체력과 직업 능력을 통해 최대한의 탱킹을 자랑합니다.")
    );
    private static final TowerType VILLAGER_TANKER_T2 = tower(
            "villager_armor_tower_villager_tanker_t2", "주민 탱커 특화 타워", 185, 247.5, 6.5, 4.8, 14, 57,
            "minecraft:iron_golem",
            List.of("체력과 어그로를 강화한 2차 탱커 타워입니다.", "다음 업그레이드에서 전방 유지력이 크게 증가합니다."),
            List.of(upgrade("villager_tanker_t3", "개쌉탱커", VILLAGER_TANKER_T3, 210))
    );
    private static final TowerType VILLAGER_MELEE_DEALER_T3 = tower(
            "villager_armor_tower_villager_melee_dealer_t3", "개쌉 근딜러", 470, 232.5, 9.0, 6.6, 8, 53,
            "minecraft:villager",
            List.of("주민 근딜 최종 업그레이드입니다.", "짧은 공격 주기로 Emerald 중첩을 빠르게 쌓습니다.")
    );
    private static final TowerType VILLAGER_MELEE_DEALER_T2 = tower(
            "villager_armor_tower_villager_melee_dealer_t2", "주민 근딜 타워", 210, 180.0, 8.0, 4.8, 11, 42,
            "minecraft:villager",
            List.of("공격 주기를 줄인 주민 근딜 2차 타워입니다.", "후속 업그레이드에서 지속 화력이 크게 올라갑니다."),
            List.of(upgrade("villager_melee_dealer_t3", "개쌉 근딜러", VILLAGER_MELEE_DEALER_T3, 260))
    );

    public static final TowerType VILLAGER_CROSSBOW_POST = tower(
            "villager_armor_tower", "주민 탱커 타워", 95, 150.0, 7.0, 4.0, 15, 35,
            "minecraft:villager",
            List.of("높은 체력과 어그로로 라인을 붙잡는 전방형 타워입니다.", "공격할수록 Emerald 중첩으로 피해량이 점진적으로 오릅니다."),
            List.of(
                    upgrade("villager_tanker_t2", "주민 탱커 특화 타워", VILLAGER_TANKER_T2, 90),
                    upgrade("villager_melee_dealer_t2", "주민 근딜 타워", VILLAGER_MELEE_DEALER_T2, 115)
            )
    );

    private static final TowerType VILLAGER_LANE_CLEAR_T3 = tower(
            "villager_bell_mortar_villager_lane_clear_t3", "라클 타워", 550, 155.8, 11.75, 12.25, 13, 19,
            "minecraft:iron_golem",
            List.of("주민 스플래시 최종 업그레이드입니다.", "넓은 폭발 범위로 몰려오는 몬스터를 안정적으로 정리합니다.")
    );
    private static final TowerType VILLAGER_LANE_CLEAR_T2 = tower(
            "villager_bell_mortar_villager_lane_clear_t2", "주민 라인클리어 타워", 240, 110.7, 11.0, 8.75, 16, 10,
            "minecraft:iron_golem",
            List.of("스플래시 범위를 강화한 주민 2차 타워입니다.", "다음 업그레이드에서 광역 피해가 크게 증가합니다."),
            List.of(upgrade("villager_lane_clear_t3", "라클 타워", VILLAGER_LANE_CLEAR_T3, 310))
    );
    private static final TowerType VILLAGER_BALANCE_T3 = tower(
            "villager_bell_mortar_villager_balance_t3", "개쌉 밸런스 타워", 520, 127.1, 12.5, 11.55, 9, 23,
            "minecraft:villager",
            List.of("주민 밸런스 최종 업그레이드입니다.", "사거리, 공격 주기, 스플래시가 고르게 강화됩니다.")
    );
    private static final TowerType VILLAGER_BALANCE_T2 = tower(
            "villager_bell_mortar_villager_balance_t2", "주민 밸런스 타워", 230, 98.4, 11.5, 8.4, 12, 12,
            "minecraft:villager",
            List.of("기본 성능을 고르게 올린 주민 2차 타워입니다.", "후속 업그레이드에서 지속 광역 화력이 올라갑니다."),
            List.of(upgrade("villager_balance_t3", "개쌉 밸런스 타워", VILLAGER_BALANCE_T3, 290))
    );

    public static final TowerType VILLAGER_BELL_MORTAR = tower(
            "villager_bell_mortar", "주민 스플래쉬 타워", 105, 82.0, 10.5, 7.0, 17, 5,
            "minecraft:villager",
            List.of("넓은 스플래시로 몰려오는 몬스터를 정리하는 라인 클리어 타워입니다.", "중첩이 쌓이면 광역 피해가 안정적으로 누적됩니다."),
            List.of(
                    upgrade("villager_lane_clear_t2", "주민 라인클리어 타워", VILLAGER_LANE_CLEAR_T2, 135),
                    upgrade("villager_balance_t2", "주민 밸런스 타워", VILLAGER_BALANCE_T2, 125)
            )
    );

    private static final TowerType VILLAGER_RANGE_T3 = tower(
            "villager_emerald_lens_villager_range_t3", "개쌉 사거리 타워", 600, 105.4, 21.0, 26.0, 10, 16,
            "minecraft:villager",
            List.of("주민 사거리 최종 업그레이드입니다.", "후방 배치에서도 라인 깊숙한 곳까지 개입합니다.")
    );
    private static final TowerType VILLAGER_RANGE_T2 = tower(
            "villager_emerald_lens_villager_range_t2", "사거리 타워", 260, 78.2, 18.0, 17.55, 13, 0,
            "minecraft:villager",
            List.of("사거리를 크게 늘린 주민 저격 2차 타워입니다.", "다음 업그레이드에서 전장 장악력이 더 올라갑니다."),
            List.of(upgrade("villager_range_t3", "개쌉 사거리 타워", VILLAGER_RANGE_T3, 340))
    );
    private static final TowerType VILLAGER_SNIPER_T3 = tower(
            "villager_emerald_lens_villager_sniper_t3", "개쌉 저거리 타워", 585, 115.6, 19.0, 31.85, 11, 14,
            "minecraft:iron_golem",
            List.of("주민 저격 최종 업그레이드입니다.", "고체력 타겟을 처리하는 단일 피해가 크게 증가합니다.")
    );
    private static final TowerType VILLAGER_SNIPER_T2 = tower(
            "villager_emerald_lens_villager_sniper_t2", "저격 타워", 255, 85.0, 17.0, 20.15, 14, 1,
            "minecraft:iron_golem",
            List.of("단일 피해를 강화한 주민 저격 2차 타워입니다.", "직업 능력 중첩으로 후반 화력이 강화됩니다."),
            List.of(upgrade("villager_sniper_t3", "개쌉 저거리 타워", VILLAGER_SNIPER_T3, 330))
    );

    public static final TowerType VILLAGER_EMERALD_LENS = tower(
            "villager_emerald_lens", "주민 저격 타워", 110, 68.0, 15.0, 13.0, 15, -10,
            "minecraft:villager",
            List.of("긴 사거리와 높은 단일 피해로 후방에서 핵심 타겟을 압박합니다.", "처치 시 중첩을 얻어 오브젝트 처리 능력이 강화됩니다."),
            List.of(
                    upgrade("villager_range_t2", "사거리 타워", VILLAGER_RANGE_T2, 150),
                    upgrade("villager_sniper_t2", "저격 타워", VILLAGER_SNIPER_T2, 145)
            )
    );

    private static final TowerType UNDEAD_LANE_CLEAR_T3 = tower(
            "undead_bone_spitter_undead_lane_clear_t3", "개씹 스플타워", 420, 136.8, 10.75, 10.5, 9, 14,
            "minecraft:skeleton",
            List.of("언데드 스플래시 최종 업그레이드입니다.", "Decay 폭발과 넓은 스플래시로 물량을 지웁니다.")
    );
    private static final TowerType UNDEAD_LANE_CLEAR_T2 = tower(
            "undead_bone_spitter_undead_lane_clear_t2", "스플 타워", 195, 97.2, 10.0, 7.5, 11, 5,
            "minecraft:skeleton",
            List.of("스플래시를 강화한 언데드 2차 타워입니다.", "후속 업그레이드에서 라인 클리어 성능이 크게 증가합니다."),
            List.of(upgrade("undead_lane_clear_t3", "개씹 스플타워", UNDEAD_LANE_CLEAR_T3, 225))
    );
    private static final TowerType UNDEAD_BALANCE_T3 = tower(
            "undead_bone_spitter_undead_balance_t3", "개씹 밸런스 타워", 495, 122.4, 13.5, 14.7, 9, 24,
            "minecraft:skeleton",
            List.of("언데드 밸런스 최종 업그레이드입니다.", "단일 피해와 처치 폭발을 함께 강화합니다.")
    );
    private static final TowerType UNDEAD_BALANCE_T2 = tower(
            "undead_bone_spitter_undead_balance_t2", "밸런스 타워", 220, 90.0, 11.5, 9.3, 11, 11,
            "minecraft:skeleton",
            List.of("사거리와 단일 피해를 강화한 언데드 2차 타워입니다.", "처치 시 Decay 폭발로 추가 피해를 냅니다."),
            List.of(upgrade("undead_balance_t3", "개씹 밸런스 타워", UNDEAD_BALANCE_T3, 275))
    );

    public static final TowerType UNDEAD_BONE_SPITTER = tower(
            "undead_bone_spitter", "언데드 폭발 타워", 100, 72.0, 9.5, 6.0, 12, 0,
            "minecraft:skeleton",
            List.of("빠른 공격과 폭발 스플래시로 물량을 갉아먹는 타워입니다.", "Decay 특성으로 처치 주변에 추가 피해를 남깁니다."),
            List.of(
                    upgrade("undead_lane_clear_t2", "스플 타워", UNDEAD_LANE_CLEAR_T2, 95),
                    upgrade("undead_balance_t2", "밸런스 타워", UNDEAD_BALANCE_T2, 120)
            )
    );

    private static final TowerType UNDEAD_TANKER_T3 = tower(
            "undead_grave_bombard_undead_tanker_t3", "극탱 타워", 560, 412.5, 7.0, 7.0, 14, 78,
            "minecraft:zombie",
            List.of("언데드 탱커 최종 업그레이드입니다.", "매우 높은 체력과 어그로로 전선을 고정합니다.")
    );
    private static final TowerType UNDEAD_TANKER_T2 = tower(
            "undead_grave_bombard_undead_tanker_t2", "탱 타워", 240, 272.25, 6.0, 4.8, 16, 60,
            "minecraft:zombie",
            List.of("체력과 어그로를 강화한 언데드 2차 타워입니다.", "후속 업그레이드에서 방어 성능이 크게 올라갑니다."),
            List.of(upgrade("undead_tanker_t3", "극탱 타워", UNDEAD_TANKER_T3, 320))
    );
    private static final TowerType UNDEAD_MELEE_T3 = tower(
            "undead_grave_bombard_undead_melee_t3", "근딜 타워", 580, 305.25, 8.25, 7.4, 13, 58,
            "minecraft:zombie",
            List.of("언데드 근딜 최종 업그레이드입니다.", "처치 폭발을 크게 강화해 전방에서 연쇄 피해를 냅니다.")
    );
    private static final TowerType UNDEAD_MELEE_T2 = tower(
            "undead_grave_bombard_undead_melee_t2", "근딜 타워", 245, 214.5, 7.25, 5.2, 15, 46,
            "minecraft:zombie",
            List.of("전방 피해를 강화한 언데드 2차 타워입니다.", "처치 시 Decay 폭발이 더 자주 위협을 만듭니다."),
            List.of(upgrade("undead_melee_t3", "근딜 타워", UNDEAD_MELEE_T3, 335))
    );

    public static final TowerType UNDEAD_GRAVE_BOMBARD = tower(
            "undead_grave_bombard", "언데드 방어 타워", 100, 165.0, 6.5, 4.0, 17, 38,
            "minecraft:zombie",
            List.of("튼튼하고 어그로가 높아 최전방을 버티는 방어형 타워입니다.", "근접 라인에서 몬스터를 붙잡고 처치 폭발로 보조 피해를 냅니다."),
            List.of(
                    upgrade("undead_tanker_t2", "탱 타워", UNDEAD_TANKER_T2, 140),
                    upgrade("undead_melee_t2", "근딜 타워", UNDEAD_MELEE_T2, 145)
            )
    );

    private static final TowerType UNDEAD_SNIPER_T3 = tower(
            "undead_soul_reaper_undead_sniper_t3", "강화 저격 타워", 640, 132.6, 17.5, 31.85, 12, 19,
            "minecraft:wither_skeleton",
            List.of("언데드 저격 최종 업그레이드입니다.", "고체력 타겟 처치와 Decay 처치 폭발에 특화됩니다.")
    );
    private static final TowerType UNDEAD_SNIPER_T2 = tower(
            "undead_soul_reaper_undead_sniper_t2", "저격 타워", 280, 97.5, 15.5, 20.15, 14, 6,
            "minecraft:wither_skeleton",
            List.of("단일 피해를 강화한 언데드 저격 2차 타워입니다.", "처치 기반 Decay 중첩으로 후반 화력이 올라갑니다."),
            List.of(upgrade("undead_sniper_t3", "강화 저격 타워", UNDEAD_SNIPER_T3, 360))
    );
    private static final TowerType UNDEAD_RANGE_T3 = tower(
            "undead_soul_reaper_undead_range_t3", "강화 사거리 타워", 615, 120.9, 19.5, 26.0, 11, 21,
            "minecraft:wither_skeleton",
            List.of("언데드 사거리 최종 업그레이드입니다.", "긴 사거리와 처치 한 상대를 폭발시켜 후방에서 라인을 제어합니다.")
    );
    private static final TowerType UNDEAD_RANGE_T2 = tower(
            "undead_soul_reaper_undead_range_t2", "사거리 타워", 270, 89.7, 16.5, 17.55, 14, 5,
            "minecraft:wither_skeleton",
            List.of("사거리를 늘린 언데드 저격 2차 타워입니다.", "강력한 적을 먼저 공격 가능합니다."),
            List.of(upgrade("undead_range_t3", "강화 사거리 타워", UNDEAD_RANGE_T3, 345))
    );

    public static final TowerType UNDEAD_SOUL_REAPER = tower(
            "undead_soul_reaper", "언데드 저격 타워", 115, 78.0, 13.5, 13.0, 16, -5,
            "minecraft:wither_skeleton",
            List.of("높은 단일 피해를 통해 안티 탱커 역할을 하는 타워입니다.", "사거리 분기와 저격 분기 모두 후반 대응력이 높습니다."),
            List.of(
                    upgrade("undead_sniper_t2", "저격 타워", UNDEAD_SNIPER_T2, 165),
                    upgrade("undead_range_t2", "사거리 타워", UNDEAD_RANGE_T2, 155)
            )
    );

    private static final TowerType BEAST_BALANCE_T3 = tower(
            "beast_wolf_den_beast_balance_t3", "강화 공속 타워", 400, 114.7, 10.5, 8.25, 6, 23,
            "minecraft:wolf",
            List.of("동물 공속 최종 업그레이드입니다.", "직업 능력 중첩과 짧은 공격 주기로 지속 화력을 냅니다.")
    );
    private static final TowerType BEAST_BALANCE_T2 = tower(
            "beast_wolf_den_beast_balance_t2", "공속 타워", 185, 88.8, 9.5, 6.0, 7, 12,
            "minecraft:wolf",
            List.of("공격 주기를 크게 줄인 동물 2차 타워입니다.", "후속 업그레이드에서 직업 능력 중첩이 강화됩니다."),
            List.of(upgrade("beast_balance_t3", "강화 공속 타워", BEAST_BALANCE_T3, 215))
    );
    private static final TowerType BEAST_SPLASH_T3 = tower(
            "beast_wolf_den_white_fang_den", "강화 라클 타워", 440, 140.6, 9.75, 8.75, 8, 19,
            "minecraft:wolf",
            List.of("동물 라인 클리어 최종 업그레이드입니다.", "넓어진 스플래시와 직업 능력 중첩으로 물량을 정리합니다.")
    );
    private static final TowerType BEAST_SPLASH_T2 = tower(
            "beast_wolf_den_beast_splash_t2", "라클 타워", 200, 99.9, 9.0, 6.25, 9, 10,
            "minecraft:wolf",
            List.of("스플래시를 강화한 동물 2차 타워입니다.", "초중반 중심 유닛입니다."),
            List.of(upgrade("white_fang_den", "강화 라클 타워", BEAST_SPLASH_T3, 240))
    );

    public static final TowerType BEAST_WOLF_DEN = tower(
            "beast_wolf_den", "댕댕이 타워(원거리)", 95, 74.0, 8.5, 5.0, 10, 5,
            "minecraft:wolf",
            List.of("빠른 공격으로 직업 중첩을 쌓아 공속을 끌어올리는 원거리 타워입니다.", "지속 싸움에 강력합니다."),
            List.of(
                    upgrade("beast_balance_t2", "공속 타워", BEAST_BALANCE_T2, 90),
                    upgrade("beast_splash_t2", "라클 타워", BEAST_SPLASH_T2, 105)
            )
    );

    private static final TowerType BEAST_MELEE_DPS_T3 = tower(
            "beast_boar_crasher_beast_melee_dps_t3", "강화 근딜 타워", 570, 271.25, 8.5, 8.25, 8, 60,
            "minecraft:pig",
            List.of("돼지 근딜 최종 업그레이드입니다.", "짧은 공격 주기와 직업 중첩으로 전방 화력을 냅니다.")
    );
    private static final TowerType BEAST_MELEE_DPS_T2 = tower(
            "beast_boar_crasher_beast_melee_dps_t2", "근딜 타워", 245, 210.0, 7.5, 6.0, 11, 49,
            "minecraft:pig",
            List.of("전방 피해를 강화한 돼지 2차 타워입니다.", "후속 업그레이드에서 근접 지속 피해가 크게 올라갑니다."),
            List.of(upgrade("beast_melee_dps_t3", "강화 근딜 타워", BEAST_MELEE_DPS_T3, 325))
    );
    private static final TowerType BEAST_TANKER_T3 = tower(
            "beast_boar_crasher_beast_tanker_t3", "강화 탱 타워", 545, 437.5, 7.0, 8.75, 12, 82,
            "minecraft:pig",
            List.of("돼지 탱커 최종 업그레이드입니다.", "높은 체력과 어그로로 라인을 단단하게 고정합니다.")
    );
    private static final TowerType BEAST_TANKER_T2 = tower(
            "beast_boar_crasher_beast_tanker_t2", "탱 타워", 235, 288.75, 6.0, 6.0, 14, 64,
            "minecraft:pig",
            List.of("체력과 어그로를 강화한 돼지 2차 타워입니다.", "후속 업그레이드에서 전방 유지력이 크게 증가합니다."),
            List.of(upgrade("beast_tanker_t3", "강화 탱 타워", BEAST_TANKER_T3, 310))
    );

    public static final TowerType BEAST_BOAR_CRASHER = tower(
            "beast_boar_crasher", "돼지 타워", 105, 175.0, 6.5, 5.0, 15, 42,
            "minecraft:pig",
            List.of("높은 체력과 어그로로 전선을 세우는 동물 진영 방어 타워입니다.", "직업 중첩으로 피해와 공속이 함께 성장합니다."),
            List.of(
                    upgrade("beast_melee_dps_t2", "근딜 타워", BEAST_MELEE_DPS_T2, 140),
                    upgrade("beast_tanker_t2", "탱 타워", BEAST_TANKER_T2, 130)
            )
    );

    private static final TowerType BEAST_RANGE_T3 = tower(
            "beast_hawk_roost_beast_range_t3", "천둥 매 성소", 565, 99.2, 22.0, 24.0, 10, 14,
            "minecraft:parrot",
            List.of("앵무새 사거리 최종 업그레이드입니다.", "긴 사거리로 라인 전체에 지속 압박을 겁니다.")
    );
    private static final TowerType BEAST_RANGE_T2 = tower(
            "beast_hawk_roost_beast_range_t2", "사거리 타워", 250, 73.6, 19.0, 16.2, 12, -2,
            "minecraft:parrot",
            List.of("사거리를 강화한 앵무새 2차 타워입니다.", "명중과 처치 모두 직업 중첩을 유지하는 데 도움을 줍니다."),
            List.of(upgrade("beast_range_t3", "천둥 매 성소", BEAST_RANGE_T3, 315))
    );
    private static final TowerType BEAST_DIVE_HUNTER_T3 = tower(
            "animal_dps_tower_t2", "강화 DPS 타워", 540, 99.2, 18.0, 19.8, 8, 6,
            "minecraft:parrot",
            List.of("동물 직업 DPS 최종 업그레이드입니다.", "빠른 공격과 직업 중첩으로 우선 타겟을 압박합니다.")
    );
    private static final TowerType BEAST_DIVE_HUNTER_T2 = tower(
            "animal_dps_tower_t3", "DPS 타워", 240, 76.8, 17.0, 14.4, 10, -5,
            "minecraft:parrot",
            List.of("공격 주기를 줄인 동물 직업 2차 타워입니다.", "후속 업그레이드에서 빠른 단일 압박 능력이 강화됩니다."),
            List.of(upgrade("animal_dps_tower_t3", "강화 DPS 타워", BEAST_DIVE_HUNTER_T3, 300))
    );

    public static final TowerType BEAST_HAWK_ROOST = tower(
            "animal_snipe_tower_t1", "앵무새 저격 타워", 115, 64.0, 16.0, 12.0, 14, -12,
            "minecraft:parrot",
            List.of("매우 긴 사거리로 라인 전체를 견제하는 저격형 타워입니다.", "명중과 처치 모두 직업 중첩을 올려 후반 화력이 커집니다."),
            List.of(
                    upgrade("beast_range_t2", "사거리 타워", BEAST_RANGE_T2, 135),
                    upgrade("animal_dps_tower_t2", "DPS 타워", BEAST_DIVE_HUNTER_T2, 125)
            )
    );

    private static final ProductionTowerBehavior VILLAGER_CROSSBOW_POST_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 0.75, 0.25, 10, 0.035, 0.0, true, false, 0.0, 0.0);
    private static final ProductionTowerBehavior VILLAGER_TANKER_T2_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 1.3875, 0.33, 11, 0.043, 0.006, true, false, 0.25, 0.08);
    private static final ProductionTowerBehavior VILLAGER_TANKER_T3_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 2.1375, 0.43, 13, 0.053, 0.012, true, false, 0.6, 0.18);
    private static final ProductionTowerBehavior VILLAGER_MELEE_DEALER_T2_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 1.025, 0.33, 12, 0.045, 0.012, true, false, 0.2, 0.06);
    private static final ProductionTowerBehavior VILLAGER_MELEE_DEALER_T3_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 1.5125, 0.41, 14, 0.055, 0.02, true, false, 0.45, 0.14);

    private static final ProductionTowerBehavior VILLAGER_BELL_MORTAR_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 2.85, 0.58, 8, 0.035, 0.0, true, false, 0.0, 0.0);
    private static final ProductionTowerBehavior VILLAGER_LANE_CLEAR_T2_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 3.9125, 0.7, 9, 0.045, 0.002, true, false, 0.35, 0.1);
    private static final ProductionTowerBehavior VILLAGER_LANE_CLEAR_T3_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 5.8375, 0.82, 11, 0.055, 0.006, true, false, 0.8, 0.22);
    private static final ProductionTowerBehavior VILLAGER_BALANCE_T2_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 3.335, 0.66, 10, 0.045, 0.012, true, false, 0.2, 0.06);
    private static final ProductionTowerBehavior VILLAGER_BALANCE_T3_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 4.3475, 0.74, 12, 0.055, 0.02, true, false, 0.45, 0.14);

    private static final ProductionTowerBehavior VILLAGER_EMERALD_LENS_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 0.35, 0.18, 7, 0.095, 0.0, false, true, 0.0, 0.0);
    private static final ProductionTowerBehavior VILLAGER_RANGE_T2_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 0.5, 0.24, 8, 0.107, 0.004, true, true, 0.25, 0.06);
    private static final ProductionTowerBehavior VILLAGER_RANGE_T3_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 0.8875, 0.32, 10, 0.121, 0.01, true, true, 0.55, 0.14);
    private static final ProductionTowerBehavior VILLAGER_SNIPER_T2_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 0.415, 0.22, 9, 0.113, 0.002, false, true, 0.15, 0.04);
    private static final ProductionTowerBehavior VILLAGER_SNIPER_T3_BEHAVIOR =
            behavior(TowerFaction.VILLAGER, "Emerald", 0.6675, 0.28, 11, 0.13, 0.006, false, true, 0.35, 0.1);

    private static final ProductionTowerBehavior UNDEAD_BONE_SPITTER_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 2.65, 0.55, 5, 0.035, 0.0, true, false, 1.5, 0.35);
    private static final ProductionTowerBehavior UNDEAD_LANE_CLEAR_T2_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 3.6625, 0.67, 6, 0.045, 0.002, true, false, 1.85, 0.45);
    private static final ProductionTowerBehavior UNDEAD_LANE_CLEAR_T3_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 5.4875, 0.79, 8, 0.055, 0.006, true, false, 2.3, 0.57);
    private static final ProductionTowerBehavior UNDEAD_BALANCE_T2_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 2.485, 0.59, 7, 0.053, 0.002, true, true, 1.65, 0.39);
    private static final ProductionTowerBehavior UNDEAD_BALANCE_T3_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 3.0825, 0.65, 9, 0.07, 0.006, true, true, 1.85, 0.45);

    private static final ProductionTowerBehavior UNDEAD_GRAVE_BOMBARD_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 0.85, 0.28, 7, 0.025, 0.0, true, false, 1.0, 0.25);
    private static final ProductionTowerBehavior UNDEAD_TANKER_T2_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 1.5125, 0.36, 8, 0.033, 0.006, true, false, 1.25, 0.33);
    private static final ProductionTowerBehavior UNDEAD_TANKER_T3_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 2.3025, 0.46, 10, 0.043, 0.012, true, false, 1.6, 0.43);
    private static final ProductionTowerBehavior UNDEAD_MELEE_T2_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 1.27, 0.38, 8, 0.037, 0.002, true, true, 2.0, 0.5);
    private static final ProductionTowerBehavior UNDEAD_MELEE_T3_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 2.0175, 0.5, 10, 0.049, 0.006, true, true, 3.0, 0.7);

    private static final ProductionTowerBehavior UNDEAD_SOUL_REAPER_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 0.45, 0.22, 6, 0.08, 0.0, false, true, 1.0, 0.25);
    private static final ProductionTowerBehavior UNDEAD_SNIPER_T2_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 0.505, 0.26, 8, 0.098, 0.002, false, true, 1.15, 0.29);
    private static final ProductionTowerBehavior UNDEAD_SNIPER_T3_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 0.7725, 0.32, 10, 0.115, 0.006, false, true, 1.35, 0.35);
    private static final ProductionTowerBehavior UNDEAD_RANGE_T2_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 0.6, 0.28, 7, 0.092, 0.004, true, true, 1.25, 0.31);
    private static final ProductionTowerBehavior UNDEAD_RANGE_T3_BEHAVIOR =
            behavior(TowerFaction.UNDEAD, "Decay", 1.0125, 0.36, 9, 0.106, 0.01, true, true, 1.55, 0.39);

    private static final ProductionTowerBehavior BEAST_WOLF_DEN_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 2.25, 0.5, 7, 0.0, 0.035, true, false, 0.0, 0.0);
    private static final ProductionTowerBehavior BEAST_BALANCE_T2_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 2.675, 0.58, 9, 0.01, 0.047, true, false, 0.2, 0.06);
    private static final ProductionTowerBehavior BEAST_BALANCE_T3_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 3.5375, 0.66, 11, 0.02, 0.055, true, false, 0.45, 0.14);
    private static final ProductionTowerBehavior BEAST_SPLASH_T2_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 3.1625, 0.62, 8, 0.01, 0.037, true, false, 0.35, 0.1);
    private static final ProductionTowerBehavior BEAST_SPLASH_T3_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 4.7875, 0.74, 10, 0.02, 0.041, true, false, 0.8, 0.22);

    private static final ProductionTowerBehavior BEAST_BOAR_CRASHER_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 0.9, 0.3, 8, 0.018, 0.02, true, false, 0.0, 0.0);
    private static final ProductionTowerBehavior BEAST_MELEE_DPS_T2_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 1.19, 0.38, 10, 0.028, 0.032, true, false, 0.2, 0.06);
    private static final ProductionTowerBehavior BEAST_MELEE_DPS_T3_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 1.715, 0.46, 12, 0.038, 0.04, true, false, 0.45, 0.14);
    private static final ProductionTowerBehavior BEAST_TANKER_T2_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 1.575, 0.38, 9, 0.026, 0.026, true, false, 0.25, 0.08);
    private static final ProductionTowerBehavior BEAST_TANKER_T3_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 2.385, 0.48, 11, 0.036, 0.032, true, false, 0.6, 0.18);

    private static final ProductionTowerBehavior BEAST_HAWK_ROOST_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 0.45, 0.2, 7, 0.03, 0.035, true, true, 0.0, 0.0);
    private static final ProductionTowerBehavior BEAST_RANGE_T2_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 0.6, 0.26, 8, 0.042, 0.039, true, true, 0.25, 0.06);
    private static final ProductionTowerBehavior BEAST_RANGE_T3_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 1.0125, 0.34, 10, 0.056, 0.045, true, true, 0.55, 0.14);
    private static final ProductionTowerBehavior BEAST_DIVE_HUNTER_T2_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 0.695, 0.28, 9, 0.04, 0.047, true, true, 0.2, 0.06);
    private static final ProductionTowerBehavior BEAST_DIVE_HUNTER_T3_BEHAVIOR =
            behavior(TowerFaction.BEAST, "Rage", 1.1075, 0.36, 11, 0.05, 0.055, true, true, 0.45, 0.14);

    private static final Map<String, CatalogEntry> ENTRIES = new LinkedHashMap<>();

    static {
        registerLine(
                VILLAGER_CROSSBOW_POST,
                VILLAGER_CROSSBOW_POST_BEHAVIOR,
                branch(VILLAGER_TANKER_T2, VILLAGER_TANKER_T2_BEHAVIOR, VILLAGER_TANKER_T3, VILLAGER_TANKER_T3_BEHAVIOR),
                branch(VILLAGER_MELEE_DEALER_T2, VILLAGER_MELEE_DEALER_T2_BEHAVIOR, VILLAGER_MELEE_DEALER_T3, VILLAGER_MELEE_DEALER_T3_BEHAVIOR)
        );
        registerLine(
                VILLAGER_BELL_MORTAR,
                VILLAGER_BELL_MORTAR_BEHAVIOR,
                branch(VILLAGER_LANE_CLEAR_T2, VILLAGER_LANE_CLEAR_T2_BEHAVIOR, VILLAGER_LANE_CLEAR_T3, VILLAGER_LANE_CLEAR_T3_BEHAVIOR),
                branch(VILLAGER_BALANCE_T2, VILLAGER_BALANCE_T2_BEHAVIOR, VILLAGER_BALANCE_T3, VILLAGER_BALANCE_T3_BEHAVIOR)
        );
        registerLine(
                VILLAGER_EMERALD_LENS,
                VILLAGER_EMERALD_LENS_BEHAVIOR,
                branch(VILLAGER_RANGE_T2, VILLAGER_RANGE_T2_BEHAVIOR, VILLAGER_RANGE_T3, VILLAGER_RANGE_T3_BEHAVIOR),
                branch(VILLAGER_SNIPER_T2, VILLAGER_SNIPER_T2_BEHAVIOR, VILLAGER_SNIPER_T3, VILLAGER_SNIPER_T3_BEHAVIOR)
        );
        registerLine(
                UNDEAD_BONE_SPITTER,
                UNDEAD_BONE_SPITTER_BEHAVIOR,
                branch(UNDEAD_LANE_CLEAR_T2, UNDEAD_LANE_CLEAR_T2_BEHAVIOR, UNDEAD_LANE_CLEAR_T3, UNDEAD_LANE_CLEAR_T3_BEHAVIOR),
                branch(UNDEAD_BALANCE_T2, UNDEAD_BALANCE_T2_BEHAVIOR, UNDEAD_BALANCE_T3, UNDEAD_BALANCE_T3_BEHAVIOR)
        );
        registerLine(
                UNDEAD_GRAVE_BOMBARD,
                UNDEAD_GRAVE_BOMBARD_BEHAVIOR,
                branch(UNDEAD_TANKER_T2, UNDEAD_TANKER_T2_BEHAVIOR, UNDEAD_TANKER_T3, UNDEAD_TANKER_T3_BEHAVIOR),
                branch(UNDEAD_MELEE_T2, UNDEAD_MELEE_T2_BEHAVIOR, UNDEAD_MELEE_T3, UNDEAD_MELEE_T3_BEHAVIOR)
        );
        registerLine(
                UNDEAD_SOUL_REAPER,
                UNDEAD_SOUL_REAPER_BEHAVIOR,
                branch(UNDEAD_SNIPER_T2, UNDEAD_SNIPER_T2_BEHAVIOR, UNDEAD_SNIPER_T3, UNDEAD_SNIPER_T3_BEHAVIOR),
                branch(UNDEAD_RANGE_T2, UNDEAD_RANGE_T2_BEHAVIOR, UNDEAD_RANGE_T3, UNDEAD_RANGE_T3_BEHAVIOR)
        );
        registerLine(
                BEAST_WOLF_DEN,
                BEAST_WOLF_DEN_BEHAVIOR,
                branch(BEAST_BALANCE_T2, BEAST_BALANCE_T2_BEHAVIOR, BEAST_BALANCE_T3, BEAST_BALANCE_T3_BEHAVIOR),
                branch(BEAST_SPLASH_T2, BEAST_SPLASH_T2_BEHAVIOR, BEAST_SPLASH_T3, BEAST_SPLASH_T3_BEHAVIOR)
        );
        registerLine(
                BEAST_BOAR_CRASHER,
                BEAST_BOAR_CRASHER_BEHAVIOR,
                branch(BEAST_MELEE_DPS_T2, BEAST_MELEE_DPS_T2_BEHAVIOR, BEAST_MELEE_DPS_T3, BEAST_MELEE_DPS_T3_BEHAVIOR),
                branch(BEAST_TANKER_T2, BEAST_TANKER_T2_BEHAVIOR, BEAST_TANKER_T3, BEAST_TANKER_T3_BEHAVIOR)
        );
        registerLine(
                BEAST_HAWK_ROOST,
                BEAST_HAWK_ROOST_BEHAVIOR,
                branch(BEAST_RANGE_T2, BEAST_RANGE_T2_BEHAVIOR, BEAST_RANGE_T3, BEAST_RANGE_T3_BEHAVIOR),
                branch(BEAST_DIVE_HUNTER_T2, BEAST_DIVE_HUNTER_T2_BEHAVIOR, BEAST_DIVE_HUNTER_T3, BEAST_DIVE_HUNTER_T3_BEHAVIOR)
        );
    }

    private ProductionTowerCatalog() {
    }

    public static Optional<CatalogEntry> find(String towerId) {
        return Optional.ofNullable(ENTRIES.get(towerId));
    }

    public static Collection<CatalogEntry> all() {
        return List.copyOf(ENTRIES.values());
    }

    public static List<CatalogEntry> forFaction(TowerFaction faction) {
        return ENTRIES.values().stream()
                .filter(entry -> entry.behavior().faction() == faction)
                .toList();
    }

    public static Optional<ProductionTowerBehavior> behavior(TowerType type) {
        return type == null ? Optional.empty() : find(type.id()).map(CatalogEntry::behavior);
    }

    public static Optional<CatalogEntry> entry(TowerType type) {
        return type == null ? Optional.empty() : find(type.id());
    }

    private static TowerType tower(
            String id,
            String displayName,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority,
            String entityTypeId,
            List<String> description
    ) {
        return tower(id, displayName, mineralCost, maxHealth, range, damage, attackIntervalTicks, aggroPriority, entityTypeId, description, List.of());
    }

    private static TowerType tower(
            String id,
            String displayName,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority,
            String entityTypeId,
            List<String> description,
            List<TowerUpgradeOption> upgrades
    ) {
        return new TowerType(
                id,
                displayName,
                TowerCategory.DIRECT,
                mineralCost,
                maxHealth,
                range,
                damage,
                attackIntervalTicks,
                aggroPriority,
                description,
                entityTypeId,
                null,
                upgrades
        );
    }

    private static ProductionTowerBehavior behavior(
            TowerFaction faction,
            String mechanicName,
            double splashRadius,
            double splashDamageMultiplier,
            int maxStacks,
            double damagePerStack,
            double attackSpeedPerStack,
            boolean stackOnHit,
            boolean stackOnKill,
            double killSplashRadius,
            double killSplashDamageMultiplier
    ) {
        return new ProductionTowerBehavior(
                faction,
                mechanicName,
                splashRadius,
                splashDamageMultiplier,
                maxStacks,
                damagePerStack,
                attackSpeedPerStack,
                stackOnHit,
                stackOnKill,
                killSplashRadius,
                killSplashDamageMultiplier
        );
    }

    private static TowerUpgradeOption upgrade(String id, String displayName, TowerType target, long mineralCost) {
        return new TowerUpgradeOption(id, displayName, target, mineralCost);
    }

    private static UpgradeBranch branch(
            TowerType tierTwo,
            ProductionTowerBehavior tierTwoBehavior,
            TowerType ultimate,
            ProductionTowerBehavior ultimateBehavior
    ) {
        return new UpgradeBranch(tierTwo, tierTwoBehavior, ultimate, ultimateBehavior);
    }

    private static void registerLine(
            TowerType starter,
            ProductionTowerBehavior starterBehavior,
            UpgradeBranch left,
            UpgradeBranch right
    ) {
        register(starter, starterBehavior, 1);
        register(left.tierTwo(), left.tierTwoBehavior(), 2);
        register(left.ultimate(), left.ultimateBehavior(), 3);
        register(right.tierTwo(), right.tierTwoBehavior(), 2);
        register(right.ultimate(), right.ultimateBehavior(), 3);
    }

    private static void register(TowerType type, ProductionTowerBehavior behavior, int tier) {
        ENTRIES.put(type.id(), new CatalogEntry(type, behavior, tier));
    }

    private record UpgradeBranch(
            TowerType tierTwo,
            ProductionTowerBehavior tierTwoBehavior,
            TowerType ultimate,
            ProductionTowerBehavior ultimateBehavior
    ) {
    }

    public record CatalogEntry(TowerType type, ProductionTowerBehavior behavior, int tier) {
        public boolean starter() {
            return tier == 1;
        }
    }
}
