package kim.biryeong.semiontd.tower.end;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.map.LaneRegionLayout;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import xyz.nucleoid.map_templates.BlockBounds;

abstract class EndTestFixture {
    private static final UUID OWNER = UUID.nameUUIDFromBytes("end-test-owner".getBytes());

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void reloadCatalogs() {
        ProductionTowerCatalogs.reloadBuiltIns(TowerBalanceConfig.defaultConfig());
    }

    @AfterEach
    void resetBalance() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    static double expectedDamageBonus(double raw) {
        return EndConfig.RUNTIME.damageScaling().apply(raw);
    }

    static double expectedHealthBonus(double raw) {
        return EndConfig.RUNTIME.healthScaling().apply(raw);
    }

    static double regenerationPerSecond(EndTower tower) {
        EndConfig.StackRule rule = EndConfig.RUNTIME.regeneration();
        int completedSteps = tower.transferStats().shulkerCount() / rule.stacksPerStep();
        return Math.min(rule.maximum(), completedSteps * rule.bonusPerStep());
    }

    static double attackRangeBonus(EndTower tower) {
        EndConfig.StackRule rule = EndConfig.RUNTIME.attackRange();
        int completedSteps = tower.transferStats().endCrystalCount() / rule.stacksPerStep();
        return Math.min(rule.maximum(), completedSteps * rule.bonusPerStep());
    }

    static void applyTransferDuration(int durationTicks) {
        applyEndAbilities(Map.of("transferTicks", (double) durationTicks));
    }

    static void applyEndAbilities(Map<String, Double> overrides) {
        TowerBalanceRuntime.apply(endConfig(overrides));
    }

    static TowerBalanceConfig endConfig(Map<String, Double> overrides) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, Map<String, Double>> abilities = new LinkedHashMap<>(defaults.abilities());
        Map<String, Double> end = new LinkedHashMap<>(abilities.get(EndTower.CONFIG_ID));
        end.putAll(overrides);
        abilities.put(EndTower.CONFIG_ID, end);
        return new TowerBalanceConfig(defaults.towers(), defaults.upgradeCosts(), abilities);
    }

    static EndTower tower(TowerType type, int x) {
        return new EndTower(type, OWNER, TeamId.BLUE, 1, new GridPosition(x, 64, 0));
    }

    static void tick(EndTower tower, PlayerLane lane, int ticks) {
        for (int index = 0; index < ticks; index++) {
            tower.tick(lane);
        }
    }

    static PlayerLane lane() {
        LaneRegionLayout layout = new LaneRegionLayout(
                1,
                new Vec3(0.5, 64.0, 0.5),
                List.of(new Vec3(0.5, 64.0, 2.5)),
                new Vec3(0.5, 64.0, 10.5),
                BlockBounds.of(new BlockPos(0, 63, 0), new BlockPos(64, 66, 10)),
                List.of(new GridPosition(0, 63, 10))
        );
        return new PlayerLane(TeamId.BLUE, 1, OWNER, null, layout);
    }
}
