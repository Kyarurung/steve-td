package kim.biryeong.semiontd.job;

import java.util.List;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.demonlord.DemonLordState;
import kim.biryeong.semiontd.tower.demonlord.DemonLordService;
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
                SemionText.mini("<gray>시작 타워 대신 마왕이 직접 레인을 지킵니다. 같은 스킬 제단은 하나만 지을 수 있고 타워 한도를 2~4칸 차지합니다.</gray>"),
                SemionText.mini("<gray>운영 단계에서는 먼저 지은 7개 스킬이 <aqua>1~5번, F, Q</aqua>에 배정됩니다. 5번은 우클릭, 나머지는 키 입력으로 사용하며 마검은 9번에 고정됩니다. 스킬 10종의 총 코스트는 32입니다.</gray>"),
                SemionText.mini("<green>성장 과정에서 보스바 체력이 0이 되면 해당 라운드에는 공격과 스킬만 막힙니다. 처치로 최대 체력과 피해량을 키우며, 다음 라운드에 최대 체력으로 복귀합니다.</green>")
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
        DemonLordService.clearPlayerState(context.player().uuid());
    }
}
