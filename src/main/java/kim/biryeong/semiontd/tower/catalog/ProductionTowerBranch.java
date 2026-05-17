package kim.biryeong.semiontd.tower.catalog;

import kim.biryeong.semiontd.tower.ProductionTowerBehavior;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.TowerType;

public record ProductionTowerBranch(
        TowerType tierTwo,
        ProductionTowerBehavior tierTwoBehavior,
        ProductionTowerCatalog.TowerFactory tierTwoFactory,
        TowerType ultimate,
        ProductionTowerBehavior ultimateBehavior,
        ProductionTowerCatalog.TowerFactory ultimateFactory
) {
}
