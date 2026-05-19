package kim.biryeong.semiontd.entity.tower.goal;

import java.util.Comparator;
import java.util.EnumSet;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.visual.SemionAnimationState;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

public final class TowerAttackMonsterGoal extends Goal {
    private final SemionTowerEntity tower;
    private int cooldownTicks;

    public TowerAttackMonsterGoal(SemionTowerEntity tower) {
        this.tower = tower;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return tower.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return tower.isAlive();
    }

    @Override
    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        if (tower.needsFinalDefenseReturn()) {
            tower.returnToFinalDefenseAreaIfNeeded();
            return;
        }

        AABB searchBox = tower.targetSearchBox();
        SemionMonsterEntity target = tower.level().getEntities(
                        tower,
                        searchBox,
                        entity -> entity instanceof SemionMonsterEntity monster
                                && monster.isAlive()
                                && monster.runtimeMonster() != null
                                && tower.defendsLane(monster.runtimeMonster().targetLaneId())
                ).stream()
                .filter(SemionMonsterEntity.class::isInstance)
                .map(SemionMonsterEntity.class::cast)
                .max(Comparator.comparingDouble(monster -> monster.runtimeMonster().targetPriorityScore()))
                .orElse(null);
        if (target == null) {
            tower.getNavigation().stop();
            tower.playAnimation(SemionAnimationState.IDLE);
            return;
        }

        tower.getLookControl().setLookAt(target);
        double attackRangeSqr = tower.attackRange() * tower.attackRange();
        double distanceSqr = tower.distanceToSqr(target);
        if (distanceSqr > attackRangeSqr) {
            tower.moveTowardTarget(target.position(), tower.chaseSpeedModifier());
            return;
        }

        tower.getNavigation().stop();
        if (cooldownTicks > 0) {
            tower.playAnimation(SemionAnimationState.IDLE);
            return;
        }

        tower.playAnimation(SemionAnimationState.ATTACK);
        double damageAmount = tower.attackDamageAmount(target);
        playRangedAttackSound();
        boolean killedPrimaryTarget = tower.damageTarget(target, damageAmount);
        tower.recordAttack(target, damageAmount, killedPrimaryTarget);
        cooldownTicks = tower.attackIntervalTicks();
    }

    private void playRangedAttackSound() {
        if (!tower.playsRangedAttackSound()) {
            return;
        }
        tower.level().playSound(
                null,
                tower.blockPosition(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.HOSTILE,
                0.7F,
                1.15F
        );
    }

}
