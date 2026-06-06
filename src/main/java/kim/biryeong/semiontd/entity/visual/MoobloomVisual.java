package kim.biryeong.semiontd.entity.visual;

public final class MoobloomVisual {
    private static final String ENTITY_TYPE_ID = "friendsandfoes:moobloom";

    private MoobloomVisual() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static boolean matches(EntityVisual visual) {
        return visual != null && ENTITY_TYPE_ID.equals(visual.entityTypeId());
    }

    public static String variant(EntityVisual visual) {
        if (visual == null) {
            return null;
        }
        Object variant = visual.properties().get(EntityVisualProperties.MOOBLOOM_VARIANT);
        return variant instanceof String string ? string : null;
    }

    public static final class Builder {
        private final EntityVisual.Builder visual = EntityVisual.builder(ENTITY_TYPE_ID);

        public Builder variant(String variant) {
            visual.property(EntityVisualProperties.MOOBLOOM_VARIANT, variant);
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
