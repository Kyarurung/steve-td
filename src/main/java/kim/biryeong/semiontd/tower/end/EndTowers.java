package kim.biryeong.semiontd.tower.end;

import static kim.biryeong.semiontd.tower.end.EndConfig.Ability.*;
import static kim.biryeong.semiontd.util.EntityTypeUtil.byId;

import java.util.List;
import java.util.Set;
import kim.biryeong.semiontd.entity.visual.BlockDisplayVisual;
import kim.biryeong.semiontd.entity.visual.EntityVisual;
import kim.biryeong.semiontd.entity.visual.ShulkerVisual;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.description.TowerDescriptionRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;

public final class EndTowers {
    public static final String CONFIG_ID = "end_global";

    public static final EntityVisual DRAGON_EGG_VISUAL = BlockDisplayVisual.builder(Blocks.DRAGON_EGG.defaultBlockState()).build();
    public static final EntityVisual PHANTOM_VISUAL = EntityVisual.builder(byId(EntityType.PHANTOM)).build();
    public static final EntityVisual DRAGON_VISUAL = EntityVisual.builder(byId(EntityType.ENDER_DRAGON)).build();

    public static final TowerType BASE_END_TOWER = TowerType.builder("base_ender_dragon", "엔더 드래곤")
            .mineralCost(0)
            .maxHealth(200.0)
            .range(5.0)
            .damage(10.0)
            .attackIntervalTicks(15)
            .aggroPriority(100)
            .visual(DRAGON_EGG_VISUAL)
            .description(dragonDescription())
            .build();

    public static final TowerType T1_ENDERMITE_TOWER = TowerType.builder("t1_endermite_tower", "엔더 마이트")
            .mineralCost(50)
            .maxHealth(50)
            .range(0)
            .damage(10)
            .attackIntervalTicks(20)
            .aggroPriority(10)
            .visual(EntityVisual.vanilla(byId(EntityType.ENDERMITE)))
            .description(List.of(
                    "<gray>공격력이 높은 엔더마이트 입니다.</gray>",
                    "<green>공격을 하지 않지만, 엔드 수정 계열의 힘 전달을 완료하면 엔더 드래곤의 공격 능력을 강화합니다.</green>"
            ))
            .build();

    public static final TowerType T2_ENDERMAN_TOWER = TowerType.builder("t2_enderman_tower", "엔더맨")
            .mineralCost(100)
            .maxHealth(50)
            .range(0)
            .damage(15)
            .attackIntervalTicks(20)
            .aggroPriority(10)
            .visual(EntityVisual.vanilla(byId(EntityType.ENDERMAN)))
            .description(List.of(
                    "<gray>공격력이 높은 엔더맨 입니다.</gray>",
                    "<green>공격을 하지 않지만, 엔드 수정 계열의 힘 전달을 완료하면 엔더 드래곤의 공격 능력을 강화합니다.</green>"
            ))
            .build();

    public static final TowerType T3_END_CRYSTAL_TOWER = TowerType.builder("t3_end_crystal_tower", "엔드 수정")
            .mineralCost(150)
            .maxHealth(50)
            .range(0)
            .damage(20)
            .attackIntervalTicks(20)
            .aggroPriority(10)
            .visual(EntityVisual.vanilla(byId(EntityType.END_CRYSTAL)))
            .description(List.of(
                    "<gray>공격력이 매우 높은 엔드 수정 입니다.</gray>",
                    "<green>공격을 하지 않지만, 엔드 수정 계열의 힘 전달을 완료하면 엔더 드래곤의 공격 능력을 강화합니다.</green>"
            ))
            .build();

    public static final TowerType T1_SHULKER_TOWER = TowerType.builder("t1_shulker_tower", "셜커")
            .mineralCost(50)
            .maxHealth(100)
            .range(0)
            .damage(5)
            .attackIntervalTicks(20)
            .aggroPriority(10)
            .visual(EntityVisual.vanilla(byId(EntityType.SHULKER)))
            .description(List.of(
                    "<gray>체력이 높은 셜커 입니다.</gray>",
                    "<yellow>받는 피해가 {ability.damageReduction:percent} 감소합니다.</yellow>",
                    "<green>공격을 하지 않지만, 셜커 계열의 힘 전달을 완료하면 엔더 드래곤의 내구력을 강화합니다.</green>"
            ))
            .build();

    public static final TowerType T2_SHULKER_TOWER = TowerType.builder("t2_shulker_tower", "견고한 셜커")
            .mineralCost(100)
            .maxHealth(150)
            .range(0)
            .damage(5)
            .attackIntervalTicks(20)
            .aggroPriority(10)
            .visual(ShulkerVisual.builder().color(DyeColor.PURPLE).build())
            .description(List.of(
                    "<gray>체력이 높은 견고한 셜커 입니다.</gray>",
                    "<yellow>받는 피해가 {ability.damageReduction:percent} 감소합니다.</yellow>",
                    "<green>공격을 하지 않지만, 셜커 계열의 힘 전달을 완료하면 엔더 드래곤의 내구력을 강화합니다.</green>"
            ))
            .build();

    public static final TowerType T3_SHULKER_TOWER = TowerType.builder("t3_shulker_tower", "완강한 셜커")
            .mineralCost(150)
            .maxHealth(200)
            .range(0)
            .damage(5)
            .attackIntervalTicks(20)
            .aggroPriority(10)
            .visual(ShulkerVisual.builder().color(DyeColor.BLACK).build())
            .description(List.of(
                    "<gray>체력이 매우 높은 완강한 셜커 입니다.</gray>",
                    "<yellow>받는 피해가 {ability.damageReduction:percent} 감소합니다.</yellow>",
                    "<green>공격을 하지 않지만, 셜커 계열의 힘 전달을 완료하면 엔더 드래곤의 내구력을 강화합니다.</green>"
            ))
            .build();



    private static final Set<String> ENDER_TOWER_IDS = Set.of(
            BASE_END_TOWER.id(),
            T1_ENDERMITE_TOWER.id(),
            T2_ENDERMAN_TOWER.id(),
            T3_END_CRYSTAL_TOWER.id(),
            T1_SHULKER_TOWER.id(),
            T2_SHULKER_TOWER.id(),
            T3_SHULKER_TOWER.id()
    );

    private static final Set<String> END_CRYSTAL_LINE_IDS = Set.of(
            T1_ENDERMITE_TOWER.id(), T2_ENDERMAN_TOWER.id(), T3_END_CRYSTAL_TOWER.id()
    );
    private static final Set<String> SHULKER_LINE_IDS = Set.of(
            T1_SHULKER_TOWER.id(), T2_SHULKER_TOWER.id(), T3_SHULKER_TOWER.id()
    );

    static {
        TowerDescriptionRegistry.registerTemplate(BASE_END_TOWER, BASE_END_TOWER.description());
        TowerDescriptionRegistry.registerTemplate(T1_ENDERMITE_TOWER, T1_ENDERMITE_TOWER.description());
        TowerDescriptionRegistry.registerTemplate(T2_ENDERMAN_TOWER, T2_ENDERMAN_TOWER.description());
        TowerDescriptionRegistry.registerTemplate(T3_END_CRYSTAL_TOWER, T3_END_CRYSTAL_TOWER.description());
        TowerDescriptionRegistry.registerTemplate(T1_SHULKER_TOWER, T1_SHULKER_TOWER.description());
        TowerDescriptionRegistry.registerTemplate(T2_SHULKER_TOWER, T2_SHULKER_TOWER.description());
        TowerDescriptionRegistry.registerTemplate(T3_SHULKER_TOWER, T3_SHULKER_TOWER.description());
    }

    private EndTowers() {
    }

    private static List<String> dragonDescription() {
        return List.of(
                "<gray>알로 소환되며, 라운드 시작 시 <#B77DE8>아기 드래곤</#B77DE8>으로 변합니다.</gray>",
                "<gray><#E66F6F>최대 체력 " + ability(DRAGON_EVOLUTION, "integer") + "</#E66F6F> 이상이면 <#B77DE8>엔더 드래곤</#B77DE8>으로 진화합니다.</gray>",
                "<gray><#B77DE8>아기 드래곤</#B77DE8> 크기는 <#E66F6F>최대 체력 " + ability(PHANTOM_SCALE_HEALTH, "integer") + "</#E66F6F>당 " + ability(PHANTOM_SCALE_STEP, "number") + "씩 증가합니다.</gray>",
                "<gray>힘 전달 " + ability(TRANSFER_TICKS, "seconds") + " 후 타워 <#D94343>사망</#D94343>, <#E66F6F>체력 " + ability(TRANSFER_HEAL, "integer") + "</#E66F6F> 회복합니다.</gray>",
                "<gray>전달 중인 셜커 타워의 <#E66F6F>최대 체력 " + ability(TRANSFER_HEAL_RATIO, "percent_integer") + "</#E66F6F>만큼 매초 회복합니다.</gray>",
                "<gray><#D94343>공격력</#D94343>: 타워 공격력의 <#D94343>" + ability(ROUND_DAMAGE_RATIO, "percent_integer") + "</#D94343>를 임시 획득, <#D94343>" + ability(PERMANENT_DAMAGE_RATIO, "percent_integer") + "</#D94343> 영구 누적</gray>",
                "<gray><#E66F6F>체력</#E66F6F>: 타워 체력의 <#E66F6F>" + ability(ROUND_HEALTH_RATIO, "percent_integer") + "</#E66F6F>를 임시 획득, <#E66F6F>" + ability(PERMANENT_HEALTH_RATIO, "percent_integer") + "</#E66F6F> 영구 누적</gray>",
                "<gray><#D9B94F>공격 범위</#D9B94F>: 엔드 수정 <#D9B94F>" + ability(SPLASH_1, "integer") + ", " + ability(SPLASH_2, "integer") + ", " + ability(SPLASH_3, "integer") + ", " + ability(SPLASH_4, "integer") + "</#D9B94F>스택마다 <#D9B94F>+" + ability(SPLASH_STEP, "number") + " 블록</#D9B94F></gray>",
                "<gray><#D9B94F>공격 속도</#D9B94F>: 엔드 수정 <#D9B94F>" + ability(ATTACK_SPEED_STACKS, "integer") + "</#D9B94F>스택마다 <#D9B94F>-" + ability(ATTACK_SPEED_STEP, "integer") + "틱</#D9B94F></gray>",
                "<gray><#D9B94F>사거리</#D9B94F>: 엔드 수정 <#D9B94F>" + ability(ATTACK_RANGE_STACKS, "integer") + "</#D9B94F>스택마다 <#D9B94F>+" + ability(ATTACK_RANGE_STEP, "number") + " 블록</#D9B94F></gray>",
                "<gray><#D94343>생명력 흡수</#D94343>: 셜커 <#D94343>" + ability(LIFE_STEAL_STACKS, "integer") + "</#D94343>스택마다 <#D94343>+" + ability(LIFE_STEAL_STEP, "percent") + "</#D94343></gray>",
                "<gray><#72A9E6>피해 감소</#72A9E6>: 셜커 <#72A9E6>" + ability(DAMAGE_REDUCTION_STACKS, "integer") + "</#72A9E6>스택마다 <#72A9E6>+" + ability(DAMAGE_REDUCTION_STEP, "percent_integer") + "</#72A9E6></gray>",
                "<gray><#79C97B>재생</#79C97B>: 셜커 <#79C97B>" + ability(REGENERATION_STACKS, "integer") + "</#79C97B>스택마다 <#79C97B>+" + ability(REGENERATION_STEP, "integer") + " HP/s</#79C97B></gray>",
                "<gray><#B77DE8>엔더 드래곤</#B77DE8>: <#D94343>최종 피해 +" + ability(DRAGON_FINAL_DAMAGE, "percent_integer") + "</#D94343> / <#D9B94F>추가 사거리 +" + ability(DRAGON_RANGE_BONUS, "number") + " 블록</#D9B94F></gray>"
        );
    }

    private static String ability(EndConfig.Ability ability, String format) {
        return "{ability." + CONFIG_ID + "." + ability.key() + ":" + format + "}";
    }

    public static boolean isEndTower(TowerType type) {
        return type != null && ENDER_TOWER_IDS.contains(type.id());
    }

    public static boolean isBaseEndTower(TowerType type) {
        return type != null && type.id().equals(BASE_END_TOWER.id());
    }

    public static boolean isEndCrystalLine(TowerType type) {
        return type != null && END_CRYSTAL_LINE_IDS.contains(type.id());
    }

    public static boolean isShulkerLine(TowerType type) {
        return type != null && SHULKER_LINE_IDS.contains(type.id());
    }

    public static boolean isTransferableTower(TowerType type) {
        return isEndCrystalLine(type) || isShulkerLine(type);
    }

    public static int transferTier(TowerType type) {
        return ProductionTowerCatalog.entry(type)
                .map(ProductionTowerCatalog.CatalogEntry::tier)
                .orElse(0);
    }

}
