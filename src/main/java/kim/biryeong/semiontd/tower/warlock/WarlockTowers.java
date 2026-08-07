package kim.biryeong.semiontd.tower.warlock;

import static kim.biryeong.semiontd.tower.catalog.ProductionTowerDefinitions.tower;
import static kim.biryeong.semiontd.tower.warlock.WarlockConfig.Ability.*;
import static kim.biryeong.semiontd.util.EntityTypeUtil.byId;

import kim.biryeong.semiontd.entity.visual.FrogVisual;
import kim.biryeong.semiontd.entity.visual.SheepVisual;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.description.TowerDescriptionRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import net.minecraft.world.item.DyeColor;

import java.util.List;

public final class WarlockTowers {
    public static final String CONFIG_ID = "warlock_global";

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
                    "<gray><dark_purple>흑마법사</dark_purple>가 데려온 양입니다.</gray>"
            )
    );

    public static final TowerType T2_SLAVE = tower(
            "t2_slave",
            "희생\"양\"",
            130,
            120,
            2,
            8,
            20,
            50,
            SheepVisual.builder().color(DyeColor.PINK).build(),
            List.of(
                    "<gray><dark_purple>흑마법사</dark_purple>가 데려온 희귀한 양입니다.</gray>",
                    "<gray>사망 시 주위 20 블록 내 적이 받는 <#ec8d34>피해</#ec8d34>를 일정 시간 동안 <#ec8d34>5%</#ec8d34> 증가시킵니다.</gray>"
            )
    );

    public static final TowerType T3_SLAVE = tower(
            "t3_slave",
            "희생\"양\"",
            280,
            185,
            2,
            12,
            20,
            70,
            SheepVisual.builder().color(DyeColor.WHITE).build(),
            List.of(
                    "<gray><dark_purple>흑마법사</dark_purple>가 데려온 양입니다. 희귀했던 색을 잃어 화가 났습니다.</gray>",
                    "<gray>사망 시 주위 20 블록 내 적이 받는 <#ec8d34>피해</#ec8d34>를 일정 시간 동안 <#ec8d34>10%</#ec8d34> 증가시킵니다.</gray>"
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
                    "<gray><dark_purple>흑마법사</dark_purple>가 키우는 박쥐입니다. 애완동물도 얄짤없네요.</gray>"
            )
    );

    public static final TowerType T2_RANGED_SLAVE = tower(
            "t2_ranged_slave",
            "애완 개구리",
            100,
            120,
            7,
            8,
            15,
            15,
            FrogVisual.builder().variant(FrogVariants.COLD).build(),
            List.of(
                    "<gray><dark_purple>흑마법사</dark_purple>가 키우는 개구리입니다.</gray>",
                    "<gray>사망 시 주위 20 블록 내 적의 <#ffe78d>공격 속도</#ffe78d>를 일정 시간 동안 <#ffe78d>5%</#ffe78d> 감소시킵니다.</gray>"
            )
    );

    public static final TowerType T3_RANGED_SLAVE = tower(
            "t3_ranged_slave",
            "애완 개구리",
            240,
            185,
            7,
            12,
            13,
            15,
            FrogVisual.builder().variant(FrogVariants.WARM).build(),
            List.of(
                    "<gray><dark_purple>흑마법사</dark_purple>가 키우는 개구리입니다.</gray>",
                    "<gray>사망 시 주위 20 블록 내 적의 <#ffe78d>공격 속도</#ffe78d>를 일정 시간 동안 <#ffe78d>10%</#ffe78d> 감소시킵니다.</gray>"
            )
    );

    static {
        TowerDescriptionRegistry.registerTemplate(
                BASE_WARLOCK_TOWER,
                baseWarlockDescription()
        );
        TowerDescriptionRegistry.registerTemplate(
                RANGED_WARLOCK_TOWER,
                rangedWarlockDescription()
        );
        TowerDescriptionRegistry.registerTemplate(
                MELEE_WARLOCK_TOWER,
                meleeWarlockDescription()
        );
        TowerDescriptionRegistry.registerTemplate(T2_SLAVE, List.of("<gray><dark_purple>흑마법사</dark_purple>가 데려온 희귀한 양입니다.</gray>", "<gray>사망 시 주위 " + "{ability.deathEffectRadius:number} 블록" + " 내 적이 받는 <#ec8d34>피해</#ec8d34>를 " + "{ability.deathEffectDurationTicks:seconds} 동안 <#ec8d34>{ability.towerDamageTakenBonus:percent}</#ec8d34> 증가시킵니다.</gray>"));
        TowerDescriptionRegistry.registerTemplate(T3_SLAVE, List.of("<gray><dark_purple>흑마법사</dark_purple>가 데려온 양입니다. 희귀했던 색을 잃어 화가 났습니다.</gray>", "<gray>사망 시 주위 " + "{ability.deathEffectRadius:number} 블록" + " 내 적이 받는 <#ec8d34>피해</#ec8d34>를 " + "{ability.deathEffectDurationTicks:seconds} 동안 <#ec8d34>{ability.towerDamageTakenBonus:percent}</#ec8d34> 증가시킵니다.</gray>"));
        TowerDescriptionRegistry.registerTemplate(T2_RANGED_SLAVE, List.of("<gray><dark_purple>흑마법사</dark_purple>가 키우는 개구리입니다.</gray>", "<gray>사망 시 주위 " + "{ability.deathEffectRadius:number} 블록" + " 내 적의 <#ffe78d>공격 속도</#ffe78d>를 " + "{ability.deathEffectDurationTicks:seconds} 동안 <#ffe78d>{ability.attackSpeedReduction:percent}</#ffe78d> 감소시킵니다.</gray>"));
        TowerDescriptionRegistry.registerTemplate(T3_RANGED_SLAVE, List.of("<gray><dark_purple>흑마법사</dark_purple>가 키우는 개구리입니다.</gray>", "<gray>사망 시 주위 " + "{ability.deathEffectRadius:number} 블록" + " 내 적의 <#ffe78d>공격 속도</#ffe78d>를 " + "{ability.deathEffectDurationTicks:seconds} 동안 <#ffe78d>{ability.attackSpeedReduction:percent}</#ffe78d> 감소시킵니다.</gray>"));
    }

    private static List<String> baseWarlockDescription() {
        return List.of(
                "<gray>치명적인 피해를 입으면 주위 " + ability(BASE_RADIUS, "blocks") + " 내 아군을 흡수하고, <#fc5454>최대 체력의 " + ability(BASE_HEAL, "percent") + "</#fc5454>를 회복합니다.</gray>",
                "<gray>희생한 타워의 <#fc5454>체력 " + ability(BASE_PERMANENT_HEALTH, "percent") + "</#fc5454>, <#ec8d34>피해 " + ability(BASE_PERMANENT_DAMAGE, "percent") + "</#ec8d34>를 영구 누적합니다.</gray>",
                "<gray>업그레이드 시 원거리 또는 근거리 전투 방식을 선택할 수 있으며, 선택 후에는 변경할 수 없습니다.</gray>",
                "<gray>흑마법사 핵심 타워는 단 한 기만 설치할 수 있습니다.</gray>"
        );
    }

    private static List<String> rangedWarlockDescription() {
        return List.of(
                "<gray><#fc5454>체력 " + ability(RANGED_THRESHOLD, "percent") + "</#fc5454> 이하이면 주위 " + globalAbility(SACRIFICE_RADIUS, "number") + " 블록 내 아군을 흡수합니다.</gray>",
                "<gray>흡수한 타워 <#fc5454>체력</#fc5454>과 <#ec8d34>피해</#ec8d34>의 " + ability(RANGED_ROUND_STAT, "percent") + "를 이번 라운드 동안 획득합니다.</gray>",
                "<gray>흡수한 타워마다 <#fc5454>체력 +" + ability(RANGED_PERMANENT_HEALTH, "percent") + "</#fc5454>, <#ec8d34>피해 +" + ability(RANGED_PERMANENT_DAMAGE, "percent") + "</#ec8d34>를 영구 누적합니다.</gray>",
                "<gray>이번 라운드에 " + globalAbility(AWAKENING_ABSORPTIONS, "integer") + "기 이상 흡수하고, 이 타워만 생존한 상태에서 <#fc5454>체력 " + globalAbility(AWAKENING_THRESHOLD, "percent") + "</#fc5454> 이하이면 <dark_purple>흑마법사</dark_purple>가 <dark_purple>각성</dark_purple>하기 시작합니다.</gray>",
                "<gray>각성 시 <#fc5454>체력 " + ability(RANGED_AWAKENING_HEAL, "number") + "</#fc5454>을 회복하고 <#20985d>재생 +" + ability(RANGED_AWAKENING_REGENERATION, "number") + " HP/s</#20985d>를 획득합니다.</gray>"
        );
    }

    private static List<String> meleeWarlockDescription() {
        return List.of(
                "<gray><#fc5454>체력 " + ability(MELEE_THRESHOLD, "percent") + "</#fc5454> 이하이면 주위 " + globalAbility(SACRIFICE_RADIUS, "number") + " 블록 내 아군을 흡수합니다.</gray>",
                "<gray>흡수한 타워 <#fc5454>체력</#fc5454>과 <#ec8d34>피해</#ec8d34>의 " + ability(MELEE_ROUND_STAT, "percent") + "를 이번 라운드 동안 획득합니다.</gray>",
                "<gray>흡수한 타워마다 <#fc5454>체력 +" + ability(MELEE_PERMANENT_HEALTH, "percent") + "</#fc5454>, <#ec8d34>피해 +" + ability(MELEE_PERMANENT_DAMAGE, "percent") + "</#ec8d34>를 영구 누적합니다.</gray>",
                "<gray>이번 라운드에 " + globalAbility(AWAKENING_ABSORPTIONS, "integer") + "기 이상 흡수하고, 이 타워만 생존한 상태에서 <#fc5454>체력 " + globalAbility(AWAKENING_THRESHOLD, "percent") + "</#fc5454> 이하이면 <dark_purple>흑마법사</dark_purple>가 <dark_purple>각성</dark_purple>하기 시작합니다.</gray>",
                "<gray>각성 시 <#ec8d34>피해 +" + ability(MELEE_AWAKENING_DAMAGE, "number") + "</#ec8d34>, 이동 속도 +" + ability(MELEE_AWAKENING_MOVE_SPEED, "percent") + "를 획득합니다.</gray>"
        );
    }

    private static String ability(WarlockConfig.Ability ability, String format) {
        return "{ability." + ability.key() + ":" + format + "}";
    }

    private static String globalAbility(WarlockConfig.Ability ability, String format) {
        return "{ability." + CONFIG_ID + "." + ability.key() + ":" + format + "}";
    }

    public static boolean isWarlockTower(TowerType towerType) {
        return isWarlockCore(towerType) || isMeleeSlave(towerType) || isRangedSlave(towerType);
    }

    public static boolean isWarlockCore(TowerType towerType) {
        if (towerType == null) {
            return false;
        }
        String id = towerType.id();
        return id.equals(BASE_WARLOCK_TOWER.id())
                || id.equals(RANGED_WARLOCK_TOWER.id())
                || id.equals(MELEE_WARLOCK_TOWER.id());
    }

    public static boolean isMeleeSlave(TowerType towerType) {
        if (towerType == null) {
            return false;
        }
        String id = towerType.id();
        return id.equals(T1_SLAVE.id()) || id.equals(T2_SLAVE.id()) || id.equals(T3_SLAVE.id());
    }

    public static boolean isRangedSlave(TowerType towerType) {
        if (towerType == null) {
            return false;
        }
        String id = towerType.id();
        return id.equals(T1_RANGED_SLAVE.id())
                || id.equals(T2_RANGED_SLAVE.id())
                || id.equals(T3_RANGED_SLAVE.id());
    }
}
