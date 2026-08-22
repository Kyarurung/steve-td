package kim.biryeong.semiontd.tower.end;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerDataKey;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.entity.visual.EntityVisual;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

public final class EndTower extends EntityBackedTower {
    public static final String CONFIG_ID = EndTowers.CONFIG_ID;
    private static final TowerDataKey<EndTowerState> STATE = TowerDataKey.of(ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "end_tower_state"), EndTowerState.class);
    private final EndTransferController transfers;
    private final EndCombat combat;
    private final EndStats stats;
    private boolean waveActive;
    private int periodicHealingTicks;

    public EndTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        this(type, ownerPlayer, teamId, laneId, position, position);
    }

    public EndTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
        EndConfig config = EndConfig.RUNTIME;
        this.transfers = new EndTransferController(config);
        this.combat = new EndCombat(config, this.transfers);
        this.stats = new EndStats(config, this.combat, this.transfers);
        initializeState();
    }

    public EndTowerState state() {
        return getDataOrDefault(STATE, EndTowerState.EGG);
    }

    @Override
    public EntityVisual visual() {
        if (!isCoreTower()) {
            return super.visual();
        }
        return switch (state()) {
            case EGG -> EndTowers.DRAGON_EGG_VISUAL;
            case PHANTOM -> EndTowers.PHANTOM_VISUAL;
            case DRAGON -> EndTowers.DRAGON_VISUAL;
        };
    }

    @Override
    public void onWaveStarted(PlayerLane lane, int currentRound) {
        waveActive = true;
        if (!isCoreTower()) {
            return;
        }
        periodicHealingTicks = 0;
        if (transfers.rollbackIncomplete()) {
            refreshTransferStats(lane);
        }
        if (state() == EndTowerState.EGG) {
            switchToPhantom(lane);
        } else if (lane != null) {
            onStateChanged(lane);
        }
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        waveActive = false;
        periodicHealingTicks = 0;
        clearTransferLifecycleState();
        resetRoundTransferBonuses(lane);
        if (isCoreTower()) {
            setData(STATE, EndTowerState.EGG);
            syncMaxHealth(effectBaseMaxHealth(), false);
        }
        super.resetForRound(lane);
    }

    void resetRoundTransferBonuses(PlayerLane lane) {
        transfers.resetRound();
        refreshTransferStats(lane);
    }

    @Override
    public void onRemoved(PlayerLane lane) {
        clearTransferLifecycleState();
        super.onRemoved(lane);
    }

    @Override
    public void onDeath(PlayerLane lane) {
        clearTransferLifecycleState();
        super.onDeath(lane);
    }

    @Override
    public void refreshType(TowerType type, PlayerLane lane) {
        if (type == null || !type().id().equals(type.id())) {
            return;
        }
        if (isCoreTower() && transfers.rollbackIncomplete()) {
            refreshTransferStats(lane);
        }
        super.refreshType(type, lane);
        reconcileEvolutionState(lane);
    }

    @Override
    protected void refreshMaxHealthAfterTypeChange(PlayerLane lane) {
        if (!isCoreTower() || !state().hatched()) {
            super.refreshMaxHealthAfterTypeChange(lane);
            return;
        }
        Optional<SemionTowerEntity> entity = towerEntity(lane);
        if (entity.isPresent()) {
            entity.get().refreshMaxHealthEffects(false);
        } else {
            syncMaxHealth(effectBaseMaxHealth(), false);
        }
    }

    @Override
    public void tick(PlayerLane lane) {
        if (isDestroyed(lane)) {
            return;
        }
        if (waveActive && isCoreTower() && state().hatched()) {
            EndTransferController.TickResult result = transfers.tick(this, lane, (currentLane, source) -> EndVfx.transfer(currentLane, this, source));
            if (result.statsChanged()) {
                refreshTransferStats(lane);
            }
            reconcileEvolutionState(lane);
            healTransferredHealth(lane, result.completionHealing());
            if (result.countsChanged()) {
                towerEntity(lane).ifPresent(SemionTowerEntity::refreshCombatStats);
            }
            tickPeriodicHealing(lane, result.periodicHealingPerSecond());
        }
        super.tick(lane);
    }

    @Override
    public double effectBaseMaxHealth() {
        return isCoreTower() && state().hatched() ? previewHatchedMaxHealth() : super.effectBaseMaxHealth();
    }

    public double previewHatchedMaxHealth() {
        return type().maxHealth() + transfers.permanentHealthBonus() + transfers.roundHealthBonus();
    }

    public double previewHatchedAttackDamage() {
        return type().damage() + transfers.permanentDamageBonus() + transfers.roundDamageBonus();
    }

    public int previewHatchedAttackIntervalTicks() {
        return combat.attackInterval(type());
    }

    public double previewHatchedAttackRange() {
        return combat.attackRange(type(), state());
    }

    @Override
    public double adjustAttackRange(double baseRange) {
        EndTowerState state = state();
        if (isCoreTower() && state == EndTowerState.EGG) {
            return 0.0;
        }
        return baseRange + combat.attackRangeBonus() + combat.dragonRangeBonus(state);
    }

    @Override
    public int adjustAttackInterval(int baseIntervalTicks) {
        if (!isCoreTower() || !state().hatched()) {
            return baseIntervalTicks;
        }
        return combat.adjustAttackInterval(baseIntervalTicks);
    }

    @Override
    public double modifyAttackDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        return isCoreTower() && state().hatched()
                ? combat.modifyAttackDamage(type(), transfers.stats().totalDamageBonus(), damageAmount)
                : damageAmount;
    }

    @Override
    public double finalDamageBonus() {
        return combat.finalDamageBonus(state());
    }

    @Override
    public double modifyIncomingDamage(SemionTowerEntity towerEntity, DamageSource damageSource, double damageAmount) {
        if (EndTowers.isShulkerLine(type())) {
            return damageAmount * Math.max(0.0, 1.0 - combat.shulkerDamageReduction(type()));
        }
        if (!isCoreTower() || !state().hatched()) {
            return damageAmount;
        }
        return damageAmount * Math.max(0.0, 1.0 - combat.damageReduction());
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
        if (!isCoreTower() || !state().hatched() || towerEntity == null || target == null) {
            return;
        }
        combat.resolveAttack(
                this,
                towerEntity,
                target,
                attemptedDamage,
                resolvedOutgoingDamage,
                dealtDamage
        );
    }

    @Override
    public List<String> runtimeDetailLines() {
        return stats.create(this, waveActive);
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (!(previousTower instanceof EndTower endTower)) {
            return;
        }
        waveActive = endTower.waveActive;
        if (!isCoreTower()) {
            EndTransferController.clearProgress(this);
            return;
        }
        if (endTower.transfers.rollbackIncomplete()) {
            endTower.refreshTransferStats(null);
        }
        transfers.copyFrom(endTower.transfers);
        periodicHealingTicks = endTower.periodicHealingTicks;
    }

    public EndTransferStats transferStats() {
        return transfers.stats();
    }

    private void refreshTransferStats(PlayerLane lane) {
        Optional<SemionTowerEntity> entity = towerEntity(lane);
        if (entity.isPresent()) {entity.get().refreshMaxHealthEffects(false);}
        else {syncMaxHealth(effectBaseMaxHealth(), false);}
    }

    double transferProgress() {
        return EndTransferController.progress(this);
    }

    private void healTransferredHealth(PlayerLane lane, double amount) {
        if (amount <= 0.0) {return;}
        Optional<SemionTowerEntity> entity = towerEntity(lane);
        if (entity.isPresent()) {healTarget(entity.get(), amount);}
        else {
            double before = health();
            syncHealth(before + amount);
            recordHealingDone(health() - before);
        }
    }

    private void switchToPhantom(PlayerLane lane) {
        if (!isCoreTower() || state() != EndTowerState.EGG) {return;}
        setData(STATE, EndTowerState.PHANTOM);
        Optional<SemionTowerEntity> entity = towerEntity(lane);
        if (entity.isPresent()) {entity.get().refreshMaxHealthEffects();}
        else {syncMaxHealth(effectBaseMaxHealth(), true);}
        if (lane != null) {onStateChanged(lane);}
    }

    private void reconcileEvolutionState(PlayerLane lane) {
        if (!isCoreTower() || !state().hatched()) {return;}
        EndTowerState nextState = EndTowerState.evolvedState(currentMaxHealth(), combat.dragonEvolutionHealth());
        if (state() == nextState) {return;}
        setData(STATE, nextState);
        if (lane != null) {onStateChanged(lane);}
    }

    public double splashRadius() {
        return isCoreTower() ? combat.splashRadius(state()) : 0.0;
    }

    double resolvedSplashDamage(double resolvedOutgoingDamage) {
        return combat.resolvedSplashDamage(resolvedOutgoingDamage);
    }

    private void tickPeriodicHealing(PlayerLane lane, double transferHealingPerSecond) {
        double totalHealing = combat.regenerationPerSecond() + Math.max(0.0, transferHealingPerSecond);
        if (totalHealing <= 0.0) {periodicHealingTicks = 0;return;}
        int intervalTicks = combat.regenerationTicks();
        periodicHealingTicks++;
        if (periodicHealingTicks < intervalTicks) {return;}
        periodicHealingTicks %= intervalTicks;
        healTransferredHealth(lane, totalHealing);
    }

    private Optional<SemionTowerEntity> towerEntity(PlayerLane lane) {
        if (lane == null || lane.arenaWorld() == null || entityId().isEmpty()) {return Optional.empty();}
        return Optional.ofNullable(lane.arenaWorld().getEntity(entityId().getAsInt())).filter(SemionTowerEntity.class::isInstance).map(SemionTowerEntity.class::cast);
    }

    public boolean stopsBeforeFriendlyTowers() {
        return isCoreTower() && state() == EndTowerState.PHANTOM;
    }

    boolean isCoreTower() {
        return EndTowers.isBaseEndTower(type());
    }

    @Override
    protected double entityAnchorYOffset() {
        return isCoreTower() && state().hatched() ? 2.0 : 1.0;
    }

    private void initializeState() {
        if (isCoreTower()) {setData(STATE, EndTowerState.EGG);}
    }

    public double phantomScaleForMaxHealth(double maxHealth) {
        return combat.phantomScale(maxHealth);
    }

    private void clearTransferLifecycleState() {
        transfers.rollbackIncomplete();
        EndTransferController.clearProgress(this);
    }
}
