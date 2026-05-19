package kim.biryeong.semiontd.tower.catalog;

import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.TowerType;

public record ProductionTowerLine(
        TowerType starter,
        ProductionTowerCatalog.TowerFactory starterFactory,
        ProductionTowerBranch left,
        ProductionTowerBranch right
) {
}
