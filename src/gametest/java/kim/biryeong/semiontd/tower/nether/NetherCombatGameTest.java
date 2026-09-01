package kim.biryeong.semiontd.tower.nether;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.job.NetherTowerJob;
import kim.biryeong.semiontd.summon.SummonRole;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.trait.BuiltInTraits;
import kim.biryeong.semiontd.trait.TraitLoadout;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public final class NetherCombatGameTest {
    @GameTest
    public void criticalPiglinGhastAndWitherRulesUseRuntimeMonsterData(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            GridPosition position = GridPosition.from(context.absolutePos(net.minecraft.core.BlockPos.ZERO));
            NetherTower piglin = tower(NetherTowers.T3_PIGLIN_BRUTE, "nether-piglin-rules", position);
            piglin.syncHealth(piglin.currentMaxHealth() * 0.30);
            SemionMonsterEntity normal = monster(context, "nether-normal", Optional.empty(), 100.0, List.of(SummonRole.RUSH), 0.0);
            SemionMonsterEntity tank = monster(context, "nether-tank", Optional.empty(), 100.0, List.of(SummonRole.TANK), 1.0);
            SemionMonsterEntity highHealth = monster(context, "nether-high-health", Optional.empty(), 200.0, List.of(SummonRole.RUSH), 2.0);
            SemionMonsterEntity income = monster(context, "nether-income", Optional.of(TeamId.BLUE), 100.0, List.of(SummonRole.RUSH), 3.0);
            BuilderIntegrationGameTestSupport.requireClose(100.0, piglin.modifyAttackDamage(null, normal, 100.0),
                    "Piglin brute must not bonus ordinary targets.");
            BuilderIntegrationGameTestSupport.requireClose(175.0, piglin.modifyAttackDamage(null, tank, 100.0),
                    "Critical piglin brute must bonus tank targets.");
            BuilderIntegrationGameTestSupport.requireClose(175.0, piglin.modifyAttackDamage(null, highHealth, 100.0),
                    "Critical piglin brute must bonus high-health targets.");
            BuilderIntegrationGameTestSupport.requireClose(200.0, piglin.modifyAttackDamage(null, income, 100.0),
                    "Piglin brute must retain income damage bonus.");

            NetherTower ghast = tower(NetherTowers.T3_GHAST, "nether-ghast-mark", position);
            ghast.syncHealth(ghast.currentMaxHealth() * 0.30);
            ghast.onAttack(null, normal, 10.0, false);
            BuilderIntegrationGameTestSupport.requireClose(
                    0.40,
                    normal.activeTimedEffectMagnitude(TimedEffectType.MONSTER_TOWER_DAMAGE_TAKEN_BONUS),
                    "Critical ghast must apply its configured damage-taken mark."
            );

            NetherTower wither = tower(NetherTowers.T3_WITHER, "nether-wither-targeting", position);
            SemionMonsterEntity boss = monster(
                    context, "nether-wither-boss", Optional.empty(), 600.0, List.of(SummonRole.TANK), 4.0
            );
            BuilderIntegrationGameTestSupport.require(
                    wither.selectAttackTarget(null, List.of(normal, boss)).orElse(null) == boss,
                    "Wither must prioritize targets above its high-health threshold."
            );
            normal.setHealth(10.0F);
            BuilderIntegrationGameTestSupport.require(
                    wither.selectAttackTarget(null, List.of(income, normal)).orElse(null) == normal,
                    "Wither must fall back to the lowest-health target."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Nether combat rules integration failed: " + failure.getMessage()));
        } finally {
            TowerBalanceRuntime.apply(defaults);
        }
    }

    @GameTest
    public void transitionPulseAndBlazeExtraAttackRemainMagicWithoutIgnite(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("nether-magic-combat");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(context, owner, NetherTowerJob.ID, "nether-magic-combat");
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            lane.assignTraitLoadout(new TraitLoadout(BuiltInTraits.IGNITE_ID, BuiltInTraits.NONE_ID));
            GridPosition magmaPosition = GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane));
            NetherTower magma = new NetherTower(
                    TowerBalanceRuntime.resolve(NetherTowers.T1_MAGMA_CUBE), owner, TeamId.RED, lane.laneId(), magmaPosition
            );
            lane.addTower(magma);
            SemionTowerEntity magmaEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, magma);
            SemionMonsterEntity transitionTarget = roleMonster(
                    context, "nether-transition-magic", Optional.empty(), 1_000.0, 100.0, 0.0,
                    List.of(SummonRole.RUSH), magmaEntity.position().add(1.0, 0.0, 0.0)
            );
            lane.activeMonsters().add(transitionTarget.runtimeMonster());
            magma.syncHealth(0.01);
            magmaEntity.setHealth(0.01F);
            magma.tick(lane);
            double transitionBaseDamage = magma.type().damage()
                    * TowerBalanceRuntime.ability(magma.type().id(), "zombieTransitionPulseDamageRatio");
            double transitionDamage = magma.resolveOutgoingDamage(magmaEntity, transitionTarget, transitionBaseDamage);
            BuilderIntegrationGameTestSupport.requireClose(
                    1_000.0 - transitionDamage,
                    transitionTarget.getHealth(),
                    "Zombie transition pulse must use magic resistance instead of armor."
            );
            BuilderIntegrationGameTestSupport.require(
                    transitionTarget.activeTimedEffectTicks(TimedEffectType.MONSTER_IGNITED) == 0,
                    "Zombie transition pulse must not apply ignite."
            );
            transitionTarget.discard();

            GridPosition blazePosition = new GridPosition(magmaPosition.x() + 8, magmaPosition.y(), magmaPosition.z());
            NetherTower blaze = new NetherTower(
                    TowerBalanceRuntime.resolve(NetherTowers.T2_BLAZE), owner, TeamId.RED, lane.laneId(), blazePosition
            );
            lane.addTower(blaze);
            SemionTowerEntity blazeEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, blaze);
            SemionMonsterEntity extraTarget = roleMonster(
                    context, "nether-extra-magic", Optional.empty(), 1_000.0, 100.0, 0.0,
                    List.of(SummonRole.RUSH), blazeEntity.position().add(2.0, 0.0, 0.0)
            );
            int extraAttackEvery = TowerBalanceRuntime.abilityInt(blaze.type().id(), "extraAttackEvery");
            for (int attack = 1; attack < extraAttackEvery; attack++) {
                blaze.syncHealth(blaze.currentMaxHealth() * 0.30);
                blazeEntity.setHealth((float) blaze.health());
                blaze.onAttack(blazeEntity, extraTarget, blaze.type().damage(), false);
            }
            blaze.syncHealth(blaze.currentMaxHealth() * 0.30);
            blazeEntity.setHealth((float) blaze.health());
            double before = extraTarget.getHealth();
            blaze.onAttack(blazeEntity, extraTarget, blaze.type().damage(), false);
            double expected = blaze.resolveOutgoingDamage(
                    blazeEntity,
                    extraTarget,
                    blaze.type().damage() * TowerBalanceRuntime.ability(blaze.type().id(), "extraAttackDamageRatio")
            );
            BuilderIntegrationGameTestSupport.requireClose(before - expected, extraTarget.getHealth(),
                    "Blaze extra attack must use magic resistance instead of armor.");
            BuilderIntegrationGameTestSupport.require(
                    extraTarget.activeTimedEffectTicks(TimedEffectType.MONSTER_IGNITED) == 0,
                    "Blaze extra attack must not apply ignite."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Nether magic combat integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }

    @GameTest
    public void magmaPulseAndGhastAttackSpeedUseConfiguredRuntimeValues(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("nether-runtime-effects");
        SemionGame game = null;
        try {
            ProductionTowerCatalogs.reloadBuiltIns(defaults);
            game = BuilderIntegrationGameTestSupport.startedGame(context, owner, NetherTowerJob.ID, "nether-runtime-effects");
            PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
            lane.assignTraitLoadout(new TraitLoadout(BuiltInTraits.IGNITE_ID, BuiltInTraits.NONE_ID));
            GridPosition position = GridPosition.from(BuilderIntegrationGameTestSupport.primaryPosition(lane));
            NetherTower magma = new NetherTower(
                    TowerBalanceRuntime.resolve(NetherTowers.T1_MAGMA_CUBE), owner, TeamId.RED, lane.laneId(), position
            );
            lane.addTower(magma);
            magma.syncHealth(magma.currentMaxHealth() * 0.30);
            SemionTowerEntity magmaEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, magma);
            SemionMonsterEntity pulseTarget = roleMonster(
                    context, "nether-magma-pulse", Optional.empty(), 100.0, 100.0, 0.0,
                    List.of(SummonRole.RUSH), magmaEntity.position().add(4.0, 0.0, 0.0)
            );
            magma.onAttack(magmaEntity, pulseTarget, magma.type().damage(), false);
            BuilderIntegrationGameTestSupport.requireClose(91.0, pulseTarget.getHealth(),
                    "Magma pulse must use magic resistance instead of armor.");
            BuilderIntegrationGameTestSupport.require(
                    pulseTarget.activeTimedEffectTicks(TimedEffectType.MONSTER_IGNITED) == 0,
                    "Magma pulse must not apply ignite."
            );

            GridPosition ghastPosition = new GridPosition(position.x() + 8, position.y(), position.z());
            NetherTower ghast = new NetherTower(
                    TowerBalanceRuntime.resolve(NetherTowers.T3_GHAST), owner, TeamId.RED, lane.laneId(), ghastPosition
            );
            lane.addTower(ghast);
            ghast.syncHealth(ghast.currentMaxHealth() * 0.50);
            SemionTowerEntity ghastEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, ghast);
            ghast.tick(lane);
            BuilderIntegrationGameTestSupport.requireClose(
                    0.375,
                    ghastEntity.activeTimedEffectMagnitude(TimedEffectType.TOWER_ATTACK_SPEED_BONUS),
                    "Ghast at half health must receive half of its attack-speed cap."
            );
            BuilderIntegrationGameTestSupport.require(
                    ghastEntity.attackIntervalTicks() == 8,
                    "Ghast attack interval must reflect its missing-health speed bonus."
            );
            context.succeed();
        } catch (RuntimeException | Error failure) {
            failure.printStackTrace();
            context.fail(Component.literal("Nether runtime effects integration failed: " + failure.getMessage()));
        } finally {
            if (game != null) {
                game.close();
            }
            TowerBalanceRuntime.apply(defaults);
        }
    }

    private static NetherTower tower(kim.biryeong.semiontd.tower.TowerType type, String seed, GridPosition position) {
        return new NetherTower(
                TowerBalanceRuntime.resolve(type),
                BuilderIntegrationGameTestSupport.stableUuid(seed),
                TeamId.RED,
                1,
                position
        );
    }

    private static SemionMonsterEntity monster(
            GameTestHelper context,
            String id,
            Optional<TeamId> senderTeam,
            double health,
            List<SummonRole> roles,
            double offset
    ) {
        return roleMonster(context, id, senderTeam, health, 0.0, 0.0, roles, new Vec3(offset, 0.0, 0.0));
    }

    private static SemionMonsterEntity roleMonster(
            GameTestHelper context,
            String id,
            Optional<TeamId> senderTeam,
            double health,
            double armor,
            double resistance,
            List<SummonRole> roles,
            Vec3 position
    ) {
        return BuilderIntegrationGameTestSupport.spawnRoleMonster(
                context, id, senderTeam, TeamId.RED, 1, health, armor, resistance, roles,
                position.x, position.y, position.z
        );
    }
}
