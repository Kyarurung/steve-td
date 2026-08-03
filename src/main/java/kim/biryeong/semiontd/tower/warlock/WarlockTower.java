package kim.biryeong.semiontd.tower.warlock;

import static kim.biryeong.semiontd.tower.warlock.WarlockConfig.Ability.*;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.damagesource.DamageSource;

public class WarlockTower extends EntityBackedTower {
    public static final String CONFIG_ID = WarlockTowers.CONFIG_ID;
    private final WarlockState state;
    private final WarlockSacrificeController sacrifices;
    private final WarlockCombat combat;
    private final WarlockStats stats;
    private PlayerLane currentLane;
    private int regenerationTicks;

    public WarlockTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
        this.state = new WarlockState();
        this.sacrifices = new WarlockSacrificeController(WarlockConfig.RUNTIME, this.state);
        this.combat = new WarlockCombat(WarlockConfig.RUNTIME);
        this.stats = new WarlockStats(this.combat);
    }

    public WarlockTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
        this.state = new WarlockState();
        this.sacrifices = new WarlockSacrificeController(WarlockConfig.RUNTIME, this.state);
        this.combat = new WarlockCombat(WarlockConfig.RUNTIME);
        this.stats = new WarlockStats(this.combat);
    }

    @Override
    public void onPlaced(PlayerLane lane) {
        currentLane = lane;
        super.onPlaced(lane);
        refreshWarlockCoreStats(lane);
    }

    @Override
    public void onRemoved(PlayerLane lane) {
        super.onRemoved(lane);
        if (currentLane == lane) {
            currentLane = null;
        }
    }

    @Override
    public double currentMaxHealth() {
        return applyTraitMaxHealth(maxHealth() * (1.0 + passiveHealthBonus())
                + state.permanentHealthBonus() + state.roundHealthBonus());
    }

    @Override
    public double modifyAttackDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        return (damageAmount + state.permanentDamageBonus() + state.roundDamageBonus()
                + combat.meleeRoundDamageBonus(this))
                * (1.0 + passiveDamageBonus());
    }

    @Override
    public double modifyOutgoingDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        return combat.modifyOutgoingDamage(type(), damageAmount);
    }

    @Override
    public double modifyResolvedOutgoingDamage(
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        return combat.modifyOutgoingDamage(type(), damageAmount);
    }

    @Override
    public double modifyAppliedDamage(
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        return combat.modifyOutgoingDamage(type(), damageAmount);
    }

    @Override
    public double modifyIncomingDamage(SemionTowerEntity towerEntity, DamageSource damageSource, double damageAmount) {
        if (damageAmount <= 0.0) {
            return damageAmount;
        }
        return damageAmount * Math.max(0.0, 1.0 - damageReduction());
    }

    @Override
    public void onDamaged(
            SemionTowerEntity towerEntity,
            DamageSource damageSource,
            double damageAmount,
            double previousHealth,
            double currentHealth
    ) {
        if (is(WarlockTowers.BASE_WARLOCK_TOWER) && currentHealth <= 0.0) {
            if (sacrifices.sacrifice(
                    this,
                    towerEntity,
                    currentLane,
                    sacrificeRadius(BASE_RADIUS),
                    Comparator.comparingInt(Tower::aggroPriority)
            )) {
                heal(towerEntity, ability(BASE_HEAL) * currentMaxHealth());
            }
            return;
        }
        if (is(WarlockTowers.RANGED_WARLOCK_TOWER) && healthRatio(currentHealth) < ability(RANGED_THRESHOLD)) {
            sacrifices.sacrifice(
                    this,
                    towerEntity,
                    currentLane,
                    sacrificeRadius(SACRIFICE_RADIUS),
                    Comparator.comparingInt(Tower::aggroPriority)
            );
            return;
        }
        if (is(WarlockTowers.MELEE_WARLOCK_TOWER) && healthRatio(currentHealth) < ability(MELEE_THRESHOLD)) {
            sacrifices.sacrifice(
                    this,
                    towerEntity,
                    currentLane,
                    sacrificeRadius(SACRIFICE_RADIUS),
                    Comparator.comparingInt(Tower::aggroPriority).reversed()
            );
        }
    }

    @Override
    public void onAttackResolved(
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double attemptedDamage,
            double resolvedOutgoingDamage,
            double dealtDamage,
            boolean killedTarget
    ) {
        combat.resolveAttack(this, towerEntity, target, resolvedOutgoingDamage, dealtDamage);
    }

    @Override
    public int adjustAttackInterval(int baseIntervalTicks) {
        if (is(WarlockTowers.RANGED_WARLOCK_TOWER)) {
            return Math.max(
                    combat.minimumAttackIntervalTicks(),
                    (int) Math.ceil(baseIntervalTicks - state.roundIntervalReduction())
            );
        }
        if (is(WarlockTowers.MELEE_WARLOCK_TOWER)) {
            return Math.max(
                    combat.minimumAttackIntervalTicks(),
                    baseIntervalTicks - combat.meleeAttackIntervalReduction(this)
            );
        }
        if (baseIntervalTicks <= 0) {
            return baseIntervalTicks;
        }
        return Math.max(combat.minimumAttackIntervalTicks(), baseIntervalTicks);
    }

    @Override
    public int minimumAttackIntervalTicks() {
        return combat.minimumAttackIntervalTicks();
    }

    @Override
    public List<String> runtimeDetailLines() {
        return stats.create(this);
    }

    @Override
    public void tick(PlayerLane lane) {
        currentLane = lane;
        super.tick(lane);
        tickRegeneration(lane);
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        currentLane = lane;
        state.resetRound();
        regenerationTicks = 0;
        super.resetForRound(lane);
        refreshWarlockCoreStats(lane);
    }

    @Override
    public void finishRoundReset(PlayerLane lane) {
        currentLane = lane;
        syncHealth(currentMaxHealth());
        onStateChanged(lane);
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (previousTower instanceof WarlockTower warlockTower) {
            state.copyFrom(warlockTower.state);
            regenerationTicks = warlockTower.regenerationTicks;
        }
    }

    void heal(SemionTowerEntity towerEntity, double amount) {
        if (towerEntity == null || amount <= 0.0) {
            return;
        }
        double nextHealth = Math.min(currentMaxHealth(), health() + amount);
        syncHealth(nextHealth);
        towerEntity.setHealth((float) nextHealth);
    }

    void refreshAfterSacrifice(PlayerLane lane, SemionTowerEntity towerEntity, double gainedHealth) {
        onStateChanged(lane);
        heal(towerEntity, gainedHealth);
        onStateChanged(lane);
    }

    private double passiveHealthBonus() {
        return sacrifices.passiveHealthBonus(this, currentLane);
    }

    private double passiveDamageBonus() {
        return sacrifices.passiveDamageBonus(this, currentLane);
    }

    double damageReduction() {
        return sacrifices.damageReduction(this);
    }

    double splashRadius() {
        return combat.splashRadius(this);
    }

    static void refreshWarlockCoreStats(PlayerLane lane) {
        if (lane == null) {
            return;
        }
        for (Tower tower : lane.towers()) {
            if (tower instanceof WarlockTower warlockTower) {
                warlockTower.syncHealth(warlockTower.health());
                warlockTower.onStateChanged(lane);
            }
        }
    }

    boolean is(TowerType towerType) {
        return type().id().equals(towerType.id());
    }

    int totalSacrificeCount() {
        return state.totalSacrificeCount();
    }

    int roundSacrificeCount() {
        return state.roundSacrificeCount();
    }

    double additionalAttackDamage() {
        return Math.max(0.0, modifyAttackDamage(null, null, type().damage()) - type().damage());
    }

    double additionalHealth() {
        return Math.max(0.0, currentMaxHealth() - applyTraitMaxHealth(maxHealth()));
    }

    int attackIntervalReduction() {
        return Math.max(0, type().attackIntervalTicks() - adjustAttackInterval(type().attackIntervalTicks()));
    }

    int maximumAttackIntervalReduction() {
        int maximumByMinimumInterval = Math.max(
                0,
                type().attackIntervalTicks() - combat.minimumAttackIntervalTicks()
        );
        return Math.min(maximumByMinimumInterval, combat.maximumAttackIntervalReduction());
    }

    double regenerationPerSecond() {
        return combat.regenerationPerSecond(this);
    }

    double maximumRegenerationPerSecond() {
        return combat.maximumRegenerationPerSecond(this);
    }

    private void tickRegeneration(PlayerLane lane) {
        double amount = regenerationPerSecond();
        if (health() <= 0.0 || amount <= 0.0) {
            regenerationTicks = 0;
            return;
        }
        if (health() >= currentMaxHealth()) {
            return;
        }
        int intervalTicks = combat.regenerationIntervalTicks();
        regenerationTicks++;
        if (regenerationTicks < intervalTicks) {
            return;
        }
        regenerationTicks %= intervalTicks;
        syncHealth(health() + amount);
        onStateChanged(lane);
    }

    double maximumDamageReduction() {
        return sacrifices.maximumDamageReduction(this);
    }

    private double sacrificeRadius(WarlockConfig.Ability key) {
        double radius = ability(key);
        return radius <= 0.0 ? Double.MAX_VALUE : radius;
    }

    private double ability(WarlockConfig.Ability key) {
        return WarlockConfig.RUNTIME.value(key);
    }

    private double healthRatio(double currentHealth) {
        double maxHealth = currentMaxHealth();
        return maxHealth <= 0.0 ? 0.0 : currentHealth / maxHealth;
    }

}
