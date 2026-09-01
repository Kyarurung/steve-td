package kim.biryeong.semiontd.tower.ancientcity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.AncientCityTowerJob;
import kim.biryeong.semiontd.summon.SummonRole;
import kim.biryeong.semiontd.trait.BuiltInTraits;
import kim.biryeong.semiontd.trait.TraitLoadout;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class AncientCityMagicCombatGameTest {
    @GameTest
    public void sensorUsesMagicAppliesOwnerMarkAndDoesNotTriggerIgnite(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("ancient-sensor-owner");
        SemionGame game = BuilderIntegrationGameTestSupport.startedGame(context, owner, AncientCityTowerJob.ID, "ancient-sensor");
        try {
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            lane.assignTraitLoadout(new TraitLoadout(BuiltInTraits.IGNITE_ID, BuiltInTraits.NONE_ID));
            AncientCityTower sensor = new AncientCityTower(
                    TowerBalanceRuntime.resolve(AncientCityTowers.SENSOR_T1), owner, TeamId.RED, 1,
                    GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane))
            );
            lane.addTower(sensor);
            lane.markWaveStarted(1);
            SemionTowerEntity towerEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, sensor);
            SemionMonsterEntity target = spawnTarget(
                    context, lane, "ancient-sensor-target", 1, towerEntity, 2.0, Optional.of(TeamId.BLUE)
            );

            sensor.tick(lane);

            double expectedDamage = 5.0 * (1.0 + 13.0 / 224.0 * 2.25) * 1.75;
            BuilderIntegrationGameTestSupport.requireClose(1_000.0 - expectedDamage, target.getHealth(),
                    "Ancient-city sensor must use magic resistance and income-target scaling.");
            BuilderIntegrationGameTestSupport.requireClose(expectedDamage, sensor.roundMagicDamageDealt(),
                    "Ancient-city sensor damage must be recorded as magic.");
            BuilderIntegrationGameTestSupport.requireClose(0.0, sensor.roundPhysicalDamageDealt(),
                    "Ancient-city sensor ability must not record physical damage.");
            BuilderIntegrationGameTestSupport.require(
                    target.activeTimedEffectTicks(TimedEffectType.MONSTER_IGNITED) == 0
                            && target.activeTimedEffectTicks(TimedEffectType.MONSTER_MARKED) > 0
                            && AncientCityMarkDomain.hasAnyActive(target.runtimeMonster()),
                    "Ancient-city magic must apply its owner mark without triggering ignite."
            );
            context.succeed();
        } finally {
            game.close();
        }
    }

    @GameTest
    public void shriekerUsesSharedAreaDamageAndAppliesSlowToLivingTargets(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("ancient-shrieker-owner");
        SemionGame game = BuilderIntegrationGameTestSupport.startedGame(context, owner, AncientCityTowerJob.ID, "ancient-shrieker");
        try {
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            AncientCityTower shrieker = new AncientCityTower(
                    TowerBalanceRuntime.resolve(AncientCityTowers.SHRIEKER_T1), owner, TeamId.RED, 1,
                    GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane))
            );
            lane.addTower(shrieker);
            lane.markWaveStarted(1);
            SemionTowerEntity towerEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, shrieker);
            SemionMonsterEntity primary = spawnTarget(context, lane, "ancient-shriek-primary", 1, towerEntity, 2.0, Optional.empty());
            SemionMonsterEntity secondary = spawnTarget(context, lane, "ancient-shriek-secondary", 1, towerEntity, 2.5, Optional.empty());

            shrieker.tick(lane);

            BuilderIntegrationGameTestSupport.require(primary.getHealth() < 1_000.0 && secondary.getHealth() < 1_000.0,
                    "Ancient-city shrieker must damage both monsters through the shared area pipeline.");
            BuilderIntegrationGameTestSupport.require(
                    primary.activeTimedEffectTicks(TimedEffectType.MONSTER_MOVE_SPEED_REDUCTION) > 0
                            && secondary.activeTimedEffectTicks(TimedEffectType.MONSTER_MOVE_SPEED_REDUCTION) > 0,
                    "Living shrieker targets must receive the configured slow."
            );
            context.succeed();
        } finally {
            game.close();
        }
    }

    @GameTest
    public void finalDefenseMagicCanTargetAnotherLane(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("ancient-cross-lane-owner");
        SemionGame game = BuilderIntegrationGameTestSupport.startedGame(context, owner, AncientCityTowerJob.ID, "ancient-cross-lane");
        try {
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            AncientCityTower sensor = new AncientCityTower(
                    TowerBalanceRuntime.resolve(AncientCityTowers.SENSOR_T1), owner, TeamId.RED, 1,
                    GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane))
            );
            lane.addTower(sensor);
            lane.markWaveStarted(1);
            sensor.moveToFinalDefense(lane, lane.nextFinalDefenseTowerPosition(sensor));
            SemionTowerEntity towerEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, sensor);
            SemionMonsterEntity target = spawnTarget(
                    context, lane, "ancient-cross-lane-target", 2, towerEntity, 2.0, Optional.of(TeamId.BLUE)
            );

            sensor.tick(lane);

            BuilderIntegrationGameTestSupport.require(
                    target.getHealth() < 1_000.0 && sensor.roundMagicDamageDealt() > 0.0,
                    "Ancient-city final-defense magic must target another lane and record magic damage."
            );
            context.succeed();
        } finally {
            game.close();
        }
    }

    private static SemionMonsterEntity spawnTarget(
            GameTestHelper context,
            PlayerLane lane,
            String id,
            int targetLane,
            SemionTowerEntity towerEntity,
            double offset,
            Optional<TeamId> sender
    ) {
        SemionMonsterEntity target = BuilderIntegrationGameTestSupport.spawnRoleMonster(
                context, id, sender, TeamId.RED, targetLane, 1_000.0, 0.0, 0.0,
                List.of(SummonRole.RUSH),
                towerEntity.getX() + offset, towerEntity.getY(), towerEntity.getZ()
        );
        target.setNoAi(true);
        target.runtimeMonster().markMinecraftEntitySpawned(target.getId(), target.getX(), target.getY(), target.getZ());
        lane.activeMonsters().add(target.runtimeMonster());
        return target;
    }
}
