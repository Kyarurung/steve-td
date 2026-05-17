package kim.biryeong.semiontd.tower.villager;

import static kim.biryeong.semiontd.tower.catalog.ProductionTowerDefinitions.tower;

import java.util.List;
import kim.biryeong.semiontd.entity.visual.VillagerVisual;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.entity.npc.VillagerProfession;

public final class VillagerTowers {
    public static final TowerType T1_SPLASH_TOWER = tower(
            "villager_splash_t1",
            "주민 원거리 기본 타워",
            70,
            5.5,
            5,
            20,
            10,
            0,
            VillagerVisual.builder().profession(VillagerProfession.NITWIT).build(),
            List.of("<gray>기본 주민 원거리 타워입니다.</gray>")
    );

    private VillagerTowers() {
    }
}
