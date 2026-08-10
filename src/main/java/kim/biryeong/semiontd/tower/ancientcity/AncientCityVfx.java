package kim.biryeong.semiontd.tower.ancientcity;

import java.util.List;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class AncientCityVfx {
    private static final DustParticleOptions DARK_SCULK = new DustParticleOptions(0x0B4F57, 0.95F);
    private static final DustParticleOptions BRIGHT_SCULK = new DustParticleOptions(0x63E6E2, 0.8F);

    private AncientCityVfx() {
    }

    public static void showGrowth(ServerLevel level, BlockPos position, boolean mainTerritory) {
        if (level == null || position == null) {
            return;
        }
        Vec3 center = Vec3.atCenterOf(position).add(0.0, 0.35, 0.0);
        level.sendParticles(ParticleTypes.SCULK_SOUL, center.x, center.y, center.z,
                mainTerritory ? 7 : 4, 0.32, 0.12, 0.32, 0.025);
        ring(level, BRIGHT_SCULK, center, 0.55, 12);
    }

    static void showCatalyst(SemionTowerEntity tower, double radius) {
        if (tower == null || !(tower.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = tower.position().add(0.0, tower.getBbHeight() * 0.45, 0.0);
        ring(level, DARK_SCULK, center, Math.max(0.5, radius), 22);
        level.sendParticles(ParticleTypes.SCULK_SOUL, center.x, center.y, center.z, 10,
                Math.min(1.0, radius * 0.25), 0.25, Math.min(1.0, radius * 0.25), 0.03);
    }

    static void showSensor(SemionTowerEntity tower, SemionMonsterEntity target) {
        if (tower == null || target == null || !(tower.level() instanceof ServerLevel level)) {
            return;
        }
        beam(level, towerCenter(tower), targetCenter(target), BRIGHT_SCULK, 14);
        ring(level, BRIGHT_SCULK, targetCenter(target), 0.55, 14);
    }

    static void showShrieker(SemionTowerEntity tower, SemionMonsterEntity target, double radius) {
        if (tower == null || target == null || !(tower.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = targetCenter(target);
        ring(level, DARK_SCULK, center, Math.max(0.5, radius * 0.55), 20);
        ring(level, BRIGHT_SCULK, center.add(0.0, 0.3, 0.0), Math.max(0.75, radius), 28);
        level.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y + 0.25, center.z, 1, 0, 0, 0, 0);
    }

    static void showWarden(SemionTowerEntity tower, List<SemionMonsterEntity> targets) {
        if (tower == null || targets == null || targets.isEmpty()
                || !(tower.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 source = towerCenter(tower);
        for (SemionMonsterEntity target : targets) {
            Vec3 destination = targetCenter(target);
            beam(level, source, destination, BRIGHT_SCULK, 18);
            level.sendParticles(ParticleTypes.SONIC_BOOM, destination.x, destination.y, destination.z,
                    1, 0, 0, 0, 0);
        }
    }

    public static void showDebug(ServerPlayer player, DebugKind kind) {
        if (player == null || kind == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 forward = horizontalLook(player);
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
        Vec3 source = player.position().add(forward.scale(3.0)).add(0.0, 1.0, 0.0);
        Vec3 target = player.position().add(forward.scale(6.0)).add(0.0, 1.0, 0.0);
        switch (kind) {
            case GROWTH -> showGrowth(level, BlockPos.containing(source.subtract(0.0, 1.0, 0.0)), true);
            case CATALYST -> {
                ring(level, DARK_SCULK, source, 2.5, 22);
                level.sendParticles(ParticleTypes.SCULK_SOUL, source.x, source.y, source.z,
                        10, 0.6, 0.25, 0.6, 0.03);
            }
            case SENSOR -> {
                beam(level, source, target, BRIGHT_SCULK, 14);
                ring(level, BRIGHT_SCULK, target, 0.55, 14);
            }
            case SHRIEK -> {
                ring(level, DARK_SCULK, target, 1.6, 20);
                ring(level, BRIGHT_SCULK, target.add(0.0, 0.3, 0.0), 3.0, 28);
                level.sendParticles(ParticleTypes.SONIC_BOOM, target.x, target.y + 0.25, target.z,
                        1, 0, 0, 0, 0);
            }
            case WARDEN -> {
                for (Vec3 destination : List.of(target, target.add(right.scale(-1.5)), target.add(right.scale(1.5)))) {
                    beam(level, source, destination, BRIGHT_SCULK, 18);
                    level.sendParticles(ParticleTypes.SONIC_BOOM, destination.x, destination.y, destination.z,
                            1, 0, 0, 0, 0);
                }
            }
        }
    }

    private static void beam(
            ServerLevel level,
            Vec3 source,
            Vec3 target,
            ParticleOptions particle,
            int points
    ) {
        Vec3 offset = target.subtract(source);
        for (int index = 0; index <= points; index++) {
            Vec3 point = source.add(offset.scale(index / (double) points));
            level.sendParticles(index % 4 == 0 ? ParticleTypes.ELECTRIC_SPARK : particle,
                    point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }

    private static void ring(ServerLevel level, ParticleOptions particle, Vec3 center, double radius, int points) {
        for (int index = 0; index < points; index++) {
            double angle = Math.PI * 2.0 * index / points;
            level.sendParticles(particle,
                    center.x + Math.cos(angle) * radius,
                    center.y,
                    center.z + Math.sin(angle) * radius,
                    1, 0, 0, 0, 0);
        }
    }

    private static Vec3 towerCenter(SemionTowerEntity tower) {
        return tower.position().add(0.0, Math.max(0.4, tower.getBbHeight() * 0.55), 0.0);
    }

    private static Vec3 targetCenter(SemionMonsterEntity target) {
        return target.position().add(0.0, Math.max(0.4, target.getBbHeight() * 0.5), 0.0);
    }

    private static Vec3 horizontalLook(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0, look.z);
        return horizontal.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : horizontal.normalize();
    }

    public enum DebugKind {
        GROWTH,
        CATALYST,
        SENSOR,
        SHRIEK,
        WARDEN
    }
}
