package kim.biryeong.semiontd.tower.catalog;

import java.util.List;
import kim.biryeong.semiontd.entity.visual.EntityVisual;
import kim.biryeong.semiontd.tower.ProductionTower;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.TowerCategory;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.TowerUpgradeOption;

public final class ProductionTowerDefinitions {
    public static final ProductionTowerCatalog.TowerFactory DEFAULT_TOWER_FACTORY = ProductionTower::new;

    private ProductionTowerDefinitions() {
    }

    public static TowerType tower(
            String id,
            String displayName,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority,
            String entityTypeId,
            List<String> description
    ) {
        return tower(id, displayName, mineralCost, maxHealth, range, damage, attackIntervalTicks, aggroPriority, entityTypeId, description, List.of());
    }

    public static TowerType tower(
            String id,
            String displayName,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority,
            EntityVisual visual,
            List<String> description
    ) {
        return tower(id, displayName, mineralCost, maxHealth, range, damage, attackIntervalTicks, aggroPriority, visual, description, List.of());
    }

    public static TowerType tower(
            String id,
            String displayName,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority,
            String entityTypeId,
            List<String> description,
            List<TowerUpgradeOption> upgrades
    ) {
        return new TowerType(
                id,
                displayName,
                TowerCategory.DIRECT,
                mineralCost,
                maxHealth,
                range,
                damage,
                attackIntervalTicks,
                aggroPriority,
                description,
                entityTypeId,
                null,
                upgrades
        );
    }

    public static TowerType tower(
            String id,
            String displayName,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority,
            EntityVisual visual,
            List<String> description,
            List<TowerUpgradeOption> upgrades
    ) {
        return new TowerType(
                id,
                displayName,
                TowerCategory.DIRECT,
                mineralCost,
                maxHealth,
                range,
                damage,
                attackIntervalTicks,
                aggroPriority,
                description,
                visual,
                upgrades
        );
    }

    public static TowerUpgradeOption upgrade(String id, String displayName, TowerType target, long mineralCost) {
        return new TowerUpgradeOption(id, displayName, target, mineralCost);
    }

}
