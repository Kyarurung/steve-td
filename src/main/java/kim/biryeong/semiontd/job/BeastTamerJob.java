package kim.biryeong.semiontd.job;

import java.util.List;
import kim.biryeong.semiontd.tower.TowerFaction;
import net.minecraft.network.chat.Component;

public final class BeastTamerJob extends FactionTowerJob {
    public BeastTamerJob() {
        super(
                "beast_tamer",
                "동물 조련사",
                TowerFaction.BEAST,
                List.of(
                        Component.literal("동물 타워만 사용할 수 있습니다."),
                        Component.literal("전투 중 분노가 충전됩니다. 분노가 높아질수록 공격속도가 오릅니다.")
                )
        );
    }
}
