package kim.biryeong.semiontd.tower;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
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

    public static Optional<CatalogEntry> entry(TowerType type) {
        return type == null ? Optional.empty() : find(type.id());
    }

    public static void registerAll(List<ProductionTowerLine> lines) {
        for (ProductionTowerLine line : lines) {
            registerLine(line);
        }
    }

    public static void registerLine(ProductionTowerLine line) {
        register(line.starter(), line.starterFactory(), 1);
        register(line.left().tierTwo(), line.left().tierTwoFactory(), 2);
        register(line.left().ultimate(), line.left().ultimateFactory(), 3);
        register(line.right().tierTwo(), line.right().tierTwoFactory(), 2);
        register(line.right().ultimate(), line.right().ultimateFactory(), 3);
    }

    private static void register(TowerType type, TowerFactory factory, int tier) {
        CatalogEntry previous = ENTRIES.putIfAbsent(type.id(), new CatalogEntry(type, factory, tier));
        if (previous != null) {
            throw new IllegalArgumentException("Duplicate production tower id: " + type.id());
        }
    }

    @FunctionalInterface
    public interface TowerFactory {
        EntityBackedTower create(
                TowerType type,
                UUID ownerPlayer,
                TeamId teamId,
                int laneId,
                GridPosition originalPosition,
                GridPosition currentPosition
        );
    }

    public record CatalogEntry(TowerType type, TowerFactory factory, int tier) {
        public CatalogEntry {
            factory = factory == null ? ProductionTowerDefinitions.DEFAULT_TOWER_FACTORY : factory;
        }

        public boolean starter() {
            return tier == 1;
        }

        public EntityBackedTower create(UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
            return create(ownerPlayer, teamId, laneId, position, position);
        }

        public EntityBackedTower create(
                UUID ownerPlayer,
                TeamId teamId,
                int laneId,
                GridPosition originalPosition,
                GridPosition currentPosition
        ) {
            return factory.create(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
        }
    }
}
