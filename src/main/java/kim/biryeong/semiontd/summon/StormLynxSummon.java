package kim.biryeong.semiontd.summon;

import java.util.List;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.goal.ApplyMonsterTimedEffectGoal;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public final class StormLynxSummon extends SummonMonsterType {
    public StormLynxSummon() {
        super(SummonDefinitions.STORM_LYNX);
    }

    @Override
    public List<Goal> createAbilityGoals(SemionMonsterEntity entity) {
        return List.of(ApplyMonsterTimedEffectGoal.self(
                entity,
                TimedEffectType.MONSTER_MOVE_SPEED_BONUS,
                SummonBalancePolicy.STORM_LYNX_MOVE_SPEED_BONUS,
                SummonBalancePolicy.STORM_LYNX_MOVE_SPEED_DURATION_TICKS,
                SummonBalancePolicy.STORM_LYNX_MOVE_SPEED_REFRESH_TICKS
        ));
    }
}
