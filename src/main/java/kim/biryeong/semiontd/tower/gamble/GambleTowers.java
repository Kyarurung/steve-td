package kim.biryeong.semiontd.tower.gamble;

import java.util.List;
import kim.biryeong.semiontd.entity.visual.BlockDisplayVisual;
import kim.biryeong.semiontd.entity.visual.EntityVisual;
import kim.biryeong.semiontd.entity.visual.VillagerVisual;
import kim.biryeong.semiontd.tower.TowerCategory;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.description.TowerDescriptionRegistry;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Blocks;

public final class GambleTowers {
    public static final TowerType DICE_T1 = support(
            "gamble_dice_t1", "주사위 타워 I", 45, 90, 5,
            BlockDisplayVisual.builder(Blocks.WHITE_CONCRETE.defaultBlockState()).scale(0.75).build(),
            "각 주사위 타워는 라운드마다 주사위 한 개를 굴려 범위 안의 내 전투 타워에 같은 효과를 줍니다."
    );
    public static final TowerType DICE_T2 = support(
            "gamble_dice_t2", "주사위 타워 II", 0, 150, 7,
            BlockDisplayVisual.builder(Blocks.WHITE_CONCRETE.defaultBlockState()).scale(0.90).build(),
            "효과 세기는 그대로이며 지원 사거리가 7칸으로 늘어납니다."
    );
    public static final TowerType DICE_T3 = support(
            "gamble_dice_t3", "주사위 타워 III", 0, 240, 9,
            BlockDisplayVisual.builder(Blocks.WHITE_CONCRETE.defaultBlockState()).scale(1.05).build(),
            "효과 세기는 그대로이며 지원 사거리가 9칸으로 늘어납니다."
    );
    public static final TowerType GAMBLER = TowerType.builder("gamble_gambler", "도박꾼 타워")
            .mineralCost(60).maxHealth(110).range(6.5).damage(10).attackIntervalTicks(13)
            .visual(EntityVisual.builder("minecraft:wandering_trader").scale(1.0).build())
            .description(List.of(
                    "준비 시간에 100 다이아를 내고 홀수·짝수 맞히기 또는 주사위 두 개 굴리기를 반복할 수 있습니다.",
                    "주사위 눈에 따라 최대 체력·공격력·사거리 중 무작위 능력치가 오르거나 내려갑니다.",
                    "기본 공격은 고정된 반경 2.5칸 안의 적에게도 피해를 줍니다.",
                    "좋은 결과가 나오면 낮은 확률로 손실 보험·행운의 일격·분산 배당 중 하나를 얻습니다.",
                    GambleAbility.LOSS_INSURANCE.defaultDetailLine(),
                    GambleAbility.LUCKY_STRIKE.defaultDetailLine(),
                    GambleAbility.SPREAD_BET.defaultDetailLine()
            )).build();
    public static final TowerType SPECTATOR_T1 = spectator(
            "gamble_spectator_t1", "구경꾼 타워 I", 45, 80,
            VillagerVisual.builder().profession(VillagerProfession.NITWIT).build(),
            "주사위는 1~6이 나옵니다."
    );
    public static final TowerType SPECTATOR_T2 = spectator(
            "gamble_spectator_t2", "구경꾼 타워 II", 0, 135,
            VillagerVisual.builder().profession(VillagerProfession.LIBRARIAN).build(),
            "주사위는 2~6만 나오며 버프의 효과가 더 강해집니다."
    );
    public static final TowerType SPECTATOR_T3 = spectator(
            "gamble_spectator_t3", "구경꾼 타워 III", 0, 220,
            VillagerVisual.builder().profession(VillagerProfession.CLERIC).build(),
            "주사위는 3~6만 나오며 버프의 효과가 가장 강해집니다."
    );

    private static final List<TowerType> ALL = List.of(
            DICE_T1, DICE_T2, DICE_T3, GAMBLER, SPECTATOR_T1, SPECTATOR_T2, SPECTATOR_T3
    );

    static {
        ALL.forEach(type -> TowerDescriptionRegistry.registerTemplate(type, type.description()));
    }

    private GambleTowers() {
    }

    public static List<TowerType> all() {
        return ALL;
    }

    public static boolean isGambleTower(TowerType type) {
        return type != null && ALL.stream().anyMatch(candidate -> candidate.id().equals(type.id()));
    }

    public static boolean isDice(TowerType type) {
        return matches(type, DICE_T1) || matches(type, DICE_T2) || matches(type, DICE_T3);
    }

    public static boolean isSpectator(TowerType type) {
        return matches(type, SPECTATOR_T1) || matches(type, SPECTATOR_T2) || matches(type, SPECTATOR_T3);
    }

    private static boolean matches(TowerType actual, TowerType expected) {
        return actual != null && actual.id().equals(expected.id());
    }

    private static TowerType support(
            String id, String name, long cost, double health, double range, EntityVisual visual, String description
    ) {
        return TowerType.builder(id, name).category(TowerCategory.SUPPORT).mineralCost(cost)
                .maxHealth(health).range(range).damage(0).attackIntervalTicks(20).aggroPriority(-20)
                .visual(visual).description(List.of(description,
                        "눈 1~2는 약화, 3~6은 강화이며 여러 지원 타워의 효과는 함께 적용됩니다.",
                        "강화 눈은 사거리·초당 회복·공격력·최대 체력 중 하나를 올립니다.",
                        "공격하지 않으며 지원 범위와 이번 라운드 눈금이 타워 머리 위에 표시됩니다."))
                .build();
    }

    private static TowerType spectator(
            String id, String name, long cost, double health, EntityVisual visual, String description
    ) {
        return support(id, name, cost, health, 5, visual,
                "각 구경꾼은 라운드마다 주사위 한 개를 굴려 범위 안의 도박꾼 타워에 같은 효과를 줍니다. "
                        + description + " 강화할수록 버프의 효과가 올라갑니다.");
    }
}
