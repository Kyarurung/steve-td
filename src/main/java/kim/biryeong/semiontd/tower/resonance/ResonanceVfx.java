package kim.biryeong.semiontd.tower.resonance;

import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;

final class ResonanceVfx {
    private static final AreaVfxSpec BUFF = AreaVfxSpec.onChange(AreaVfxStyles.BUFF);
    private static final AreaVfxSpec DEBUFF = AreaVfxSpec.onChange(AreaVfxStyles.DEBUFF);
    private static final AreaVfxSpec PULSE = AreaVfxSpec.onTrigger(AreaVfxStyles.PULSE);
    private static final AreaVfxSpec SPLASH = AreaVfxSpec.onTrigger(AreaVfxStyles.SPLASH);

    private ResonanceVfx() {
    }

    static AreaVfxSpec buff() {
        return BUFF;
    }

    static AreaVfxSpec debuff() {
        return DEBUFF;
    }

    static AreaVfxSpec areaAttack(boolean pulse) {
        return pulse ? PULSE : SPLASH;
    }

    static void secondaryAttack(SemionTowerEntity tower, SemionMonsterEntity target) {
        TowerVfxService.showSecondaryAttack(tower, target);
    }
}
