package kim.biryeong.semiontd.summon;

import java.util.List;
import kim.biryeong.semiontd.entity.goal.SiegeTrueDamageGoal;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public final class SiegeBreakerSummon extends SummonMonsterType {
    public SiegeBreakerSummon() {
        super(SummonDefinitions.SIEGE_BREAKER);
    }

    @Override
    public List<Goal> createAbilityGoals(SemionMonsterEntity entity) {
        return List.of(new SiegeTrueDamageGoal(
                entity,
                SummonBalancePolicy.SIEGE_BREAKER_PROGRESS_THRESHOLD,
                SummonBalancePolicy.SIEGE_BREAKER_TRUE_DAMAGE,
                SummonBalancePolicy.SIEGE_BREAKER_TRUE_DAMAGE_COOLDOWN_TICKS,
                SummonBalancePolicy.SUPPORT_HEAL_RETRY_TICKS
        ));
    }
}
