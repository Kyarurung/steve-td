package kim.biryeong.semiontd.tower.warlock;

import static kim.biryeong.semiontd.tower.catalog.ProductionTowerDefinitions.tower;
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
            List.of(
                    "<gray>흑마법사 핵심 타워입니다.</gray>",
                    "<gray>치명적인 피해를 입을 경우 주위 6블록 이내의 아군 하나를 희생하여 최대 체력의 35%만큼 회복합니다.</gray>",
                    "<green>희생된 타워 체력의 2.5%, 공격력의 5%를 영구적으로 얻습니다.</green>",
                    "<gray>흑마법사 타워는 업그레이드를 통해 전투 방식을 정할 수 있습니다. 단, 한번 정한 방식은 되돌릴 수 없습니다.</gray>",
                    "<red><bold>흑마법사 타워는 단 한기만 설치할 수 있습니다.</bold></red>"
            )
    );

    public static final TowerType RANGED_WARLOCK_TOWER = tower(
            "ranged_warlock_tower",
            "흑마법사 타워",
            0,
            60,
            7,
            8,
            20,
            20,
            byId(EntityType.WITCH),
            List.of(
                    "<gray>흑마법사 핵심 타워입니다.</gray>",
                    "<gray>체력이 30% 미만일 때 흡수된 타워의 체력과 공격력의 40%를 이번 라운드 동안 획득합니다.</gray>",
                    "<red> 흡수할 타워는 낮은 공격 우선순위를 가진 타워부터 흡수됩니다.</red>",
                    "<gray>또한 흡수한 대상의 공격속도가 이 타워의 기본 공격속도보다 빠를 경우 이번 라운드 동안 그 차이만큼 획득합니다. (최대 15 감소)",
                    "<green>흡수한 타워마다 해당 타워의 체력 2.5%, 공격력의 5%를 영구적으로 얻습니다.</green>",
                    "<green>이 게임동안 흡수를 5번 할때마다, 0.5%의 생명력 흡수를 얻습니다. (최대 30%)</green>",
                    "<green>이 게임동안 흡수를 10번 할 때마다 0.5블록의 스플래시 범위를 얻습니다. 75% 피해</green>",
                    "<green>이 타워가 한 라운드에 흡수한 타워가 5기가 넘어갈 경우, 이 타워가 받는 피해량이 10% 감소합니다.",
                    "<green>생존 중인 애완 타워마다 체력이 5%, 공격력이 15% 증가합니다.</green>",
                    "<green>최대 체력 25%, 공격력 75%까지 증가합니다.</green>"
            )
    );

    public static final TowerType MELEE_WARLOCK_TOWER = tower(
            "melee_warlock_tower",
            "흑마법사 타워",
            0,
            100,
            3,
            5,
            20,
            80,
            byId(EntityType.WITCH),
            List.of(
                    "<gray>흑마법사 핵심 타워입니다.</gray>",
                    "<red><bold>피격 시 체력이 30% 미만일 경우, 주위 아군 하나를 희생</bold></red><gray>합니다.</gray>",
                    "<green>희생되는 타워는 공격 우선순위가 높은 타워일수록 먼저 희생됩니다.</green>",
                    "<gray>타워를 희생할 경우 이번 라운드 동안 해당 타워의 체력과 공격력의 60%를 얻습니다.</gray>",
                    "<green>이번 라운드에서 희생되는 타워당 스플래시 범위가 0.25 증가합니다.",
                    "<green>흡수한 타워마다 해당 타워의 체력 5%, 공격력의 2.5%를 추가로 얻습니다.</green>",
                    "<green>이번 게임에서 타워를 5기 흡수할 때 마다, 받는 피해가 2.5% 감소하고 최대 25%까지 감소합니다.",
                    "<green>이번 게임에서 타워를 흡수할 떄 마다 생명력 흡수를 1%씩 얻습니다. (최대 30%)",
                    "<green>생존 중인 희생양마다 체력이 15%, 공격력이 5% 증가합니다.</green>",
                    "<green>최대 체력 75%, 공격력 25%까지 증가합니다.</green>"
            )
    );

    public static final TowerType T1_SLAVE = tower(
            "t1_slave",
            "희생\"양\"",
            70,
            70,
            2,
            3,
            20,
            30,
            SheepVisual.builder().color(DyeColor.RED).build(),
            List.of(
                    "<gray>흑마법사가 데려온 양입니다.<gray>"
            )
    );

    public static final TowerType T2_SLAVE = tower(
            "t2_slave",
            "희생\"양\"",
            130,
            100,
            2,
            7,
            20,
            50,
            SheepVisual.builder().color(DyeColor.PINK).build(),
            List.of(
                    "<gray>흑마법사가 데려온 양입니다. 이 양은 더 희귀하데요.<gray>",
                    "<green> 이 타워가 어떤 이유든 사망할 때, 주위 5블록 이내의 적이 받는 피해를 영구적으로 5% 증가시킵니다.</green>"
            )
    );

    public static final TowerType T3_SLAVE = tower(
            "t3_slave",
            "희생\"양\"",
            280,
            150,
            2,
            10,
            20,
            70,
            SheepVisual.builder().color(DyeColor.WHITE).build(),
            List.of(
                    "<gray>흑마법사가 데려온 양입니다. 희귀했던 자기 색을 뺏겨서 화가 났습니다.<gray>",
                    "<green> 이 타워가 어떤 이유든 사망할 때, 주위 5블록 이내의 적이 받는 피해를 영구적으로 10% 증가시킵니다.</green>"
            )
    );

    public static final TowerType T1_RANGED_SLAVE = tower(
            "t1_ranged_slave",
            "애완 박쥐",
            80,
            50,
            7,
            5,
            18,
            20,
            byId(EntityType.BAT),
            List.of(
                    "<gray>흑마법사가 키우는 박쥐입니다. 애완 동물도 얄짤없네요.</gray>"
            )
    );

    public static final TowerType T2_RANGED_SLAVE = tower(
            "t2_ranged_slave",
            "애완 개구리",
            100,
            60,
            7,
            8,
            16,
            15,
            FrogVisual.builder().variant(FrogVariants.COLD).build(),
            List.of(
                    "<gray>흑마법사가 키우는 박쥐입니다. 얄짤없네요.</gray>",
                    "<green> 어떤 형태든 사망 시 10블록 내의 적의 공격속도를 5% 감소시킵니다. </green>"
            )
    );

    public static final TowerType T3_RANGED_SLAVE = tower(
            "t3_ranged_slave",
            "애완 개구리",
            240,
            100,
            7,
            15,
            14,
            15,
            FrogVisual.builder().variant(FrogVariants.WARM).build(),
            List.of(
                    "<gray>흑마법사가 키우는 박쥐입니다. 얄짤없네요.</gray>",
                    "<green> 어떤 형태든 사망 시 10블록 내의 적의 공격속도를 10% 감소시킵니다. </green>"
            )
    );

    static {
        TowerDescriptionRegistry.registerTemplate(BASE_WARLOCK_TOWER, List.of(
                "<gray>흑마법사 핵심 타워입니다.</gray>",
                "<gray>흑마법사 타워는 <red><bold>치명적인 피해를 입을 경우 주위 {ability.baseSacrificeRadius:blocks} 이내의 아군 하나를 희생</bold></red><gray>하여 최대 체력의 {ability.baseFatalHealRatio:percent}만큼 회복합니다.</gray>",
                "<green>희생된 타워 체력의 {ability.basePermanentHealthRatio:percent}, 공격력의 {ability.basePermanentDamageRatio:percent}를 영구적으로 얻습니다.</green>",
                "<gray>흑마법사 타워는 업그레이드를 통해 전투 방식을 정할 수 있습니다. 단, 한번 정한 방식은 되돌릴 수 없습니다.</gray>",
                "<red><bold>흑마법사 타워는 단 한기만 설치할 수 있습니다.</bold></red>"
        ));
        TowerDescriptionRegistry.registerTemplate(RANGED_WARLOCK_TOWER, List.of(
                "<gray>흑마법사 핵심 타워입니다.</gray>",
                "<gray>원거리 희생 타워가 살아있을 때 강해지는 흑마법사 타워입니다.</gray>",
                "<green>생존 중인 애완 타워마다 체력이 {ability.healthBonusPerStack:percent}, 공격력이 {ability.damageBonusPerStack:percent} 증가합니다.</green>",
                "<green>최대 체력 {ability.maxHealthBonus:percent}, 공격력 {ability.maxDamageBonus:percent}까지 증가합니다.</green>",
                "<gray>기본 흑마법사 타워가 희생으로 얻은 영구 스탯은 업그레이드 후에도 유지됩니다.</gray>"
        ));
        TowerDescriptionRegistry.registerTemplate(MELEE_WARLOCK_TOWER, List.of(
                "<gray>흑마법사 핵심 타워입니다.</gray>",
                "<gray>근접 희생 타워가 살아있을 때 강해지는 흑마법사 타워입니다.</gray>",
                "<green>생존 중인 희생양마다 체력이 {ability.healthBonusPerStack:percent}, 공격력이 {ability.damageBonusPerStack:percent} 증가합니다.</green>",
                "<green>최대 체력 {ability.maxHealthBonus:percent}, 공격력 {ability.maxDamageBonus:percent}까지 증가합니다.</green>",
                "<gray>기본 흑마법사 타워가 희생으로 얻은 영구 스탯은 업그레이드 후에도 유지됩니다.</gray>"
        ));
        TowerDescriptionRegistry.registerTemplate(T2_SLAVE, List.of(
                "<gray>흑마법사가 데려온 양입니다. 이 양은 더 희귀하데요.<gray>",
                "<green> 이 타워가 어떤 이유든 사망할 때, 주위 {ability.deathEffectRadius:blocks} 이내의 적이 받는 피해를 {ability.deathEffectDurationTicks:seconds} 동안 {ability.towerDamageTakenBonus:percent} 증가시킵니다.</green>"
        ));
        TowerDescriptionRegistry.registerTemplate(T3_SLAVE, List.of(
                "<gray>흑마법사가 데려온 양입니다. 희귀했던 자기 색을 뺏겨서 화가 났습니다.<gray>",
                "<green> 이 타워가 어떤 이유든 사망할 때, 주위 {ability.deathEffectRadius:blocks} 이내의 적이 받는 피해를 {ability.deathEffectDurationTicks:seconds} 동안 {ability.towerDamageTakenBonus:percent} 증가시킵니다.</green>"
        ));
        TowerDescriptionRegistry.registerTemplate(T2_RANGED_SLAVE, List.of(
                "<gray>흑마법사가 키우는 박쥐입니다. 얄짤없네요.</gray>",
                "<green> 어떤 형태든 사망 시 {ability.deathEffectRadius:blocks} 내의 적의 공격속도를 {ability.deathEffectDurationTicks:seconds} 동안 {ability.attackSpeedReduction:percent} 감소시킵니다. </green>"
        ));
        TowerDescriptionRegistry.registerTemplate(T3_RANGED_SLAVE, List.of(
                "<gray>흑마법사가 키우는 박쥐입니다. 얄짤없네요.</gray>",
                "<green> 어떤 형태든 사망 시 {ability.deathEffectRadius:blocks} 내의 적의 공격속도를 {ability.deathEffectDurationTicks:seconds} 동안 {ability.attackSpeedReduction:percent} 감소시킵니다. </green>"
        ));
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
