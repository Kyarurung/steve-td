package kim.biryeong.semiontd.tower.illager;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;
import net.minecraft.resources.ResourceLocation;

final class IllagerCombat {
    private static final ResourceLocation RAID_DAMAGE_SOURCE = raidSource("damage");
    private static final ResourceLocation RAID_ATTACK_SPEED_SOURCE = raidSource("attack_speed");
    private static final ResourceLocation RAID_DAMAGE_REDUCTION_SOURCE = raidSource("damage_reduction");

    private final IllagerConfig config;
    private final IllagerTargetPolicy targetPolicy;

    IllagerCombat(IllagerConfig config, IllagerTargetPolicy targetPolicy) {
        this.config = config;
        this.targetPolicy = targetPolicy == null ? IllagerTargetPolicy.DEFAULT : targetPolicy;
    }

    Optional<SemionMonsterEntity> selectAttackTarget(
            IllagerTower tower,
            List<SemionMonsterEntity> candidates
    ) {
        Optional<SemionMonsterEntity> forced = selectForcedAttackTarget(tower, candidates);
        if (forced.isPresent()) {
            return forced;
        }
        return switch (targetPolicy) {
            case LOW_HEALTH -> candidates.stream()
                    .filter(IllagerCombat::validRuntimeMonster)
                    .min(Comparator.comparingDouble(monster -> monster.runtimeMonster().health()));
            case HIGH_HEALTH -> candidates.stream()
                    .filter(IllagerCombat::validRuntimeMonster)
                    .max(Comparator.comparingDouble(monster -> monster.runtimeMonster().maxHealth()));
            case INCOME -> candidates.stream()
                    .filter(IllagerCombat::validRuntimeMonster)
                    .filter(monster -> monster.runtimeMonster().ownerPlayer().isPresent())
                    .max(Comparator.comparingDouble(monster -> monster.runtimeMonster().targetPriorityScore()));
            case DEFAULT -> Optional.empty();
        };
    }

    Optional<SemionMonsterEntity> selectForcedAttackTarget(
            IllagerTower tower,
            List<SemionMonsterEntity> candidates
    ) {
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }
        return candidates.stream()
                .filter(IllagerCombat::validRuntimeMonster)
                .filter(monster -> IllagerMarkDomain.activeMark(monster.runtimeMonster(), tower.ownerPlayer())
                        .map(mark -> mark.forcesTargetFor(tower.position()))
                        .orElse(false))
                .max(Comparator.comparingDouble(monster -> monster.runtimeMonster().targetPriorityScore()));
    }

    double modifyAttackDamage(IllagerTower tower, SemionMonsterEntity target, double damageAmount) {
        double multiplier = 1.0;
        Monster monster = target == null ? null : target.runtimeMonster();
        boolean raidActive = IllagerRaidController.active(tower.ownerPlayer());
        Optional<IllagerMark> mark = IllagerMarkDomain.activeMark(monster, tower.ownerPlayer());
        if (mark.isPresent()) {
            multiplier += mark.get().damageTakenBonus();
            if (raidActive) {
                multiplier += value(tower, IllagerAbilityKey.RAID_MARKED_DAMAGE_BONUS);
            }
        }
        if (monster != null && monster.ownerPlayer().isPresent()) {
            multiplier += value(tower, IllagerAbilityKey.INCOME_DAMAGE_BONUS);
            if (raidActive) {
                multiplier += value(tower, IllagerAbilityKey.RAID_INCOME_DAMAGE_BONUS);
            }
        }
        return damageAmount * Math.max(0.0, multiplier);
    }

    void onAttack(
            IllagerTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        applyMark(tower, target);
        applySplash(tower, towerEntity, target, damageAmount);
    }

    void refreshRaidTimedEffects(IllagerTower tower, PlayerLane lane) {
        if (lane == null || tower.health() <= 0.0 || !IllagerRaidController.active(tower.ownerPlayer())
                || tower.entityId().isEmpty()) {
            return;
        }
        if (!(lane.arenaWorld().getEntity(tower.entityId().getAsInt()) instanceof SemionTowerEntity towerEntity)) {
            return;
        }
        int ticks = IllagerRaidController.timedEffectTicks();
        refreshTimedEffect(towerEntity, TimedEffectType.TOWER_DAMAGE_BONUS, RAID_DAMAGE_SOURCE,
                IllagerRaidController.damageBonus(tower.ownerPlayer()), ticks);
        refreshTimedEffect(towerEntity, TimedEffectType.TOWER_ATTACK_SPEED_BONUS, RAID_ATTACK_SPEED_SOURCE,
                IllagerRaidController.attackSpeedBonus(tower.ownerPlayer()), ticks);
        refreshTimedEffect(towerEntity, TimedEffectType.TOWER_DAMAGE_REDUCTION, RAID_DAMAGE_REDUCTION_SOURCE,
                value(tower, IllagerAbilityKey.RAID_DAMAGE_REDUCTION), ticks);
    }

    private void applyMark(IllagerTower tower, SemionMonsterEntity target) {
        Monster monster = target == null ? null : target.runtimeMonster();
        if (monster == null) {
            return;
        }
        int duration = ticks(tower, IllagerAbilityKey.MARK_DURATION_TICKS);
        double damageBonus = value(tower, IllagerAbilityKey.MARK_DAMAGE_TAKEN_BONUS);
        double forceTargetRadius = value(tower, IllagerAbilityKey.FORCE_TARGET_RADIUS);
        if (IllagerRaidController.active(tower.ownerPlayer())) {
            duration += ticks(tower, IllagerAbilityKey.RAID_MARK_DURATION_BONUS_TICKS);
            damageBonus += value(tower, IllagerAbilityKey.RAID_MARK_DAMAGE_TAKEN_BONUS);
            damageBonus += raidTargetPolicyMarkBonus(tower);
            forceTargetRadius += value(tower, IllagerAbilityKey.RAID_FORCE_TARGET_RADIUS_BONUS);
        }
        if (duration <= 0 || damageBonus <= 0) {
            return;
        }
        IllagerMarkDomain.apply(monster, tower.ownerPlayer(), damageBonus, duration, tower.position(), forceTargetRadius);
        target.applyTimedEffect(TimedEffectType.MONSTER_MARKED, 1.0, duration);
    }

    private void applySplash(
            IllagerTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        if (towerEntity == null || target == null) {
            return;
        }
        double splashRadius = value(tower, IllagerAbilityKey.SPLASH_RADIUS);
        double splashRatio = value(tower, IllagerAbilityKey.SPLASH_DAMAGE_RATIO);
        if (IllagerRaidController.active(tower.ownerPlayer())) {
            splashRadius += value(tower, IllagerAbilityKey.RAID_SPLASH_RADIUS_BONUS);
            splashRatio += value(tower, IllagerAbilityKey.RAID_SPLASH_DAMAGE_RATIO_BONUS);
        }
        if (splashRadius <= 0 || splashRatio <= 0) {
            return;
        }
        double finalSplashRatio = splashRatio;
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTarget(
                AreaEffectIds.tower(tower, "splash"), towerEntity, target, splashRadius,
                AreaVfxSpec.onTrigger(AreaVfxStyles.SPLASH)
        );
        TowerAreaDamage.applyBasicAttackSplash(
                tower,
                towerEntity,
                request,
                entity -> damageAmount * finalSplashRatio,
                true
        );
    }

    private double raidTargetPolicyMarkBonus(IllagerTower tower) {
        return switch (targetPolicy) {
            case LOW_HEALTH -> value(tower, IllagerAbilityKey.RAID_LOW_HEALTH_MARK_DAMAGE_TAKEN_BONUS);
            case HIGH_HEALTH -> value(tower, IllagerAbilityKey.RAID_HIGH_HEALTH_MARK_DAMAGE_TAKEN_BONUS);
            case DEFAULT, INCOME -> 0.0;
        };
    }

    private double value(IllagerTower tower, IllagerAbilityKey ability) {
        return config.value(tower.type(), ability);
    }

    private int ticks(IllagerTower tower, IllagerAbilityKey ability) {
        return config.ticks(tower.type(), ability);
    }

    private static void refreshTimedEffect(
            SemionTowerEntity towerEntity,
            TimedEffectType type,
            ResourceLocation sourceId,
            double magnitude,
            int ticks
    ) {
        if (magnitude > 0.0) {
            towerEntity.refreshTimedEffect(type, sourceId, magnitude, ticks);
        }
    }

    private static boolean validRuntimeMonster(SemionMonsterEntity monster) {
        return monster != null && monster.runtimeMonster() != null;
    }

    private static ResourceLocation raidSource(String path) {
        return ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "illager_raid/" + path);
    }
}
