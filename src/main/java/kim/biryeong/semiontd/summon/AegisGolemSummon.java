package kim.biryeong.semiontd.summon;

import java.util.List;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.goal.ApplyMonsterTimedEffectGoal;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public final class AegisGolemSummon extends SummonMonsterType {
    public AegisGolemSummon() {
        super(SummonDefinitions.AEGIS_GOLEM);
    }

    @Override
    public List<Goal> createAbilityGoals(SemionMonsterEntity entity) {
        return List.of(new ApplyMonsterTimedEffectGoal(
                entity,
                TimedEffectType.MONSTER_DAMAGE_REDUCTION,
                SummonBalancePolicy.AEGIS_GOLEM_DAMAGE_REDUCTION,
                SummonBalancePolicy.AEGIS_GOLEM_PROTECTION_RADIUS,
                SummonBalancePolicy.AEGIS_GOLEM_PROTECTION_DURATION_TICKS,
                SummonBalancePolicy.AEGIS_GOLEM_PROTECTION_COOLDOWN_TICKS,
                SummonBalancePolicy.SUPPORT_HEAL_RETRY_TICKS,
                Integer.MAX_VALUE
        ));
    }
}
