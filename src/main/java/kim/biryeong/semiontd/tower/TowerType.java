package kim.biryeong.semiontd.tower;

import java.util.List;
import java.util.Optional;
import kim.biryeong.semiontd.entity.monster.DamageType;
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
        List<TowerUpgradeOption> upgradeOptions,
        DamageType primaryDamageType
) {
    public static Builder builder(String id, String displayName) {
        return new Builder(id, displayName);
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
                description,
                visual,
                upgradeOptions,
                DamageType.PHYSICAL
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
        if (mineralCost < 0
                || !Double.isFinite(maxHealth)
                || maxHealth <= 0
                || !Double.isFinite(range)
                || range < 0
                || !Double.isFinite(damage)
                || damage < 0
                || attackIntervalTicks < 1) {
            throw new IllegalArgumentException("Tower numeric values are invalid.");
        }
        visual = visual == null ? EntityVisual.vanilla(EntityVisual.DEFAULT_TOWER_ENTITY_TYPE) : visual;
        description = description == null ? List.of() : List.copyOf(description);
        upgradeOptions = upgradeOptions == null ? List.of() : List.copyOf(upgradeOptions);
        primaryDamageType = primaryDamageType == null ? DamageType.PHYSICAL : primaryDamageType;
    }

    public TowerType withPrimaryDamageType(DamageType damageType) {
        return new TowerType(
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
                visual,
                upgradeOptions,
                damageType
        );
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

    public static final class Builder {
        private final String id;
        private final String displayName;
        private TowerCategory category = TowerCategory.DIRECT;
        private long mineralCost;
        private double maxHealth = 1.0;
        private double range;
        private double damage;
        private int attackIntervalTicks = 20;
        private int aggroPriority;
        private List<String> description = List.of();
        private EntityVisual visual;
        private List<TowerUpgradeOption> upgradeOptions = List.of();
        private DamageType primaryDamageType = DamageType.PHYSICAL;

        private Builder(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public Builder category(TowerCategory category) {
            this.category = category;
            return this;
        }

        public Builder mineralCost(long mineralCost) {
            this.mineralCost = mineralCost;
            return this;
        }

        public Builder maxHealth(double maxHealth) {
            this.maxHealth = maxHealth;
            return this;
        }

        public Builder range(double range) {
            this.range = range;
            return this;
        }

        public Builder damage(double damage) {
            this.damage = damage;
            return this;
        }

        public Builder attackIntervalTicks(int attackIntervalTicks) {
            this.attackIntervalTicks = attackIntervalTicks;
            return this;
        }

        public Builder aggroPriority(int aggroPriority) {
            this.aggroPriority = aggroPriority;
            return this;
        }

        public Builder description(List<String> description) {
            this.description = description;
            return this;
        }

        public Builder visual(EntityVisual visual) {
            this.visual = visual;
            return this;
        }

        public Builder upgradeOptions(List<TowerUpgradeOption> upgradeOptions) {
            this.upgradeOptions = upgradeOptions;
            return this;
        }

        public Builder primaryDamageType(DamageType primaryDamageType) {
            this.primaryDamageType = primaryDamageType;
            return this;
        }

        public TowerType build() {
            return new TowerType(
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
                    visual,
                    upgradeOptions,
                    primaryDamageType
            );
        }
    }
}
