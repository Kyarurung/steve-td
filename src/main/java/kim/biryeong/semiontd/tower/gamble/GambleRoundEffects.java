package kim.biryeong.semiontd.tower.gamble;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import net.minecraft.resources.ResourceLocation;

public final class GambleRoundEffects {
    private static final Map<PlayerLane, Map<UUID, Set<ResourceLocation>>> ACTIVE_SOURCES = new WeakHashMap<>();
    private static final List<TimedEffectType> EFFECT_TYPES = List.of(
            TimedEffectType.TOWER_DAMAGE_TAKEN_BONUS,
            TimedEffectType.TOWER_ATTACK_SPEED_REDUCTION,
            TimedEffectType.TOWER_RANGE_BONUS,
            TimedEffectType.TOWER_MAX_HEALTH_BONUS,
            TimedEffectType.TOWER_ATTACK_SPEED_BONUS,
            TimedEffectType.TOWER_DAMAGE_BONUS
    );

    private GambleRoundEffects() {
    }

    public static ResourceLocation sourceId(Tower tower) {
        String owner = tower.ownerPlayer().toString().replace("-", "");
        var position = tower.originalPosition();
        return ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID,
                "gamble/support/" + owner + "/" + position.x() + "_" + position.y() + "_" + position.z());
    }

    public static void clearSource(PlayerLane lane, UUID owner, ResourceLocation sourceId) {
        if (lane == null || sourceId == null) {
            return;
        }
        for (Tower target : lane.towers()) {
            if (owner.equals(target.ownerPlayer())) {
                towerEntity(target, lane).ifPresent(entity -> clearSource(entity, sourceId));
            }
        }
        GambleRollLabels.clearSource(lane, owner, sourceId);
    }

    public static synchronized void rememberSource(
            PlayerLane lane, UUID owner, ResourceLocation sourceId
    ) {
        if (lane == null || owner == null || sourceId == null) {
            return;
        }
        ACTIVE_SOURCES.computeIfAbsent(lane, ignored -> new LinkedHashMap<>())
                .computeIfAbsent(owner, ignored -> new LinkedHashSet<>()).add(sourceId);
    }

    public static synchronized void clearAll(PlayerLane lane, UUID owner) {
        if (lane == null || owner == null) {
            return;
        }
        LinkedHashSet<ResourceLocation> sources = new LinkedHashSet<>(ACTIVE_SOURCES
                .getOrDefault(lane, Map.of()).getOrDefault(owner, Set.of()));
        lane.towers().stream()
                .filter(tower -> owner.equals(tower.ownerPlayer()))
                .filter(tower -> tower instanceof GambleSupportTower)
                .map(GambleRoundEffects::sourceId)
                .forEach(sources::add);
        for (Tower target : lane.towers()) {
            if (!owner.equals(target.ownerPlayer())) {
                continue;
            }
            towerEntity(target, lane).ifPresent(entity -> sources.forEach(source -> clearSource(entity, source)));
        }
        Map<UUID, Set<ResourceLocation>> byOwner = ACTIVE_SOURCES.get(lane);
        if (byOwner != null) {
            byOwner.remove(owner);
            if (byOwner.isEmpty()) ACTIVE_SOURCES.remove(lane);
        }
        GambleRollLabels.clearAll(lane, owner);
    }

    static java.util.Optional<SemionTowerEntity> towerEntity(Tower tower, PlayerLane lane) {
        if (!(tower instanceof EntityBackedTower backed) || backed.entityId().isEmpty()
                || lane == null || lane.arenaWorld() == null) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.ofNullable(lane.arenaWorld().getEntity(backed.entityId().getAsInt()))
                .filter(SemionTowerEntity.class::isInstance).map(SemionTowerEntity.class::cast);
    }

    private static void clearSource(SemionTowerEntity entity, ResourceLocation sourceId) {
        EFFECT_TYPES.forEach(type -> entity.setPersistentEffect(type, sourceId, 0.0));
    }
}
