package kim.biryeong.semiontd.tower.resonance;

import com.faboslav.friendsandfoes.common.entity.MoobloomEntity;
import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.entity.SemionEntityTypes;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.visual.MoobloomVisual;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.mixin.accessor.MoobloomAccessor;
import kim.biryeong.semiontd.test.tower.TestTower;
import kim.biryeong.semiontd.tower.TowerCategory;
import kim.biryeong.semiontd.tower.TowerType;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.AABB;

public final class ResonanceMoobloomVisualGameTest {
    @GameTest
    public void overlayEntityUsesMoobloomVariantCollisionAndRemovalLifecycle(GameTestHelper context) {
        BlockPos anchor = context.absolutePos(BlockPos.ZERO);
        SemionTowerEntity towerEntity = spawnProbe(context, anchor, "dandelion", "resonance_moobloom_visual_probe");
        context.runAfterDelay(2, () -> {
            List<MoobloomEntity> visuals = visuals(context, anchor, 2.0);
            BuilderIntegrationGameTestSupport.require(visuals.size() == 1,
                    "Moobloom tower should spawn one overlay entity.");
            MoobloomEntity visual = visuals.getFirst();
            BuilderIntegrationGameTestSupport.require(!visual.shouldBeSaved(),
                    "Runtime Moobloom overlay should not be saved into chunks.");
            requireClose(0.75, towerEntity.getScale(), "Moobloom tower collision scale should remain shortened.");
            requireClose(1.35, towerEntity.getBbHeight(), "Moobloom tower collision height should match its visual.");
            BuilderIntegrationGameTestSupport.require(
                    "dandelion".equals(visual.getEntityData().get(MoobloomAccessor.semiontd$dataVariant())),
                    "Moobloom overlay should retain its configured variant."
            );
            BuilderIntegrationGameTestSupport.require(towerEntity.ownsMoobloomVisualEntity(visual),
                    "Overlay should resolve back to its owning tower.");
            BuilderIntegrationGameTestSupport.require(visual.isNoAi() && visual.isInvulnerable() && visual.noPhysics,
                    "Moobloom overlay should remain cosmetic and passive.");
            towerEntity.discard();
            context.runAfterDelay(1, () -> {
                BuilderIntegrationGameTestSupport.require(visuals(context, anchor, 2.0).isEmpty(),
                        "Removing a tower should remove its Moobloom overlay.");
                context.succeed();
            });
        });
    }

    @GameTest
    public void staticOverlayResyncsOnlyAfterOwnerPositionChanges(GameTestHelper context) {
        BlockPos anchor = context.absolutePos(BlockPos.ZERO);
        SemionTowerEntity towerEntity = spawnProbe(context, anchor, "sunflower", "resonance_moobloom_sync_probe");
        context.runAfterDelay(2, () -> {
            MoobloomEntity visual = visuals(context, anchor, 3.0).getFirst();
            double shiftedX = visual.getX() + 0.75;
            visual.teleportTo(shiftedX, visual.getY(), visual.getZ());
            context.runAfterDelay(2, () -> {
                requireClose(shiftedX, visual.getX(), "Static overlay should skip redundant teleport synchronization.");
                towerEntity.teleportTo(towerEntity.getX() + 1.0, towerEntity.getY(), towerEntity.getZ());
                context.runAfterDelay(1, () -> {
                    requireClose(towerEntity.getX(), visual.getX(), "Overlay should resync after its owner moves.");
                    towerEntity.discard();
                    context.succeed();
                });
            });
        });
    }

    private static SemionTowerEntity spawnProbe(
            GameTestHelper context,
            BlockPos anchor,
            String variant,
            String id
    ) {
        TowerType type = new TowerType(
                id,
                "Moobloom Visual Probe",
                TowerCategory.DIRECT,
                0,
                50.0,
                4.0,
                1.0,
                20,
                0,
                MoobloomVisual.builder().variant(variant).build(),
                List.of()
        );
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid(id);
        SemionTowerEntity entity = new SemionTowerEntity(SemionEntityTypes.TOWER, context.getLevel());
        entity.configure(new TestTower(type, owner, TeamId.RED, 1, GridPosition.from(anchor)), null);
        entity.setNoAi(true);
        entity.setPos(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5);
        context.getLevel().addFreshEntity(entity);
        return entity;
    }

    private static List<MoobloomEntity> visuals(GameTestHelper context, BlockPos anchor, double radius) {
        return context.getLevel().getEntitiesOfClass(MoobloomEntity.class, new AABB(anchor).inflate(radius));
    }

    private static void requireClose(double expected, double actual, String message) {
        BuilderIntegrationGameTestSupport.require(Math.abs(expected - actual) <= 0.01,
                message + " Expected " + expected + ", got " + actual + '.');
    }
}
