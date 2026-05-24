package kim.biryeong.semiontd.buildguide;

import kim.biryeong.gcbserver.packet.s2c.GCBParticleS2CPacket;
import kim.biryeong.gcbserver.player.GCBPlayer;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.tower.Tower;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class BuildGuideIndicatorService {
    public enum DeliveryPath {
        GCB,
        VANILLA
    }

    private BuildGuideIndicatorService() {
    }

    public static DeliveryPath deliveryPath(ServerPlayer player) {
        return player instanceof GCBPlayer gcbPlayer && gcbPlayer.gcb$hasMod()
                ? DeliveryPath.GCB
                : DeliveryPath.VANILLA;
    }

    public static void showPlacement(ServerPlayer player, GridPosition position) {
        if (player == null || position == null) {
            return;
        }
        if (deliveryPath(player) == DeliveryPath.GCB) {
            showGcbMarker(player, position, "minecraft:soul_fire_flame");
        } else {
            showVanillaMarker(player, position, ParticleTypes.SOUL_FIRE_FLAME);
        }
    }

    public static void showUpgradeTarget(ServerPlayer player, Tower tower) {
        if (tower == null) {
            return;
        }
        showPlacement(player, tower.position());
    }

    private static void showGcbMarker(ServerPlayer player, GridPosition position, String particleId) {
        double x = position.x() + 0.5;
        double y = position.y() + 1.25;
        double z = position.z() + 0.5;
        var options = new GCBParticleS2CPacket.ShapeOptions(
                0.0,
                1.2,
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
                new GCBParticleS2CPacket.Line(
                        0.18,
                        options,
                        new GCBParticleS2CPacket.Vec(x - 0.5, y, z - 0.5),
                        new GCBParticleS2CPacket.Vec(x + 0.5, y, z - 0.5),
                        new GCBParticleS2CPacket.Vec(x + 0.5, y, z + 0.5),
                        new GCBParticleS2CPacket.Vec(x - 0.5, y, z + 0.5),
                        new GCBParticleS2CPacket.Vec(x - 0.5, y, z - 0.5)
                )
        ).send(player);
    }

    private static void showVanillaMarker(ServerPlayer player, GridPosition position, net.minecraft.core.particles.SimpleParticleType particleType) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        double x = position.x() + 0.5;
        double y = position.y() + 1.25;
        double z = position.z() + 0.5;
        level.sendParticles(player, particleType, true, true, x, y, z, 8, 0.45, 0.35, 0.45, 0.01);
    }
}
