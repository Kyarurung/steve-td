package kim.biryeong.semiontd.entity.tower.vfx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import kim.biryeong.semiontd.tower.TowerType;
import org.junit.jupiter.api.Test;

final class BuilderPaletteRegistryTest {
    @Test
    void firstMatchingDescriptorWins() {
        TowerType type = TowerType.builder("test:overlap", "overlap").build();
        BuilderPaletteRegistry registry = new BuilderPaletteRegistry(BuilderPalette.DEFAULT)
                .register(new BuilderPaletteDescriptor(
                        candidate -> candidate.id().startsWith("test:"), BuilderPalette.OCEAN))
                .register(candidate -> candidate.id().equals("test:overlap"), BuilderPalette.ANCIENT_CITY);
        assertEquals(BuilderPalette.OCEAN, registry.resolve(type));
    }

    @Test
    void unknownAndNullTypesUseFallback() {
        TowerType unknown = TowerType.builder("test:unknown", "unknown").build();
        BuilderPaletteRegistry registry = new BuilderPaletteRegistry(BuilderPalette.DEFAULT);
        assertEquals(BuilderPalette.DEFAULT, registry.resolve(unknown));
        assertEquals(BuilderPalette.DEFAULT, registry.resolve(null));
    }
}
