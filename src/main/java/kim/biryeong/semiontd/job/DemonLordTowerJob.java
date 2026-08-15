package kim.biryeong.semiontd.job;

import java.util.List;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.demonlord.DemonLordState;
import kim.biryeong.semiontd.tower.demonlord.DemonLordStates;
import kim.biryeong.semiontd.tower.demonlord.DemonLordTowers;
import kim.biryeong.semiontd.ui.SemionText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The demon lord builder: the player is the defense, the towers are only a skill bar.
 *
 * <p>Round start puts the player into 전투 상태 with a full health pool; killing monsters feeds the
 * level curve, which is this builder's only source of scaling.
 */
public final class DemonLordTowerJob extends SemionJob {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "demon_lord_towers");

    public DemonLordTowerJob() {
        super(
                ID,
                Component.literal("마왕 빌더"),
                List.of(SemionText.mini("<gray>타워 대신 마왕 본인이 레인에서 직접 싸우는 빌더입니다.</gray>"))
        );
    }

    @Override
    public List<Component> description() {
        return List.of(
                SemionText.mini("<gray>타워는 <yellow>스킬을 주는 제단</yellow>일 뿐, 공격도 방어도 어그로도 없습니다.</gray>"),
                SemionText.mini("<gray>스킬은 <green>종류별로 하나만</green> 지을 수 있고 코스트를 차지합니다.</gray>"),
                SemionText.mini("<gray>핫바 <aqua>4~8번</aqua>에 스킬이 놓이고, 그 슬롯으로 손을 옮기면 즉시 발동합니다.</gray>"),
                SemionText.mini("<yellow>라운드가 시작되면 자기 레인 중앙으로 끌려가 [전투 상태]가 됩니다.</yellow>"),
                SemionText.mini("<red>보스바 체력이 다 닳으면 [전투 제외]가 되어 다음 라운드까지 아무것도 못 합니다.</red>"),
                SemionText.mini("<green>몹을 처치할수록 레벨이 올라 체력과 피해량이 함께 성장합니다.</green>")
        );
    }

    @Override
    public boolean canUseTower(JobContext context, TowerType towerType) {
        return DemonLordTowers.isDemonLordTower(towerType);
    }

    @Override
    public void onMatchStarted(JobContext context) {
        DemonLordStates.clear(context.player().uuid());
        DemonLordStates.getOrCreate(context.player().uuid());
    }

    /**
     * Every round hands the demon lord a fresh pool, which is also how a player who was knocked out
     * last round comes back: {@code enterCombat} refills health and clears cooldowns.
     */
    @Override
    public void onRoundStarted(JobContext context, int round) {
        DemonLordState state = DemonLordStates.getOrCreate(context.player().uuid());
        if (state != null) {
            state.enterCombat();
        }
    }

    /**
     * Kills are the builder's entire scaling curve, so experience is weighted by how tanky the
     * victim was rather than being a flat per-kill number.
     */
    @Override
    public void onMonsterKilled(JobContext context, Monster monster, long mineralReward) {
        if (monster == null) {
            return;
        }
        DemonLordState state = DemonLordStates.get(context.player().uuid());
        if (state == null || !state.inCombat()) {
            return;
        }
        double perMaxHealth = TowerBalanceRuntime.ability(
                DemonLordTowers.GLOBAL_CONFIG_ID, "experiencePerMaxHealth", 0.02);
        state.addExperience(Math.max(0.0, monster.maxHealth() * perMaxHealth));
    }

    @Override
    public void onEliminated(JobContext context) {
        DemonLordStates.clear(context.player().uuid());
    }
}
