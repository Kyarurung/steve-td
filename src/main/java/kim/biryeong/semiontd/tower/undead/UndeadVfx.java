package kim.biryeong.semiontd.tower.undead;

import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;

final class UndeadVfx {
    private static final AreaVfxSpec DEBUFF = AreaVfxSpec.onChange(AreaVfxStyles.DEBUFF);
    private static final AreaVfxSpec PULSE = AreaVfxSpec.onTrigger(AreaVfxStyles.PULSE);

    private UndeadVfx() {
    }

    static AreaVfxSpec debuff() {
        return DEBUFF;
    }

    static AreaVfxSpec pulse() {
        return PULSE;
    }

    static void secondaryAttack(SemionTowerEntity tower, SemionMonsterEntity target) {
        TowerVfxService.showSecondaryAttack(tower, target);
    }
}
