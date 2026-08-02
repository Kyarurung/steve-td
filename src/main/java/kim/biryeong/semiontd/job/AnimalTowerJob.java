package kim.biryeong.semiontd.job;

import java.util.List;
import java.util.Set;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.animal.AnimalTowers;
import kim.biryeong.semiontd.ui.SemionText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class AnimalTowerJob extends SemionJob {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "animal_towers");
    private static final Set<String> ALLOWED_TOWER_IDS = Set.of(
            AnimalTowers.T1_PIG_TOWER.id(),
            AnimalTowers.T2_PIG_TOWER.id(),
            AnimalTowers.T3_PIG_TOWER.id(),
            AnimalTowers.T4_PIG_LEADER_TOWER.id(),
            AnimalTowers.T1_WOLF_TOWER.id(),
            AnimalTowers.T2_WOLF_DPS_TOWER.id(),
            AnimalTowers.T3_WOLF_DPS_TOWER.id(),
            AnimalTowers.T4_WOLF_LEADER_TOWER.id(),
            AnimalTowers.T1_RABBIT_TOWER.id(),
            AnimalTowers.T2_RABBIT_TOWER.id(),
            AnimalTowers.T3_RABBIT_TOWER.id(),
            AnimalTowers.T4_RABBIT_LEADER_TOWER.id(),
            AnimalTowers.T1_FOX_TOWER.id(),
            AnimalTowers.T2_FOX_TOWER.id(),
            AnimalTowers.T3_FOX_TOWER.id(),
            AnimalTowers.T4_FOX_LEADER_TOWER.id()
    );

    public AnimalTowerJob() {
        super(
                ID,
                Component.literal("동물 빌더"),
                List.of(
                        SemionText.mini("<gray>같은 계열 타워를 모아 무리 스택을 쌓는 빌더입니다.</gray>"),
                        SemionText.mini("<gold>최대 무리의 T3 한 기를 계열당 하나의 우두머리로 승급할 수 있습니다.</gold>"),
                        SemionText.mini("<gray>우두머리는 최대 무리를 유지하는 동안 주변 같은 계열 타워를 강화합니다.</gray>")
                )
        );
    }

    @Override
    public boolean canUseTower(JobContext context, TowerType towerType) {
        return towerType != null && ALLOWED_TOWER_IDS.contains(towerType.id());
    }
}
