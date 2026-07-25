package kim.biryeong.semiontd.trait;

import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.util.Scheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class TraitVfx {
    private TraitVfx() {
    }

    public static void showIgniteApplied(SemionMonsterEntity target) {
        if (target != null && target.level() instanceof ServerLevel level) {
            showIgniteApplied(level, center(target));
        }
    }

    public static void showIgniteActive(SemionMonsterEntity target) {
        if (target != null && target.level() instanceof ServerLevel level) {
            showIgniteActive(level, center(target));
        }
    }

    public static void showIgniteTick(SemionMonsterEntity target) {
        if (target != null && target.level() instanceof ServerLevel level) {
            showIgniteTick(level, center(target));
        }
    }

    public static void showIgniteDebug(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        Vec3 center = player.getEyePosition().add(player.getLookAngle().scale(3.0)).add(0.0, -0.35, 0.0);
        UUID playerId = player.getUUID();
        showIgniteApplied(level, center);
        for (int delay = 5; delay <= 80; delay += 5) {
            int scheduledDelay = delay;
            Scheduler.INSTANCE.submit(server -> {
                ServerPlayer viewer = server.getPlayerList().getPlayer(playerId);
                if (viewer == null) {
                    return;
                }
                ServerLevel currentLevel = (ServerLevel) viewer.level();
                showIgniteActive(currentLevel, center);
                if (scheduledDelay % 20 == 0) {
                    showIgniteTick(currentLevel, center);
                }
            }, scheduledDelay);
        }
    }

    private static Vec3 center(SemionMonsterEntity target) {
        return new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.55, target.getZ());
    }

    private static void showIgniteApplied(ServerLevel level, Vec3 center) {
        level.sendParticles(ParticleTypes.SMALL_FLAME, center.x, center.y, center.z, 12, 0.28, 0.38, 0.28, 0.025);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 5, 0.22, 0.30, 0.22, 0.012);
    }

    private static void showIgniteActive(ServerLevel level, Vec3 center) {
        level.sendParticles(ParticleTypes.SMALL_FLAME, center.x, center.y, center.z, 2, 0.20, 0.30, 0.20, 0.008);
    }

    private static void showIgniteTick(ServerLevel level, Vec3 center) {
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z, 8, 0.30, 0.42, 0.30, 0.018);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 3, 0.20, 0.28, 0.20, 0.008);
    }
}
