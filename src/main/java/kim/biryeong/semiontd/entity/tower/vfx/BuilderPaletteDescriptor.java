package kim.biryeong.semiontd.entity.tower.vfx;

import java.util.Objects;
import java.util.function.Predicate;
import kim.biryeong.semiontd.tower.TowerType;

record BuilderPaletteDescriptor(Predicate<TowerType> matcher, BuilderPalette palette) {
    BuilderPaletteDescriptor {
        Objects.requireNonNull(matcher, "matcher");
        Objects.requireNonNull(palette, "palette");
    }

    boolean matches(TowerType type) {
        return matcher.test(type);
    }
}
