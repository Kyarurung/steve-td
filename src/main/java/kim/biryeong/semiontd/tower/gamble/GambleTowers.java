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
            "라운드 시작에 사거리 안의 다른 내 타워마다 d6을 굴려 이번 라운드 효과를 부여합니다."
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
            .mineralCost(60).maxHealth(100).range(6).damage(8).attackIntervalTicks(16)
            .visual(EntityVisual.builder("minecraft:wandering_trader").scale(1.0).build())
            .description(List.of(
                    "준비 단계에 100 다이아를 내고 홀·짝 또는 2d6 도박을 반복합니다.",
                    "도박은 최대 체력·공격력·사거리에 <gold>고정 수치</gold>를 가감하며 복리로 계산하지 않습니다.",
                    "성공하면 낮은 확률로 손실 보험·행운의 일격·분산 배당을 얻습니다."
            )).build();
    public static final TowerType SPECTATOR_T1 = spectator(
            "gamble_spectator_t1", "구경꾼 타워 I", 45, 80,
            VillagerVisual.builder().profession(VillagerProfession.NITWIT).build(), 1,
            "각 대상마다 1~6을 굴립니다."
    );
    public static final TowerType SPECTATOR_T2 = spectator(
            "gamble_spectator_t2", "구경꾼 타워 II", 0, 135,
            VillagerVisual.builder().profession(VillagerProfession.LIBRARIAN).build(), 2,
            "각 대상마다 2~6을 굴리고 긍정 효과가 1.25배가 됩니다."
    );
    public static final TowerType SPECTATOR_T3 = spectator(
            "gamble_spectator_t3", "구경꾼 타워 III", 0, 220,
            VillagerVisual.builder().profession(VillagerProfession.CLERIC).build(), 3,
            "각 대상마다 3~6을 굴리고 긍정 효과가 1.5배가 됩니다."
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
                        "눈 1~2는 디버프, 3~6은 버프이며 출처가 다른 효과는 %p로 가산됩니다."))
                .build();
    }

    private static TowerType spectator(
            String id, String name, long cost, double health, EntityVisual visual, int tier, String description
    ) {
        return support(id, name, cost, health, 5, visual,
                description + " 강화할수록 저점과 긍정 효과가 올라갑니다. (T" + tier + ")");
    }
}
