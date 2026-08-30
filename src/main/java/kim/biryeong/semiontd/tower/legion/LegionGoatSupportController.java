package kim.biryeong.semiontd.tower.legion;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.api.SemionTdApi;
import kim.biryeong.semiontd.api.area.AreaEffectOutcome;
import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.api.area.TowerAreaEffectRequest;
import kim.biryeong.semiontd.api.area.TowerAreaTargetMode;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import net.minecraft.resources.ResourceLocation;

/** Selects deterministic goat support stacks and applies them through the shared area-effect pipeline. */
final class LegionGoatSupportController {
    private static final int ABSOLUTE_MAX_STACKS = 3;
    private static final ResourceLocation[] DAMAGE_SOURCES = stackSources("goat_damage");
    private static final ResourceLocation[] DAMAGE_REDUCTION_SOURCES = stackSources("goat_damage_reduction");
    private static final ResourceLocation[] CLONE_DAMAGE_SOURCES = stackSources("goat_clone_damage");
    private static final ResourceLocation[] CLONE_DAMAGE_REDUCTION_SOURCES = stackSources("goat_clone_damage_reduction");
    private static final Comparator<LegionGoatTower> STACK_ORDER = Comparator
            .comparingInt((LegionGoatTower tower) -> tower.originalPosition().x())
            .thenComparingInt(tower -> tower.originalPosition().y())
            .thenComparingInt(tower -> tower.originalPosition().z())
            .thenComparing(tower -> tower.type().id());

    private final LegionGoatTower owner;

    LegionGoatSupportController(LegionGoatTower owner) {
        this.owner = owner;
    }

    boolean execute(PlayerLane lane) {
        SemionTowerEntity source = towerEntity(owner, lane).orElse(null);
        if (source == null) {
            return false;
        }
        TowerAreaEffectRequest request = TowerAreaEffectRequest.aroundTower(
                AreaEffectIds.tower(owner, "goat_buff"),
                source,
                owner.radius(),
                TowerAreaTargetMode.REGISTERED_AND_CLONES,
                AreaVfxSpec.onChange(AreaVfxStyles.BUFF)
        ).withFilter(target -> isBuffTarget(target.tower()) && stackIndexFor(target.tower(), lane).isPresent());
        return SemionTdApi.areaEffects().applyToTowers(request, target -> {
            OptionalInt stackIndex = stackIndexFor(target.tower(), lane);
            if (stackIndex.isEmpty() || target.entity().isEmpty()) {
                return AreaEffectOutcome.UNCHANGED;
            }
            SemionTowerEntity entity = target.entity().orElseThrow();
            boolean applied;
            if (target.illusion()) {
                applied = applyEffect(entity, TimedEffectType.TOWER_DAMAGE_BONUS,
                        CLONE_DAMAGE_SOURCES[stackIndex.getAsInt()], owner.value("cloneDamageBonus"));
                applied |= applyEffect(entity, TimedEffectType.TOWER_DAMAGE_REDUCTION,
                        CLONE_DAMAGE_REDUCTION_SOURCES[stackIndex.getAsInt()], owner.value("cloneDamageReduction"));
            } else {
                applied = applyEffect(entity, TimedEffectType.TOWER_DAMAGE_BONUS,
                        DAMAGE_SOURCES[stackIndex.getAsInt()], owner.value("damageBonus"));
                applied |= applyEffect(entity, TimedEffectType.TOWER_DAMAGE_REDUCTION,
                        DAMAGE_REDUCTION_SOURCES[stackIndex.getAsInt()], owner.value("damageReduction"));
            }
            return applied ? AreaEffectOutcome.APPLIED : AreaEffectOutcome.UNCHANGED;
        }).appliedCount() > 0;
    }

    int maxStacks() {
        return LegionGoatRules.maxStacks(owner.configuredMaxStacks(), ABSOLUTE_MAX_STACKS);
    }

    private boolean isBuffTarget(Tower target) {
        return target != null
                && !(target instanceof LegionGoatTower)
                && target.health() > 0.0
                && target.ownerPlayer().equals(owner.ownerPlayer())
                && target.teamId() == owner.teamId()
                && target.laneId() == owner.laneId()
                && LegionTowers.isLegionTower(target.type())
                && withinRange(target);
    }

    private OptionalInt stackIndexFor(Tower target, PlayerLane lane) {
        List<LegionGoatTower> providers = lane.towers().stream()
                .filter(LegionGoatTower.class::isInstance)
                .map(LegionGoatTower.class::cast)
                .filter(goat -> canBuff(goat, target))
                .sorted(STACK_ORDER)
                .toList();
        return LegionGoatRules.providerIndex(providers, owner, maxStacks());
    }

    private boolean canBuff(LegionGoatTower goat, Tower target) {
        return goat.health() > 0.0
                && target != null
                && target.health() > 0.0
                && target.ownerPlayer().equals(goat.ownerPlayer())
                && target.teamId() == goat.teamId()
                && target.laneId() == goat.laneId()
                && LegionGoatRules.withinRange(goat.position(), target.position(), goat.radius());
    }

    private boolean withinRange(Tower target) {
        return LegionGoatRules.withinRange(owner.position(), target.position(), owner.radius());
    }

    private boolean applyEffect(
            SemionTowerEntity entity,
            TimedEffectType type,
            ResourceLocation source,
            double magnitude
    ) {
        return magnitude > 0.0
                && entity.refreshTimedEffect(type, source, magnitude, owner.ticks("buffDurationTicks"));
    }

    private static Optional<SemionTowerEntity> towerEntity(Tower target, PlayerLane lane) {
        if (!(target instanceof EntityBackedTower entityBackedTower) || entityBackedTower.entityId().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(lane.arenaWorld().getEntity(entityBackedTower.entityId().getAsInt()))
                .filter(SemionTowerEntity.class::isInstance)
                .map(SemionTowerEntity.class::cast);
    }

    private static ResourceLocation supportId(String path) {
        return ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "tower_support/" + path);
    }

    private static ResourceLocation[] stackSources(String path) {
        ResourceLocation[] sources = new ResourceLocation[ABSOLUTE_MAX_STACKS];
        for (int index = 0; index < ABSOLUTE_MAX_STACKS; index++) {
            sources[index] = supportId(path + "_" + (index + 1));
        }
        return sources;
    }
}
