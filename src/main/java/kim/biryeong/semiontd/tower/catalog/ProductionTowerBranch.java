package kim.biryeong.semiontd.tower.catalog;

import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.TowerType;

public record ProductionTowerBranch(
        TowerType tierTwo,
        ProductionTowerCatalog.TowerFactory tierTwoFactory,
        TowerType ultimate,
        ProductionTowerCatalog.TowerFactory ultimateFactory
) {
}
