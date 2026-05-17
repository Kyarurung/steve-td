package kim.biryeong.semiontd.summon;

import java.util.List;
import kim.biryeong.semiontd.entity.goal.AreaAllyHealGoal;
import kim.biryeong.semiontd.entity.goal.SingleAllyHealGoal;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public final class GroveAlpacaSummon extends SummonMonsterType {
    public GroveAlpacaSummon() {
        super(SummonDefinitions.GROVE_ALPACA);
    }

    @Override
    public List<Goal> createAbilityGoals(SemionMonsterEntity entity) {
        return List.of(
                new SingleAllyHealGoal<>(
                        entity,
                        SemionMonsterEntity.class,
                        SummonBalancePolicy.T3_SUPPORT_SINGLE_HEAL_RADIUS,
                        SummonBalancePolicy.T3_SUPPORT_SINGLE_HEAL_AMOUNT,
                        SummonBalancePolicy.T3_SUPPORT_SINGLE_HEAL_COOLDOWN_TICKS,
                        SummonBalancePolicy.SUPPORT_HEAL_RETRY_TICKS
                ),
                new AreaAllyHealGoal<>(
                        entity,
                        SemionMonsterEntity.class,
                        SummonBalancePolicy.T3_SUPPORT_AREA_HEAL_RADIUS,
                        SummonBalancePolicy.T3_SUPPORT_AREA_HEAL_AMOUNT,
                        SummonBalancePolicy.T3_SUPPORT_AREA_HEAL_MAX_TARGETS,
                        SummonBalancePolicy.T3_SUPPORT_AREA_HEAL_COOLDOWN_TICKS,
                        SummonBalancePolicy.SUPPORT_HEAL_RETRY_TICKS
                )
        );
    }
}
