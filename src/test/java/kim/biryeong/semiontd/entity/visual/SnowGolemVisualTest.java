package kim.biryeong.semiontd.entity.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SnowGolemVisualTest {
    @Test
    void snowGolemStoresPumpkinAndScaleVisualState() {
        EntityVisual visual = SnowGolemVisual.builder().hasPumpkin(false).scale(0.75).build();

        assertEquals("minecraft:snow_golem", visual.entityTypeId());
        assertEquals(false, visual.properties().get("snow_golem_has_pumpkin"));
        assertEquals(0.75, visual.scale());
    }
}
