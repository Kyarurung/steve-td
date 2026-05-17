package kim.biryeong.semiontd.tower;

import java.util.List;
import java.util.Optional;
import kim.biryeong.semiontd.entity.visual.EntityVisual;

public record TowerType(
        String id,
        String displayName,
        TowerCategory category,
        long mineralCost,
        double maxHealth,
        double range,
        double damage,
        int attackIntervalTicks,
        int aggroPriority,
        List<String> description,
        EntityVisual visual,
        List<TowerUpgradeOption> upgradeOptions
) {
    public TowerType(
            String id,
            String displayName,
            TowerCategory category,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority
    ) {
        this(id, displayName, category, mineralCost, maxHealth, range, damage, attackIntervalTicks, aggroPriority, List.of());
    }

    public TowerType(
            String id,
            String displayName,
            TowerCategory category,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority,
            List<TowerUpgradeOption> upgradeOptions
    ) {
        this(
                id,
                displayName,
                category,
                mineralCost,
                maxHealth,
                range,
                damage,
                attackIntervalTicks,
                aggroPriority,
                List.of(),
                EntityVisual.vanilla(EntityVisual.DEFAULT_TOWER_ENTITY_TYPE),
                upgradeOptions
        );
    }

    public TowerType(
            String id,
            String displayName,
            TowerCategory category,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority,
            String entityTypeId
    ) {
        this(
                id,
                displayName,
                category,
                mineralCost,
                maxHealth,
                range,
                damage,
                attackIntervalTicks,
                aggroPriority,
                List.of(),
                EntityVisual.vanilla(entityTypeId),
                List.of()
        );
    }

    public TowerType(
            String id,
            String displayName,
            TowerCategory category,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority,
            String entityTypeId,
            List<TowerUpgradeOption> upgradeOptions
    ) {
        this(
                id,
                displayName,
                category,
                mineralCost,
                maxHealth,
                range,
                damage,
                attackIntervalTicks,
                aggroPriority,
                List.of(),
                EntityVisual.vanilla(entityTypeId),
                upgradeOptions
        );
    }

    public TowerType(
            String id,
            String displayName,
            TowerCategory category,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority,
            String entityTypeId,
            String blockbenchModelId,
            List<TowerUpgradeOption> upgradeOptions
    ) {
        this(
                id,
                displayName,
                category,
                mineralCost,
                maxHealth,
                range,
                damage,
                attackIntervalTicks,
                aggroPriority,
                EntityVisual.modeled(entityTypeId, blockbenchModelId),
                upgradeOptions
        );
    }

    public TowerType(
            String id,
            String displayName,
            TowerCategory category,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority,
            EntityVisual visual,
            List<TowerUpgradeOption> upgradeOptions
    ) {
        this(
                id,
                displayName,
                category,
                mineralCost,
                maxHealth,
                range,
                damage,
                attackIntervalTicks,
                aggroPriority,
                List.of(),
                visual,
                upgradeOptions
        );
    }

    public TowerType(
            String id,
            String displayName,
            TowerCategory category,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority,
            List<String> description,
            String entityTypeId
    ) {
        this(
                id,
                displayName,
                category,
                mineralCost,
                maxHealth,
                range,
                damage,
                attackIntervalTicks,
                aggroPriority,
                description,
                EntityVisual.vanilla(entityTypeId),
                List.of()
        );
    }

    public TowerType(
            String id,
            String displayName,
            TowerCategory category,
            long mineralCost,
            double maxHealth,
            double range,
            double damage,
            int attackIntervalTicks,
            int aggroPriority,
            List<String> description,
            String entityTypeId,
            String blockbenchModelId,
            List<TowerUpgradeOption> upgradeOptions
    ) {
        this(
                id,
                displayName,
                category,
                mineralCost,
                maxHealth,
                range,
                damage,
                attackIntervalTicks,
                aggroPriority,
                description,
                EntityVisual.modeled(entityTypeId, blockbenchModelId),
                upgradeOptions
        );
    }

    public TowerType {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Tower id cannot be blank.");
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = id;
        }
        if (category == null) {
            category = TowerCategory.DIRECT;
        }
        if (mineralCost < 0 || maxHealth <= 0 || range < 0 || damage < 0 || attackIntervalTicks < 1) {
            throw new IllegalArgumentException("Tower numeric values are invalid.");
        }
        visual = visual == null ? EntityVisual.vanilla(EntityVisual.DEFAULT_TOWER_ENTITY_TYPE) : visual;
        description = description == null ? List.of() : List.copyOf(description);
        upgradeOptions = List.copyOf(upgradeOptions);
    }

    public boolean hasUpgradeOptions() {
        return !upgradeOptions.isEmpty();
    }

    public Optional<String> blockbenchModel() {
        return visual.blockbenchModel();
    }

    public String entityTypeId() {
        return visual.entityTypeId();
    }

    public String blockbenchModelId() {
        return visual.blockbenchModelId();
    }
}
