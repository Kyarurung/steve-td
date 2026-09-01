package kim.biryeong.semiontd.gametest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import kim.biryeong.semiontd.config.AttackKind;
import kim.biryeong.semiontd.config.EconomyConfig;
import kim.biryeong.semiontd.config.WaveConfig;
import kim.biryeong.semiontd.entity.SemionEntityTypes;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.DamageType;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.AssignedParticipant;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.MatchMode;
import kim.biryeong.semiontd.game.ParticipantSelectionPlan;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.summon.SummonRole;
import kim.biryeong.semiontd.summon.SummonTier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;

public final class BuilderIntegrationGameTestSupport {
    private BuilderIntegrationGameTestSupport() {
    }

    public static SemionGame startedGame(
            GameTestHelper context,
            UUID owner,
            ResourceLocation jobId,
            String playerName
    ) {
        SemionGame game = new SemionGame(
                EconomyConfig.defaultConfig(),
                WaveConfig.defaultConfig(),
                SyntheticArenaFactory.create(context.getLevel(), context.absolutePos(BlockPos.ZERO))
        );
        require(game.selectJob(owner, jobId), "Builder selection must succeed for " + jobId + '.');
        require(game.start(
                context.getLevel().getServer(),
                new ParticipantSelectionPlan(
                        MatchMode.NORMAL,
                        List.of(new AssignedParticipant(owner, playerName, TeamId.RED, 1)),
                        Set.of(),
                        1
                )
        ), "Builder integration game must start for " + jobId + '.');
        return game;
    }

    public static PlayerLane lane(SemionGame game, UUID owner) {
        return game.playerLane(owner).orElseThrow();
    }

    public static BlockPos primaryPosition(PlayerLane lane) {
        return BlockPos.containing(lane.laneLayout().positionAt(0.35));
    }

    public static List<BlockPos> emptyPositions(PlayerLane lane, BlockPos origin, int count) {
        List<BlockPos> positions = new ArrayList<>(count);
        for (int radius = 0; radius <= 8 && positions.size() < count; radius++) {
            for (int dx = -radius; dx <= radius && positions.size() < count; dx++) {
                for (int dz = -radius; dz <= radius && positions.size() < count; dz++) {
                    BlockPos candidate = origin.offset(dx, 0, dz);
                    if (!positions.contains(candidate)
                            && lane.canPlaceTowerAt(candidate)
                            && !lane.hasTowerAt(GridPosition.from(candidate))) {
                        positions.add(candidate);
                    }
                }
            }
        }
        require(positions.size() == count, "Expected " + count + " empty tower positions, found " + positions.size() + '.');
        return positions;
    }

    public static SemionMonsterEntity spawnMonster(
            GameTestHelper context,
            PlayerLane lane,
            String id,
            int targetLaneId,
            double maximumHealth,
            double x,
            double y,
            double z
    ) {
        return spawnMonster(context, lane, id, targetLaneId, maximumHealth, 1.0, x, y, z);
    }

    public static SemionMonsterEntity spawnMonster(
            GameTestHelper context,
            PlayerLane lane,
            String id,
            int targetLaneId,
            double maximumHealth,
            double attackDamage,
            double x,
            double y,
            double z
    ) {
        Monster monster = new Monster(
                id,
                lane.teamId(),
                targetLaneId,
                Optional.empty(),
                Optional.empty(),
                maximumHealth,
                0.0,
                attackDamage,
                AttackKind.MELEE,
                "minecraft:zombie",
                0L
        );
        SemionMonsterEntity entity = new SemionMonsterEntity(SemionEntityTypes.MONSTER, context.getLevel());
        entity.configureFrom(monster, lane.laneLayout());
        entity.setNoAi(true);
        entity.setPos(x, y, z);
        context.getLevel().addFreshEntity(entity);
        monster.markMinecraftEntitySpawned(entity.getId(), x, y, z);
        lane.activeMonsters().add(monster);
        return entity;
    }

    public static SemionMonsterEntity spawnRoleMonster(
            GameTestHelper context,
            String id,
            Optional<TeamId> senderTeam,
            TeamId targetTeam,
            int targetLaneId,
            double maximumHealth,
            double armor,
            double resistance,
            List<SummonRole> roles,
            double x,
            double y,
            double z
    ) {
        Monster monster = new Monster(
                id,
                targetTeam,
                targetLaneId,
                Optional.empty(),
                senderTeam,
                maximumHealth,
                armor,
                0.0,
                AttackKind.MELEE,
                "minecraft:zombie",
                null,
                DamageType.PHYSICAL,
                resistance,
                SummonTier.T1,
                roles,
                0L
        );
        SemionMonsterEntity entity = new SemionMonsterEntity(SemionEntityTypes.MONSTER, context.getLevel());
        entity.configureFrom(monster, null);
        entity.setNoGravity(true);
        entity.setPos(x, y, z);
        context.getLevel().addFreshEntity(entity);
        return entity;
    }

    public static SemionTowerEntity towerEntity(PlayerLane lane, EntityBackedTower tower) {
        return (SemionTowerEntity) lane.arenaWorld().getEntity(tower.entityId().orElseThrow());
    }

    public static UUID stableUuid(String seed) {
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }

    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void requireClose(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 0.0001) {
            throw new AssertionError(message + " Expected " + expected + ", got " + actual + '.');
        }
    }
}
