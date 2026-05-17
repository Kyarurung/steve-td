package kim.biryeong.semiontd.entity.visual;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.item.DyeColor;

public final class WolfVisual {
    private WolfVisual() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final EntityVisual.Builder visual = EntityVisual.builder("minecraft:wolf");

        public Builder variant(ResourceKey<WolfVariant> variant) {
            visual.propertyValue(EntityVisualProperties.WOLF_VARIANT, variant);
            return this;
        }

        public Builder soundVariant(ResourceKey<WolfSoundVariant> soundVariant) {
            visual.propertyValue(EntityVisualProperties.WOLF_SOUND_VARIANT, soundVariant);
            return this;
        }

        public Builder collarColor(DyeColor color) {
            visual.propertyValue(EntityVisualProperties.COLLAR_COLOR, color);
            return this;
        }

        public Builder tame(boolean tame) {
            visual.propertyValue(EntityVisualProperties.TAME, tame);
            return this;
        }

        public Builder sitting(boolean sitting) {
            visual.propertyValue(EntityVisualProperties.SITTING, sitting);
            return this;
        }

        public EntityVisual build() {
            return visual.build();
        }
    }
}
