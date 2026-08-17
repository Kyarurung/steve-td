package kim.biryeong.semiontd.job;

import java.util.List;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.gamble.GambleRoundEffects;
import kim.biryeong.semiontd.tower.gamble.GambleTowers;
import kim.biryeong.semiontd.ui.SemionText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class GambleTowerJob extends SemionJob {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            SemionTd.MOD_ID, "gamble_towers"
    );

    public GambleTowerJob() {
        super(ID, Component.literal("겜블 빌더"), List.of(
                SemionText.mini("<green><bold>시작</bold></green> <gray>주사위·도박꾼·구경꾼 중 필요한 타워를 골라 설치하세요.</gray>"),
                SemionText.mini("<aqua><bold>운영</bold></aqua> <gray>라운드 지원 눈을 배치하고 도박꾼에게 고정 능력치를 누적하세요.</gray>"),
                SemionText.mini("<light_purple><bold>성장</bold></light_purple> <gray>구경꾼의 저점을 높이고 도박꾼의 세 고유 능력을 노리세요.</gray>")
        ));
    }

    @Override
    public boolean canUseTower(JobContext context, TowerType towerType) {
        return GambleTowers.isGambleTower(towerType);
    }

    @Override
    public boolean includesTowerInCatalog(TowerType towerType) {
        return GambleTowers.isGambleTower(towerType);
    }

    @Override
    public void onMatchStarted(JobContext context) {
        clear(context);
    }

    @Override
    public void onRoundEnded(JobContext context, int round) {
        clear(context);
    }

    @Override
    public void onEliminated(JobContext context) {
        clear(context);
    }

    private static void clear(JobContext context) {
        context.game().playerLane(context.player().uuid())
                .ifPresent(lane -> GambleRoundEffects.clearAll(lane, context.player().uuid()));
    }
}
