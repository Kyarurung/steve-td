package kim.biryeong.semiontd.tower.ocean;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;
import net.minecraft.world.damagesource.DamageSource;

final class OceanCombat {
    private static final double EPSILON = 1.0E-9;
    private final OceanConfig config;
    private final OceanResourceState state;

    OceanCombat(OceanConfig config, OceanResourceState state) {
        this.config = config;
        this.state = state;
    }

    Optional<SemionMonsterEntity> selectAttackTarget(OceanTower tower, List<SemionMonsterEntity> candidates) {
        if (!OceanTowers.isHunter(tower.type()) || state.water() <= 0.0) {
            return Optional.empty();
        }
        return candidates.stream()
                .filter(candidate -> candidate != null && candidate.runtimeMonster() != null)
                .max(Comparator.comparingDouble(candidate -> candidate.runtimeMonster().maxHealth()));
    }

    double modifyAttackDamage(OceanTower tower, SemionMonsterEntity target, double damageAmount) {
        if (state.water() <= 0.0) {
            return damageAmount * config.global(OceanAbilityKey.DEHYDRATED_DAMAGE_MULTIPLIER);
        }
        if (OceanTowers.isHunter(tower.type()) && isIncomeTarget(target)
                && canPayAttackAndExtra(tower, OceanAbilityKey.INCOME_WATER_COST)) {
            return damageAmount * incomeWaterMultiplier(tower);
        }
        return damageAmount * normalWaterMultiplier(tower);
    }

    int adjustAttackInterval(OceanTower tower, int baseIntervalTicks) {
        if (state.water() > 0.0 || tower.type().damage() <= 0.0) {
            return baseIntervalTicks;
        }
        double remainingSpeed = Math.max(
                0.01,
                1.0 - config.global(OceanAbilityKey.DEHYDRATED_ATTACK_SPEED_REDUCTION)
        );
        return Math.max(1, (int) Math.ceil(baseIntervalTicks / remainingSpeed));
    }

    double modifyIncomingDamage(OceanTower tower, DamageSource damageSource, double damageAmount) {
        if (!OceanTowers.isTank(tower.type()) || state.water() <= 0.0) {
            return damageAmount;
        }
        return damageAmount * Math.max(0.0, 1.0 - config.value(tower.type(), OceanAbilityKey.DAMAGE_REDUCTION));
    }

    void onAttack(
            OceanTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        double baseCost = config.value(tower.type(), OceanAbilityKey.ATTACK_WATER_COST);
        double extraCost = 0.0;
        if (OceanTowers.isSplash(tower.type())
                && state.water() + EPSILON >= baseCost + config.value(tower.type(), OceanAbilityKey.SPLASH_WATER_COST)) {
            extraCost = config.value(tower.type(), OceanAbilityKey.SPLASH_WATER_COST);
            splash(tower, towerEntity, target, damageAmount);
        } else if (OceanTowers.isHunter(tower.type()) && isIncomeTarget(target)
                && state.water() + EPSILON >= baseCost + config.value(tower.type(), OceanAbilityKey.INCOME_WATER_COST)) {
            extraCost = config.value(tower.type(), OceanAbilityKey.INCOME_WATER_COST);
        }
        state.drainWater(baseCost + extraCost);
    }

    double waterDamageMultiplier(OceanTower tower) {
        if (state.water() <= 0.0) {
            return config.global(OceanAbilityKey.DEHYDRATED_DAMAGE_MULTIPLIER);
        }
        return normalWaterMultiplier(tower);
    }

    double incomeWaterMultiplier(OceanTower tower) {
        return 1.0 + config.global(OceanAbilityKey.INCOME_COEFFICIENT_MULTIPLIER)
                * config.value(tower.type(), OceanAbilityKey.WATER_DAMAGE_COEFFICIENT)
                * waterRoot();
    }

    private double normalWaterMultiplier(OceanTower tower) {
        return OceanRules.damageMultiplier(
                state.water(),
                config.value(tower.type(), OceanAbilityKey.WATER_DAMAGE_COEFFICIENT),
                config.global(OceanAbilityKey.WATER_SOFT_CAP),
                config.global(OceanAbilityKey.WATER_SCALE)
        );
    }

    private double waterRoot() {
        return OceanRules.waterRoot(
                state.water(),
                config.global(OceanAbilityKey.WATER_SOFT_CAP),
                config.global(OceanAbilityKey.WATER_SCALE)
        );
    }

    private boolean canPayAttackAndExtra(OceanTower tower, OceanAbilityKey extraCost) {
        return state.water() + EPSILON >= config.value(tower.type(), OceanAbilityKey.ATTACK_WATER_COST)
                + config.value(tower.type(), extraCost);
    }

    private void splash(
            OceanTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        if (towerEntity == null || target == null) {
            return;
        }
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTarget(
                AreaEffectIds.tower(tower, "ocean_splash"),
                towerEntity,
                target,
                config.value(tower.type(), OceanAbilityKey.SPLASH_RADIUS),
                AreaVfxSpec.onTrigger(AreaVfxStyles.SPLASH)
        );
        TowerAreaDamage.applyBasicAttackSplash(
                tower,
                towerEntity,
                request,
                ignored -> damageAmount * config.value(tower.type(), OceanAbilityKey.SPLASH_DAMAGE_RATIO),
                true
        );
    }

    private static boolean isIncomeTarget(SemionMonsterEntity target) {
        Monster monster = target == null ? null : target.runtimeMonster();
        return monster != null && monster.senderTeam().isPresent();
    }
}
