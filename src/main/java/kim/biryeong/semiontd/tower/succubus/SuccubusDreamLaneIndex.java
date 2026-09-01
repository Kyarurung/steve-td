package kim.biryeong.semiontd.tower.succubus;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.Tower;

final class SuccubusDreamLaneIndex {
    private final Map<PlayerLane, LaneEntries> lanes = new IdentityHashMap<>();
    private final Map<TowerKey, PlayerLane> towerLanes = new HashMap<>();
    private final Map<UUID, PlayerLane> monsterLanes = new HashMap<>();
    private final Map<TowerKey, PlayerLane> lullabyLanes = new HashMap<>();

    void indexTower(PlayerLane lane, TowerKey key) {
        index(lane, key, towerLanes, LaneEntries::towerKeys);
    }

    void indexMonster(PlayerLane lane, UUID monsterId) {
        index(lane, monsterId, monsterLanes, LaneEntries::monsterIds);
    }

    void indexLullaby(PlayerLane lane, TowerKey key) {
        index(lane, key, lullabyLanes, LaneEntries::lullabyKeys);
    }

    void removeTower(TowerKey key) {
        remove(key, towerLanes, LaneEntries::towerKeys);
    }

    void removeMonster(UUID monsterId) {
        remove(monsterId, monsterLanes, LaneEntries::monsterIds);
    }

    void removeLullaby(TowerKey key) {
        remove(key, lullabyLanes, LaneEntries::lullabyKeys);
    }

    Snapshot snapshot(PlayerLane lane) {
        LaneEntries entries = lanes.get(lane);
        return entries == null ? Snapshot.EMPTY : entries.snapshot();
    }

    Snapshot removeLane(PlayerLane lane) {
        LaneEntries entries = lanes.remove(lane);
        if (entries == null) return Snapshot.EMPTY;
        Snapshot snapshot = entries.snapshot();
        snapshot.towerKeys().forEach(key -> towerLanes.remove(key, lane));
        snapshot.monsterIds().forEach(id -> monsterLanes.remove(id, lane));
        snapshot.lullabyKeys().forEach(key -> lullabyLanes.remove(key, lane));
        return snapshot;
    }

    private <K> void index(PlayerLane lane, K key, Map<K, PlayerLane> owners,
                           java.util.function.Function<LaneEntries, Set<K>> entries) {
        if (lane == null || key == null) return;
        PlayerLane previous = owners.put(key, lane);
        if (previous != null && previous != lane) {
            LaneEntries previousEntries = lanes.get(previous);
            if (previousEntries != null) {
                entries.apply(previousEntries).remove(key);
                removeIfEmpty(previous, previousEntries);
            }
        }
        entries.apply(lanes.computeIfAbsent(lane, ignored -> new LaneEntries())).add(key);
    }

    private <K> void remove(K key, Map<K, PlayerLane> owners,
                            java.util.function.Function<LaneEntries, Set<K>> entries) {
        if (key == null) return;
        PlayerLane lane = owners.remove(key);
        if (lane == null) return;
        LaneEntries laneEntries = lanes.get(lane);
        if (laneEntries == null) return;
        entries.apply(laneEntries).remove(key);
        removeIfEmpty(lane, laneEntries);
    }

    private void removeIfEmpty(PlayerLane lane, LaneEntries entries) {
        if (entries.isEmpty()) lanes.remove(lane);
    }

    record TowerKey(UUID owner, GridPosition originalPosition) {
        static TowerKey of(Tower tower) {
            return new TowerKey(tower.ownerPlayer(), tower.originalPosition());
        }
    }

    record Snapshot(Set<TowerKey> towerKeys, Set<UUID> monsterIds, Set<TowerKey> lullabyKeys) {
        private static final Snapshot EMPTY = new Snapshot(Set.of(), Set.of(), Set.of());
    }

    private static final class LaneEntries {
        private final Set<TowerKey> towerKeys = new LinkedHashSet<>();
        private final Set<UUID> monsterIds = new LinkedHashSet<>();
        private final Set<TowerKey> lullabyKeys = new LinkedHashSet<>();

        private Set<TowerKey> towerKeys() {
            return towerKeys;
        }

        private Set<UUID> monsterIds() {
            return monsterIds;
        }

        private Set<TowerKey> lullabyKeys() {
            return lullabyKeys;
        }

        private boolean isEmpty() {
            return towerKeys.isEmpty() && monsterIds.isEmpty() && lullabyKeys.isEmpty();
        }

        private Snapshot snapshot() {
            return new Snapshot(Set.copyOf(towerKeys), Set.copyOf(monsterIds), Set.copyOf(lullabyKeys));
        }
    }
}
