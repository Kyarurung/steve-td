package kim.biryeong.semiontd.tower.ancientcity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.monster.DamageType;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;
import net.minecraft.world.damagesource.DamageSource;

final class AncientCityCombat {
    private final AncientCityConfig config;
    private final AncientCityCombatState state;

    AncientCityCombat(AncientCityConfig config, AncientCityCombatState state) {
        this.config = config;
        this.state = state;
    }

    void tick() {
        state.tick();
    }

    double modifyIncomingDamage(AncientCityTower tower, DamageSource damageSource, double damageAmount) {
        if (tower.role() != AncientCityRole.CATALYST || !AncientCityTerritoryController.resonanceActive(tower)) {
            return damageAmount;
        }
        return damageAmount * Math.max(
                0.0,
                1.0 - config.value(tower.type(), AncientCityAbilityKey.SCULK_DAMAGE_REDUCTION)
        );
    }

    void onDamaged(
            AncientCityTower tower,
            SemionTowerEntity towerEntity,
            double damageAmount,
            double currentHealth
    ) {
        if (tower.role() != AncientCityRole.CATALYST || towerEntity == null || damageAmount <= 0.0
                || currentHealth <= 0.0 || state.retaliationCooldownTicks() > 0) {
            return;
        }
        state.startRetaliationCooldown(config.ticks(tower.type(), AncientCityAbilityKey.RETALIATION_COOLDOWN_TICKS));
        double radius = config.value(tower.type(), AncientCityAbilityKey.RETALIATION_RADIUS);
        double baseDamage = config.value(tower.type(), AncientCityAbilityKey.MAGIC_DAMAGE);
        if (radius <= 0.0 || baseDamage <= 0.0) {
            return;
        }
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTower(
                AreaEffectIds.tower(tower, "catalyst_retaliation"),
                towerEntity,
                radius,
                AreaVfxSpec.onTrigger(AreaVfxStyles.PULSE)
        );
        TowerAreaDamage.apply(
                tower,
                towerEntity,
                request,
                target -> magicDamage(tower, target, baseDamage, true),
                true,
                (target, dealtDamage, killed) -> {},
                DamageType.MAGIC
        );
        AncientCityVfx.showCatalyst(towerEntity, radius);
    }

    boolean execute(AncientCityTower tower, PlayerLane lane) {
        if (tower.role() == AncientCityRole.CATALYST) {
            return false;
        }
        SemionTowerEntity towerEntity = tower.ownRuntimeEntity(lane).orElse(null);
        if (towerEntity == null) {
            return false;
        }
        List<SemionMonsterEntity> candidates = candidates(tower, towerEntity);
        if (candidates.isEmpty()) {
            return false;
        }
        return switch (tower.role()) {
            case SENSOR -> castSensor(tower, towerEntity, candidates);
            case SHRIEKER -> castShrieker(tower, towerEntity, candidates);
            case WARDEN -> castWarden(tower, towerEntity, candidates);
            case CATALYST -> false;
        };
    }

    double magicDamage(AncientCityTower tower, SemionMonsterEntity target, double baseDamage, boolean includeMark) {
        double bonus = AncientCityTerritoryController.resonanceBonus(tower);
        Monster monster = target == null ? null : target.runtimeMonster();
        if (includeMark) {
            bonus += AncientCityMarkDomain.damageBonus(monster, tower.ownerPlayer());
        }
        bonus = AncientCityRules.combinedMagicBonus(
                bonus,
                config.global(AncientCityAbilityKey.MAX_COMBINED_DAMAGE_BONUS)
        );
        return AncientCityRules.incomeAdjustedMagicDamage(
                Math.max(0.0, baseDamage) * (1.0 + bonus),
                monster != null && monster.senderTeam().isPresent(),
                config.global(AncientCityAbilityKey.INCOME_MAGIC_DAMAGE_MULTIPLIER)
        );
    }

    private boolean castSensor(
            AncientCityTower tower,
            SemionTowerEntity towerEntity,
            List<SemionMonsterEntity> candidates
    ) {
        SemionMonsterEntity target = primaryTarget(towerEntity, candidates);
        double baseDamage = config.value(tower.type(), AncientCityAbilityKey.MAGIC_DAMAGE);
        var result = tower.damageTargetResult(
                towerEntity,
                target,
                magicDamage(tower, target, baseDamage, true),
                DamageType.MAGIC
        );
        if (result.killed()) {
            tower.onKill(towerEntity, target, baseDamage);
        } else if (result.dealtDamage() > 0.0 && target.isAlive()) {
            int duration = config.ticks(tower.type(), AncientCityAbilityKey.MARK_DURATION_TICKS);
            AncientCityMarkDomain.apply(
                    target.runtimeMonster(),
                    tower.ownerPlayer(),
                    towerEntity.getUUID(),
                    config.value(tower.type(), AncientCityAbilityKey.MARK_DAMAGE_BONUS),
                    duration
            );
            target.applyTimedEffect(TimedEffectType.MONSTER_MARKED, 1.0, duration);
        }
        AncientCityVfx.showSensor(towerEntity, target);
        return true;
    }

    private boolean castShrieker(
            AncientCityTower tower,
            SemionTowerEntity towerEntity,
            List<SemionMonsterEntity> candidates
    ) {
        SemionMonsterEntity primary = primaryTarget(towerEntity, candidates);
        double radius = config.value(tower.type(), AncientCityAbilityKey.MAGIC_RADIUS);
        double baseDamage = config.value(tower.type(), AncientCityAbilityKey.MAGIC_DAMAGE);
        int slowTicks = config.ticks(tower.type(), AncientCityAbilityKey.SLOW_DURATION_TICKS);
        double slow = config.value(tower.type(), AncientCityAbilityKey.SLOW_MAGNITUDE);
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTarget(
                AreaEffectIds.tower(tower, "shriek"),
                towerEntity,
                primary,
                radius,
                AreaVfxSpec.onTrigger(AreaVfxStyles.PULSE)
        ).including(primary.getUUID());
        TowerAreaDamage.apply(
                tower,
                towerEntity,
                request,
                target -> magicDamage(tower, target, baseDamage, true),
                true,
                (target, dealtDamage, killed) -> {
                    if (!killed && target.isAlive() && slow > 0.0 && slowTicks > 0) {
                        target.applyTimedEffect(TimedEffectType.MONSTER_MOVE_SPEED_REDUCTION, slow, slowTicks);
                    }
                },
                DamageType.MAGIC
        );
        AncientCityVfx.showShrieker(towerEntity, primary, radius);
        return true;
    }

    private boolean castWarden(
            AncientCityTower tower,
            SemionTowerEntity towerEntity,
            List<SemionMonsterEntity> candidates
    ) {
        ArrayList<SemionMonsterEntity> ordered = new ArrayList<>(candidates);
        ordered.sort(Comparator.comparingDouble((SemionMonsterEntity target) -> maxHealth(target)).reversed()
                .thenComparingDouble(target -> target.distanceToSqr(towerEntity))
                .thenComparing(target -> target.getUUID().toString()));
        int targetCount = Math.max(1, config.integer(tower.type(), AncientCityAbilityKey.TARGET_COUNT));
        if (AncientCityTerritoryController.resonanceActive(tower)) {
            targetCount += Math.max(0, config.integer(tower.type(), AncientCityAbilityKey.SCULK_EXTRA_TARGETS));
        }
        List<SemionMonsterEntity> targets = ordered.stream().limit(targetCount).toList();
        double baseDamage = config.value(tower.type(), AncientCityAbilityKey.MAGIC_DAMAGE);
        double secondaryRatio = Math.max(0.0, config.value(tower.type(), AncientCityAbilityKey.SECONDARY_DAMAGE_RATIO));
        for (int index = 0; index < targets.size(); index++) {
            SemionMonsterEntity target = targets.get(index);
            boolean primary = index == 0;
            double targetBaseDamage = primary ? baseDamage : baseDamage * secondaryRatio;
            var result = tower.damageTargetResult(
                    towerEntity,
                    target,
                    magicDamage(tower, target, targetBaseDamage, primary),
                    DamageType.MAGIC
            );
            if (result.killed()) {
                tower.onKill(towerEntity, target, targetBaseDamage);
            }
        }
        AncientCityVfx.showWarden(towerEntity, targets);
        return true;
    }

    private static SemionMonsterEntity primaryTarget(
            SemionTowerEntity towerEntity,
            List<SemionMonsterEntity> candidates
    ) {
        SemionMonsterEntity current = towerEntity.currentAttackTarget();
        if (current != null && candidates.contains(current)) {
            return current;
        }
        return candidates.stream().min(Comparator.comparingDouble(target -> target.distanceToSqr(towerEntity))).orElseThrow();
    }

    private static List<SemionMonsterEntity> candidates(AncientCityTower tower, SemionTowerEntity towerEntity) {
        double range = tower.type().range();
        double rangeSqr = range * range;
        return towerEntity.level().getEntities(
                        towerEntity,
                        towerEntity.getBoundingBox().inflate(range),
                        entity -> entity instanceof SemionMonsterEntity target && towerEntity.isValidAttackTarget(target)
                ).stream()
                .map(SemionMonsterEntity.class::cast)
                .filter(target -> target.distanceToSqr(towerEntity) <= rangeSqr)
                .toList();
    }

    private static double maxHealth(SemionMonsterEntity target) {
        Monster monster = target.runtimeMonster();
        return monster == null ? target.getMaxHealth() : monster.maxHealth();
    }
}
