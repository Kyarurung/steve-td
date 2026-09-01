package kim.biryeong.semiontd.tower.resonance;

import java.util.Set;
import kim.biryeong.semiontd.api.SemionTdApi;
import kim.biryeong.semiontd.api.area.AreaEffectOutcome;
import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.api.area.TowerAreaEffectRequest;
import kim.biryeong.semiontd.api.area.TowerAreaTargetMode;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import net.minecraft.world.damagesource.DamageSource;

final class ResonanceCombat {
    private final ResonanceConfig config;

    ResonanceCombat(ResonanceConfig config) {
        this.config = config;
    }

    int adjustAttackInterval(ResonanceTower tower, int baseIntervalTicks) {
        return ResonanceRules.adjustedAttackInterval(
                baseIntervalTicks,
                tower.resonanceSnapshot().auraAttackSpeedBonus() + selfAttackSpeedBonus(tower)
        );
    }

    double modifyAttackDamage(ResonanceTower tower, SemionMonsterEntity target, double damage) {
        double bonus = tower.aspect() == ResonanceAspect.FOCUS ? focusDamageBonus(tower) : 0.0;
        ResonanceSnapshot state = tower.resonanceSnapshot();
        if (target != null && state.auraDamageVsSlowedBonus() > 0.0 && hasFrostDebuff(target)) {
            bonus += state.auraDamageVsSlowedBonus();
        }
        return ResonanceRules.adjustedDamage(damage, bonus);
    }

    double modifyIncomingDamage(ResonanceTower tower, DamageSource source, double damage) {
        int level = tower.resonanceLevel();
        if (tower.aspect() != ResonanceAspect.AMPLIFY || level <= 0) {
            return damage;
        }
        ResonanceAbilityKey key = switch (level) {
            case 1 -> ResonanceAbilityKey.BLOOM_LEVEL_1_DAMAGE_REDUCTION;
            case 2 -> ResonanceAbilityKey.BLOOM_LEVEL_2_DAMAGE_REDUCTION;
            default -> ResonanceAbilityKey.BLOOM_LEVEL_3_DAMAGE_REDUCTION;
        };
        return ResonanceRules.reducedDamage(damage, value(tower, key));
    }

    void resolveAttack(
            ResonanceTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        if (tower.resonanceLevel() <= 0 || towerEntity == null || target == null) {
            return;
        }
        switch (tower.aspect()) {
            case FOCUS -> focusStrike(tower, towerEntity, target, damageAmount);
            case WAVE -> waveAttack(tower, towerEntity, target, damageAmount);
            case FROST -> frostAttack(tower, towerEntity, target, damageAmount);
            case AMPLIFY -> bloomProtect(tower, towerEntity);
        }
    }

    private double selfAttackSpeedBonus(ResonanceTower tower) {
        return switch (tower.aspect()) {
            case FOCUS -> switch (tower.resonanceLevel()) {
                case 1 -> value(tower, ResonanceAbilityKey.FOCUS_LEVEL_1_ATTACK_SPEED_BONUS);
                case 2 -> value(tower, ResonanceAbilityKey.FOCUS_LEVEL_2_ATTACK_SPEED_BONUS);
                default -> tower.resonanceLevel() >= 3
                        ? value(tower, ResonanceAbilityKey.FOCUS_LEVEL_3_ATTACK_SPEED_BONUS) : 0.0;
            };
            case WAVE -> tower.resonanceLevel() >= 1
                    ? value(tower, ResonanceAbilityKey.WAVE_LEVEL_1_ATTACK_SPEED_BONUS) : 0.0;
            default -> 0.0;
        };
    }

    private double focusDamageBonus(ResonanceTower tower) {
        return switch (tower.resonanceLevel()) {
            case 2 -> value(tower, ResonanceAbilityKey.FOCUS_LEVEL_2_DAMAGE_BONUS);
            default -> tower.resonanceLevel() >= 3
                    ? value(tower, ResonanceAbilityKey.FOCUS_LEVEL_3_DAMAGE_BONUS) : 0.0;
        };
    }

    private static boolean hasFrostDebuff(SemionMonsterEntity target) {
        return target.activeTimedEffectMagnitude(TimedEffectType.MONSTER_MOVE_SPEED_REDUCTION) > 0.0
                || target.activeTimedEffectMagnitude(TimedEffectType.MONSTER_ATTACK_SPEED_REDUCTION) > 0.0;
    }

    private void focusStrike(
            ResonanceTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        if (tower.resonanceLevel() < 3
                || !tower.chargeReady(integer(tower, ResonanceAbilityKey.FOCUS_STRIKE_EVERY_ATTACKS))) {
            return;
        }
        double damage = damageAmount * value(tower, ResonanceAbilityKey.FOCUS_STRIKE_DAMAGE_RATIO);
        if (damage <= 0.0) {
            return;
        }
        boolean killed = tower.dealMagicDamage(towerEntity, target, damage);
        ResonanceVfx.secondaryAttack(towerEntity, target);
        if (killed) {
            tower.recordMagicKill(towerEntity, target, damage);
        }
    }

    private void waveAttack(
            ResonanceTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        if (tower.resonanceLevel() >= 2) {
            ResonanceAbilityKey radius = tower.resonanceLevel() >= 3
                    ? ResonanceAbilityKey.WAVE_LEVEL_3_SPLASH_RADIUS
                    : ResonanceAbilityKey.WAVE_LEVEL_2_SPLASH_RADIUS;
            ResonanceAbilityKey ratio = tower.resonanceLevel() >= 3
                    ? ResonanceAbilityKey.WAVE_LEVEL_3_SPLASH_DAMAGE_RATIO
                    : ResonanceAbilityKey.WAVE_LEVEL_2_SPLASH_DAMAGE_RATIO;
            areaDamage(tower, towerEntity, target, value(tower, radius), damageAmount * value(tower, ratio), false, 0, 0.0, 0.0);
        }
        if (tower.resonanceLevel() >= 3
                && tower.chargeReady(integer(tower, ResonanceAbilityKey.WAVE_PULSE_EVERY_ATTACKS))) {
            areaDamage(
                    tower,
                    towerEntity,
                    target,
                    value(tower, ResonanceAbilityKey.WAVE_PULSE_RADIUS),
                    damageAmount * value(tower, ResonanceAbilityKey.WAVE_PULSE_DAMAGE_RATIO),
                    true,
                    0,
                    0.0,
                    0.0
            );
        }
    }

    private void frostAttack(
            ResonanceTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        applyFrostDebuff(tower, target);
        if (tower.resonanceLevel() >= 3
                && tower.chargeReady(integer(tower, ResonanceAbilityKey.FROST_PULSE_EVERY_ATTACKS))) {
            areaDamage(
                    tower,
                    towerEntity,
                    target,
                    value(tower, ResonanceAbilityKey.FROST_PULSE_RADIUS),
                    damageAmount * value(tower, ResonanceAbilityKey.FROST_PULSE_DAMAGE_RATIO),
                    true,
                    ticks(tower, ResonanceAbilityKey.FROST_PULSE_SLOW_TICKS),
                    value(tower, ResonanceAbilityKey.FROST_PULSE_SLOW_MAGNITUDE),
                    value(tower, ResonanceAbilityKey.FROST_PULSE_ATTACK_SPEED_REDUCTION)
            );
        }
    }

    private void bloomProtect(ResonanceTower tower, SemionTowerEntity towerEntity) {
        if (tower.resonanceLevel() < 3
                || !tower.chargeReady(integer(tower, ResonanceAbilityKey.BLOOM_PROTECT_EVERY_ATTACKS))) {
            return;
        }
        double healAmount = tower.type().damage() * value(tower, ResonanceAbilityKey.BLOOM_PROTECT_HEAL_RATIO);
        double reduction = value(tower, ResonanceAbilityKey.BLOOM_PROTECT_DAMAGE_REDUCTION);
        int duration = ticks(tower, ResonanceAbilityKey.BLOOM_PROTECT_TICKS);
        TowerAreaEffectRequest request = new TowerAreaEffectRequest(
                AreaEffectIds.tower(tower, "bloom_protect"),
                towerEntity,
                towerEntity.position(),
                value(tower, ResonanceAbilityKey.BLOOM_PROTECT_RADIUS),
                TowerAreaTargetMode.ENTITIES,
                true,
                candidate -> candidate.tower() instanceof ResonanceTower,
                ResonanceVfx.buff()
        );
        SemionTdApi.areaEffects().applyToTowers(request, candidate ->
                protectTower(tower, candidate.entity().orElseThrow(), healAmount, reduction, duration)
                        ? AreaEffectOutcome.APPLIED : AreaEffectOutcome.UNCHANGED);
    }

    private static boolean protectTower(
            ResonanceTower tower,
            SemionTowerEntity target,
            double healAmount,
            double reduction,
            int duration
    ) {
        boolean changed = false;
        if (healAmount > 0.0 && tower.healResonanceTarget(target, healAmount)) {
            target.playHealingAnimation();
            changed = true;
        }
        if (reduction > 0.0 && duration > 0) {
            double previous = target.activeTimedEffectMagnitude(TimedEffectType.TOWER_DAMAGE_REDUCTION);
            target.applyTimedEffect(TimedEffectType.TOWER_DAMAGE_REDUCTION, reduction, duration);
            changed |= Double.compare(previous,
                    target.activeTimedEffectMagnitude(TimedEffectType.TOWER_DAMAGE_REDUCTION)) != 0;
        }
        return changed;
    }

    private void applyFrostDebuff(ResonanceTower tower, SemionMonsterEntity target) {
        ResonanceAbilityKey ticks = switch (tower.resonanceLevel()) {
            case 1 -> ResonanceAbilityKey.FROST_LEVEL_1_SLOW_TICKS;
            case 2 -> ResonanceAbilityKey.FROST_LEVEL_2_SLOW_TICKS;
            default -> ResonanceAbilityKey.FROST_LEVEL_3_SLOW_TICKS;
        };
        ResonanceAbilityKey move = switch (tower.resonanceLevel()) {
            case 1 -> ResonanceAbilityKey.FROST_LEVEL_1_SLOW_MAGNITUDE;
            case 2 -> ResonanceAbilityKey.FROST_LEVEL_2_SLOW_MAGNITUDE;
            default -> ResonanceAbilityKey.FROST_LEVEL_3_SLOW_MAGNITUDE;
        };
        ResonanceAbilityKey attack = switch (tower.resonanceLevel()) {
            case 1 -> ResonanceAbilityKey.FROST_LEVEL_1_ATTACK_SPEED_REDUCTION;
            case 2 -> ResonanceAbilityKey.FROST_LEVEL_2_ATTACK_SPEED_REDUCTION;
            default -> ResonanceAbilityKey.FROST_LEVEL_3_ATTACK_SPEED_REDUCTION;
        };
        int duration = ticks(tower, ticks);
        double moveReduction = value(tower, move);
        double attackReduction = value(tower, attack);
        if (duration > 0 && moveReduction > 0.0) {
            target.applyTimedEffect(TimedEffectType.MONSTER_MOVE_SPEED_REDUCTION, moveReduction, duration);
        }
        if (duration > 0 && attackReduction > 0.0) {
            target.applyTimedEffect(TimedEffectType.MONSTER_ATTACK_SPEED_REDUCTION, attackReduction, duration);
        }
    }

    private void areaDamage(
            ResonanceTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double radius,
            double damageAmount,
            boolean includePrimaryTarget,
            int debuffTicks,
            double moveSpeedReduction,
            double attackSpeedReduction
    ) {
        if (radius <= 0.0 || damageAmount <= 0.0
                && (debuffTicks <= 0 || moveSpeedReduction <= 0.0 && attackSpeedReduction <= 0.0)) {
            return;
        }
        AreaVfxSpec vfx = damageAmount > 0.0
                ? ResonanceVfx.areaAttack(includePrimaryTarget)
                : ResonanceVfx.debuff();
        MonsterAreaEffectRequest request = new MonsterAreaEffectRequest(
                AreaEffectIds.tower(tower, damageAmount > 0.0 ? "area_damage" : "area_debuff"),
                towerEntity,
                target.position(),
                radius,
                includePrimaryTarget ? Set.of() : Set.of(target.getUUID()),
                null,
                vfx
        );
        SemionTdApi.areaEffects().applyToMonsters(request, monster -> {
            boolean killed = false;
            boolean changed = damageAmount > 0.0;
            if (damageAmount > 0.0) {
                killed = tower.dealMagicDamage(towerEntity, monster, damageAmount);
                if (killed) {
                    tower.recordMagicKill(towerEntity, monster, damageAmount);
                }
            }
            if (debuffTicks > 0 && moveSpeedReduction > 0.0) {
                double previous = monster.activeTimedEffectMagnitude(TimedEffectType.MONSTER_MOVE_SPEED_REDUCTION);
                monster.applyTimedEffect(TimedEffectType.MONSTER_MOVE_SPEED_REDUCTION, moveSpeedReduction, debuffTicks);
                changed |= Double.compare(previous,
                        monster.activeTimedEffectMagnitude(TimedEffectType.MONSTER_MOVE_SPEED_REDUCTION)) != 0;
            }
            if (debuffTicks > 0 && attackSpeedReduction > 0.0) {
                double previous = monster.activeTimedEffectMagnitude(TimedEffectType.MONSTER_ATTACK_SPEED_REDUCTION);
                monster.applyTimedEffect(TimedEffectType.MONSTER_ATTACK_SPEED_REDUCTION, attackSpeedReduction, debuffTicks);
                changed |= Double.compare(previous,
                        monster.activeTimedEffectMagnitude(TimedEffectType.MONSTER_ATTACK_SPEED_REDUCTION)) != 0;
            }
            if (killed) {
                return AreaEffectOutcome.KILLED;
            }
            return changed ? AreaEffectOutcome.APPLIED : AreaEffectOutcome.UNCHANGED;
        });
    }

    private double value(ResonanceTower tower, ResonanceAbilityKey ability) {
        return config.value(tower.type(), ability);
    }

    private int integer(ResonanceTower tower, ResonanceAbilityKey ability) {
        return config.integer(tower.type(), ability);
    }

    private int ticks(ResonanceTower tower, ResonanceAbilityKey ability) {
        return config.ticks(tower.type(), ability);
    }
}
