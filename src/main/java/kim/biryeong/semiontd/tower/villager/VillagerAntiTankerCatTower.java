package kim.biryeong.semiontd.tower.villager;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.summon.SummonRole;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.phys.Vec3;

public class VillagerAntiTankerCatTower extends EntityBackedTower {
    private double killStackDamage;

    public VillagerAntiTankerCatTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public VillagerAntiTankerCatTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition originalPosition, GridPosition currentPosition) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    @Override
    public Optional<SemionMonsterEntity> selectAttackTarget(
            SemionTowerEntity towerEntity,
            List<SemionMonsterEntity> candidates
    ) {
        if (candidates == null) {
            return Optional.empty();
        }
        return candidates.stream()
                .filter(candidate -> candidate != null && candidate.isAlive())
                .max(Comparator.comparingDouble(candidate -> {
                    Monster monster = candidate.runtimeMonster();
                    return monster == null ? candidate.getMaxHealth() : monster.maxHealth();
                }));
    }

    @Override
    public double modifyAttackDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        double adjustedDamage = damageAmount + killStackDamage;
        Monster runtimeMonster = target == null ? null : target.runtimeMonster();
        if (runtimeMonster == null || runtimeMonster.senderTeam().isEmpty()) {
            return adjustedDamage;
        }
        if (runtimeMonster.summonRoles().contains(SummonRole.TANK)) {
            return adjustedDamage * (1.0 + value(VillagerAbilityKey.TANK_BONUS));
        }
        return adjustedDamage * (1.0 + value(VillagerAbilityKey.NON_WAVE_BONUS));
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

    @Override
    public java.util.List<String> runtimeDetailLines() {
        return java.util.List.of(killStackLine(killStackDamage, stackDamageStep(), stackDamageCap()));
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (previousTower instanceof VillagerAntiTankerCatTower catTower) {
            this.killStackDamage = Math.min(stackDamageCap(), catTower.killStackDamage);
        }
    }

    private double stackDamageStep() {
        return value(VillagerAbilityKey.STACK_DAMAGE);
    }

    private double stackDamageCap() {
        return value(VillagerAbilityKey.STACK_DAMAGE_CAP);
    }

    private String killStackLine(double damage, double step, double cap) {
        int stacks = step <= 0.0 ? 0 : (int) Math.round(damage / step);
        int maxStacks = step <= 0.0 ? 0 : (int) Math.round(cap / step);
        return "사망 스택 " + stacks + "/" + maxStacks + " (공격력 +" + oneDecimal(damage) + ")";
    }

    private void incrementDeathStack() {
        killStackDamage = VillagerCombat.addCappedDamage(
                killStackDamage, stackDamageStep(), stackDamageCap()
        );
    }

    private double value(VillagerAbilityKey ability) {
        return VillagerConfig.RUNTIME.value(type(), ability);
    }
}
