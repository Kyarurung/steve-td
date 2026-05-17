package kim.biryeong.semiontd.entity.visual;

import net.minecraft.world.entity.animal.axolotl.Axolotl;

public final class AxolotlVisual {
    private AxolotlVisual() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final EntityVisual.Builder visual = EntityVisual.builder("minecraft:axolotl");

        public Builder variant(Axolotl.Variant variant) {
            visual.propertyValue(EntityVisualProperties.AXOLOTL_VARIANT, variant);
            return this;
        }

        public EntityVisual build() {
            return visual.build();
        }
    }
}
