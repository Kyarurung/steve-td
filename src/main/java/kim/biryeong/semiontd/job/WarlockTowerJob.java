package kim.biryeong.semiontd.job;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.summon.SummonMonsterType;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.warlock.WarlockTowers;
import kim.biryeong.semiontd.ui.SemionText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class WarlockTowerJob extends SemionJob {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "warlock_towers");

    public WarlockTowerJob() {
        super(
                ID,
                Component.literal("흑마법사"),
                List.of(
                        SemionText.mini("<gray>아군 타워를 희생해 <dark_purple>흑마법사</dark_purple> 타워를 키우는 빌더입니다.</gray>")
                )
        );
    }

    @Override
    public List<Component> description() {
        ArrayList<Component> lines = new ArrayList<>();
        lines.add(SemionText.mini("<gray>아군 타워를 희생해 <dark_purple>흑마법사</dark_purple> 타워를 키우는 빌더입니다.</gray>"));
        lines.add(SemionText.mini("<gray>흡수로 얻는 추가 피해는 <#ec8d34>" + ability("damageSoftCap") + "</#ec8d34>까지 그대로 적용되며, 이후 완만하게 증가합니다.</gray>"));
        if (WarlockTowers.awakeningEnabled()) {
            lines.add(SemionText.mini("<gray>한 라운드에 충분한 타워를 흡수하고 <dark_purple>흑마법사</dark_purple>만 생존한 채 체력이 낮아지면 <dark_purple>각성</dark_purple>합니다.</gray>"));
            lines.add(SemionText.mini("<gray>원거리는 회복·재생, 근거리는 피해·이동 속도를 얻으며 각성은 라운드 종료 시 해제됩니다.</gray>"));
        }
        lines.add(SemionText.mini("<red><dark_purple>흑마법사</dark_purple> 타워는 한 라인에 하나만 운용할 수 있습니다.</red>"));
        return List.copyOf(lines);
    }

    @Override
    public boolean canUseTower(JobContext context, TowerType towerType) {
        if (!WarlockTowers.isWarlockTower(towerType)) {
            return false;
        }
        if (!WarlockTowers.isWarlockCore(towerType) || !towerType.id().equals(WarlockTowers.BASE_WARLOCK_TOWER.id())) {
            return true;
        }
        return context.game().playerLane(context.player().uuid())
                .map(lane -> lane.towers().stream()
                        .map(Tower::type)
                        .noneMatch(WarlockTowers::isWarlockCore))
                .orElse(true);
    }

    @Override
    public boolean includesTowerInCatalog(TowerType towerType) {
        return WarlockTowers.isWarlockTower(towerType);
    }

    private static String ability(String key) {
        return BigDecimal.valueOf(TowerBalanceRuntime.ability(WarlockTowers.CONFIG_ID, key))
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }
}
