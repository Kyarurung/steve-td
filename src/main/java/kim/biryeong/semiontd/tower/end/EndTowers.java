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
                    "<gray><#ec8d34>피해</#ec8d34>가 낮은 엔더마이트 입니다.</gray>",
                    "<gray>이 타워는 공격을 하지 않습니다.</gray>",
                    "<gray>힘 전달을 완료하면 <#cc00fa>엔더 드래곤</#cc00fa>의 <#ec8d34>피해</#ec8d34>를 강화합니다.</gray>"
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
                    "<gray><#ec8d34>피해</#ec8d34>가 보통인 엔더맨 입니다.</gray>",
                    "<gray>이 타워는 공격을 하지 않습니다.</gray>",
                    "<gray>힘 전달을 완료하면 <#cc00fa>엔더 드래곤</#cc00fa>의 <#ec8d34>피해</#ec8d34>를 강화합니다.</gray>"
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
                    "<gray><#ec8d34>피해</#ec8d34>가 높은 엔드 수정 입니다.</gray>",
                    "<gray>이 타워는 공격을 하지 않습니다.</gray>",
                    "<gray>힘 전달을 완료하면 <#cc00fa>엔더 드래곤</#cc00fa>의 <#ec8d34>피해</#ec8d34>를 강화합니다.</gray>"
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
                    "<gray><#fc5454>체력</#fc5454>이 낮은 셜커입니다.</gray>",
                    "<gray><#f3ba59>피해</#f3ba59>를 <#f3ba59>{ability.damageReduction:percent} 감소</#f3ba59>합니다.</gray>",
                    "<gray>이 타워는 공격을 하지 않습니다.</gray>",
                    "<gray>힘 전달을 완료하면 <#cc00fa>엔더 드래곤</#cc00fa>의 <#fc5454>체력</#fc5454>을 강화합니다.</gray>"
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
                    "<gray><#fc5454>체력</#fc5454>이 보통인 견고한 셜커입니다.</gray>",
                    "<gray><#f3ba59>피해</#f3ba59>를 <#f3ba59>{ability.damageReduction:percent} 감소</#f3ba59>합니다.</gray>",
                    "<gray>이 타워는 공격을 하지 않습니다.</gray>",
                    "<gray>힘 전달을 완료하면 <#cc00fa>엔더 드래곤</#cc00fa>의 <#fc5454>체력</#fc5454>을 강화합니다.</gray>"
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
                    "<gray><#fc5454>체력</#fc5454>이 높은 완강한 셜커입니다.</gray>",
                    "<gray><#f3ba59>피해</#f3ba59>를 <#f3ba59>{ability.damageReduction:percent} 감소</#f3ba59>합니다.</gray>",
                    "<gray>이 타워는 공격을 하지 않습니다.</gray>",
                    "<gray>힘 전달을 완료하면 <#cc00fa>엔더 드래곤</#cc00fa>의 <#fc5454>체력</#fc5454>을 강화합니다.</gray>"
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
                "<gray>알로 소환되며, 라운드 시작 시 <#cc00fa>아기 드래곤</#cc00fa>으로 변합니다.</gray>",
                "<gray><#cc00fa>아기 드래곤</#cc00fa> 크기는 <#fc5454>최대 체력 " + ability(PHANTOM_SCALE_HEALTH, "integer") + "</#fc5454>당 " + ability(PHANTOM_SCALE_STEP, "number") + "씩 증가합니다.</gray>",
                "<gray><#fc5454>최대 체력 " + ability(DRAGON_EVOLUTION, "integer") + "</#fc5454> 이상이면 <#cc00fa>엔더 드래곤</#cc00fa>으로 진화합니다.</gray>",
                "<gray><#cc00fa>엔더 드래곤</#cc00fa>으로 진화하면 추가 능력을 획득합니다.</gray>",
                "<gray>힘 전달 " + ability(TRANSFER_TICKS, "seconds") + " 후 타워 사망, <#fc5454>체력 " + ability(TRANSFER_HEAL, "integer") + "</#fc5454>을 회복합니다.</gray>",
                "<gray>전달 중인 셜커 타워의 <#fc5454>최대 체력 " + ability(TRANSFER_HEAL_RATIO, "percent_integer") + "</#fc5454>만큼 회복합니다.</gray>",
                "<gray>타워 <#ec8d34>피해</#ec8d34>의 <#ec8d34>" + ability(ROUND_DAMAGE_RATIO, "percent_integer") + "</#ec8d34>를 임시 획득, <#ec8d34>" + ability(PERMANENT_DAMAGE_RATIO, "percent_integer") + "</#ec8d34> 영구 누적</gray>",
                "<gray>타워 <#fc5454>체력</#fc5454>의 <#fc5454>" + ability(ROUND_HEALTH_RATIO, "percent_integer") + "</#fc5454>를 임시 획득, <#fc5454>" + ability(PERMANENT_HEALTH_RATIO, "percent_integer") + "</#fc5454> 영구 누적</gray>"
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
