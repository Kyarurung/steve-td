package kim.biryeong.semiontd.tower.animal;

import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;

final class AnimalVfx {
    private static final AreaVfxSpec SPLASH = AreaVfxSpec.onTrigger(AreaVfxStyles.SPLASH);

    private AnimalVfx() {
    }

    static AreaVfxSpec splash() {
        return SPLASH;
    }

    static void secondaryAttack(SemionTowerEntity tower, SemionMonsterEntity target) {
        TowerVfxService.showSecondaryAttack(tower, target);
    }
}
