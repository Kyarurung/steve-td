package kim.biryeong.semiontd.job;

import java.util.List;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.legion.LegionTowers;
import kim.biryeong.semiontd.ui.SemionText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class LegionTowerJob extends SemionJob {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "legion_towers");

    public LegionTowerJob() {
        super(
                ID,
                Component.literal("무리 빌더"),
                List.of(
                        SemionText.mini("<gray>여러 타워와 분신을 전개해 물량으로 전선을 유지하는 빌더입니다.</gray>"),
                        SemionText.mini("<gray>같은 계열을 모으면 무리 효과가 강화되고, 지원 타워로 본체와 분신을 함께 강화할 수 있습니다.</gray>")
                )
        );
    }

    @Override
    public boolean canUseTower(JobContext context, TowerType towerType) {
        if (!LegionTowers.isLegionTower(towerType)) {
            return false;
        }
        if (!towerType.id().equals(LegionTowers.ILLUSION_TOWER.id())) {
            return true;
        }
        return context.game().playerLane(context.player().uuid())
                .map(lane -> lane.towers().stream()
                        .map(Tower::type)
                        .noneMatch(type -> type.id().equals(LegionTowers.ILLUSION_TOWER.id())))
                .orElse(true);
    }

    @Override
    public boolean includesTowerInCatalog(TowerType towerType) {
        return LegionTowers.isLegionTower(towerType);
    }
}
