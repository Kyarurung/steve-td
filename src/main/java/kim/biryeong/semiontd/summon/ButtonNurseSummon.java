package kim.biryeong.semiontd.summon;

import java.util.List;
import kim.biryeong.semiontd.entity.goal.SingleAllyHealGoal;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public final class ButtonNurseSummon extends SummonMonsterType {
    public ButtonNurseSummon() {
        super(SummonDefinitions.BUTTON_NURSE);
    }

    @Override
    public List<Goal> createAbilityGoals(SemionMonsterEntity entity) {
        return List.of(
                new SingleAllyHealGoal<>(
                        entity,
                        SemionMonsterEntity.class,
                        SummonBalancePolicy.T1_SUPPORT_SINGLE_HEAL_RADIUS,
                        SummonBalancePolicy.T1_SUPPORT_SINGLE_HEAL_AMOUNT,
                        SummonBalancePolicy.T1_SUPPORT_SINGLE_HEAL_COOLDOWN_TICKS,
                        SummonBalancePolicy.SUPPORT_HEAL_RETRY_TICKS
                )
        );
    }
}
