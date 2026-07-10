package kim.biryeong.semiontd.job;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.nether.NetherTower;
import kim.biryeong.semiontd.tower.nether.NetherTowers;
import kim.biryeong.semiontd.ui.SemionText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class NetherTowerJob extends SemionJob {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "nether");

    public NetherTowerJob() {
        super(
                ID,
                Component.literal("네더 빌더"),
                List.of(
                        SemionText.mini("<gray>체력을 소모하고 흡혈로 버티는 타워를 사용합니다.</gray>"),
                        SemionText.mini("<gray>네더 상태에서 첫 사망 시 좀비 상태로 전환됩니다.</gray>")
                )
        );
    }

    @Override
    public List<Component> description() {
        return List.of(
                SemionText.mini("<gray>웨이브에 몬스터가 남아 있으면 네더 상태 타워는 초당 최대 체력의 "
                        + percent("netherDecayMaxHealthRatioPerSecond") + "를 잃고 기본 "
                        + percent("netherLifeStealRatio") + " 흡혈을 가집니다.</gray>"),
                SemionText.mini("<red>[좀비] 처음 체력이 0이 되면 최대 체력의 "
                        + percent("zombieReviveHealthRatio") + "로 회복하고 좀비 상태가 됩니다. 초당 최대 체력의 "
                        + percent("zombieDecayMaxHealthRatioPerSecond") + "를 잃으며 기본 흡혈은 "
                        + percent("zombieLifeStealRatio") + "입니다.</red>"),
                SemionText.mini("<gray>체력이 " + percent("lowHealthThreshold")
                        + " 이하로 내려가면 잃은 체력에 비례해 공격력과 흡혈이 증가합니다. 공격력은 최대 "
                        + percent("lowHealthDamageBonusCap") + ", 흡혈은 최대 "
                        + percent("lifeStealBonusCap") + "까지 증가합니다.</gray>"),
                SemionText.mini("<yellow>체력이 " + percent("criticalHealthThreshold")
                        + " 이하일 때 타워별 추가 능력이 발동합니다.</yellow>")
        );
    }

    @Override
    public boolean canUseTower(JobContext context, TowerType towerType) {
        return NetherTowers.isNetherTower(towerType);
    }

    private static String percent(String key) {
        BigDecimal value = BigDecimal.valueOf(TowerBalanceRuntime.ability(NetherTower.CONFIG_ID, key) * 100.0)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return value.toPlainString() + "%";
    }
}
