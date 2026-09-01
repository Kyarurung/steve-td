package kim.biryeong.semiontd.tower.nether;

import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;

final class NetherVfx {
    private NetherVfx() {
    }

    static AreaVfxSpec splash() {
        return AreaVfxSpec.onTrigger(AreaVfxStyles.SPLASH);
    }

    static AreaVfxSpec pulse() {
        return AreaVfxSpec.onTrigger(AreaVfxStyles.PULSE);
    }

    static void transition(SemionTowerEntity tower) {
        TowerVfxService.showNetherTransition(tower);
    }

    static void secondaryAttack(SemionTowerEntity tower, SemionMonsterEntity target) {
        TowerVfxService.showSecondaryAttack(tower, target);
    }
}
