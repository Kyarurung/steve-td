package kim.biryeong.semiontd.summon;

import java.util.List;
import kim.biryeong.semiontd.entity.goal.SiegeTrueDamageGoal;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public final class BombardToadSummon extends SummonMonsterType {
    public BombardToadSummon() {
        super(SummonDefinitions.BOMBARD_TOAD);
    }

    @Override
    public List<Goal> createAbilityGoals(SemionMonsterEntity entity) {
        return List.of(new SiegeTrueDamageGoal(
                entity,
                SummonBalancePolicy.BOMBARD_TOAD_PROGRESS_THRESHOLD,
                SummonBalancePolicy.BOMBARD_TOAD_TRUE_DAMAGE,
                SummonBalancePolicy.BOMBARD_TOAD_TRUE_DAMAGE_COOLDOWN_TICKS,
                SummonBalancePolicy.SUPPORT_HEAL_RETRY_TICKS
        ));
    }
}
