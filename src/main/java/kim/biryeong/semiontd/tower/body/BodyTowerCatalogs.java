package kim.biryeong.semiontd.tower.body;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.TowerType;

public final class BodyTowerCatalogs {
    private BodyTowerCatalogs() {
    }

    public static void register() {
        for (TowerType type : BodyTowers.all()) {
            int tier = BodyTowers.tier(type);
            TowerType resolved = TowerBalanceRuntime.resolve(type);
            if (tier == 1) {
                ProductionTowerCatalog.registerStarter(resolved, BodyTower::new);
            } else {
                ProductionTowerCatalog.register(resolved, BodyTower::new, tier);
            }
        }

        link(BodyTowers.HEART_T1, BodyTowers.HEART_T2, 145);
        link(BodyTowers.HEART_T2, BodyTowers.HEART_T3, 270);
        link(BodyTowers.BRAIN_T1, BodyTowers.BRAIN_T2, 110);
        link(BodyTowers.BRAIN_T2, BodyTowers.BRAIN_T3, 200);
        link(BodyTowers.SKIN_T1, BodyTowers.SKIN_T2, 110);
        link(BodyTowers.SKIN_T2, BodyTowers.SKIN_T3, 210);
        link(BodyTowers.EYE_T1, BodyTowers.EYE_T2, 110);
        link(BodyTowers.EYE_T2, BodyTowers.EYE_T3, 220);
        link(BodyTowers.GENITAL_T1, BodyTowers.GENITAL_T2, 100);
        link(BodyTowers.GENITAL_T2, BodyTowers.GENITAL_T3, 205);
    }

    private static void link(TowerType from, TowerType to, long fallbackCost) {
        TowerType target = ProductionTowerCatalog.find(to.id())
                .map(ProductionTowerCatalog.CatalogEntry::type)
                .orElse(to);
        ProductionTowerCatalog.linkUpgrade(
                from,
                to.id(),
                to.displayName(),
                target,
                TowerBalanceRuntime.upgradeCost(from, to.id(), fallbackCost)
        );
    }
}
