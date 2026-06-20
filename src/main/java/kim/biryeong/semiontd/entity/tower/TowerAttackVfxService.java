package kim.biryeong.semiontd.entity.tower;

import kim.biryeong.gcbserver.packet.s2c.GCBParticleS2CPacket;
import kim.biryeong.gcbserver.player.GCBPlayer;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class TowerAttackVfxService {
    private static final double RANGED_ATTACK_RANGE_THRESHOLD = 3.0;
    private static final double VIEW_DISTANCE = 48.0;
    private static final double VIEW_DISTANCE_SQR = VIEW_DISTANCE * VIEW_DISTANCE;
    private static final int VANILLA_PARTICLE_COUNT = 8;
    private static final double VANILLA_OFFSET = 0.25;
    private static final double VANILLA_SPEED = 0.02;
    private static final int GCB_SPHERE_POINTS = 8;
    private static final double GCB_SPHERE_EXPAND = 0.25;
    private static final double GCB_SPHERE_TIME = 0.25;

    private TowerAttackVfxService() {
    }

    public static void showHit(SemionTowerEntity tower, SemionMonsterEntity target) {
        if (tower == null || target == null || !(target.level() instanceof ServerLevel level)) {
            return;
        }

        AttackVisualKind kind = visualKind(tower.attackRange());
        double x = target.getX();
        double y = target.getY() + Math.max(0.2, target.getBbHeight() * 0.55);
        double z = target.getZ();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level || player.distanceToSqr(x, y, z) > VIEW_DISTANCE_SQR) {
                continue;
            }
            if (player instanceof GCBPlayer gcbPlayer && gcbPlayer.gcb$hasMod()) {
                showGcbHit(player, kind.particleId(), x, y, z);
            } else {
                showVanillaHit(level, player, vanillaType(kind), x, y, z);
            }
        }
    }

    static AttackVisualKind visualKind(double attackRange) {
        return attackRange > RANGED_ATTACK_RANGE_THRESHOLD
                ? AttackVisualKind.RANGED
                : AttackVisualKind.MELEE;
    }

    private static void showGcbHit(ServerPlayer player, String particleId, double x, double y, double z) {
        var options = new GCBParticleS2CPacket.ShapeOptions(
                GCB_SPHERE_EXPAND,
                GCB_SPHERE_TIME,
                GCBParticleS2CPacket.Vec.UNIT_X,
                GCBParticleS2CPacket.Vec.UNIT_Y,
                GCBParticleS2CPacket.Vec.UNIT_Z,
                player.getUUID()
        );
        new GCBParticleS2CPacket(
                particleId,
                GCBParticleS2CPacket.Vec.ZERO,
                1,
                0.0,
                true,
                "",
                new GCBParticleS2CPacket.Sphere(
                        GCBParticleS2CPacket.Vec.UNIT_Y,
                        GCB_SPHERE_POINTS,
                        options,
                        new GCBParticleS2CPacket.Vec(x, y, z)
                )
        ).send(player);
    }

    private static void showVanillaHit(
            ServerLevel level,
            ServerPlayer player,
            SimpleParticleType particleType,
            double x,
            double y,
            double z
    ) {
        level.sendParticles(
                player,
                particleType,
                true,
                true,
                x,
                y,
                z,
                VANILLA_PARTICLE_COUNT,
                VANILLA_OFFSET,
                VANILLA_OFFSET,
                VANILLA_OFFSET,
                VANILLA_SPEED
        );
    }

    private static SimpleParticleType vanillaType(AttackVisualKind kind) {
        return switch (kind) {
            case MELEE -> ParticleTypes.SWEEP_ATTACK;
            case RANGED -> ParticleTypes.CRIT;
        };
    }

    enum AttackVisualKind {
        MELEE("minecraft:sweep_attack"),
        RANGED("minecraft:crit");

        private final String particleId;

        AttackVisualKind(String particleId) {
            this.particleId = particleId;
        }

        String particleId() {
            return particleId;
        }
    }
}
