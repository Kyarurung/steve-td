package kim.biryeong.semiontd.tower.queen;

import java.util.List;
import kim.biryeong.semiontd.entity.visual.EntityVisual;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.description.TowerDescriptionRegistry;
import kim.biryeong.semiontd.util.EntityTypeUtil;
import net.minecraft.world.entity.EntityType;

public final class QueenTowers {
    public static final TowerType QUEEN = TowerType.builder("queen", "붉은 여왕")
            .mineralCost(70).maxHealth(140).range(9).damage(0).attackIntervalTicks(120).aggroPriority(1)
            .visual(EntityVisual.builder(EntityTypeUtil.byId(EntityType.EVOKER)).build())
            .description(List.of(
                    "가장 강한 적에게 <red>약체화</red>를 가해 최대체력·공격력·크기를 크게 <red>축소</red>합니다.",
                    "적이 남아 있는 동안 <gold>저놈의 목을 쳐라!</gold>가 {ability.queen_global.giantChargeTicks:seconds} 동안 충전됩니다. 반경 {ability.queen_global.giantAccelerationRadius:blocks}에서 카드병정이 교전하면 2배로 충전됩니다.",
                    "붉은 여왕과 카드병정이 약체화한 적을 자이언트가 현재 체력 {ability.queen_global.giantInitialExecutionHealth:number} 이하부터 <dark_red>처형</dark_red>하며, 성공할수록 처형선이 영구히 증가합니다.",
                    "플레이어당 1기이며 판매할 수 없습니다."
            )).build();

    public static final TowerType RANDOM_CARD_SOLDIER = TowerType.builder("queen_random_card_soldier", "무작위 카드병정")
            .mineralCost(25).maxHealth(45).range(8).damage(0).attackIntervalTicks(10).aggroPriority(0)
            .visual(EntityVisual.builder(EntityTypeUtil.byId(EntityType.VINDICATOR)).build())
            .description(List.of(
                    "설치할 때 표준 52장 중 한 장을 복원 추첨합니다.",
                    "공격할 때마다 최대체력·공격력·크기를 낮추는 <red>약체화</red>를 누적하고 반경 {ability.queen_global.cardSplashRadius:blocks}의 추가 적 {ability.queen_global.cardSplashExtraTargets:int}기도 함께 축소합니다. 사망 시 반경 {ability.queen_global.cardDeathRadius:blocks}에 축소 {ability.queen_global.cardDeathShrinkPoints:number}을 남깁니다.",
                    "하트는 치유, 다이아는 속공, 클로버는 탱킹, 스페이드는 더 넓은 범위 약체화를 담당하며 직접 처치하지 못합니다.",
                    "라인에 수직인 가로 5장을 맞추면 <light_purple>포커 족보</light_purple>가 체력·공속·치유·축소를 강화합니다."
            )).build();

    private static final List<TowerType> ALL = List.of(QUEEN, RANDOM_CARD_SOLDIER);

    static {ALL.forEach(type -> TowerDescriptionRegistry.registerTemplate(type, type.description()));}

    private QueenTowers() {}

    public static List<TowerType> all() {return ALL;}

    public static boolean isQueenTower(TowerType type) {
        return type != null && ALL.stream().anyMatch(candidate -> candidate.id().equals(type.id()));
    }
}
