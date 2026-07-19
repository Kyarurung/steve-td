package kim.biryeong.semiontd.entity.tower.vfx;

import kim.biryeong.gcbserver.packet.s2c.GCBParticleS2CPacket;
import net.minecraft.SharedConstants;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TowerVfxServiceTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void preservesDustColorAndScaleForGcb() {
        var payload = TowerVfxService.gcbPayload(
                new DustParticleOptions(0x512DA8, 1.15F),
                "minecraft:witch",
                new GCBParticleS2CPacket.Sphere(
                        GCBParticleS2CPacket.Vec.UNIT_Y,
                        1,
                        GCBParticleS2CPacket.ShapeOptions.DEFAULT,
                        GCBParticleS2CPacket.Vec.ZERO
                )
        );

        assertEquals("dust_color_transition", payload.particle());
        assertEquals(0x512DA8 + "," + 0x512DA8 + ",1.15", payload.data());
    }
}
