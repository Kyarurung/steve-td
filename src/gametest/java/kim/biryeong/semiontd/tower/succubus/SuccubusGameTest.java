package kim.biryeong.semiontd.tower.succubus;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.config.AttackKind;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.SemionEntityTypes;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.map.LaneRegionLayout;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.map_templates.BlockBounds;

public final class SuccubusGameTest {
    private static final UUID OWNER = UUID.nameUUIDFromBytes("succubus-gametest".getBytes(StandardCharsets.UTF_8));

    @GameTest
    public void thirdSleepExecutesAndWakeImmunityBlocksImmediateRestacking(GameTestHelper context) {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
        SuccubusAbsorption.clear(OWNER);
        PlayerLane lane = testLane(context);
        SuccubusTower succubus = tower(SuccubusTowers.SUCCUBUS, position(context, 3, 2, 4));
        lane.addTower(succubus);
        SemionMonsterEntity target = spawnMonster(context, lane, "dream-target", position(context, 4, 2, 4));
        try {
            for (int sleep = 1; sleep <= 2; sleep++) {
                require(SuccubusDreams.add(target, lane, succubus, 10), "Ten stacks must put the target to sleep.");
                require(SuccubusDreams.isAsleep(target), "The target must enter dreamland.");
                for (int tick = 0; tick < SuccubusBalance.SLEEP_DURATION_TICKS; tick++) SuccubusDreams.tick(lane);
                require(!SuccubusDreams.isAsleep(target), "Five seconds must wake the target.");
                require(!SuccubusDreams.add(target, lane, succubus, 1), "Awakened immunity must block dream stacks.");
                for (int tick = 0; tick < SuccubusBalance.AWAKENED_IMMUNITY_TICKS; tick++) SuccubusDreams.tick(lane);
            }

            SuccubusDreams.add(target, lane, succubus, 10);
            require(!target.isAlive() && SuccubusDreams.sleepCount(target) == 0,
                    "The Succubus must execute and clear the third-sleep target.");
            require(SuccubusAbsorption.kills(OWNER) == 1, "Execution must absorb exactly once.");
            requireClose(0.30, SuccubusAbsorption.attack(OWNER), "Execution attack absorption");
            requireClose(10.0, SuccubusAbsorption.health(OWNER), "Execution health absorption");
            requireClose(170.0, succubus.currentMaxHealth(), "Absorbed maximum health");
            requireClose(170.0, succubus.health(), "Absorbed health must heal immediately");
            context.succeed();
        } finally {
            SuccubusDreams.clearLane(lane);
            SuccubusAbsorption.clear(OWNER);
        }
    }

    @GameTest
    public void onlyRecordedSuccubusBasicKillsAreAbsorbed(GameTestHelper context) {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
        SuccubusAbsorption.clear(OWNER);
        PlayerLane lane = testLane(context);
        SuccubusTower succubus = tower(SuccubusTowers.SUCCUBUS, position(context, 3, 2, 4));
        lane.addTower(succubus);
        SemionTowerEntity source = (SemionTowerEntity) context.getLevel().getEntity(succubus.entityId().orElseThrow());
        try {
            SemionMonsterEntity unrecorded = spawnMonster(context, lane, "unrecorded", position(context, 5, 2, 4));
            var unrecordedResult = succubus.damageTargetResult(source, unrecorded, 2_000.0);
            require(unrecordedResult.killed(), "Setup damage must kill.");
            require(SuccubusAbsorption.kills(OWNER) == 0, "Unrecorded damage must not absorb.");

            SemionMonsterEntity direct = spawnMonster(context, lane, "direct", position(context, 6, 2, 4));
            var result = succubus.damageTargetResult(source, direct, 2_000.0);
            source.recordAttack(direct, 2_000.0, result.outgoingDamage(), result.dealtDamage(), result.killed());
            require(SuccubusAbsorption.kills(OWNER) == 1, "Recorded basic kill must absorb once.");
            requireClose(0.30, SuccubusAbsorption.attack(OWNER), "Basic kill attack absorption");
            requireClose(10.0, SuccubusAbsorption.health(OWNER), "Basic kill health absorption");
            succubus.resetForRound(lane);
            requireClose(170.0, succubus.currentMaxHealth(), "Absorption must persist across rounds");
            succubus.syncHealth(0.0);
            succubus.onRemoved(lane);
            SuccubusTower replacement = tower(SuccubusTowers.SUCCUBUS, position(context, 3, 2, 6));
            lane.addTower(replacement);
            requireClose(170.0, replacement.currentMaxHealth(), "Reinstalled Succubus must restore absorption");
            requireClose(170.0, replacement.health(), "Reinstalled Succubus must restore enhanced health");
            context.succeed();
        } finally {
            SuccubusDreams.clearLane(lane);
            SuccubusAbsorption.clear(OWNER);
        }
    }

    @GameTest
    public void sleepwalkerReductionUsesDreamingMonsterSourceOnly(GameTestHelper context) {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
        PlayerLane lane = testLane(context);
        SuccubusTower source = tower(SuccubusTowers.DREAM_DUST_T1, position(context, 2, 2, 4));
        lane.addTower(source);
        SemionMonsterEntity attacker = spawnMonster(context, lane, "dream-attacker", position(context, 3, 2, 4));
        SuccubusDreams.add(attacker, lane, source, 1);
        try {
            assertReduction(context, lane, attacker, SuccubusTowers.SLEEPWALKER_T1, 0.10, 4);
            assertReduction(context, lane, attacker, SuccubusTowers.SLEEPWALKER_T2, 0.15, 5);
            assertReduction(context, lane, attacker, SuccubusTowers.SLEEPWALKER_T3, 0.20, 6);
            context.succeed();
        } finally {
            SuccubusDreams.clearLane(lane);
        }
    }

    private static void assertReduction(GameTestHelper context, PlayerLane lane, SemionMonsterEntity attacker,
                                        kim.biryeong.semiontd.tower.TowerType type, double reduction, int x) {
        SuccubusTower tower = tower(type, position(context, x, 2, 4));
        lane.addTower(tower);
        SemionTowerEntity entity = (SemionTowerEntity) context.getLevel().getEntity(tower.entityId().orElseThrow());
        double reduced = tower.modifyIncomingDamage(entity, context.getLevel().damageSources().mobAttack(attacker), 100.0);
        double environmental = tower.modifyIncomingDamage(entity, context.getLevel().damageSources().fellOutOfWorld(), 100.0);
        requireClose(100.0 * (1.0 - reduction), reduced, type.id() + " reduction");
        requireClose(100.0, environmental, type.id() + " environment exclusion");
    }

    private static SuccubusTower tower(kim.biryeong.semiontd.tower.TowerType type, GridPosition position) {
        return new SuccubusTower(TowerBalanceRuntime.resolve(type), OWNER, TeamId.RED, 1, position, position);
    }

    private static SemionMonsterEntity spawnMonster(GameTestHelper context, PlayerLane lane, String id,
                                                     GridPosition position) {
        Monster monster = new Monster(id, TeamId.RED, 1, Optional.empty(), Optional.empty(),
                1_000.0, 0.0, 10.0, AttackKind.MELEE, "minecraft:zombie", 0L);
        SemionMonsterEntity entity = new SemionMonsterEntity(SemionEntityTypes.MONSTER, context.getLevel());
        entity.configureFrom(monster, lane.laneLayout());
        entity.setNoAi(true);
        entity.setPos(position.x() + 0.5, position.y() + 1.0, position.z() + 0.5);
        require(context.getLevel().addFreshEntity(entity), "Monster must spawn.");
        monster.markMinecraftEntitySpawned(entity.getId(), entity.getX(), entity.getY(), entity.getZ());
        lane.activeMonsters().add(monster);
        return entity;
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

    private static void requireClose(double expected, double actual, String message) {
        require(Math.abs(expected - actual) < 1.0E-6, message + ": expected " + expected + ", got " + actual);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
