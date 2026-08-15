package kim.biryeong.semiontd.job;

import static kim.biryeong.semiontd.tower.warlock.WarlockFormatting.warningText;
import static kim.biryeong.semiontd.tower.warlock.WarlockFormatting.warlockText;

import java.util.ArrayList;
import java.util.List;
import kim.biryeong.semiontd.SemionTd;
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
                        SemionText.mini("<gray>아군 타워를 희생해 " + warlockText("흑마법사") + " 타워를 키우는 빌더입니다.</gray>")
                )
        );
    }

    @Override
    public List<Component> description() {
        ArrayList<Component> lines = new ArrayList<>();
        lines.add(SemionText.mini("<gray>아군 타워를 희생해 " + warlockText("흑마법사") + " 타워를 키우는 빌더입니다.</gray>"));
        lines.add(SemionText.mini("<gray>능력치는 높아질수록 증가 효율이 감소합니다.</gray>"));
        if (WarlockTowers.awakeningEnabled()) {
            lines.add(SemionText.mini("<gray>한 라운드에 충분한 타워를 흡수하고 " + warlockText("흑마법사") + "만 생존한 채 체력이 낮아지면 " + warlockText("각성") + "합니다.</gray>"));
            lines.add(SemionText.mini("<gray>원거리는 회복·재생, 근거리는 피해·이동 속도를 얻으며 각성은 라운드 종료 시 해제됩니다.</gray>"));
        } else {
            lines.add(SemionText.mini(warningText("현재 각성은 비활성화 상태입니다.")));
        }
        lines.add(SemionText.mini(warningText(warlockText("흑마법사") + " 타워는 한 라인에 하나만 운용할 수 있습니다.")));
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
}
