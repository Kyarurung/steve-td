package kim.biryeong.semiontd.tower.legion;

import kim.biryeong.semiontd.entity.visual.ChickenVisual;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.entity.animal.ChickenVariants;

import java.util.List;

import static kim.biryeong.semiontd.tower.catalog.ProductionTowerDefinitions.*;

public class LegionTowers {
    public static final TowerType T1_CHICKEN = tower(
            "t1_chicken",
            "닭 타워",
            60,
            30,
            6,
            4,
            15,
            20,
            ChickenVisual.builder().variant(ChickenVariants.DEFAULT).build(),
            List.of(
                    "<gray> 닭들은 늑구가 싫어서 동물 빌더에 들어가지 않기로 결심했다네요.</gray>",
                    "<green> 웨이브 방어 시작 시 자신의 체력과 공격력의 50%를 가진 분신을 1기 소환합니다.",
                    "<green> 분신은 본체 타워보다 공격 우선순위가 높습니다. </green>"
            )
    );

    public static final TowerType T2_CHICKEN_TOWER = tower(
            "t2_legion_chicken_tower",
            "인싸 닭 타워",
            100,
            40,
            6,
            7,
            15,
            15,
            ChickenVisual.builder().variant(ChickenVariants.WARM).build(),
            List.of(
                    "<gray> 친구를 더 데려온 닭 타워 입니다. 친구를 많이 데려오려고 해서 그런지 모습이 바뀌었네요.</gray>",
                    "<green> 웨이브 방어 시작 시 자신의 체력과 공격력의 50%를 가진 분신을 1기 소환합니다.",
                    "<green> 분신은 본체 타워보다 공격 우선순위가 높습니다. </green>",
                    "<green> 공격 시 공객 대상 주위 0.75 블록 내의 대상에게 50% 스플래시 피해를 입힙니다."
            )
    );

    public static final TowerType T2_DPS_CHICKEN_TOWER = tower(
            "t2_dps_chicken_tower",
            "아찐 닭 타워",
            100,
            80,
            7,
            12,
            15,
            10,
            ChickenVisual.builder().variant(ChickenVariants.COLD).build(),
            List.of(
                    "<gray> 이 닭은 친구가 없어서 분신을 소환할 수 없답니다. </gray>",
                    "<green> "
            )
    );


}
