package kim.biryeong.semiontd.tower.warlock;

import static kim.biryeong.semiontd.tower.catalog.ProductionTowerDefinitions.tower;
import static kim.biryeong.semiontd.tower.description.TowerDescriptionTemplate.*;
import static kim.biryeong.semiontd.tower.warlock.WarlockConfig.Ability.*;
import static kim.biryeong.semiontd.tower.warlock.WarlockFormatting.warlockText;
import static kim.biryeong.semiontd.util.EntityTypeUtil.byId;

import kim.biryeong.semiontd.entity.visual.FrogVisual;
import kim.biryeong.semiontd.entity.visual.SheepVisual;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.description.TowerDescriptionRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;

public final class WarlockTowers {
    public static final String CONFIG_ID = "warlock_global";
    private static final String SCALING_EFFICIENCY_DESCRIPTION = "<gray>능력치는 높아질수록 증가 효율이 감소합니다.</gray>";

    private WarlockTowers() {
    }

    public static final TowerType BASE_WARLOCK_TOWER = tower(
            "base_warlock_tower",
            "흑마법사 타워",
            0,
            80,
            4,
            5,
            20,
            30,
            byId(EntityType.WITCH),
            baseWarlockDescription()
    );

    public static final TowerType RANGED_WARLOCK_TOWER = tower(
            "ranged_warlock_tower",
            "원거리 흑마법사 타워",
            0,
            100,
            7,
            8,
            20,
            20,
            byId(EntityType.WITCH),
            rangedWarlockDescription()
    );

    public static final TowerType MELEE_WARLOCK_TOWER = tower(
            "melee_warlock_tower",
            "근거리 흑마법사 타워",
            0,
            120,
            3,
            7,
            20,
            80,
            byId(EntityType.WITCH),
            meleeWarlockDescription()
    );

    public static final TowerType T1_SLAVE = tower(
            "t1_slave",
            "희생\"양\"",
            50,
            75,
            2,
            4,
            20,
            30,
            SheepVisual.builder().color(DyeColor.RED).build(),
            List.of(
                    "<gray>" + warlockText("흑마법사") + "가 데려온 양입니다.</gray>"
            )
    );

    public static final TowerType T2_SLAVE = tower(
            "t2_slave",
            "희생\"양\"",
            85,
            120,
            2,
            8,
            20,
            50,
            SheepVisual.builder().color(DyeColor.PINK).build(),
            List.of(
                    "<gray>" + warlockText("흑마법사") + "가 데려온 희귀한 양입니다.</gray>",
                    "<gray>사망 시 주위 20 블록 내 적이 받는 " + attackDamageText("피해") + "를 일정 시간 동안 " + attackDamageText("5%") + " 증가시킵니다.</gray>"
            )
    );

    public static final TowerType T3_SLAVE = tower(
            "t3_slave",
            "희생\"양\"",
            135,
            185,
            2,
            12,
            20,
            70,
            SheepVisual.builder().color(DyeColor.WHITE).build(),
            List.of(
                    "<gray>" + warlockText("흑마법사") + "가 데려온 양입니다. 희귀했던 색을 잃어 화가 났습니다.</gray>",
                    "<gray>사망 시 주위 20 블록 내 적이 받는 " + attackDamageText("피해") + "를 일정 시간 동안 " + attackDamageText("10%") + " 증가시킵니다.</gray>"
            )
    );

    public static final TowerType T1_RANGED_SLAVE = tower(
            "t1_ranged_slave",
            "애완 박쥐",
            55,
            70,
            7,
            5,
            17,
            20,
            byId(EntityType.BAT),
            List.of(
                    "<gray>" + warlockText("흑마법사") + "가 키우는 박쥐입니다. 애완동물도 얄짤없네요.</gray>"
            )
    );

    public static final TowerType T2_RANGED_SLAVE = tower(
            "t2_ranged_slave",
            "애완 개구리",
            90,
            120,
            7,
            8,
            15,
            15,
            FrogVisual.builder().variant(FrogVariants.COLD).build(),
            List.of(
                    "<gray>" + warlockText("흑마법사") + "가 키우는 개구리입니다.</gray>",
                    "<gray>사망 시 주위 20 블록 내 적의 " + attackSpeedText("공격 속도") + "를 일정 시간 동안 " + attackSpeedText("5%") + " 감소시킵니다.</gray>"
            )
    );

    public static final TowerType T3_RANGED_SLAVE = tower(
            "t3_ranged_slave",
            "애완 개구리",
            140,
            185,
            7,
            12,
            13,
            15,
            FrogVisual.builder().variant(FrogVariants.WARM).build(),
            List.of(
                    "<gray>" + warlockText("흑마법사") + "가 키우는 개구리입니다.</gray>",
                    "<gray>사망 시 주위 20 블록 내 적의 " + attackSpeedText("공격 속도") + "를 일정 시간 동안 " + attackSpeedText("10%") + " 감소시킵니다.</gray>"
            )
    );

    static {
        TowerDescriptionRegistry.registerTemplate(BASE_WARLOCK_TOWER, baseWarlockDescription());
        TowerDescriptionRegistry.registerTemplate(RANGED_WARLOCK_TOWER, rangedWarlockDescription());
        TowerDescriptionRegistry.registerTemplate(MELEE_WARLOCK_TOWER, meleeWarlockDescription());
        TowerDescriptionRegistry.registerTemplate(T2_SLAVE, List.of("<gray>" + warlockText("흑마법사") + "가 데려온 희귀한 양입니다.</gray>", "<gray>사망 시 주위 {ability.deathEffectRadius:number}블록 내 적이 받는 " + attackDamageText("피해") + "를 해당 라운드 동안 " + attackDamageText("{ability.towerDamageTakenBonus:percent}") + " 증가시킵니다.</gray>"));
        TowerDescriptionRegistry.registerTemplate(T3_SLAVE, List.of("<gray>" + warlockText("흑마법사") + "가 데려온 양입니다. 희귀했던 색을 잃어 화가 났습니다.</gray>", "<gray>사망 시 주위 {ability.deathEffectRadius:number}블록 내 적이 받는 " + attackDamageText("피해") + "를 해당 라운드 동안 " + attackDamageText("{ability.towerDamageTakenBonus:percent}") + " 증가시킵니다.</gray>"));
        TowerDescriptionRegistry.registerTemplate(T2_RANGED_SLAVE, List.of("<gray>" + warlockText("흑마법사") + "가 키우는 개구리입니다.</gray>", "<gray>사망 시 주위 {ability.deathEffectRadius:number}블록 내 적의 " + attackSpeedText("공격 속도") + "를 해당 라운드 동안 " + attackSpeedText("{ability.attackSpeedReduction:percent}") + " 감소시킵니다.</gray>"));
        TowerDescriptionRegistry.registerTemplate(T3_RANGED_SLAVE, List.of("<gray>" + warlockText("흑마법사") + "가 키우는 개구리입니다.</gray>", "<gray>사망 시 주위 {ability.deathEffectRadius:number}블록 내 적의 " + attackSpeedText("공격 속도") + "를 해당 라운드 동안 " + attackSpeedText("{ability.attackSpeedReduction:percent}") + " 감소시킵니다.</gray>"));
    }

    private static List<String> baseWarlockDescription() {
        return List.of(
                "<gray>치명적인 피해를 입으면 주위 " + ability(BASE_RADIUS, "blocks") + " 내 아군을 흡수하고, " + healthText("최대 체력의 " + ability(BASE_HEAL, "percent")) + "를 회복합니다.</gray>",
                "<gray>희생한 타워의 " + healthText("체력 " + ability(BASE_PERMANENT_HEALTH, "percent")) + ", " + attackDamageText("피해 " + ability(BASE_PERMANENT_DAMAGE, "percent")) + "를 영구 누적합니다.</gray>",
                "<gray>업그레이드 시 원거리 또는 근거리 전투 방식을 선택할 수 있으며, 선택 후에는 변경할 수 없습니다.</gray>",
                "<gray>흑마법사 핵심 타워는 단 한 기만 설치할 수 있습니다.</gray>",
                SCALING_EFFICIENCY_DESCRIPTION
        );
    }

    private static List<String> rangedWarlockDescription() {
        List<String> lines = new ArrayList<>(List.of(
                "<gray>" + healthText("체력 " + ability(RANGED_THRESHOLD, "percent")) + " 이하이면 주위 " + globalAbility(SACRIFICE_RADIUS, "number") + " 블록 내 아군을 흡수합니다.</gray>",
                "<gray>흡수한 타워 " + healthText("체력") + "과 " + attackDamageText("피해") + "의 " + ability(RANGED_ROUND_STAT, "percent") + "를 이번 라운드 동안 획득합니다.</gray>",
                "<gray>흡수한 타워마다 " + healthText("체력 +" + ability(RANGED_PERMANENT_HEALTH, "percent")) + ", " + attackDamageText("피해 +" + ability(RANGED_PERMANENT_DAMAGE, "percent")) + "를 영구 누적합니다.</gray>",
                "<gray>생존 중인 " + attackDamageText("개구리 계열") + "마다 " + healthText("체력 +" + ability(RANGED_PET_HEALTH, "percent")) + ", " + attackDamageText("피해 +" + ability(RANGED_PET_DAMAGE, "percent")) + "를 얻으며, 최대 " + healthText("체력 +" + ability(RANGED_PET_HEALTH_CAP, "percent")) + ", " + attackDamageText("피해 +" + ability(RANGED_PET_DAMAGE_CAP, "percent")) + "까지 증가합니다.</gray>"
        ));
        if (WarlockConfig.AWAKENING_ENABLED) {
            lines.addAll(List.of(
                "<gray>이번 라운드에 " + globalAbility(AWAKENING_ABSORPTIONS, "integer") + "기 이상 흡수하고, 이 타워만 생존한 상태에서 " + healthText("체력 " + globalAbility(AWAKENING_THRESHOLD, "percent")) + " 이하이면 " + warlockText("흑마법사") + "가 " + warlockText("각성") + "하기 시작합니다.</gray>",
                "<gray>각성 시 " + healthText("체력 " + ability(RANGED_AWAKENING_HEAL, "number")) + "을 회복하고 " + regenerationText("재생 +" + ability(RANGED_AWAKENING_REGENERATION, "number") + " HP/s") + "를 획득하며, 라운드 종료 시 각성이 해제됩니다.</gray>"
            ));
        }
        lines.add(SCALING_EFFICIENCY_DESCRIPTION);
        return List.copyOf(lines);
    }

    private static List<String> meleeWarlockDescription() {
        List<String> lines = new ArrayList<>(List.of(
                "<gray>" + healthText("체력 " + ability(MELEE_THRESHOLD, "percent")) + " 이하이면 주위 " + globalAbility(SACRIFICE_RADIUS, "number") + " 블록 내 아군을 흡수합니다.</gray>",
                "<gray>흡수한 타워 " + healthText("체력") + "과 " + attackDamageText("피해") + "의 " + ability(MELEE_ROUND_STAT, "percent") + "를 이번 라운드 동안 획득합니다.</gray>",
                "<gray>흡수한 타워마다 " + healthText("체력 +" + ability(MELEE_PERMANENT_HEALTH, "percent")) + ", " + attackDamageText("피해 +" + ability(MELEE_PERMANENT_DAMAGE, "percent")) + "를 영구 누적합니다.</gray>",
                "<gray>생존 중인 " + healthText("양 계열") + "마다 " + healthText("체력 +" + ability(MELEE_PET_HEALTH, "percent")) + ", " + attackDamageText("피해 +" + ability(MELEE_PET_DAMAGE, "percent")) + "를 얻으며, 최대 " + healthText("체력 +" + ability(MELEE_PET_HEALTH_CAP, "percent")) + ", " + attackDamageText("피해 +" + ability(MELEE_PET_DAMAGE_CAP, "percent")) + "까지 증가합니다.</gray>",
                "<gray>핵심 타워 외 다른 타워가 남아 있지 않다면, 이번 라운드에 희생한 타워 1기당 " + lifeStealText("생명력 흡수 +" + ability(MELEE_LIFE_STEP, "percent")) + "를 얻으며, 최대 " + lifeStealText(ability(MELEE_LIFE_CAP, "percent")) + "까지 증가합니다.</gray>"
        ));
        if (WarlockConfig.AWAKENING_ENABLED) {
            lines.addAll(List.of(
                "<gray>이번 라운드에 " + globalAbility(AWAKENING_ABSORPTIONS, "integer") + "기 이상 흡수하고, 이 타워만 생존한 상태에서 " + healthText("체력 " + globalAbility(AWAKENING_THRESHOLD, "percent")) + " 이하이면 " + warlockText("흑마법사") + "가 " + warlockText("각성") + "하기 시작합니다.</gray>",
                "<gray>각성 시 " + attackDamageText("피해 +" + ability(MELEE_AWAKENING_DAMAGE, "number")) + ", 이동 속도 +" + ability(MELEE_AWAKENING_MOVE_SPEED, "percent") + "를 획득하며, 라운드 종료 시 각성이 해제됩니다.</gray>"
            ));
        }
        lines.add(SCALING_EFFICIENCY_DESCRIPTION);
        return List.copyOf(lines);
    }

    private static String ability(WarlockConfig.Ability ability, String format) {
        return "{ability." + ability.key() + ":" + format + "}";
    }

    private static String globalAbility(WarlockConfig.Ability ability, String format) {
        return "{ability." + CONFIG_ID + "." + ability.key() + ":" + format + "}";
    }

    public static boolean awakeningEnabled() {
        return WarlockConfig.AWAKENING_ENABLED;
    }

    public static boolean isWarlockTower(TowerType towerType) {
        return isWarlockCore(towerType) || isMeleeSlave(towerType) || isRangedSlave(towerType);
    }

    public static boolean isWarlockCore(TowerType towerType) {
        if (towerType == null) {return false;}
        String id = towerType.id();
        return id.equals(BASE_WARLOCK_TOWER.id()) || id.equals(RANGED_WARLOCK_TOWER.id()) || id.equals(MELEE_WARLOCK_TOWER.id());
    }

    public static boolean isMeleeSlave(TowerType towerType) {
        if (towerType == null) {return false;}
        String id = towerType.id();
        return id.equals(T1_SLAVE.id()) || id.equals(T2_SLAVE.id()) || id.equals(T3_SLAVE.id());
    }

    public static boolean isRangedSlave(TowerType towerType) {
        if (towerType == null) {return false;}
        String id = towerType.id();
        return id.equals(T1_RANGED_SLAVE.id()) || id.equals(T2_RANGED_SLAVE.id()) || id.equals(T3_RANGED_SLAVE.id());
    }
}
