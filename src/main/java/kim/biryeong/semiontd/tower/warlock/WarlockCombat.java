package kim.biryeong.semiontd.tower.warlock;

import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;

final class WarlockCombat {
    private final WarlockConfig config;

    WarlockCombat(WarlockConfig config) {
        this.config = config;
    }

    double splashRadius(WarlockTower tower) {
        int sacrifices = tower.path() == WarlockPath.RANGED
                ? tower.totalSacrificeCount()
                : tower.roundSacrificeCount();
        return config.path(tower.path()).splash().radius(sacrifices);
    }

    double splashRadiusForCount(WarlockPath path, int sacrificeCount) {
        return config.path(path).splash().radius(sacrificeCount);
    }

    double maximumSplashRadius(WarlockPath path) {
        return config.path(path).splash().maximumRadius();
    }

    double lifeStealRatio(WarlockTower tower) {
        int sacrifices = tower.path() == WarlockPath.MELEE
                ? tower.roundSacrificeCount()
                : tower.totalSacrificeCount();
        return lifeStealRatioForCount(tower.path(), sacrifices, tower.isLastSurvivingTower());
    }

    double lifeStealRatioForCount(WarlockPath path, int sacrificeCount) {
        return lifeStealRatioForCount(path, sacrificeCount, true);
    }

    double lifeStealRatioForCount(WarlockPath path, int sacrificeCount, boolean lastSurvivingTower) {
        if (path == WarlockPath.MELEE && !lastSurvivingTower) {
            return 0.0;
        }
        WarlockConfig.StackRule rule = config.path(path).lifeSteal();
        double ratio = (Math.max(0, sacrificeCount) / rule.sacrificesPerStep()) * rule.bonusPerStep();
        return Math.min(rule.maximum(), ratio);
    }

    double maximumLifeSteal(WarlockPath path) {
        return config.path(path).lifeSteal().maximum();
    }

    int meleeAttackIntervalReduction(int roundSacrificeCount) {
        WarlockConfig.CombatRule rule = config.combat();
        int reduction = (int) Math.floor(Math.max(0, roundSacrificeCount) * rule.meleeReductionPerSacrifice());
        return Math.min(rule.maximumIntervalReductionTicks(), reduction);
    }

    int minimumAttackIntervalTicks() {
        return config.combat().minimumIntervalTicks();
    }

    int maximumAttackIntervalReduction() {
        return config.combat().maximumIntervalReductionTicks();
    }

    void resolveAttack(
            WarlockTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double attemptedDamage,
            double resolvedOutgoingDamage,
            double dealtDamage
    ) {
        if (towerEntity == null || target == null) {
            return;
        }
        double lifeSteal = lifeStealRatio(tower);
        applySplash(tower, towerEntity, target, attemptedDamage, resolvedOutgoingDamage, lifeSteal);
        tower.heal(towerEntity, Math.max(0.0, dealtDamage) * lifeSteal);
    }

    double resolvedSplashDamage(WarlockPath path, double resolvedOutgoingDamage) {
        if (!Double.isFinite(resolvedOutgoingDamage) || resolvedOutgoingDamage <= 0.0) {
            return 0.0;
        }
        return resolvedOutgoingDamage * config.path(path).splash().damageRatio();
    }

    private void applySplash(
            WarlockTower tower,
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double attemptedDamage,
            double resolvedOutgoingDamage,
            double lifeSteal
    ) {
        double radius = tower.splashRadius();
        double splashDamage = resolvedSplashDamage(tower.path(), resolvedOutgoingDamage);
        double igniteAttackDamage = resolvedSplashDamage(tower.path(), attemptedDamage);
        if (radius <= 0.0 || splashDamage <= 0.0) {
            return;
        }
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTarget(
                AreaEffectIds.tower(tower, "splash"), towerEntity, target, radius,
                AreaVfxSpec.onTrigger(AreaVfxStyles.SPLASH)
        );
        TowerAreaDamage.applyResolved(
                tower,
                towerEntity,
                request,
                ignored -> splashDamage,
                true,
                (splashTarget, dealtSplashDamage, killed) -> {
                    tower.heal(towerEntity, dealtSplashDamage * lifeSteal);
                    towerEntity.applyIgniteFromBasicAttack(splashTarget, igniteAttackDamage, killed);
                }
        );
    }
}
