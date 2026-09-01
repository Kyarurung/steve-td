package kim.biryeong.semiontd.tower.nether;

import java.util.Optional;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.TowerDataKey;
import net.minecraft.resources.ResourceLocation;

final class NetherDecayController {
    private static final TowerDataKey<NetherTowerState> STATE = TowerDataKey.of(
            ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "nether_tower_state"),
            NetherTowerState.class
    );

    private final NetherConfig config;
    private final NetherCombat combat;
    private final NetherCombatState combatState;

    NetherDecayController(NetherConfig config, NetherCombat combat, NetherCombatState combatState) {
        this.config = config;
        this.combat = combat;
        this.combatState = combatState;
    }

    void initialize(NetherTower tower) {
        tower.setData(STATE, NetherTowerState.NETHER);
    }

    NetherTowerState state(NetherTower tower) {
        return tower.getDataOrDefault(STATE, NetherTowerState.NETHER);
    }

    void tick(NetherTower tower, PlayerLane lane, SemionTowerEntity entity) {
        if (entity == null || !shouldDecay(tower, lane)) {
            return;
        }
        double reduction = combatState.decayReductionTicks() > 0
                ? config.value(tower.type(), NetherAbilityKey.DECAY_REDUCTION_RATIO)
                : 0.0;
        double damage = decayDamage(
                tower.currentMaxHealth(),
                decayRatioPerSecond(tower),
                reduction
        );
        double nextHealth = tower.health() - damage;
        if (nextHealth <= 0.0 && state(tower) == NetherTowerState.NETHER) {
            reviveAsZombie(tower, lane, entity);
            return;
        }
        tower.syncHealth(nextHealth);
        entity.setHealth((float) tower.health());
        if (tower.health() <= 0.0 && state(tower) == NetherTowerState.ZOMBIE) {
            entity.discard();
        }
    }

    boolean shouldReviveDestroyed(NetherTower tower, PlayerLane lane) {
        if (tower.wasEntityUnloaded()
                || state(tower) != NetherTowerState.NETHER
                || tower.entityId().isEmpty()) {
            return false;
        }
        Optional<SemionTowerEntity> entity = tower.runtimeEntity(lane);
        return entity.isEmpty() || !entity.get().isAlive() || entity.get().isRemoved();
    }

    void reviveDestroyed(NetherTower tower, PlayerLane lane) {
        reviveAsZombie(tower, lane, tower.runtimeEntity(lane).orElse(null));
    }

    void onDamaged(NetherTower tower, SemionTowerEntity entity, double currentHealth) {
        if (state(tower) == NetherTowerState.NETHER && currentHealth <= 0.0) {
            reviveAsZombie(tower, null, entity);
        }
    }

    void resetRound(NetherTower tower) {
        tower.setData(STATE, NetherTowerState.NETHER);
    }

    double decayRatioPerSecond(NetherTower tower) {
        return state(tower) == NetherTowerState.ZOMBIE
                ? config.global(NetherAbilityKey.ZOMBIE_DECAY)
                : config.global(NetherAbilityKey.NETHER_DECAY);
    }

    static double decayDamage(double maximumHealth, double ratioPerSecond, double reductionRatio) {
        double base = maximumHealth * ratioPerSecond / 20.0;
        return base * Math.max(0.0, 1.0 - reductionRatio);
    }

    private void reviveAsZombie(NetherTower tower, PlayerLane lane, SemionTowerEntity entity) {
        tower.setData(STATE, NetherTowerState.ZOMBIE);
        tower.syncHealth(
                tower.currentMaxHealth()
                        * Math.max(0.01, config.global(NetherAbilityKey.ZOMBIE_REVIVE_HEALTH))
        );
        if (entity == null || entity.isRemoved()) {
            if (lane != null) {
                tower.onRemoved(lane);
                tower.onPlaced(lane);
                entity = tower.runtimeEntity(lane).orElse(null);
            }
        } else {
            entity.setHealth((float) tower.health());
        }
        combat.onZombieTransition(tower, entity);
        NetherVfx.transition(entity);
        if (lane != null) {
            tower.onStateChanged(lane);
        }
    }

    private boolean shouldDecay(NetherTower tower, PlayerLane lane) {
        return lane != null
                && !tower.deployedAtFinalDefense()
                && !lane.activeMonsters().isEmpty()
                && tower.health() > 0.0;
    }
}
