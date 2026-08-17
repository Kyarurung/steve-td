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
                SemionText.mini("<green><bold>시작</bold></green> <gray>도박꾼으로 적을 막고 주사위 타워와 구경꾼으로 지원하세요.</gray>"),
                SemionText.mini("<aqua><bold>운영</bold></aqua> <gray>주사위 타워는 범위 안 전투 타워를, 구경꾼은 가장 강한 도박꾼 하나를 지원합니다.</gray>"),
                SemionText.mini("<light_purple><bold>성장</bold></light_purple> <gray>지원을 강화하고 도박꾼의 무작위 능력치와 손실 보험을 키우세요.</gray>")
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
