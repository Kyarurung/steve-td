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
            "gamble_dice_t1", "주사위 타워 I", 45, 10, 3.5,
            BlockDisplayVisual.builder(Blocks.WHITE_CONCRETE.defaultBlockState()).scale(0.75).build(),
            List.of(
                    "각 주사위 타워는 라운드마다 주사위 한 개를 굴려 범위 안의 내 전투 타워에 같은 효과를 줍니다.",
                    "눈 1~2는 약화, 3~4는 단일 강화, 5~6은 복합 강화입니다.",
                    "강화하면 약화 수치는 유지되고, 긍정 효과와 지원 범위만 증가합니다.",
                    "공격하지 않으며 지원 범위와 이번 라운드 눈금이 타워 머리 위에 표시됩니다."
            )
    );
    public static final TowerType DICE_T2 = support(
            "gamble_dice_t2", "주사위 타워 II", 0, 10, 5,
            BlockDisplayVisual.builder(Blocks.WHITE_CONCRETE.defaultBlockState()).scale(0.90).build(),
            List.of(
                    "긍정 효과가 같은 눈의 I단계보다 2배로 증가하며, 약화 수치는 증가하지 않습니다.",
                    "지원 범위가 5칸으로 넓어집니다.",
                    "눈 1~2는 약화, 3~4는 단일 강화, 5~6은 복합 강화입니다.",
                    "공격하지 않으며 지원 범위와 이번 라운드 눈금이 타워 머리 위에 표시됩니다."
            )
    );
    public static final TowerType DICE_T3 = support(
            "gamble_dice_t3", "주사위 타워 III", 0, 10, 6.5,
            BlockDisplayVisual.builder(Blocks.WHITE_CONCRETE.defaultBlockState()).scale(1.05).build(),
            List.of(
                    "긍정 효과가 같은 눈의 I단계보다 3.5배로 증가하며, 약화 수치는 증가하지 않습니다.",
                    "지원 범위가 6.5칸으로 넓어집니다.",
                    "눈 1~2는 약화, 3~4는 단일 강화, 5~6은 복합 강화입니다.",
                    "공격하지 않으며 지원 범위와 이번 라운드 눈금이 타워 머리 위에 표시됩니다."
            )
    );
    public static final TowerType GAMBLER = TowerType.builder("gamble_gambler", "도박꾼 타워")
            .mineralCost(60).maxHealth(110).range(6.5).damage(10).attackIntervalTicks(13)
            .visual(EntityVisual.builder("minecraft:wandering_trader").scale(1.0).build())
            .description(List.of(
                    "준비 시간에 홀수·짝수는 50 다이아, 주사위 두 개는 100 다이아를 내고 반복할 수 있습니다.",
                    "주사위 눈에 따라 최대 체력·공격력·사거리 중 무작위 능력치가 오르거나 내려갑니다.",
                    "주사위 두 개의 합이 {ability.gamble_global.twoDiceCompoundMinSum:integer} 이상이면 서로 다른 능력치 두 개가 보상을 나눠 받습니다.",
                    "기본 공격은 반경 {ability.gamble_global.baseSplashRadius:blocks} 안의 적에게도 피해를 줍니다.",
                    "좋은 결과가 나오면 {ability.gamble_global.abilityRewardChance:percent} 확률로 능력치 상승 대신 손실 보험을 얻습니다.",
                    "손실 보험은 능력치 감소량을 {ability.gamble_global.lossInsuranceReduction:percent} 줄입니다."
            )).build();
    public static final TowerType SPECTATOR_T1 = support(
            "gamble_spectator_t1", "구경꾼 타워 I", 45, 10, 3.5,
            VillagerVisual.builder().profession(VillagerProfession.NITWIT).build(),
            List.of(
                    "누적 도박 점수가 가장 높은 도박꾼 하나를 집중 지원합니다.",
                    "도박꾼 하나에는 구경꾼이 최대 {ability.gamble_global.maxSpectatorsPerGambler:integer}기까지 연결됩니다.",
                    "눈 1~2는 능력치 2개 약화, 3~4는 능력치 2개 강화, 5~6은 네 능력치 모두 강화입니다.",
                    "눈 6이 나오면 다이아 {ability.faceSixDiamondReward:integer}개를 얻습니다.",
                    "강화하면 약화 수치는 유지되고, 긍정 효과와 연결 범위만 증가합니다.",
                    "공격하지 않으며 연결선과 이번 라운드 눈금이 타워 머리 위에 표시됩니다."
            )
    );
    public static final TowerType SPECTATOR_T2 = support(
            "gamble_spectator_t2", "구경꾼 타워 II", 0, 10, 5,
            VillagerVisual.builder().profession(VillagerProfession.LIBRARIAN).build(),
            List.of(
                    "누적 도박 점수가 가장 높은 도박꾼 하나를 집중 지원합니다.",
                    "도박꾼 하나에는 구경꾼이 최대 {ability.gamble_global.maxSpectatorsPerGambler:integer}기까지 연결됩니다.",
                    "긍정 효과가 같은 눈의 I단계보다 2배로 증가하며, 약화 수치는 증가하지 않습니다.",
                    "눈 1~2는 능력치 2개 약화, 3~4는 능력치 2개 강화, 5~6은 네 능력치 모두 강화입니다.",
                    "눈 6이 나오면 다이아 {ability.faceSixDiamondReward:integer}개를 얻습니다.",
                    "공격하지 않으며 연결선과 이번 라운드 눈금이 타워 머리 위에 표시됩니다."
            )
    );
    public static final TowerType SPECTATOR_T3 = support(
            "gamble_spectator_t3", "구경꾼 타워 III", 0, 10, 6.5,
            VillagerVisual.builder().profession(VillagerProfession.CLERIC).build(),
            List.of(
                    "누적 도박 점수가 가장 높은 도박꾼 하나를 집중 지원합니다.",
                    "도박꾼 하나에는 구경꾼이 최대 {ability.gamble_global.maxSpectatorsPerGambler:integer}기까지 연결됩니다.",
                    "긍정 효과가 같은 눈의 I단계보다 3.5배로 증가하며, 약화 수치는 증가하지 않습니다.",
                    "눈 1~2는 능력치 2개 약화, 3~4는 능력치 2개 강화, 5~6은 네 능력치 모두 강화입니다.",
                    "눈 6이 나오면 다이아 {ability.faceSixDiamondReward:integer}개를 얻습니다.",
                    "공격하지 않으며 연결선과 이번 라운드 눈금이 타워 머리 위에 표시됩니다."
            )
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
            String id, String name, long cost, double health, double range,
            EntityVisual visual, List<String> description
    ) {
        return TowerType.builder(id, name).category(TowerCategory.SUPPORT).mineralCost(cost)
                .maxHealth(health).range(range).damage(0).attackIntervalTicks(20).aggroPriority(-20)
                .visual(visual).description(description)
                .build();
    }
}
