package kim.biryeong.semiontd.tower.ocean;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.OceanTowerJob;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class OceanAbilityCombatGameTest {
    @GameTest
    public void tankTransferExcludesTanksRespectsCooldownAndStopsAtFinalDefense(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("ocean-tank-transfer-owner");
        SemionGame game = BuilderIntegrationGameTestSupport.startedGame(context, owner, OceanTowerJob.ID, "ocean-tank");
        try {
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            GridPosition origin = GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane));
            OceanTower tank = tower(owner, OceanTowers.T1_PUFFERFISH, origin);
            OceanTower target = tower(owner, OceanTowers.T1_COD, offset(origin, 1, 0));
            OceanTower otherTank = tower(owner, OceanTowers.T2_GUARDIAN, offset(origin, -1, 0));
            lane.addTower(tank);
            lane.addTower(target);
            lane.addTower(otherTank);
            var entity = BuilderIntegrationGameTestSupport.towerEntity(lane, tank);

            tank.onDamaged(entity, null, 30.0, 100.0, 70.0);
            BuilderIntegrationGameTestSupport.requireClose(124.0, target.water(),
                    "A tier-one ocean tank hit must transfer its configured cap.");
            BuilderIntegrationGameTestSupport.requireClose(96.0, tank.water(),
                    "A successful transfer must spend water exactly once.");
            BuilderIntegrationGameTestSupport.requireClose(100.0, otherTank.water(),
                    "Ocean transfer must exclude another tank.");

            tank.onDamaged(entity, null, 30.0, 70.0, 40.0);
            BuilderIntegrationGameTestSupport.requireClose(124.0, target.water(),
                    "Ocean transfer must remain blocked during cooldown.");
            for (int tick = 0; tick < 100; tick++) {
                tank.tick(lane);
            }
            tank.onDamaged(entity, null, 30.0, 40.0, 10.0);
            BuilderIntegrationGameTestSupport.requireClose(148.0, target.water(),
                    "Ocean transfer must reactivate after the configured cooldown.");

            tank.moveToFinalDefense(lane, lane.nextFinalDefenseTowerPosition(tank));
            for (int tick = 0; tick < 100; tick++) {
                tank.tick(lane);
            }
            tank.onDamaged(entity, null, 30.0, 40.0, 10.0);
            BuilderIntegrationGameTestSupport.requireClose(148.0, target.water(),
                    "Ocean transfer must stop at final defense.");
            context.succeed();
        } finally {
            game.close();
        }
    }

    @GameTest
    public void empoweredSupportSpendsWaterAndAppliesConfiguredEffects(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("ocean-support-owner");
        SemionGame game = BuilderIntegrationGameTestSupport.startedGame(context, owner, OceanTowerJob.ID, "ocean-support");
        try {
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            GridPosition origin = GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane));
            OceanTower support = tower(owner, OceanTowers.T1_TROPICAL_FISH, origin);
            OceanTower target = tower(owner, OceanTowers.T1_COD, offset(origin, 1, 0));
            lane.addTower(support);
            lane.addTower(target);
            var targetEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, target);
            support.addWater(50.0);
            lane.markWaveStarted(1);
            support.tick(lane);

            BuilderIntegrationGameTestSupport.requireClose(126.0, support.water(),
                    "Empowered ocean support must spend three times its base water cost.");
            BuilderIntegrationGameTestSupport.requireClose(
                    0.12,
                    targetEntity.activeTimedEffectMagnitude(TimedEffectType.TOWER_DAMAGE_BONUS),
                    "Empowered ocean support must apply its configured damage bonus."
            );
            BuilderIntegrationGameTestSupport.requireClose(
                    0.15,
                    targetEntity.activeTimedEffectMagnitude(TimedEffectType.TOWER_ATTACK_SPEED_BONUS),
                    "Empowered ocean support must apply its configured attack-speed bonus."
            );
            context.succeed();
        } finally {
            game.close();
        }
    }

    @GameTest
    public void healerSpendsOnlyWhenAValidTowerNeedsHealing(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("ocean-healer-owner");
        SemionGame game = BuilderIntegrationGameTestSupport.startedGame(context, owner, OceanTowerJob.ID, "ocean-healer");
        try {
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            GridPosition origin = GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane));
            OceanTower healer = tower(owner, OceanTowers.T1_SQUID, origin);
            OceanTower target = tower(owner, OceanTowers.T1_COD, offset(origin, 1, 0));
            lane.addTower(healer);
            lane.addTower(target);
            var targetEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, target);
            target.syncHealth(target.currentMaxHealth());
            healer.onWaveStarted(lane, 1);
            healer.tick(lane);
            BuilderIntegrationGameTestSupport.requireClose(100.0, healer.water(),
                    "Ocean healer must not spend water without a damaged target.");

            target.syncHealth(10.0);
            targetEntity.setHealth(10.0F);
            healer.tick(lane);
            BuilderIntegrationGameTestSupport.requireClose(32.5, target.health(),
                    "Empowered ocean healer must heal by the configured multiplied amount.");
            BuilderIntegrationGameTestSupport.requireClose(70.0, healer.water(),
                    "Empowered ocean healer must spend the configured multiplied cost.");
            context.succeed();
        } finally {
            game.close();
        }
    }

    @GameTest
    public void dehydrationDamagesTowerAndSlowsAttackWithoutWater(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("ocean-dehydration-owner");
        SemionGame game = BuilderIntegrationGameTestSupport.startedGame(context, owner, OceanTowerJob.ID, "ocean-dehydration");
        try {
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            OceanTower tower = tower(
                    owner,
                    OceanTowers.T1_COD,
                    GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane))
            );
            lane.addTower(tower);
            tower.spendWater(tower.water());
            tower.onWaveStarted(lane, 1);
            double before = tower.health();
            for (int tick = 0; tick < 20; tick++) {
                tower.tick(lane);
            }
            BuilderIntegrationGameTestSupport.require(tower.health() < before,
                    "A dehydrated ocean tower must take configured periodic health damage.");
            BuilderIntegrationGameTestSupport.require(
                    tower.adjustAttackInterval(tower.type().attackIntervalTicks())
                            > tower.type().attackIntervalTicks(),
                    "A dehydrated ocean combat tower must attack more slowly."
            );
            context.succeed();
        } finally {
            game.close();
        }
    }

    private static OceanTower tower(UUID owner, kim.biryeong.semiontd.tower.TowerType type, GridPosition position) {
        return new OceanTower(TowerBalanceRuntime.resolve(type), owner, TeamId.RED, 1, position);
    }

    private static GridPosition offset(GridPosition position, int x, int z) {
        return new GridPosition(position.x() + x, position.y(), position.z() + z);
    }
}
