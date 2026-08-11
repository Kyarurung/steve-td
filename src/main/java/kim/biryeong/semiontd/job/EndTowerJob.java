package kim.biryeong.semiontd.job;

import static kim.biryeong.semiontd.tower.end.EndConfig.Ability.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.end.EndTowers;
import kim.biryeong.semiontd.tower.end.EndConfig.Ability;
import kim.biryeong.semiontd.ui.SemionText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class EndTowerJob extends SemionJob {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "end_towers");

    public EndTowerJob() {
        super(
                ID,
                Component.literal("엔드 빌더"),
                List.of(
                        SemionText.mini("<gray>타워를 설치해 <#cc00fa>엔더 드래곤</#cc00fa>을</gray>"),
                        SemionText.mini("<gray>성장시키는 빌더입니다.</gray>")
                )
        );
    }

    @Override
    public List<Component> description() {
        return List.of(
                SemionText.mini("<gray>아군 타워의 <#fc5454>체력</#fc5454>과 <#ec8d34>피해</#ec8d34>를</gray>"),
                SemionText.mini("<gray>흡수해 " + seconds() + "에 걸쳐 힘을 얻습니다.</gray>"),
                SemionText.mini("<gray><#fc5454>체력 " + percent(ROUND_HEALTH_RATIO) + "</#fc5454>, <#ec8d34>피해 " + percent(ROUND_DAMAGE_RATIO) + "</#ec8d34>를</gray>"),
                SemionText.mini("<gray>해당 라운드 동안 얻고,</gray>"),
                SemionText.mini("<gray><#fc5454>체력 " + percent(PERMANENT_HEALTH_RATIO) + "</#fc5454>, <#ec8d34>피해 " + percent(PERMANENT_DAMAGE_RATIO) + "</#ec8d34>를 영구 누적합니다.</gray>"),
                SemionText.mini("<gray>흡수로 얻는 추가 <#fc5454>체력은 " + number(HEALTH_THRESHOLD) + "</#fc5454>, <#ec8d34>피해는 " + number(DAMAGE_THRESHOLD) + "</#ec8d34>까지 그대로 적용됩니다.</gray>"),
                SemionText.mini("<gray>기준을 넘긴 누적 능력치는 완만하게 적용됩니다.</gray>"),
                Component.empty(),
                SemionText.mini("<gray><#fc5454>셜커</#fc5454> 계열은 <#fc5454>체력</#fc5454>을,</gray>"),
                SemionText.mini("<gray><#ec8d34>엔드 수정</#ec8d34> 계열은 <#ec8d34>피해</#ec8d34>를 강화합니다.</gray>"),
                Component.empty(),
                SemionText.mini("<gray><#cc00fa>엔더 드래곤</#cc00fa>으로 진화하면</gray>"),
                SemionText.mini("<gray>추가 <yellow>고유 능력</yellow>을 획득합니다.</gray>")
        );
    }

    @Override
    public boolean canUseTower(JobContext context, TowerType towerType) {
        if (!EndTowers.isEndTower(towerType)) {
            return false;
        }
        if (!EndTowers.isBaseEndTower(towerType) || context == null) {
            return true;
        }
        return context.game().playerLane(context.player().uuid())
                .map(lane -> lane.towers().stream()
                        .map(Tower::type)
                        .noneMatch(EndTowers::isBaseEndTower))
                .orElse(true);
    }

    private static String seconds() {
        return number(TowerBalanceRuntime.ability(EndTowers.CONFIG_ID, Ability.TRANSFER_TICKS.key()) / 20.0) + "초";
    }

    private static String percent(Ability ability) {
        return number(TowerBalanceRuntime.ability(EndTowers.CONFIG_ID, ability.key()) * 100.0) + "%";
    }

    private static String number(Ability ability) {
        return number(TowerBalanceRuntime.ability(EndTowers.CONFIG_ID, ability.key()));
    }

    private static String number(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }
}
