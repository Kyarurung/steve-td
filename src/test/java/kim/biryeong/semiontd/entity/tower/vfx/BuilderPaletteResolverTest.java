package kim.biryeong.semiontd.entity.tower.vfx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import kim.biryeong.semiontd.tower.TowerType;
import org.junit.jupiter.api.Test;

final class BuilderPaletteResolverTest {
    @Test
    void firstMatchingRegistrationWins() {
        TowerType type = TowerType.builder("test:overlap", "overlap").build();
        BuilderPaletteResolver resolver = new BuilderPaletteResolver(BuilderPalette.DEFAULT)
                .register(candidate -> candidate.id().startsWith("test:"), BuilderPalette.OCEAN)
                .register(candidate -> candidate.id().equals("test:overlap"), BuilderPalette.ANCIENT_CITY);
        assertEquals(BuilderPalette.OCEAN, resolver.resolve(type));
    }

    @Test
    void unknownAndNullTypesUseFallback() {
        TowerType unknown = TowerType.builder("test:unknown", "unknown").build();
        BuilderPaletteResolver resolver = new BuilderPaletteResolver(BuilderPalette.DEFAULT);
        assertEquals(BuilderPalette.DEFAULT, resolver.resolve(unknown));
        assertEquals(BuilderPalette.DEFAULT, resolver.resolve(null));
    }
}
