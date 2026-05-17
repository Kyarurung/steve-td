package kim.biryeong.semiontd.entity.visual;

import net.minecraft.world.item.DyeColor;

public final class SheepVisual {
    private SheepVisual() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final EntityVisual.Builder visual = EntityVisual.builder("minecraft:sheep");

        public Builder color(DyeColor color) {
            visual.propertyValue(EntityVisualProperties.SHEEP_COLOR, color);
            return this;
        }

        public Builder sheared(boolean sheared) {
            visual.propertyValue(EntityVisualProperties.SHEARED, sheared);
            return this;
        }

        public EntityVisual build() {
            return visual.build();
        }
    }
}
