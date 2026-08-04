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
            "원거리 흑마법사 타워",
            0,
            100,
            7,
            8,
            20,
            20,
            byId(EntityType.WITCH),
            List.of(
                    "<gray>흑마법사 핵심 타워입니다.</gray>",
                    "<gray>체력이 30% 미만일 때 주위 25블록 이내의 타워를 흡수하여 해당 타워 체력과 공격력의 40%를 이번 라운드 동안 획득합니다.</gray>",
                    "<red> 흡수할 타워는 낮은 공격 우선순위를 가진 타워부터 흡수됩니다.</red>",
                    "<gray>또한 흡수한 대상의 공격속도가 이 타워의 기본 공격속도보다 빠를 경우 이번 라운드 동안 그 차이만큼 획득합니다. (최대 15 감소)",
                    "<green>이번 라운드에 흡수한 타워 4기마다 초당 재생이 10 증가합니다.</green>",
                    "<green>흡수한 타워마다 해당 타워의 체력 2.5%, 공격력의 5%를 영구적으로 얻습니다.</green>",
                    "<green>이 게임동안 흡수를 5번 할때마다, 0.5%의 생명력 흡수를 얻습니다. (최대 12%)</green>",
                    "<green>타워를 흡수할 때마다 공격 범위가 0.1블록 증가합니다. (최대 8블록, 50% 피해)</green>",
                    "<green>이 타워가 한 라운드에 흡수한 타워가 3기가 넘어갈 경우, 이 타워가 받는 피해량이 10% 감소합니다.",
                    "<green>생존 중인 애완 타워마다 체력이 5%, 공격력이 15% 증가합니다.</green>",
                    "<green>최대 체력 25%, 공격력 75%까지 증가합니다.</green>"
            )
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
            List.of(
                    "<gray>흑마법사 핵심 타워입니다.</gray>",
                    "<gray>피격 시 체력이 30% 미만일 경우, 주위 25블록 이내의 아군 하나를 희생합니다.</gray>",
                    "<green>희생되는 타워는 공격 우선순위가 높은 타워일수록 먼저 희생됩니다.</green>",
                    "<gray>타워를 희생할 경우 이번 라운드 동안 해당 타워의 체력과 공격력의 60%를 얻습니다.</gray>",
                    "<green>이번 라운드에 타워를 흡수할 때마다 공격 주기가 1틱 감소합니다. (최소 5틱)</green>",
                    "<green>이번 라운드에 흡수한 타워 4기마다 공격력이 15 증가합니다.</green>",
                    "<green>이번 라운드에 흡수한 타워마다 공격 범위가 0.25블록 증가합니다. (최대 2블록, 75% 피해)</green>",
                    "<green>흡수한 타워마다 해당 타워의 체력 5%, 공격력의 2.5%를 추가로 얻습니다.</green>",
                    "<green>이번 게임에서 타워를 5기 흡수할 때 마다, 피해가 2.5% 감소하고 최대 25%까지 감소합니다.",
                    "<green>이번 라운드에 흡수한 타워마다 생명력 흡수를 1%씩 얻습니다. (최대 20%)",
                    "<green>생존 중인 희생양마다 체력이 15%, 공격력이 5% 증가합니다.</green>",
                    "<green>최대 체력 75%, 공격력 25%까지 증가합니다.</green>"
            )
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
                    "<gray>흑마법사가 데려온 양입니다.<gray>"
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
                    "<gray>흑마법사가 데려온 양입니다. 이 양은 더 희귀하데요.<gray>",
                    "<green> 이 타워가 어떤 이유든 사망할 때, 주위 20블록 이내의 적이 받는 피해를 영구적으로 5% 증가시킵니다.</green>"
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
                    "<gray>흑마법사가 데려온 양입니다. 희귀했던 자기 색을 뺏겨서 화가 났습니다.<gray>",
                    "<green> 이 타워가 어떤 이유든 사망할 때, 주위 20블록 이내의 적이 받는 피해를 영구적으로 10% 증가시킵니다.</green>"
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
                    "<gray>흑마법사가 키우는 박쥐입니다. 애완 동물도 얄짤없네요.</gray>"
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
                    "<gray>흑마법사가 키우는 박쥐입니다. 얄짤없네요.</gray>",
                    "<green> 어떤 형태든 사망 시 20블록 내의 적의 공격속도를 5% 감소시킵니다. </green>"
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
                    "<gray>흑마법사가 키우는 박쥐입니다. 얄짤없네요.</gray>",
                    "<green> 어떤 형태든 사망 시 20블록 내의 적의 공격속도를 10% 감소시킵니다. </green>"
            )
    );

    static {
        TowerDescriptionRegistry.registerTemplate(BASE_WARLOCK_TOWER, List.of(
                "<gray>흑마법사 핵심 타워입니다.</gray>",
                "<gray>흑마법사 타워는 <red><bold>치명적인 피해를 입을 경우 주위 {ability.sacrificeRadius:blocks} 이내의 아군 하나를 희생</bold></red><gray>하여 최대 체력의 {ability.fatalHeal:percent}만큼 회복합니다.</gray>",
                "<green>희생된 타워 체력의 {ability.permanentHealth:percent}, 공격력의 {ability.permanentDamage:percent}를 영구적으로 얻습니다.</green>",
                "<gray>흑마법사 타워는 업그레이드를 통해 전투 방식을 정할 수 있습니다. 단, 한번 정한 방식은 되돌릴 수 없습니다.</gray>",
                "<red><bold>흑마법사 타워는 단 한기만 설치할 수 있습니다.</bold></red>"
        ));
        TowerDescriptionRegistry.registerTemplate(RANGED_WARLOCK_TOWER, List.of(
                "<gray>흑마법사 핵심 타워입니다.</gray>",
                "<gray>체력이 {ability.threshold:percent} 미만일 때 주위 {ability.warlock_global.sacrificeRadius:blocks} 이내의 타워를 흡수합니다. 흡수된 타워의 체력과 공격력의 {ability.roundStat:percent}를 이번 라운드 동안 획득합니다.</gray>",
                "<red> 흡수할 타워는 먼저 설치된 타워부터 흡수됩니다.</red>",
                "<gray>또한 흡수한 대상의 공격속도가 이 타워의 기본 공격속도보다 빠를 경우 이번 라운드 동안 그 차이만큼 획득합니다. (최대 {ability.warlock_global.speedCap:integer} 감소, 최소 {ability.warlock_global.minInterval:integer}틱)</gray>",
                "<green>이번 라운드에 흡수한 타워 {ability.regenEvery:integer}기마다 초당 재생이 {ability.regenStep:number} 증가합니다.</green>",
                "<green>흡수한 타워마다 해당 타워의 체력 {ability.permanentHealth:percent}, 공격력의 {ability.permanentDamage:percent}를 영구적으로 얻습니다.</green>",
                "<green>이 게임동안 흡수를 {ability.lifeEvery:integer}번 할때마다, {ability.lifeStep:percent}의 생명력 흡수를 얻습니다. (최대 {ability.lifeCap:percent})</green>",
                "<green>타워를 흡수할 때마다 공격 범위가 {ability.splashStep:precise_blocks} 증가합니다. (최대 {ability.splashCap:blocks}, {ability.splashDamage:percent} 피해)</green>",
                "<green>이 타워가 한 라운드에 흡수한 타워가 {ability.defenseThreshold:integer}기가 넘어갈 경우, 이 타워가 받는 피해량이 {ability.defense:percent} 감소합니다.",
                "<green>생존 중인 애완 타워마다 체력이 {ability.petHealth:percent}, 공격력이 {ability.petDamage:percent} 증가합니다.</green>",
                "<green>최대 체력 {ability.petHealthCap:percent}, 공격력 {ability.petDamageCap:percent}까지 증가합니다.</green>"
        ));
        TowerDescriptionRegistry.registerTemplate(MELEE_WARLOCK_TOWER, List.of(
                "<gray>흑마법사 핵심 타워입니다.</gray>",
                "<gray>피격 시 체력이 {ability.threshold:percent} 미만일 경우 주위 {ability.warlock_global.sacrificeRadius:blocks} 이내의 아군 하나를 희생합니다.</gray>",
                "<green>흡수할 타워는 먼저 설치된 타워부터 흡수됩니다.</green>",
                "<gray>타워를 희생할 경우 이번 라운드 동안 해당 타워의 체력과 공격력의 {ability.roundStat:percent}를 얻습니다.</gray>",
                "<green>이번 라운드에 타워를 흡수할 때마다 공격 주기가 {ability.speedStep:integer}틱 감소합니다. (최소 {ability.warlock_global.minInterval:integer}틱)</green>",
                "<green>이번 라운드에 흡수한 타워 {ability.damageEvery:integer}기마다 공격력이 {ability.damageStep:number} 증가합니다.</green>",
                "<green>이번 라운드에 흡수한 타워마다 공격 범위가 {ability.splashStep:precise_blocks} 증가합니다. (최대 {ability.splashCap:blocks}, {ability.splashDamage:percent} 피해)</green>",
                "<green>흡수한 타워마다 해당 타워의 체력 {ability.permanentHealth:percent}, 공격력의 {ability.permanentDamage:percent}를 추가로 얻습니다.</green>",
                "<green>이번 게임에서 타워를 {ability.defenseEvery:integer}기 흡수할 때 마다, 피해가 {ability.defenseStep:percent} 감소하고 최대 {ability.defenseCap:percent}까지 감소합니다.",
                "<green>이번 라운드에 흡수한 타워마다 생명력 흡수를 {ability.lifeStep:percent}씩 얻습니다. (최대 {ability.lifeCap:percent})",
                "<green>생존 중인 희생양마다 체력이 {ability.petHealth:percent}, 공격력이 {ability.petDamage:percent} 증가합니다.</green>",
                "<green>최대 체력 {ability.petHealthCap:percent}, 공격력 {ability.petDamageCap:percent}까지 증가합니다.</green>"
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
