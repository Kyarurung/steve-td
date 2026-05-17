package kim.biryeong.semiontd.tower.catalog;

import java.util.List;
import kim.biryeong.semiontd.entity.visual.EntityVisual;
import kim.biryeong.semiontd.tower.ProductionTower;
import kim.biryeong.semiontd.tower.ProductionTowerBehavior;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.TowerCategory;
import kim.biryeong.semiontd.tower.TowerFaction;
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

    public static ProductionTowerBehavior behavior(
            TowerFaction faction,
            String mechanicName,
            double splashRadius,
            double splashDamageMultiplier,
            int maxStacks,
            double damagePerStack,
            double attackSpeedPerStack,
            boolean stackOnHit,
            boolean stackOnKill,
            double killSplashRadius,
            double killSplashDamageMultiplier
    ) {
        return new ProductionTowerBehavior(
                faction,
                mechanicName,
                splashRadius,
                splashDamageMultiplier,
                maxStacks,
                damagePerStack,
                attackSpeedPerStack,
                stackOnHit,
                stackOnKill,
                killSplashRadius,
                killSplashDamageMultiplier
        );
    }

    public static TowerUpgradeOption upgrade(String id, String displayName, TowerType target, long mineralCost) {
        return new TowerUpgradeOption(id, displayName, target, mineralCost);
    }

    public static ProductionTowerBranch branch(
            TowerType tierTwo,
            ProductionTowerBehavior tierTwoBehavior,
            TowerType ultimate,
            ProductionTowerBehavior ultimateBehavior
    ) {
        return branch(tierTwo, tierTwoBehavior, DEFAULT_TOWER_FACTORY, ultimate, ultimateBehavior, DEFAULT_TOWER_FACTORY);
    }

    public static ProductionTowerBranch branch(
            TowerType tierTwo,
            ProductionTowerBehavior tierTwoBehavior,
            ProductionTowerCatalog.TowerFactory tierTwoFactory,
            TowerType ultimate,
            ProductionTowerBehavior ultimateBehavior,
            ProductionTowerCatalog.TowerFactory ultimateFactory
    ) {
        return new ProductionTowerBranch(tierTwo, tierTwoBehavior, tierTwoFactory, ultimate, ultimateBehavior, ultimateFactory);
    }

    public static ProductionTowerLine line(
            TowerType starter,
            ProductionTowerBehavior starterBehavior,
            ProductionTowerBranch left,
            ProductionTowerBranch right
    ) {
        return line(starter, starterBehavior, DEFAULT_TOWER_FACTORY, left, right);
    }

    public static ProductionTowerLine line(
            TowerType starter,
            ProductionTowerBehavior starterBehavior,
            ProductionTowerCatalog.TowerFactory starterFactory,
            ProductionTowerBranch left,
            ProductionTowerBranch right
    ) {
        return new ProductionTowerLine(starter, starterBehavior, starterFactory, left, right);
    }
}
