package kim.biryeong.semiontd.job;

import java.util.List;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.adversary.AdversaryBalance;
import kim.biryeong.semiontd.tower.adversary.AdversaryFoxTower;
import kim.biryeong.semiontd.tower.adversary.AdversaryProgressStates;
import kim.biryeong.semiontd.tower.adversary.AdversaryTeamEffects;
import kim.biryeong.semiontd.tower.adversary.AdversaryTowers;
import kim.biryeong.semiontd.ui.SemionText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class AdversaryTowerJob extends SemionJob {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "adversary_towers");

    public AdversaryTowerJob() {
        super(
                ID,
                Component.literal("대적자 빌더"),
                List.of(
                        SemionText.mini("<gray><gold>여우 최대 4기</gold>와 <red>숙적</red>을 함께 운용합니다.</gray>"),
                        SemionText.mini("<gray>숙적 점수를 공유하고, 각 여우의 전직을 직접 선택합니다.</gray>")
                )
        );
    }

    @Override
    public List<Component> description() {
        return List.of(
                SemionText.mini("<gold>여우는 플레이어당 최대 4기까지 설치할 수 있습니다.</gold>"),
                SemionText.mini("<gray>숙적은 타워 슬롯을 차지하며, 웨이브가 시작되면 설치한 자리에서 적으로 변합니다.</gray>"),
                SemionText.mini("<gray>숙적은 플레이어 특성 효과를 받지 않습니다.</gray>"),
                SemionText.mini("<gray>숙적을 여우가 직접 처치하면 종류에 맞는 <yellow>전직 점수</yellow>를 얻습니다. 강화 숙적은 2점을 줍니다.</gray>"),
                SemionText.mini("<gray>전직 점수는 모든 여우가 공유하지만, 전직할 때 필요한 점수를 사용합니다.</gray>"),
                SemionText.mini("<gray>같은 전직 계열은 한 여우만 선택할 수 있고, 중간 형태로 웨이브를 한 번 완료해야 최종 전직할 수 있습니다.</gray>"),
                SemionText.mini("<gray>최종 전직 후 남은 점수는 모든 최종 여우의 피해를 올립니다.</gray>"),
                SemionText.mini("<gray>숙적을 처치하면 체력을 회복하며, 여러 적에게 집중 공격받을수록 받는 피해가 줄어듭니다.</gray>"),
                SemionText.mini("<gray>준비 단계의 업그레이드 메뉴에서 첫 전직은 <aqua>200 다이아</aqua>, 최종 전직은 <aqua>400 다이아</aqua>를 사용해 직접 선택합니다. 인컴 적은 점수를 주지 않습니다.</gray>"),
                SemionText.mini("<aqua>첫 전직은 질풍 여우, 종지기 여우, 추적자 여우, 메아리 여우 중 하나이며 각 계열에서 최종 형태 2종으로 갈립니다.</aqua>"),
                SemionText.mini("<red>숙적을 판매해 점수가 부족해지면 최근에 전직한 여우부터 강등됩니다.</red>"),
                SemionText.mini("<yellow>여우를 판매하면 사용 중이던 점수와 전직 계열을 반환합니다.</yellow>")
        );
    }

    @Override
    public boolean canUseTower(JobContext context, TowerType towerType) {
        if (!AdversaryTowers.isAdversaryTower(towerType)) {
            return false;
        }
        if (!AdversaryTowers.isFox(towerType)
                || !AdversaryTowers.matches(towerType, AdversaryTowers.FOX)
                || context == null) {
            return true;
        }
        int maximum = AdversaryBalance.globalInt("maxFoxTowers", AdversaryBalance.MAX_FOX_TOWERS);
        return context.game().playerLane(context.player().uuid())
                .map(lane -> lane.towers().stream()
                        .map(Tower::type)
                        .filter(AdversaryTowers::isFox)
                        .count() < maximum)
                .orElse(true);
    }

    @Override
    public boolean includesTowerInCatalog(TowerType towerType) {
        return AdversaryTowers.isAdversaryTower(towerType);
    }

    @Override
    public void onMatchStarted(JobContext context) {
        AdversaryProgressStates.clear(context.player().uuid());
        var team = context.game().teams().get(context.player().teamId());
        if (team != null) {
            AdversaryTeamEffects.registerTeam(context.player().uuid(), team.laneGroup());
        }
    }

    @Override
    public void onRoundStarted(JobContext context, int round) {
        context.game().playerLane(context.player().uuid())
                .ifPresent(lane -> AdversaryProgressStates.reconcileLane(context.player().uuid(), lane));
    }

    @Override
    public void onRoundEnded(JobContext context, int round) {
        context.game().playerLane(context.player().uuid())
                .ifPresent(lane -> lane.towers().stream()
                        .filter(AdversaryFoxTower.class::isInstance)
                        .map(AdversaryFoxTower.class::cast)
                        .filter(tower -> context.player().uuid().equals(tower.ownerPlayer()))
                        .forEach(tower -> AdversaryProgressStates.state(context.player().uuid())
                                .recordCompletedWave(tower.foxId(), tower.form())));
    }

    @Override
    public void onEliminated(JobContext context) {
        AdversaryTeamEffects.unregisterPlayer(context.player().uuid());
        AdversaryProgressStates.clear(context.player().uuid());
    }
}
