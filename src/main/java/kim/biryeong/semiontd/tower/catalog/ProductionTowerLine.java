package kim.biryeong.semiontd.tower.catalog;

import kim.biryeong.semiontd.tower.ProductionTowerBehavior;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.TowerType;

public record ProductionTowerLine(
        TowerType starter,
        ProductionTowerBehavior starterBehavior,
        ProductionTowerCatalog.TowerFactory starterFactory,
        ProductionTowerBranch left,
        ProductionTowerBranch right
) {
}
