package kim.biryeong.semiontd.tower.illager;

import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;

final class IllagerRaidVfx {
    private IllagerRaidVfx() {
    }

    static void showActivation(SemionTowerEntity towerEntity) {
        TowerVfxService.showIllagerRaidActivation(towerEntity);
    }
}
