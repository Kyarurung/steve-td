package kim.biryeong.semiontd.tower;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.catalog.ProductionTowerBranch;
import kim.biryeong.semiontd.tower.catalog.ProductionTowerDefinitions;
import kim.biryeong.semiontd.tower.catalog.ProductionTowerLine;

public final class ProductionTowerCatalog {
    private static final Map<String, CatalogEntry> ENTRIES = new LinkedHashMap<>();

    private ProductionTowerCatalog() {
    }

    public static Optional<CatalogEntry> find(String towerId) {
        return Optional.ofNullable(ENTRIES.get(towerId));
    }

    public static Collection<CatalogEntry> all() {
        return List.copyOf(ENTRIES.values());
    }

    public static List<CatalogEntry> forFaction(TowerFaction faction) {
        return ENTRIES.values().stream()
                .filter(entry -> entry.behavior().faction() == faction)
                .toList();
    }

    public static Optional<ProductionTowerBehavior> behavior(TowerType type) {
        return type == null ? Optional.empty() : find(type.id()).map(CatalogEntry::behavior);
    }

    public static Optional<CatalogEntry> entry(TowerType type) {
        return type == null ? Optional.empty() : find(type.id());
    }

    public static void registerAll(List<ProductionTowerLine> lines) {
        for (ProductionTowerLine line : lines) {
            registerLine(line);
        }
    }

    public static void registerLine(ProductionTowerLine line) {
        register(line.starter(), line.starterBehavior(), line.starterFactory(), 1);
        registerBranch(line.left());
        registerBranch(line.right());
    }

    private static void registerBranch(ProductionTowerBranch branch) {
        register(branch.tierTwo(), branch.tierTwoBehavior(), branch.tierTwoFactory(), 2);
        register(branch.ultimate(), branch.ultimateBehavior(), branch.ultimateFactory(), 3);
    }

    private static void register(TowerType type, ProductionTowerBehavior behavior, TowerFactory factory, int tier) {
        CatalogEntry previous = ENTRIES.putIfAbsent(type.id(), new CatalogEntry(type, behavior, factory, tier));
        if (previous != null) {
            throw new IllegalArgumentException("Duplicate production tower id: " + type.id());
        }
    }

    @FunctionalInterface
    public interface TowerFactory {
        BaseAttackableTower create(
                TowerType type,
                ProductionTowerBehavior behavior,
                UUID ownerPlayer,
                TeamId teamId,
                int laneId,
                GridPosition originalPosition,
                GridPosition currentPosition
        );
    }

    public record CatalogEntry(TowerType type, ProductionTowerBehavior behavior, TowerFactory factory, int tier) {
        public CatalogEntry {
            factory = factory == null ? ProductionTowerDefinitions.DEFAULT_TOWER_FACTORY : factory;
        }

        public boolean starter() {
            return tier == 1;
        }

        public BaseAttackableTower create(UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
            return create(ownerPlayer, teamId, laneId, position, position);
        }

        public BaseAttackableTower create(
                UUID ownerPlayer,
                TeamId teamId,
                int laneId,
                GridPosition originalPosition,
                GridPosition currentPosition
        ) {
            return factory.create(type, behavior, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
        }
    }
}
