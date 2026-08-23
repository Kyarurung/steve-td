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
        var progression = tower.progressionSnapshot();
        return config.path(tower.path()).splash().radius(progression.splashSacrificeCount(tower.path()));
    }

    double maximumSplashRadius(WarlockPath path) {
        return config.path(path).splash().maximumRadius();
    }

    double lifeStealRatio(WarlockTower tower) {
        var progression = tower.progressionSnapshot();
        return lifeStealRatioForCount(
                tower.path(),
                progression.lifeStealSacrificeCount(tower.path()),
                tower.isLastSurvivingTower()
        );
    }

    double lifeStealRatioForCount(WarlockPath path, int sacrificeCount, boolean lastSurvivingTower) {
        if (path == WarlockPath.MELEE && !lastSurvivingTower) {
            return 0.0;
        }
        return config.path(path).lifeSteal().value(sacrificeCount);
    }

    double maximumLifeSteal(WarlockPath path) {
        return config.path(path).lifeSteal().maximum();
    }

    int meleeAttackIntervalReduction(int roundSacrificeCount) {
        return config.combat().meleeIntervalReduction(roundSacrificeCount);
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
