package kim.biryeong.semiontd.entity.visual;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.frog.FrogVariant;

public final class FrogVisual {
    private FrogVisual() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final EntityVisual.Builder visual = EntityVisual.builder("minecraft:frog");

        public Builder variant(ResourceKey<FrogVariant> variant) {
            visual.propertyValue(EntityVisualProperties.FROG_VARIANT, variant);
            return this;
        }

        public EntityVisual build() {
            return visual.build();
        }
    }
}
