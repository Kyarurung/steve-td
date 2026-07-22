package kim.biryeong.semiontd.job;

import java.util.List;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.ocean.OceanTowers;
import kim.biryeong.semiontd.ui.SemionText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class OceanTowerJob extends SemionJob {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "ocean");

    public OceanTowerJob() {
        super(
                ID,
                Component.literal("바다 빌더"),
                List.of(SemionText.mini("<gray>물을 저장하고 소비해 전투력이 성장하는 물고기 빌더입니다.</gray>"))
        );
    }

    @Override
    public List<Component> description() {
        return List.of(
                SemionText.mini("<gray>바다 전투 타워는 공격과 능력에 물을 소모하며, 저장한 물은 라운드가 끝나도 유지됩니다.</gray>"),
                SemionText.mini("<gray>물을 많이 저장할수록 공격력이 증가하고, 물 타워와 탱커가 아군에게 물을 공급합니다.</gray>"),
                SemionText.mini("<gray>물 타워는 웨이브 시작 시 반경 2블록 안의 살아 있는 타워를 공급 대상으로 고정합니다.</gray>"),
                SemionText.mini("<red>물이 없으면 능력이 정지하고 공격력·공격 속도가 크게 감소하며, 매초 최대 체력 비례 피해를 받습니다.</red>")
        );
    }

    @Override
    public boolean canUseTower(JobContext context, TowerType towerType) {
        return OceanTowers.isOceanTower(towerType);
    }
}
