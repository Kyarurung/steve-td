package kim.biryeong.semiontd.entity.visual;

public final class SnowGolemVisual {
    private SnowGolemVisual() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final EntityVisual.Builder visual = EntityVisual.builder("minecraft:snow_golem");

        public Builder hasPumpkin(boolean hasPumpkin) {
            visual.propertyValue(EntityVisualProperties.SNOW_GOLEM_HAS_PUMPKIN, hasPumpkin);
            return this;
        }

        public Builder scale(double scale) {
            visual.scale(scale);
            return this;
        }

        public EntityVisual build() {
            return visual.build();
        }
    }
}
