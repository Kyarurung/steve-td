package kim.biryeong.semiontd.command;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.map.LaneRegionLayout;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.pet.PetBondService;
import kim.biryeong.semiontd.tower.pet.PetTower;
import kim.biryeong.semiontd.tower.pet.PetTowers;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.map_templates.BlockBounds;

public final class SemionCommandsPetVfxGameTest {
    private static final UUID OWNER = UUID.nameUUIDFromBytes("pet-vfx-command".getBytes(StandardCharsets.UTF_8));

    @GameTest
    public void petHealDebugSelectionCoversFailureAndSuccess(GameTestHelper context) {
        TowerBalanceConfig config = TowerBalanceConfig.defaultConfig();
        TowerBalanceRuntime.apply(config);
        ProductionTowerCatalogs.reloadBuiltIns(config);
        PlayerLane lane = testLane(context);
        if (SemionCommands.tryShowPetHealVfx(lane)) {
            throw new AssertionError("The Pet heal debug path must fail without a living yard.");
        }

        lane.addTower(tower(PetTowers.KEEPER_T1, position(context, 3, 2, 4)));
        lane.addTower(tower(PetTowers.BIRD_T1, position(context, 4, 2, 4)));
        lane.addTower(tower(PetTowers.DOG_T1, position(context, 3, 2, 5)));
        PetBondService.refresh(lane);

        if (!SemionCommands.tryShowPetHealVfx(lane)) {
            throw new AssertionError("The Pet heal debug path must accept a living same-yard bird and companion.");
        }
        context.succeed();
    }

    private static PetTower tower(TowerType type, GridPosition position) {
        return new PetTower(TowerBalanceRuntime.resolve(type), OWNER, TeamId.RED, 1, position, position);
    }

    private static PlayerLane testLane(GameTestHelper context) {
        BlockPos min = context.absolutePos(new BlockPos(0, 1, 0));
        BlockPos max = context.absolutePos(new BlockPos(10, 5, 14));
        Vec3 spawn = Vec3.atCenterOf(context.absolutePos(new BlockPos(1, 2, 1)));
        Vec3 boss = Vec3.atCenterOf(context.absolutePos(new BlockPos(5, 2, 13)));
        LaneRegionLayout layout = new LaneRegionLayout(1, spawn,
                List.of(Vec3.atCenterOf(context.absolutePos(new BlockPos(5, 2, 7)))), boss,
                BlockBounds.of(min, max), List.of(position(context, 8, 2, 11)));
        return new PlayerLane(TeamId.RED, 1, OWNER, context.getLevel(), layout);
    }

    private static GridPosition position(GameTestHelper context, int x, int y, int z) {
        return GridPosition.from(context.absolutePos(new BlockPos(x, y, z)));
    }
}
