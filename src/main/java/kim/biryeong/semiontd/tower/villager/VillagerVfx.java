package kim.biryeong.semiontd.tower.villager;

import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;

final class VillagerVfx {
    private static final AreaVfxSpec BUFF = AreaVfxSpec.onChange(AreaVfxStyles.BUFF);
    private static final AreaVfxSpec CORPSE_EXPLOSION = AreaVfxSpec.onTrigger(AreaVfxStyles.CORPSE_EXPLOSION);
    private static final AreaVfxSpec PULSE = AreaVfxSpec.onTrigger(AreaVfxStyles.PULSE);

    private VillagerVfx() {
    }

    static AreaVfxSpec buff() {
        return BUFF;
    }

    static AreaVfxSpec corpseExplosion() {
        return CORPSE_EXPLOSION;
    }

    static AreaVfxSpec pulse() {
        return PULSE;
    }

    static void secondaryAttack(SemionTowerEntity tower, SemionMonsterEntity target) {
        TowerVfxService.showSecondaryAttack(tower, target);
    }
}
