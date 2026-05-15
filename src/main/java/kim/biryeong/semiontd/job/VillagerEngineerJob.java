package kim.biryeong.semiontd.job;

import java.util.List;
import kim.biryeong.semiontd.tower.TowerFaction;
import net.minecraft.network.chat.Component;

public final class VillagerEngineerJob extends FactionTowerJob {
    public VillagerEngineerJob() {
        super(
                "villager_engineer",
                "주민 기술자",
                TowerFaction.VILLAGER,
                List.of(
                        Component.literal("주민 타워만 사용할 수 있습니다."),
                        Component.literal("매 라운드 진행 시 타워가 성장합니다.")
                )
        );
    }
}
