package kim.biryeong.semiontd.tower.legion;

import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;

final class LegionCombat {
    private LegionCombat() {
    }

    static void splash(Tower tower, SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        if (towerEntity == null || target == null) {
            return;
        }
        double radius = LegionConfig.RUNTIME.value(tower.type(), LegionAbilityKey.SPLASH_RADIUS);
        double ratio = LegionConfig.RUNTIME.value(tower.type(), LegionAbilityKey.SPLASH_DAMAGE_RATIO);
        if (radius <= 0.0 || ratio <= 0.0) {
            return;
        }
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTarget(
                AreaEffectIds.tower(tower, "splash"), towerEntity, target, radius, LegionVfx.splash()
        );
        TowerAreaDamage.applyBasicAttackSplash(
                tower, towerEntity, request, monster -> damageAmount * ratio, true
        );
    }

    static double attackMultiplier(int stacks, double bonusPerStack) {
        return 1.0 + Math.max(0, stacks) * bonusPerStack;
    }

    static int attackInterval(int baseTicks, double multiplier) {
        return Math.max(1, (int) Math.ceil(baseTicks / multiplier));
    }
}
