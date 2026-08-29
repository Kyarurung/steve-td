package kim.biryeong.semiontd.tower.frost;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.game.TeamLaneGroup;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.adversary.AdversaryRivalTower;
import net.minecraft.server.level.ServerLevel;

/** 혹한 빌더의 팀 레인 등록과 냉기 분출 장치 계열의 웨이브 스택·디버프 갱신. */
public final class FrostTeamEffects {
    private static final Map<UUID, TeamLaneGroup> REGISTERED_TEAMS = new ConcurrentHashMap<>();

    private FrostTeamEffects() {
    }

    public static void registerTeam(UUID playerId, TeamLaneGroup laneGroup) {
        if (playerId == null) {
            return;
        }
        if (laneGroup == null) {
            REGISTERED_TEAMS.remove(playerId);
        } else {
            REGISTERED_TEAMS.put(playerId, laneGroup);
        }
    }

    public static void unregisterPlayer(UUID playerId) {
        if (playerId != null) {
            REGISTERED_TEAMS.remove(playerId);
        }
    }

    static int snapshotEruptionStacks(UUID ownerPlayer, TeamId teamId, PlayerLane fallbackLane) {
        TeamLaneGroup laneGroup = REGISTERED_TEAMS.get(ownerPlayer);
        List<PlayerLane> lanes = laneGroup != null && laneGroup.teamId() == teamId
                ? List.copyOf(laneGroup.lanes())
                : fallbackLane == null ? List.of() : List.of(fallbackLane);
        int total = 0;
        for (PlayerLane lane : lanes) {
            if (lane == null || lane.teamId() != teamId || !ownerPlayer.equals(lane.ownerPlayer())) {
                continue;
            }
            UUID laneOwner = lane.ownerPlayer();
            List<Tower> ownedTowers = List.copyOf(lane.towers()).stream()
                    .filter(tower -> laneOwner.equals(tower.ownerPlayer()))
                    .toList();
            total += stacksForFamily(ownedTowers, FrostFamily.VANGUARD);
            total += stacksForFamily(ownedTowers, FrostFamily.ICE_BREAKER);
            total += stacksForFamily(ownedTowers, FrostFamily.FROZEN_DUMPLING);
            total += stacksForFamily(ownedTowers, FrostFamily.ICEBOX);
        }
        return FrostBalance.clampEruptionStacks(total);
    }

    static int refreshEruptionAura(
            FrostEruptionCoolingTower tower,
            PlayerLane ownLane,
            int stacks
    ) {
        if (tower == null || ownLane == null || stacks <= 0) {
            return 0;
        }
        List<PlayerLane> targetLanes = targetLanes(tower, ownLane);
        int affected = 0;
        int duration = Math.max(1, FrostBalance.eruptionAuraDurationTicks());
        for (PlayerLane lane : targetLanes) {
            if (lane == null || lane.teamId() != tower.teamId() || lane.arenaWorld() == null) {
                continue;
            }
            boolean own = tower.ownerPlayer().equals(lane.ownerPlayer());
            double damageReduction = FrostBalance.eruptionDamageReduction(stacks, own);
            double attackSpeedReduction = FrostBalance.eruptionAttackSpeedReduction(stacks, own);
            for (Monster monster : List.copyOf(lane.activeMonsters())) {
                SemionMonsterEntity entity = monsterEntity(monster, lane.arenaWorld());
                if (entity == null
                        || monster.targetTeam() != tower.teamId()
                        || AdversaryRivalTower.kindOf(monster).isPresent()) {
                    continue;
                }
                applyNonStackingEruptionAura(entity, damageReduction, attackSpeedReduction, duration);
                affected++;
            }
        }
        return affected;
    }

    private static List<PlayerLane> targetLanes(FrostEruptionCoolingTower tower, PlayerLane ownLane) {
        if (!FrostTowers.isExpandedEruptionCoolingDevice(tower.type())) {
            return List.of(ownLane);
        }
        TeamLaneGroup laneGroup = REGISTERED_TEAMS.get(tower.ownerPlayer());
        if (laneGroup == null || laneGroup.teamId() != tower.teamId()) {
            return List.of(ownLane);
        }
        return List.copyOf(laneGroup.lanes());
    }

    private static void applyNonStackingEruptionAura(
            SemionMonsterEntity entity,
            double damageReduction,
            double attackSpeedReduction,
            int duration
    ) {
        // 비출처 효과는 같은 유형 중 가장 강한 하나만 유지하므로 여러 아군 혹한 빌더의 분출 효과가 합산되지 않는다.
        entity.applyTimedEffect(TimedEffectType.MONSTER_ATTACK_DAMAGE_REDUCTION, damageReduction, duration);
        entity.applyTimedEffect(TimedEffectType.MONSTER_ATTACK_SPEED_REDUCTION, attackSpeedReduction, duration);
    }

    private static int stacksForFamily(List<Tower> towers, FrostFamily family) {
        int familyCount = (int) towers.stream()
                .map(Tower::type)
                .filter(type -> switch (family) {
                    case VANGUARD -> FrostTowers.isVanguard(type);
                    case ICE_BREAKER -> FrostTowers.isIceBreaker(type);
                    case FROZEN_DUMPLING -> FrostTowers.isFrozenDumpling(type);
                    case ICEBOX -> FrostTowers.isIcebox(type);
                })
                .count();
        return FrostBalance.eruptionStacksForFamilyCount(familyCount);
    }

    private static SemionMonsterEntity monsterEntity(Monster monster, ServerLevel level) {
        if (monster == null || !monster.hasMinecraftEntity()) {
            return null;
        }
        return level.getEntity(monster.minecraftEntityId()) instanceof SemionMonsterEntity entity
                && entity.runtimeMonster() == monster
                && entity.isAlive()
                && !entity.isRemoved()
                ? entity
                : null;
    }

    private enum FrostFamily {
        VANGUARD,
        ICE_BREAKER,
        FROZEN_DUMPLING,
        ICEBOX
    }
}
