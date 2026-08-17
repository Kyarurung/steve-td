package kim.biryeong.semiontd.tower.gamble;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.config.AttackKind;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.SemionEntityTypes;
import kim.biryeong.semiontd.entity.boss.BossMonster;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.game.TeamLaneGroup;
import kim.biryeong.semiontd.map.LaneRegionLayout;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.Tower;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.map_templates.BlockBounds;

public final class GambleGameTest {
    private static final List<TimedEffectType> SUPPORT_EFFECTS = List.of(
            TimedEffectType.TOWER_DAMAGE_TAKEN_BONUS,
            TimedEffectType.TOWER_ATTACK_SPEED_REDUCTION,
            TimedEffectType.TOWER_RANGE_BONUS,
            TimedEffectType.TOWER_MAX_HEALTH_BONUS,
            TimedEffectType.TOWER_ATTACK_SPEED_BONUS,
            TimedEffectType.TOWER_DAMAGE_BONUS
    );

    @GameTest(maxTicks = 120)
    public void supportRollsAreOwnerFilteredStackBySourceAndLiveUntilRoundCleanup(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        TowerBalanceRuntime.apply(defaults);
        ProductionTowerCatalogs.reloadBuiltIns(defaults);
        UUID owner = stableUuid("gamble-support-owner");
        UUID otherOwner = stableUuid("gamble-support-other");
        PlayerLane lane = testLane(context, owner);
        TeamLaneGroup group = new TeamLaneGroup(TeamId.RED, BossMonster.defaultBoss(TeamId.RED));
        group.addLane(lane);
        prepareFloor(context);

        GambleSupportTower dice = support(GambleTowers.DICE_T3, owner, floor(context, 3, 2, 3));
        GambleSupportTower spectator = support(GambleTowers.SPECTATOR_T3, owner, floor(context, 5, 2, 3));
        GamblerTower target = gambler(owner, floor(context, 4, 2, 5));
        GamblerTower foreign = gambler(otherOwner, floor(context, 4, 2, 4));
        try {
            lane.addTower(dice);
            lane.addTower(spectator);
            lane.addTower(target);
            lane.addTower(foreign);
            lane.markWaveStarted(1);

            require(dice.affectedTargets() == 2 && spectator.affectedTargets() == 1,
                    "Dice must support other owned towers while spectators support only the owned gambler.");
            require(dice.linkedTargets() == 2 && spectator.linkedTargets() == 1,
                    "Every affected tower must have a visible connection from its support tower.");
            require(sum(dice.lastRollCounts()) == 1 && sum(spectator.lastRollCounts()) == 1,
                    "Each support tower must roll exactly one face per round, regardless of target count.");
            require(spectator.lastRollCounts()[0] == 0 && spectator.lastRollCounts()[1] == 0,
                    "Tier 3 spectators must never roll below three.");

            var diceSource = GambleRoundEffects.sourceId(dice);
            var spectatorSource = GambleRoundEffects.sourceId(spectator);
            SemionTowerEntity diceEntity = entity(lane, dice);
            SemionTowerEntity spectatorEntity = entity(lane, spectator);
            SemionTowerEntity targetEntity = entity(lane, target);
            SemionTowerEntity foreignEntity = entity(lane, foreign);
            require(close(diceEntity.attackRange(), 0.0) && close(spectatorEntity.attackRange(), 0.0),
                    "Support entities must have no combat range and therefore never attack.");
            require(GambleRollLabels.hasVisibleLabel(lane, owner, dice)
                            && GambleRollLabels.hasVisibleLabel(lane, owner, spectator),
                    "Each support tower must display its round face above itself.");
            require(GambleRollLabels.count(lane, owner) == 2,
                    "The lane must keep one face label for each support tower.");
            require(sourceCount(targetEntity, diceSource) == 1 && sourceCount(targetEntity, spectatorSource) == 1,
                    "The target must retain one independently sourced result from each support.");
            require(sourceCount(diceEntity, diceSource) == 0 && sourceCount(spectatorEntity, spectatorSource) == 0,
                    "Support towers must exclude themselves from their own roll.");
            require(sourceCount(diceEntity, spectatorSource) == 0
                            && sourceCount(spectatorEntity, diceSource) == 1,
                    "The spectator must never affect a non-gambler, while dice support still affects spectators.");
            require(sourceCount(foreignEntity, diceSource) == 0 && sourceCount(foreignEntity, spectatorSource) == 0,
                    "A tower with another owner must never receive gamble support.");

            dice.onLaneCleared(lane);
            spectator.onLaneCleared(lane);
            require(GambleRollLabels.count(lane, owner) == 0,
                    "Floating faces must disappear as soon as the owner's lane is cleared.");
            require(sourceCount(targetEntity, diceSource) == 1 && sourceCount(targetEntity, spectatorSource) == 1,
                    "Clearing the lane must hide the faces without ending surviving support effects early.");

            require(lane.killTower(dice), "The dice tower must be destroyable for persistence coverage.");
            require(sourceCount(targetEntity, diceSource) == 0
                            && sourceCount(targetEntity, spectatorSource) == 1,
                    "A destroyed support tower must immediately remove only its own result.");
            GambleRoundEffects.clearAll(lane, owner);
            require(GambleRollLabels.count(lane, owner) == 0,
                    "Round cleanup must remove every floating face label.");
            require(sourceCount(targetEntity, diceSource) == 0 && sourceCount(targetEntity, spectatorSource) == 0,
                    "Round cleanup must remove every gamble source exactly.");

            lane.resetForRound();
            lane.markWaveStarted(2);
            SemionTowerEntity nextTarget = entity(lane, target);
            require(sourceCount(nextTarget, diceSource) == 1 && sourceCount(nextTarget, spectatorSource) == 1,
                    "The next round must produce fresh effects after the destroyed support respawns.");
            GambleRoundEffects.clearAll(lane, owner);
            require(sourceCount(nextTarget, diceSource) == 0 && sourceCount(nextTarget, spectatorSource) == 0,
                    "Elimination cleanup must share the exact round cleanup behavior.");
            context.succeed();
        } finally {
            group.closeRuntime();
            TowerBalanceRuntime.apply(defaults);
        }
    }

    @GameTest(maxTicks = 120)
    public void rolledStatePreservesHealthRatioAndBasicSplashCoexistsWithSpreadBet(GameTestHelper context) {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        TowerBalanceRuntime.apply(defaults);
        UUID owner = stableUuid("gamble-combat-owner");
        PlayerLane lane = testLane(context, owner);
        TeamLaneGroup group = new TeamLaneGroup(TeamId.RED, BossMonster.defaultBoss(TeamId.RED));
        group.addLane(lane);
        prepareFloor(context);
        GamblerTower original = gambler(owner, floor(context, 4, 2, 3));
        SemionMonsterEntity primary = null;
        SemionMonsterEntity secondary = null;
        SemionMonsterEntity splashTarget = null;
        try {
            lane.addTower(original);
            GambleState upgradedState = new GambleState(
                    50.0, 5.0, 0.5, 0.5,
                    EnumSet.of(GambleAbility.SPREAD_BET), 4, "능력치 테스트"
            );
            original.setData(GamblerTower.STATE, upgradedState);
            original.syncMaxHealth(160.0, false);
            original.syncHealth(80.0);
            entity(lane, original).setHealth(80.0F);

            GamblerTower replacement = gambler(owner, original.position());
            replacement.copyFrom(original, 0);
            require(lane.replaceTower(original, replacement), "Self-upgrade replacement must succeed.");
            require(close(replacement.currentMaxHealth(), 160.0) && close(replacement.health(), 80.0),
                    "A fixed max-health upgrade must preserve the exact 50% health ratio.");
            require(close(replacement.adjustAttackRange(6.5), 7.0),
                    "The range result must add the rolled amount to the base range.");
            require(close(replacement.modifyAttackDamage(null, null, 10.0), 15.0),
                    "The damage result must add the rolled amount to the base damage.");
            require(close(replacement.splashRadius(), 2.0),
                    "The attack-area result must add the rolled amount to the base splash radius.");

            SemionTowerEntity source = entity(lane, replacement);
            primary = spawnTarget(context, lane, source.position().add(0.0, 0.0, 2.0), "gamble-primary");
            splashTarget = spawnTarget(context, lane, primary.position().add(0.5, 0.0, 0.0), "gamble-splash");
            secondary = spawnTarget(context, lane, primary.position().add(2.5, 0.0, 0.0), "gamble-secondary");
            replacement.onAttackResolved(source, primary, 100.0, 100.0, 100.0, false);
            require(close(splashTarget.runtimeMonster().health(), 40.0),
                    "Every basic attack must deal 60% finalized damage inside the rolled splash radius.");
            require(close(secondary.runtimeMonster().health(), 100.0),
                    "A target outside the basic splash radius must not take splash damage.");
            splashTarget.discard();
            splashTarget = null;
            for (int attack = 2; attack <= 3; attack++) {
                replacement.onAttackResolved(source, primary, 100.0, 100.0, 100.0, false);
            }
            require(close(secondary.runtimeMonster().health(), 100.0),
                    "Spread bet must not trigger before the fourth attack.");
            replacement.onAttackResolved(source, primary, 100.0, 100.0, 100.0, false);
            require(close(secondary.runtimeMonster().health(), 50.0),
                    "The fourth attack must deal 50% of the finalized primary outgoing damage.");
            context.succeed();
        } finally {
            if (primary != null) primary.discard();
            if (secondary != null) secondary.discard();
            if (splashTarget != null) splashTarget.discard();
            group.closeRuntime();
            TowerBalanceRuntime.apply(defaults);
        }
    }

    private static GambleSupportTower support(kim.biryeong.semiontd.tower.TowerType type,
                                               UUID owner, GridPosition position) {
        return new GambleSupportTower(TowerBalanceRuntime.resolve(type), owner, TeamId.RED, 1, position, position);
    }

    private static GamblerTower gambler(UUID owner, GridPosition position) {
        return new GamblerTower(TowerBalanceRuntime.resolve(GambleTowers.GAMBLER),
                owner, TeamId.RED, 1, position, position);
    }

    private static SemionMonsterEntity spawnTarget(
            GameTestHelper context, PlayerLane lane, Vec3 position, String id
    ) {
        Monster runtime = new Monster(id, TeamId.RED, 1, Optional.empty(), Optional.empty(),
                100.0, 0.0, 1.0, AttackKind.MELEE, "minecraft:zombie", 0L);
        SemionMonsterEntity entity = new SemionMonsterEntity(SemionEntityTypes.MONSTER, context.getLevel());
        entity.configureFrom(runtime, lane.laneLayout());
        entity.setNoAi(true);
        entity.setPos(position.x, position.y, position.z);
        require(context.getLevel().addFreshEntity(entity), "Gamble target must spawn.");
        runtime.markMinecraftEntitySpawned(entity.getId(), position.x, position.y, position.z);
        lane.activeMonsters().add(runtime);
        return entity;
    }

    private static int sourceCount(SemionTowerEntity entity, net.minecraft.resources.ResourceLocation source) {
        return (int) SUPPORT_EFFECTS.stream().filter(type -> entity.hasTimedEffectSource(type, source)).count();
    }

    private static int sum(int[] values) {
        return java.util.Arrays.stream(values).sum();
    }

    private static SemionTowerEntity entity(PlayerLane lane, Tower tower) {
        return GambleRoundEffects.towerEntity(tower, lane).orElseThrow();
    }

    private static PlayerLane testLane(GameTestHelper context, UUID owner) {
        BlockPos min = context.absolutePos(new BlockPos(0, 1, 0));
        BlockPos max = context.absolutePos(new BlockPos(14, 6, 14));
        LaneRegionLayout layout = new LaneRegionLayout(
                1,
                Vec3.atCenterOf(context.absolutePos(new BlockPos(1, 2, 1))),
                List.of(Vec3.atCenterOf(context.absolutePos(new BlockPos(7, 2, 7)))),
                Vec3.atCenterOf(context.absolutePos(new BlockPos(7, 2, 13))),
                BlockBounds.of(min, max),
                List.of(GridPosition.from(context.absolutePos(new BlockPos(10, 2, 11))))
        );
        return new PlayerLane(TeamId.RED, 1, owner, context.getLevel(), layout);
    }

    private static GridPosition floor(GameTestHelper context, int x, int y, int z) {
        return GridPosition.from(context.absolutePos(new BlockPos(x, y, z)));
    }

    private static void prepareFloor(GameTestHelper context) {
        for (int x = 1; x <= 12; x++) {
            for (int z = 1; z <= 12; z++) {
                BlockPos floor = context.absolutePos(new BlockPos(x, 2, z));
                context.getLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
                context.getLevel().setBlock(floor.above(), Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static UUID stableUuid(String seed) {
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean close(double first, double second) {
        return Math.abs(first - second) < 0.0001;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
