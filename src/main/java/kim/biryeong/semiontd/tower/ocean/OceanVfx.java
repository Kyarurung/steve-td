package kim.biryeong.semiontd.tower.ocean;

import java.util.List;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

final class OceanVfx {
    private static final DustParticleOptions WATER_STREAM = new DustParticleOptions(0x29B6F6, 0.75F);
    private static final DustParticleOptions WATER_GLOW = new DustParticleOptions(0xB2EBF2, 0.55F);
    private static final int STREAM_POINTS = 7;

    private OceanVfx() {
    }

    static void showWaterSourcePulse(ServerLevel level, Vec3 source, int tier, boolean burst) {
        if (level == null || source == null) {
            return;
        }
        level.sendParticles(
                WATER_STREAM,
                source.x,
                source.y,
                source.z,
                burst ? tier * 4 : tier + 1,
                0.28,
                0.1,
                0.28,
                0.01
        );
        level.sendParticles(
                WATER_GLOW,
                source.x,
                source.y + 0.08,
                source.z,
                burst ? tier * 2 : 1,
                0.2,
                0.08,
                0.2,
                0.005
        );
    }

    static void showWaterTransfer(ServerLevel level, Vec3 source, List<OceanTower> targets, boolean burst) {
        if (level == null || source == null || targets == null || targets.isEmpty()) {
            return;
        }
        for (OceanTower target : targets) {
            Vec3 destination = new Vec3(
                    target.position().x() + 0.5,
                    target.position().y() + 1.2,
                    target.position().z() + 0.5
            );
            Vec3 offset = destination.subtract(source);
            for (int point = 1; point <= STREAM_POINTS; point++) {
                Vec3 position = source.add(offset.scale(point / (double) (STREAM_POINTS + 1)));
                level.sendParticles(
                        point % 2 == 0 ? WATER_GLOW : WATER_STREAM,
                        position.x,
                        position.y,
                        position.z,
                        burst ? 2 : 1,
                        0.012,
                        0.012,
                        0.012,
                        0.002
                );
            }
            level.sendParticles(
                    WATER_GLOW,
                    destination.x,
                    destination.y,
                    destination.z,
                    burst ? 5 : 3,
                    0.1,
                    0.08,
                    0.1,
                    0.006
            );
        }
    }
}
