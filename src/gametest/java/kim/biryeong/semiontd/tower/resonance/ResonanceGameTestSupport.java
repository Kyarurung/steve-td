package kim.biryeong.semiontd.tower.resonance;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.ResonanceTowerJob;
import kim.biryeong.semiontd.summon.SummonRole;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.TowerType;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

abstract class ResonanceGameTestSupport implements CustomTestMethodInvoker {
    static SemionGame startedGame(GameTestHelper context, UUID owner) {
        return BuilderIntegrationGameTestSupport.startedGame(context, owner, ResonanceTowerJob.ID, "resonance-tester");
    }

    static ResonanceTower addTower(PlayerLane lane, UUID owner, TowerType type, BlockPos position) {
        GridPosition grid = GridPosition.from(position);
        ResonanceTower tower = new ResonanceTower(
                TowerBalanceRuntime.resolve(type), owner, TeamId.RED, lane.laneId(), grid, grid
        );
        lane.addTower(tower);
        return tower;
    }

    static SemionMonsterEntity spawnRoleMonster(
            GameTestHelper context,
            String id,
            Vec3 position,
            double maximumHealth,
            double armor,
            double resistance
    ) {
        return BuilderIntegrationGameTestSupport.spawnRoleMonster(
                context,
                id,
                Optional.empty(),
                TeamId.RED,
                1,
                maximumHealth,
                armor,
                resistance,
                List.of(SummonRole.RUSH),
                position.x,
                position.y,
                position.z
        );
    }

    static void requireClose(double expected, double actual, String message) {
        BuilderIntegrationGameTestSupport.require(Math.abs(expected - actual) <= 0.01,
                message + " Expected " + expected + ", got " + actual + '.');
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
        context.setBlock(0, 0, 0, Blocks.AIR);
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
        method.invoke(this, context);
    }
}
