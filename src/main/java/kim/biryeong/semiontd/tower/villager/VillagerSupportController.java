package kim.biryeong.semiontd.tower.villager;

import java.util.Optional;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.api.SemionTdApi;
import kim.biryeong.semiontd.api.area.AreaEffectOutcome;
import kim.biryeong.semiontd.api.area.TowerAreaEffectRequest;
import kim.biryeong.semiontd.api.area.TowerAreaTargetMode;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerDataKey;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import net.minecraft.resources.ResourceLocation;

/** Coordinates the Allay, weapon-smith, and armorer support transitions. */
final class VillagerSupportController {
    private static final int MINIMUM_REDUCED_INTERVAL_TICKS = 20;
    private static final ResourceLocation WEAPON_SMITH_SOURCE = supportId("weapon_smith");
    private static final ResourceLocation ARMORER_SOURCE = supportId("armorer");
    private static final TowerDataKey<Long> HEAL_BLOCKED_UNTIL = TowerDataKey.of(
            supportId("allay_heal_blocked_until"), Long.class
    );
    private static final TowerDataKey<Long> WEAPON_SMITH_BLOCKED_UNTIL = TowerDataKey.of(
            supportId("weapon_smith_blocked_until"), Long.class
    );
    private static final TowerDataKey<Long> ARMORER_BLOCKED_UNTIL = TowerDataKey.of(
            supportId("armorer_blocked_until"), Long.class
    );

    private final VillagerAllayTower owner;

    VillagerSupportController(VillagerAllayTower owner) {
        this.owner = owner;
    }

    boolean execute(PlayerLane lane) {
        if (owner.isType(VillagerTowers.T1_ALLAY_TOWER)
                || owner.isType(VillagerTowers.T2_ALLAY_TOWER)) {
            return applyHeal(lane, radius(), healAmount(lane, value(VillagerAbilityKey.HEAL_AMOUNT)));
        }
        if (owner.isType(VillagerTowers.T2_WEAPON_SMITH_TOWER)
                || owner.isType(VillagerTowers.T3_WEAPON_SMITH_TOWER)) {
            return applyWeaponSmithBuff(lane, radius(), value(VillagerAbilityKey.WEAPON_BUFF));
        }
        if (owner.isType(VillagerTowers.T3_ARMORER_TOWER)) {
            return applyArmorerSupport(lane);
        }
        return false;
    }

    int cooldownTicksAfterExecute(PlayerLane lane, int baseTicks) {
        return reducedTicks(baseTicks, intervalReduction(lane), minimumReducedIntervalTicks());
    }

    private boolean applyHeal(PlayerLane lane, double radius, double amount) {
        SemionTowerEntity source = towerEntity(owner, lane).orElse(null);
        if (source == null) {
            return false;
        }
        TowerAreaEffectRequest request = supportRequest(source, radius, "heal")
                .withFilter(target -> canApplyAt(
                        target.tower().getDataOrDefault(HEAL_BLOCKED_UNTIL, 0L),
                        lane.arenaWorld().getGameTime()
                ));
        return SemionTdApi.areaEffects().applyToTowers(request, target -> {
            if (!heal(target.tower(), lane, amount)) {
                return AreaEffectOutcome.UNCHANGED;
            }
            block(target.tower(), HEAL_BLOCKED_UNTIL, lane);
            return AreaEffectOutcome.APPLIED;
        }).appliedCount() > 0;
    }

    private boolean applyWeaponSmithBuff(PlayerLane lane, double radius, double magnitude) {
        SemionTowerEntity source = towerEntity(owner, lane).orElse(null);
        if (source == null) {
            return false;
        }
        TowerAreaEffectRequest request = supportRequest(source, radius, "weapon_smith")
                .withFilter(target -> canApplyAt(
                        target.tower().getDataOrDefault(WEAPON_SMITH_BLOCKED_UNTIL, 0L),
                        lane.arenaWorld().getGameTime()
                ) && target.entity().isPresent());
        return SemionTdApi.areaEffects().applyToTowers(request, target -> {
            SemionTowerEntity entity = target.entity().orElseThrow();
            boolean damageApplied = entity.applyTimedEffect(
                    TimedEffectType.TOWER_DAMAGE_BONUS,
                    WEAPON_SMITH_SOURCE,
                    magnitude,
                    ticks(VillagerAbilityKey.BUFF_DURATION_TICKS)
            );
            boolean speedApplied = entity.applyTimedEffect(
                    TimedEffectType.TOWER_ATTACK_SPEED_BONUS,
                    WEAPON_SMITH_SOURCE,
                    magnitude,
                    ticks(VillagerAbilityKey.BUFF_DURATION_TICKS)
            );
            if (!damageApplied && !speedApplied) {
                return AreaEffectOutcome.UNCHANGED;
            }
            block(target.tower(), WEAPON_SMITH_BLOCKED_UNTIL, lane);
            return AreaEffectOutcome.APPLIED;
        }).appliedCount() > 0;
    }

    private boolean applyArmorerSupport(PlayerLane lane) {
        SemionTowerEntity source = towerEntity(owner, lane).orElse(null);
        if (source == null) {
            return false;
        }
        TowerAreaEffectRequest request = supportRequest(source, radius(), "armorer")
                .withFilter(target -> canApplyAt(
                        target.tower().getDataOrDefault(ARMORER_BLOCKED_UNTIL, 0L),
                        lane.arenaWorld().getGameTime()
                ));
        return SemionTdApi.areaEffects().applyToTowers(request, target -> {
            boolean healed = heal(
                    target.tower(), lane, healAmount(lane, value(VillagerAbilityKey.HEAL_AMOUNT))
            );
            boolean reducedDamage = target.entity()
                    .map(entity -> entity.applyTimedEffect(
                            TimedEffectType.TOWER_DAMAGE_REDUCTION,
                            ARMORER_SOURCE,
                            value(VillagerAbilityKey.DAMAGE_REDUCTION),
                            ticks(VillagerAbilityKey.BUFF_DURATION_TICKS)
                    ))
                    .orElse(false);
            if (!healed && !reducedDamage) {
                return AreaEffectOutcome.UNCHANGED;
            }
            block(target.tower(), ARMORER_BLOCKED_UNTIL, lane);
            return AreaEffectOutcome.APPLIED;
        }).appliedCount() > 0;
    }

    private TowerAreaEffectRequest supportRequest(
            SemionTowerEntity source,
            double radius,
            String effect
    ) {
        return TowerAreaEffectRequest.aroundTower(
                AreaEffectIds.tower(owner, effect),
                source,
                radius,
                TowerAreaTargetMode.REGISTERED,
                VillagerVfx.buff()
        );
    }

    private boolean heal(Tower target, PlayerLane lane, double amount) {
        Optional<SemionTowerEntity> targetEntity = towerEntity(target, lane);
        if (targetEntity.isPresent()) {
            boolean healed = owner.healEntity(targetEntity.get(), amount);
            if (healed) {
                targetEntity.get().playHealingAnimation();
            }
            return healed;
        }
        if (target.health() <= 0.0 || target.health() >= target.currentMaxHealth()) {
            return false;
        }
        double before = target.health();
        target.syncHealth(before + amount);
        double healed = Math.max(0.0, target.health() - before);
        owner.recordSupportHealing(healed);
        return healed > 0.0;
    }

    private void block(Tower target, TowerDataKey<Long> key, PlayerLane lane) {
        long gameTime = lane.arenaWorld().getGameTime();
        target.setData(key, gameTime + supportBlockTicks(lane));
    }

    private double radius() {
        return value(VillagerAbilityKey.RADIUS);
    }

    private double value(VillagerAbilityKey ability) {
        return VillagerConfig.RUNTIME.value(owner.type(), ability);
    }

    private int ticks(VillagerAbilityKey ability) {
        return VillagerConfig.RUNTIME.ticks(owner.type(), ability);
    }

    private double healAmount(PlayerLane lane, double baseAmount) {
        return baseAmount * (1.0 + activeEffect(lane, TimedEffectType.TOWER_HEAL_AMOUNT_BONUS));
    }

    private int supportBlockTicks(PlayerLane lane) {
        return reducedTicks(
                ticks(VillagerAbilityKey.SUPPORT_BLOCK_TICKS),
                intervalReduction(lane),
                minimumReducedIntervalTicks()
        );
    }

    private int minimumReducedIntervalTicks() {
        return VillagerTowers.isAdvVillagerTower(owner.type())
                && (owner.isType(VillagerTowers.T1_ALLAY_TOWER)
                || owner.isType(VillagerTowers.T2_ALLAY_TOWER)
                || owner.isType(VillagerTowers.T3_ARMORER_TOWER))
                ? MINIMUM_REDUCED_INTERVAL_TICKS
                : 1;
    }

    private double intervalReduction(PlayerLane lane) {
        return activeEffect(lane, TimedEffectType.TOWER_ABILITY_INTERVAL_REDUCTION);
    }

    private double activeEffect(PlayerLane lane, TimedEffectType type) {
        return towerEntity(owner, lane)
                .map(entity -> entity.activeTimedEffectMagnitude(type))
                .orElse(0.0);
    }

    static int reducedTicks(int baseTicks, double reduction, int minimumTicks) {
        return Math.max(minimumTicks, (int) Math.ceil(baseTicks * Math.max(0.01, 1.0 - reduction)));
    }

    static boolean canApplyAt(long blockedUntil, long gameTime) {
        return blockedUntil <= gameTime;
    }

    private static Optional<SemionTowerEntity> towerEntity(Tower target, PlayerLane lane) {
        if (!(target instanceof EntityBackedTower entityBackedTower)
                || entityBackedTower.entityId().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(lane.arenaWorld().getEntity(entityBackedTower.entityId().getAsInt()))
                .filter(SemionTowerEntity.class::isInstance)
                .map(SemionTowerEntity.class::cast);
    }

    private static ResourceLocation supportId(String path) {
        return ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "tower_support/" + path);
    }
}
