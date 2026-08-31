package kim.biryeong.semiontd.tower.legion;

import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;

final class LegionVfx {
    private static final AreaVfxSpec BUFF = AreaVfxSpec.onChange(AreaVfxStyles.BUFF);
    private static final AreaVfxSpec SPLASH = AreaVfxSpec.onTrigger(AreaVfxStyles.SPLASH);

    private LegionVfx() {
    }

    static AreaVfxSpec buff() {
        return BUFF;
    }

    static AreaVfxSpec splash() {
        return SPLASH;
    }
}
