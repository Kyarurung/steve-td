package kim.biryeong.semiontd.tower.undead;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.phys.Vec3;

public class UndeadRangedSkeletonTower extends EntityBackedTower {
    private double killStackDamage;

    public UndeadRangedSkeletonTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public UndeadRangedSkeletonTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    @Override
    public double modifyAttackDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        return damageAmount + killStackDamage;
    }

    @Override
    public java.util.List<String> runtimeDetailLines() {
        double step = stackDamageStep();
        int stacks = step <= 0.0 ? 0 : (int) Math.round(killStackDamage / step);
        int maxStacks = step <= 0.0 ? 0 : (int) Math.round(stackDamageCap() / step);
        return java.util.List.of("사망 스택 " + stacks + "/" + maxStacks + " (공격력 +" + oneDecimal(killStackDamage) + ")");
    }

    @Override
    public void onAttack(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount, boolean killedTarget) {
        heal(towerEntity, damageAmount);
        for (SemionMonsterEntity extraTarget : pickExtraTargets(towerEntity, target, extraTargetCount())) {
            boolean killed = damageBasicAttackTargetResult(towerEntity, extraTarget, damageAmount).killed();
            UndeadVfx.secondaryAttack(towerEntity, extraTarget);
            heal(towerEntity, damageAmount);
            if (killed) {
                onKill(towerEntity, extraTarget, damageAmount);
            }
        }
    }

    @Override
    public void onKill(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
    }

    @Override
    public void onNearbyMonsterDeath(PlayerLane lane, Monster monster, Vec3 deathPosition) {
        if (isWithinDeathStackRange(deathPosition)) {
            incrementDeathStack();
        }
    }

    @Override
    public void onNearbyTowerDeath(PlayerLane lane, Tower destroyedTower) {
        if (destroyedTower != null && isWithinDeathStackRange(destroyedTower.position())) {
            incrementDeathStack();
        }
    }

    private void incrementDeathStack() {
        killStackDamage = UndeadCombat.addCappedDamage(killStackDamage, stackDamageStep(), stackDamageCap());
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (previousTower instanceof UndeadRangedSkeletonTower skeletonTower) {
            killStackDamage = Math.min(stackDamageCap(), skeletonTower.killStackDamage);
        }
    }

    private List<SemionMonsterEntity> pickExtraTargets(SemionTowerEntity towerEntity, SemionMonsterEntity primary, int count) {
        if (towerEntity == null || count <= 0) {
            return List.of();
        }
        double extraTargetRange = towerEntity.attackRange() + Math.max(0.0, value(UndeadAbilityKey.EXTRA_TARGET_RANGE_BONUS));
        List<SemionMonsterEntity> candidates = new ArrayList<>(towerEntity.level().getEntities(
                towerEntity,
                towerEntity.targetSearchBox(),
                entity -> entity instanceof SemionMonsterEntity monster
                        && monster.isAlive()
                        && monster != primary
                        && monster.runtimeMonster() != null
                        && towerEntity.defendsLane(monster.runtimeMonster().targetLaneId())
                        && towerEntity.distanceToSqr(monster) <= extraTargetRange * extraTargetRange
        ).stream()
                .filter(SemionMonsterEntity.class::isInstance)
                .map(SemionMonsterEntity.class::cast)
                .toList());
        java.util.Collections.shuffle(candidates, new java.util.Random(towerEntity.level().random.nextLong()));
        return candidates.stream().limit(count).toList();
    }

    private void heal(SemionTowerEntity towerEntity, double damageAmount) {
        double healing = UndeadCombat.lifeStealAmount(damageAmount, lifeStealRatio());
        if (towerEntity != null && healing > 0.0) {
            towerEntity.healTarget(towerEntity, healing);
        }
    }

    private int extraTargetCount() {
        return UndeadConfig.RUNTIME.integer(type(), UndeadAbilityKey.EXTRA_TARGETS);
    }

    private double lifeStealRatio() {
        return value(UndeadAbilityKey.LIFE_STEAL_RATIO);
    }

    private double stackDamageStep() {
        return value(UndeadAbilityKey.STACK_DAMAGE);
    }

    private double stackDamageCap() {
        return value(UndeadAbilityKey.STACK_DAMAGE_CAP);
    }

    private double value(UndeadAbilityKey ability) {
        return UndeadConfig.RUNTIME.value(type(), ability);
    }
}
