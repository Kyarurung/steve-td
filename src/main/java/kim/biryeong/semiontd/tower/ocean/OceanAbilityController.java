package kim.biryeong.semiontd.tower.ocean;

import java.util.List;
import java.util.Objects;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import net.minecraft.world.phys.Vec3;

final class OceanAbilityController {
    private static final double EPSILON = 1.0E-9;
    private final OceanConfig config;
    private final OceanResourceState state;
    private PlayerLane currentLane;

    OceanAbilityController(OceanConfig config, OceanResourceState state) {
        this.config = config;
        this.state = state;
    }

    void attach(PlayerLane lane) {
        currentLane = lane;
    }

    boolean execute(OceanTower tower, PlayerLane lane) {
        if (!state.waveActive() || (!OceanTowers.isSupport(tower.type()) && !OceanTowers.isHealer(tower.type()))) {
            return false;
        }
        return OceanTowers.isHealer(tower.type()) ? healNearbyTowers(tower, lane) : supportNearbyTowers(tower, lane);
    }

    int cooldownTicks(OceanTower tower, int fallback) {
        if (OceanTowers.isHealer(tower.type())) {
            return Math.max(1, config.ticks(tower.type(), OceanAbilityKey.HEAL_INTERVAL_TICKS));
        }
        return OceanTowers.isSupport(tower.type())
                ? Math.max(1, config.ticks(tower.type(), OceanAbilityKey.SUPPORT_INTERVAL_TICKS))
                : fallback;
    }

    void tick(OceanTower tower, PlayerLane lane) {
        attach(lane);
        state.tickTransferCooldown();
        if (!state.waveActive() || state.water() > 0.0 || tower.health() <= 0.0) {
            state.clearDehydration();
            return;
        }
        if (!state.tickDehydration(20)) {
            return;
        }
        tower.syncHealth(tower.health() - tower.currentMaxHealth()
                * config.global(OceanAbilityKey.DEHYDRATION_MAX_HEALTH_DAMAGE_PER_SECOND));
        tower.ownRuntimeEntity(lane).ifPresent(entity -> {
            OceanVfx.showDehydrated(
                    lane.arenaWorld(),
                    new Vec3(entity.getX(), entity.getY() + 0.12, entity.getZ())
            );
            entity.setHealth((float) tower.health());
        });
    }

    void onDamaged(
            OceanTower tower,
            SemionTowerEntity towerEntity,
            double previousHealth,
            double currentHealth
    ) {
        if (tower.deployedAtFinalDefense() || !OceanTowers.isTank(tower.type()) || currentLane == null
                || currentHealth <= 0.0 || state.water() <= 0.0 || state.transferCooldownTicks() > 0) {
            return;
        }
        double pool = Math.min(
                Math.max(0.0, previousHealth - currentHealth),
                config.value(tower.type(), OceanAbilityKey.TRANSFER_CAP)
        );
        if (pool <= 0.0) {
            return;
        }
        List<OceanTower> recipients = nearbyCombatTowers(
                tower,
                currentLane,
                config.value(tower.type(), OceanAbilityKey.TRANSFER_RADIUS)
        ).stream()
                .filter(target -> target != tower)
                .filter(target -> !OceanTowers.isTank(target.type()))
                .toList();
        if (recipients.isEmpty()
                || !state.spendWater(config.value(tower.type(), OceanAbilityKey.TRANSFER_WATER_COST))) {
            return;
        }
        double share = pool / recipients.size();
        recipients.forEach(target -> target.addWater(share));
        state.startTransferCooldown(config.ticks(tower.type(), OceanAbilityKey.TRANSFER_COOLDOWN_TICKS));
        OceanVfx.showWaterSupply(
                currentLane.arenaWorld(),
                new Vec3(towerEntity.getX(), towerEntity.getY() + towerEntity.getBbHeight() * 0.5, towerEntity.getZ()),
                recipients,
                true
        );
    }

    static List<OceanTower> nearbyCombatTowers(OceanTower source, PlayerLane lane, double radius) {
        if (source == null || lane == null || radius <= 0.0) {
            return List.of();
        }
        double radiusSqr = radius * radius;
        return lane.towers().stream()
                .filter(OceanTower.class::isInstance)
                .map(OceanTower.class::cast)
                .filter(target -> target.health() > 0.0)
                .filter(target -> OceanRules.distanceSquared(
                        target.position().x(), target.position().y(), target.position().z(),
                        source.position().x(), source.position().y(), source.position().z()
                ) <= radiusSqr)
                .toList();
    }

    private boolean supportNearbyTowers(OceanTower tower, PlayerLane lane) {
        boolean empowered = empowered();
        double effectMultiplier = empowered
                ? config.global(OceanAbilityKey.EMPOWERED_ABILITY_EFFECT_MULTIPLIER)
                : 1.0;
        double cost = abilityCost(tower, empowered);
        if (state.water() + EPSILON < cost) {
            return false;
        }
        List<SemionTowerEntity> targets = nearbyCombatTowers(
                tower, lane, config.value(tower.type(), OceanAbilityKey.SUPPORT_RADIUS)
        ).stream()
                .filter(target -> target != tower && target.type().damage() > 0.0)
                .map(target -> tower.runtimeEntity(target, lane).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        if (targets.isEmpty() || !state.spendWater(cost)) {
            return false;
        }
        int duration = config.ticks(tower.type(), OceanAbilityKey.BUFF_DURATION_TICKS);
        for (SemionTowerEntity target : targets) {
            target.applyTimedEffect(
                    TimedEffectType.TOWER_DAMAGE_BONUS,
                    config.value(tower.type(), OceanAbilityKey.DAMAGE_BONUS) * effectMultiplier,
                    duration
            );
            target.applyTimedEffect(
                    TimedEffectType.TOWER_ATTACK_SPEED_BONUS,
                    config.value(tower.type(), OceanAbilityKey.ATTACK_SPEED_BONUS) * effectMultiplier,
                    duration
            );
        }
        return true;
    }

    private boolean healNearbyTowers(OceanTower tower, PlayerLane lane) {
        boolean empowered = empowered();
        double healAmount = config.value(tower.type(), OceanAbilityKey.HEAL_AMOUNT)
                * (empowered ? config.global(OceanAbilityKey.EMPOWERED_ABILITY_EFFECT_MULTIPLIER) : 1.0);
        double cost = abilityCost(tower, empowered);
        if (state.water() + EPSILON < cost) {
            return false;
        }
        List<SemionTowerEntity> targets = nearbyCombatTowers(
                tower, lane, config.value(tower.type(), OceanAbilityKey.HEAL_RADIUS)
        ).stream()
                .filter(target -> target != tower && target.health() < target.currentMaxHealth())
                .map(target -> tower.runtimeEntity(target, lane).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        if (targets.isEmpty() || !state.spendWater(cost)) {
            return false;
        }
        targets.forEach(target -> {
            if (tower.healRuntimeTarget(target, healAmount)) {
                target.playHealingAnimation();
            }
        });
        return true;
    }

    private boolean empowered() {
        return state.water() + EPSILON >= config.global(OceanAbilityKey.EMPOWERED_ABILITY_WATER_THRESHOLD);
    }

    private double abilityCost(OceanTower tower, boolean empowered) {
        return config.value(tower.type(), OceanAbilityKey.ABILITY_WATER_COST)
                * (empowered ? config.global(OceanAbilityKey.EMPOWERED_ABILITY_WATER_COST_MULTIPLIER) : 1.0);
    }
}
