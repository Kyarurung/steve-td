package kim.biryeong.semiontd.tower.animal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.phys.Vec3;

public class AnimalFoxTower extends AnimalPackTower {
    private double killBonusDamage;

    public AnimalFoxTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public AnimalFoxTower(
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
    public Optional<SemionMonsterEntity> selectAttackTarget(SemionTowerEntity towerEntity, List<SemionMonsterEntity> candidates) {
        if (towerEntity == null || candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }
        double attackRangeSqr = towerEntity.attackRange() * towerEntity.attackRange();
        List<FoxTargetCandidate> foxCandidates = candidates.stream()
                .map(candidate -> new FoxTargetCandidate(
                        candidate,
                        candidate.getHealth(),
                        candidate.getMaxHealth(),
                        towerEntity.distanceToSqr(candidate),
                        towerEntity.distanceToSqr(candidate) <= attackRangeSqr
                ))
                .toList();
        return AnimalExecuteTargetingPolicy.select(foxCandidates, effectiveExecuteThreshold())
                .map(FoxTargetCandidate::monster);
    }

    @Override
    public double modifyAttackDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        double adjustedDamage = damageAmount + killBonusDamage;
        if (target == null || target.getMaxHealth() <= 0.0F || target.getHealth() / target.getMaxHealth() > effectiveExecuteThreshold()) {
            return adjustedDamage;
        }
        double bonusRatio = value(AnimalAbilityKey.EXECUTE_DAMAGE_BONUS_RATIO) + currentStacks() * value(AnimalAbilityKey.EXECUTE_DAMAGE_BONUS_PER_STACK);
        if (hasLeaderAura()) {
            bonusRatio += leaderValue(AnimalAbilityKey.LEADER_EXECUTE_DAMAGE_BONUS);
        }
        return adjustedDamage * (1.0 + Math.max(0.0, bonusRatio));
    }

    @Override
    public void onNearbyMonsterDeath(PlayerLane lane, Monster monster, Vec3 deathPosition) {
        if (monster == null || !isWithinDeathStackRange(deathPosition)) {
            return;
        }
        killBonusDamage = Math.min(killBonusDamageCap(), killBonusDamage + killBonusDamageStep());
    }

    @Override
    public java.util.List<String> runtimeDetailLines() {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>(super.runtimeDetailLines());
        double step = killBonusDamageStep();
        int stacks = step <= 0.0 ? 0 : (int) Math.round(killBonusDamage / step);
        int maxStacks = step <= 0.0 ? 0 : (int) Math.round(killBonusDamageCap() / step);
        lines.add("사망 보너스 " + stacks + "/" + maxStacks + " (공격력 +" + oneDecimal(killBonusDamage) + ")");
        if (hasLeaderAura()) {
            lines.add("우두머리 효과 처형 기준 +" + percent(leaderValue(AnimalAbilityKey.LEADER_EXECUTE_THRESHOLD_BONUS))
                    + "p, 처형 추가 피해 +" + percent(leaderValue(AnimalAbilityKey.LEADER_EXECUTE_DAMAGE_BONUS)) + "p");
        }
        return lines;
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (previousTower instanceof AnimalFoxTower foxTower) {
            killBonusDamage = Math.min(killBonusDamageCap(), foxTower.killBonusDamage);
        }
    }

    @Override
    protected boolean isStackFamily(Tower tower) {
        return tower != null && (
                tower.type().id().equals(AnimalTowers.T1_FOX_TOWER.id())
                        || tower.type().id().equals(AnimalTowers.T2_FOX_TOWER.id())
                        || tower.type().id().equals(AnimalTowers.T3_FOX_TOWER.id())
                        || tower.type().id().equals(AnimalTowers.T4_FOX_LEADER_TOWER.id())
        );
    }

    @Override
    protected int maxStacks() {
        return AnimalConfig.RUNTIME.integer(type(), AnimalAbilityKey.MAX_STACKS);
    }

    @Override
    protected TowerType leaderBaseType() {
        return AnimalTowers.T3_FOX_TOWER;
    }

    @Override
    protected TowerType leaderType() {
        return AnimalTowers.T4_FOX_LEADER_TOWER;
    }

    private double effectiveExecuteThreshold() {
        double threshold = AnimalExecuteTargetingPolicy.effectiveThreshold(
                value(AnimalAbilityKey.EXECUTE_HEALTH_THRESHOLD),
                currentStacks(),
                value(AnimalAbilityKey.EXECUTE_THRESHOLD_PER_STACK),
                value(AnimalAbilityKey.MAX_EXECUTE_HEALTH_THRESHOLD)
        );
        if (!hasLeaderAura()) {
            return threshold;
        }
        return Math.min(leaderValue(AnimalAbilityKey.LEADER_EXECUTE_THRESHOLD_CAP),
                threshold + leaderValue(AnimalAbilityKey.LEADER_EXECUTE_THRESHOLD_BONUS));
    }

    private double value(AnimalAbilityKey ability) {
        return AnimalConfig.RUNTIME.value(type(), ability);
    }

    private double killBonusDamageStep() {
        return value(AnimalAbilityKey.KILL_BONUS_DAMAGE);
    }

    private double killBonusDamageCap() {
        return value(AnimalAbilityKey.KILL_BONUS_DAMAGE_CAP);
    }

    private record FoxTargetCandidate(
            SemionMonsterEntity monster,
            double currentHealth,
            double maxHealth,
            double distanceSqr,
            boolean inAttackRange
    ) implements AnimalExecuteTargetingPolicy.Candidate {
    }
}
