package kim.biryeong.semiontd.tower.villager;

import java.lang.reflect.Method;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.VillagerAdvTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

abstract class VillagerAdvGameTestSupport implements CustomTestMethodInvoker {
    static SemionGame startedGame(GameTestHelper context, UUID owner) {
        VillagerAdvStates.clear(owner);
        return startedGameWithoutClear(context, owner);
    }

    static SemionGame startedGameWithoutClear(GameTestHelper context, UUID owner) {
        return BuilderIntegrationGameTestSupport.startedGame(
                context,
                owner,
                VillagerAdvTowerJob.ID,
                "villager-adv-tester"
        );
    }

    static PlayerLane lane(SemionGame game, UUID owner) {
        return BuilderIntegrationGameTestSupport.lane(game, owner);
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
