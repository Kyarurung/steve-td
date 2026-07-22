package kim.biryeong.semiontd.tower.end;

import static kim.biryeong.semiontd.tower.catalog.ProductionTowerDefinitions.tower;
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
    public static final double PHANTOM_BASE_SCALE = 1.0;
    public static final double PHANTOM_SCALE_PER_100_MAX_HEALTH = 0.2;
    public static final EntityVisual DRAGON_EGG_VISUAL = BlockDisplayVisual.builder(Blocks.DRAGON_EGG.defaultBlockState())
            .build();
    public static final EntityVisual PHANTOM_VISUAL = EntityVisual.builder(byId(EntityType.PHANTOM))
            .scale(PHANTOM_BASE_SCALE)
            .build();
    public static final EntityVisual DRAGON_VISUAL = EntityVisual.builder(byId(EntityType.ENDER_DRAGON))
            .build();

    public static final TowerType BASE_END_TOWER = tower(
            "base_ender_dragon",
            "엔더 드래곤",
            0,
            200.0,
            5.0,
            10.0,
            15,
            100,
            DRAGON_EGG_VISUAL,
            List.of(
                    "<gray>엔더 드래곤이 부화하는 핵심 타워입니다.</gray>"
            )
    );

    public static final TowerType T1_ENDERMITE_TOWER = tower(
            "t1_endermite_tower",
            "엔더 마이트",
            50,
            50,
            0,
            10,
            20,
            10,
            byId(EntityType.ENDERMITE),
            List.of(
                    "<gray>공격력이 높은 엔더마이트 입니다.</gray>",
                    "<green>공격을 하지 않지만, 엔드 수정 계열의 힘 전달을 완료하면 엔더 드래곤의 공격 능력을 강화합니다.</green>"
            )
    );

    public static final TowerType T2_ENDERMAN_TOWER = tower(
            "t2_enderman_tower",
            "엔더맨",
            80,
            50,
            0,
            15,
            20,
            10,
            byId(EntityType.ENDERMAN),
            List.of(
                    "<gray>공격력이 높은 엔더맨 입니다.</gray>",
                    "<green>공격을 하지 않지만, 엔드 수정 계열의 힘 전달을 완료하면 엔더 드래곤의 공격 능력을 강화합니다.</green>"
            )
    );

    public static final TowerType T3_END_CRYSTAL_TOWER = tower(
            "t3_end_crystal_tower",
            "엔드 수정",
            130,
            50,
            0,
            20,
            20,
            10,
            byId(EntityType.END_CRYSTAL),
            List.of(
                    "<gray>공격력이 매우 높은 엔드 수정 입니다.</gray>",
                    "<green>공격을 하지 않지만, 엔드 수정 계열의 힘 전달을 완료하면 엔더 드래곤의 공격 능력을 강화합니다.</green>"
            )
    );

    public static final TowerType T1_SHULKER_TOWER = tower(
            "t1_shulker_tower",
            "셜커",
            50,
            100,
            0,
            5,
            20,
            10,
            byId(EntityType.SHULKER),
            List.of(
                    "<gray>체력이 높은 셜커 입니다.</gray>",
                    "<yellow>받는 피해가 {ability.damageReduction:percent} 감소합니다.</yellow>",
                    "<green>공격을 하지 않지만, 셜커 계열의 힘 전달을 완료하면 엔더 드래곤의 내구력을 강화합니다.</green>"
            )
    );

    public static final TowerType T2_SHULKER_TOWER = tower(
            "t2_shulker_tower",
            "견고한 셜커",
            80,
            150,
            0,
            5,
            20,
            10,
            ShulkerVisual.builder().color(DyeColor.PURPLE).build(),
            List.of(
                    "<gray>체력이 높은 견고한 셜커 입니다.</gray>",
                    "<yellow>받는 피해가 {ability.damageReduction:percent} 감소합니다.</yellow>",
                    "<green>공격을 하지 않지만, 셜커 계열의 힘 전달을 완료하면 엔더 드래곤의 내구력을 강화합니다.</green>"
            )
    );

    public static final TowerType T3_SHULKER_TOWER = tower(
            "t3_shulker_tower",
            "완강한 셜커",
            130,
            200,
            0,
            5,
            20,
            10,
            ShulkerVisual.builder().color(DyeColor.BLACK).build(),
            List.of(
                    "<gray>체력이 매우 높은 완강한 셜커 입니다.</gray>",
                    "<yellow>받는 피해가 {ability.damageReduction:percent} 감소합니다.</yellow>",
                    "<green>공격을 하지 않지만, 셜커 계열의 힘 전달을 완료하면 엔더 드래곤의 내구력을 강화합니다.</green>"
            )
    );



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
        List<String> dragonDescription = List.of(
                "<gray>알로 소환되며, 라운드 시작 시 <#B77DE8>아기 드래곤</#B77DE8>으로 변합니다.</gray>",
                "<gray><#E66F6F>최대 체력 {ability.end_global.dragonEvolutionMaxHealth:integer}</#E66F6F> 이상이면 <#B77DE8>엔더 드래곤</#B77DE8>으로 진화합니다.</gray>",
                "<gray><#B77DE8>아기 드래곤</#B77DE8> 크기는 <#E66F6F>최대 체력 100</#E66F6F>당 0.2씩 증가합니다.</gray>",
                "<gray>힘 전달 {ability.end_global.absorptionDurationTicks:seconds} 후 타워 <#D94343>사망</#D94343>, <#E66F6F>체력 {ability.end_global.absorptionHealAmount:integer}</#E66F6F> 회복합니다.</gray>",
                "<gray>전달 중 타워 당 <#E66F6F>체력</#E66F6F>을 초당 <#79C97B>+{ability.end_global.transferHealingPerTower:integer} 재생</#79C97B>합니다.</gray>",
                "<gray><#D94343>공격력</#D94343>: 타워 공격력의 <#D94343>{ability.end_global.roundDamageRatio:percent_integer}</#D94343>를 임시 획득, <#D94343>{ability.end_global.permanentDamageRatio:percent_integer}</#D94343> 영구 누적</gray>",
                "<gray><#E66F6F>체력</#E66F6F>: 타워 체력의 <#E66F6F>{ability.end_global.roundHealthRatio:percent_integer}</#E66F6F>를 임시 획득, <#E66F6F>{ability.end_global.permanentHealthRatio:percent_integer}</#E66F6F> 영구 누적</gray>",
                "<gray><#D9B94F>공격 범위</#D9B94F>: 엔드 수정 <#D9B94F>{ability.end_global.endCrystalSplashThreshold1:integer} / {ability.end_global.endCrystalSplashThreshold2:integer} / {ability.end_global.endCrystalSplashThreshold3:integer} / {ability.end_global.endCrystalSplashThreshold4:integer}</#D9B94F>스택에서 <#D9B94F>+1블록</#D9B94F></gray>",
                "<gray><#D9B94F>공격 속도</#D9B94F>: 엔드 수정 <#D9B94F>{ability.end_global.endCrystalAttackIntervalEvery:integer}</#D9B94F>스택마다 <#D9B94F>-{ability.end_global.attackIntervalReductionPerStep:integer}틱</#D9B94F></gray>",
                "<gray><#D9B94F>사거리</#D9B94F>: 엔드 수정 <#D9B94F>{ability.end_global.endCrystalAttackRangeEvery:integer}</#D9B94F>스택마다 <#D9B94F>+{ability.end_global.attackRangePerStep:blocks}</#D9B94F></gray>",
                "<gray><#D94343>생명력 흡수</#D94343>: 셜커 <#D94343>{ability.end_global.shulkerLifeStealEvery:integer}</#D94343>스택마다 <#D94343>+{ability.end_global.lifeStealPerStep:percent_integer}</#D94343></gray>",
                "<gray><#72A9E6>피해 감소</#72A9E6>: 셜커 <#72A9E6>{ability.end_global.shulkerReductionEvery:integer}</#72A9E6>스택마다 <#72A9E6>+{ability.end_global.damageReductionPerStep:percent_integer}</#72A9E6></gray>",
                "<gray><#79C97B>재생</#79C97B>: 셜커 <#79C97B>{ability.end_global.shulkerRegenerationEvery:integer}</#79C97B>스택마다 초당 <#79C97B>+{ability.end_global.regenerationPerStep:integer}</#79C97B></gray>",
                "<gray><#B77DE8>엔더 드래곤</#B77DE8> 진화 시 <#D94343>최종 피해</#D94343>: <#D94343>+{ability.end_global.dragonFinalDamageBonus:percent_integer}</#D94343> / <#C892E3>저항</#C892E3>: <#C892E3>+{ability.end_global.dragonIncomeDebuffResistance:percent_integer}</#C892E3></gray>",
                "<gray><#D9B94F>추가 사거리</#D9B94F>: <#D9B94F>+{ability.end_global.dragonAttackRangeBonus:blocks}블록</#D9B94F> / <#D94343>공격력 증가율</#D94343>: <#D94343>+{ability.end_global.dragonDamageBonus:percent_integer}</#D94343></gray>"
        );
        TowerDescriptionRegistry.registerTemplate(BASE_END_TOWER, dragonDescription);
        TowerDescriptionRegistry.registerTemplate(T1_ENDERMITE_TOWER, List.of(
                "<gray>공격력이 높은 엔더마이트 입니다.</gray>",
                "<green>공격을 하지 않지만, 엔드 수정 계열의 힘 전달을 완료하면 엔더 드래곤의 공격 능력을 강화합니다.</green>"
        ));
        TowerDescriptionRegistry.registerTemplate(T2_ENDERMAN_TOWER, List.of(
                "<gray>공격력이 높은 엔더맨 입니다.</gray>",
                "<green>공격을 하지 않지만, 엔드 수정 계열의 힘 전달을 완료하면 엔더 드래곤의 공격 능력을 강화합니다.</green>"
        ));
        TowerDescriptionRegistry.registerTemplate(T3_END_CRYSTAL_TOWER, List.of(
                "<gray>공격력이 매우 높은 엔드 수정 입니다.</gray>",
                "<green>공격을 하지 않지만, 엔드 수정 계열의 힘 전달을 완료하면 엔더 드래곤의 공격 능력을 강화합니다.</green>"
        ));
        TowerDescriptionRegistry.registerTemplate(T1_SHULKER_TOWER, List.of(
                "<gray>체력이 높은 셜커 입니다.</gray>",
                "<yellow>받는 피해가 {ability.damageReduction:percent} 감소합니다.</yellow>",
                "<green>공격을 하지 않지만, 셜커 계열의 힘 전달을 완료하면 엔더 드래곤의 내구력을 강화합니다.</green>"
        ));
        TowerDescriptionRegistry.registerTemplate(T2_SHULKER_TOWER, List.of(
                "<gray>체력이 높은 견고한 셜커 입니다.</gray>",
                "<yellow>받는 피해가 {ability.damageReduction:percent} 감소합니다.</yellow>",
                "<green>공격을 하지 않지만, 셜커 계열의 힘 전달을 완료하면 엔더 드래곤의 내구력을 강화합니다.</green>"
        ));
        TowerDescriptionRegistry.registerTemplate(T3_SHULKER_TOWER, List.of(
                "<gray>체력이 매우 높은 완강한 셜커 입니다.</gray>",
                "<yellow>받는 피해가 {ability.damageReduction:percent} 감소합니다.</yellow>",
                "<green>공격을 하지 않지만, 셜커 계열의 힘 전달을 완료하면 엔더 드래곤의 내구력을 강화합니다.</green>"
        ));
    }

    private EndTowers() {
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

    public static boolean isAbsorbableTower(TowerType type) {
        return isEndCrystalLine(type) || isShulkerLine(type);
    }

    public static int absorptionTier(TowerType type) {
        return ProductionTowerCatalog.entry(type)
                .map(ProductionTowerCatalog.CatalogEntry::tier)
                .orElse(0);
    }

    public static double phantomScaleForMaxHealth(double maxHealth) {
        double scale = PHANTOM_BASE_SCALE + Math.max(0.0, maxHealth) / 100.0 * PHANTOM_SCALE_PER_100_MAX_HEALTH;
        return Math.min(5.0, scale);
    }

}
