package kim.biryeong.semiontd.entity.visual;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import kim.biryeong.semiontd.entity.model.SemionBilModelCache;

public record EntityVisual(
        String entityTypeId,
        String blockbenchModelId,
        Map<String, Object> properties
) {
    public static final String DEFAULT_TOWER_ENTITY_TYPE = "minecraft:villager";

    public EntityVisual {
        entityTypeId = SemionBilModelCache.normalize(entityTypeId);
        blockbenchModelId = SemionBilModelCache.normalize(blockbenchModelId);
        if (entityTypeId == null && blockbenchModelId == null) {
            entityTypeId = DEFAULT_TOWER_ENTITY_TYPE;
        }
        properties = normalizeProperties(properties);
    }

    public static EntityVisual vanilla(String entityTypeId) {
        return new EntityVisual(entityTypeId, null, Map.of());
    }

    public static EntityVisual modeled(String entityTypeId, String blockbenchModelId) {
        return new EntityVisual(entityTypeId, blockbenchModelId, Map.of());
    }

    public static Builder builder(String entityTypeId) {
        return new Builder(entityTypeId);
    }

    public Optional<String> blockbenchModel() {
        return Optional.ofNullable(blockbenchModelId);
    }

    Optional<String> property(String key) {
        return property(key, String.class);
    }

    Optional<Object> propertyValue(String key) {
        return Optional.ofNullable(properties.get(normalizeKey(key)));
    }

    <T> Optional<T> property(String key, Class<T> valueType) {
        Object value = properties.get(normalizeKey(key));
        return valueType.isInstance(value) ? Optional.of(valueType.cast(value)) : Optional.empty();
    }

    EntityVisual withProperty(String key, String value) {
        return withPropertyValue(key, value);
    }

    EntityVisual withPropertyValue(String key, Object value) {
        LinkedHashMap<String, Object> nextProperties = new LinkedHashMap<>(properties);
        putNormalized(nextProperties, key, value);
        return new EntityVisual(entityTypeId, blockbenchModelId, nextProperties);
    }

    private static Map<String, Object> normalizeProperties(Map<String, Object> properties) {
        if (properties == null || properties.isEmpty()) {
            return Map.of();
        }

        LinkedHashMap<String, Object> normalized = new LinkedHashMap<>();
        properties.forEach((key, value) -> putNormalized(normalized, key, value));
        return Map.copyOf(normalized);
    }

    private static void putNormalized(Map<String, Object> properties, String key, Object value) {
        if (key == null || key.isBlank() || value == null) {
            return;
        }
        if (value instanceof String stringValue) {
            if (stringValue.isBlank()) {
                return;
            }
            properties.put(normalizeKey(key), stringValue.trim());
            return;
        }
        properties.put(normalizeKey(key), value);
    }

    private static String normalizeKey(String key) {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
    }

    public static final class Builder {
        private final String entityTypeId;
        private String blockbenchModelId;
        private final LinkedHashMap<String, Object> properties = new LinkedHashMap<>();

        private Builder(String entityTypeId) {
            this.entityTypeId = entityTypeId;
        }

        public Builder blockbenchModel(String blockbenchModelId) {
            this.blockbenchModelId = blockbenchModelId;
            return this;
        }

        Builder property(String key, String value) {
            putNormalized(properties, key, value);
            return this;
        }

        Builder propertyValue(String key, Object value) {
            putNormalized(properties, key, value);
            return this;
        }

        public EntityVisual build() {
            return new EntityVisual(entityTypeId, blockbenchModelId, properties);
        }
    }
}
