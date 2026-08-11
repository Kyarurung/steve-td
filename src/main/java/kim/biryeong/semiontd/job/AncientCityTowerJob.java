package kim.biryeong.semiontd.job;

import java.util.List;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.ancientcity.AncientCityStates;
import kim.biryeong.semiontd.tower.ancientcity.AncientCityTowers;
import kim.biryeong.semiontd.ui.SemionText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class AncientCityTowerJob extends SemionJob {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "ancient_city");

    public AncientCityTowerJob() {
        super(
                ID,
                Component.literal("고대 도시 빌더"),
                List.of(SemionText.mini("<gray>스컬크 영토를 확장해 마법 능력을 증폭하는 빌더입니다.</gray>"))
        );
    }

    @Override
    public List<Component> description() {
        return List.of(
                SemionText.mini("<gray>라운드 진행과 적 처치로 <aqua>스컬크 영토</aqua>를 넓히는 마법 빌더입니다.</gray>"),
                SemionText.mini("<gray>스컬크 위에 있는 타워는 <aqua>공명</aqua>하여 마법 능력이 강해집니다.</gray>"),
                SemionText.mini("<gray>감지체는 적에게 표식을 남겨 고대 도시 타워의 마법 피해를 강화합니다.</gray>")
        );
    }

    @Override
    public boolean canUseTower(JobContext context, TowerType towerType) {
        return AncientCityTowers.isAncientCityTower(towerType);
    }

    @Override
    public void onMatchStarted(JobContext context) {
        AncientCityStates.clear(context.player().uuid());
    }

    @Override
    public void onRoundStarted(JobContext context, int round) {
        AncientCityStates.onRoundStarted(context.player().uuid(), round);
    }

    @Override
    public void onMonsterKilled(JobContext context, Monster monster, long mineralReward) {
        AncientCityStates.onMonsterKilled(context, monster);
    }

    @Override
    public void onEliminated(JobContext context) {
        AncientCityStates.clear(context.player().uuid());
    }
}
