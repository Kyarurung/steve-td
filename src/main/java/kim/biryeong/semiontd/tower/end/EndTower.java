package kim.biryeong.semiontd.tower.end;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerDataKey;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import kim.biryeong.semiontd.tower.area.TowerAreaDamage;
import kim.biryeong.semiontd.entity.visual.EntityVisual;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

public final class EndTower extends EntityBackedTower {
    public static final String CONFIG_ID = EndTowers.CONFIG_ID;
    private static final double TRANSFER_PARTICLE_SOURCE_HEIGHT = 1.25;
    private static final double TRANSFER_PARTICLE_TARGET_HEIGHT = 3.0;
    private static final int TRANSFER_PARTICLE_INTERVAL_TICKS = 5;
    private static final List<String> SPLASH_THRESHOLD_KEYS = List.of(
            "endCrystalSplashThreshold1",
            "endCrystalSplashThreshold2",
            "endCrystalSplashThreshold3",
            "endCrystalSplashThreshold4"
    );
    private static final TowerDataKey<EndTowerState> STATE = TowerDataKey.of(
            ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "end_tower_state"),
            EndTowerState.class
    );
    private static final TowerDataKey<Double> TRANSFER_PROGRESS = TowerDataKey.of(
            ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "end_transfer_progress"),
            Double.class
    );

    private final EndAbsorptionState absorption = new EndAbsorptionState();
    private final EndBalanceProvider balance;
    private boolean waveActive;
    private double roundHealthBonus;
    private double roundDamageBonus;
    private double syncedPermanentHealthBonus;
    private double syncedPermanentDamageBonus;
    private int absorbedEndCrystalCount;
    private int absorbedShulkerCount;
    private int roundCompletedTransferCount;
    private int regenerationTicks;
    private int transferHealingTicks;

    public EndTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        this(type, ownerPlayer, teamId, laneId, position, EndBalanceProvider.RUNTIME);
    }

    EndTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition position,
            EndBalanceProvider balance
    ) {
        super(type, ownerPlayer, teamId, laneId, position);
        this.balance = Objects.requireNonNull(balance, "balance");
        initializeState();
    }

    public EndTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        this(
                type,
                ownerPlayer,
                teamId,
                laneId,
                originalPosition,
                currentPosition,
                EndBalanceProvider.RUNTIME
        );
    }

    EndTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition,
            EndBalanceProvider balance
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
        this.balance = Objects.requireNonNull(balance, "balance");
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
        regenerationTicks = 0;
        transferHealingTicks = 0;
        if (rollbackIncompleteTransfers()) {
            refreshAbsorbedStats(lane);
        }
        if (isEgg()) {
            switchToPhantom(lane);
        } else if (lane != null) {
            onStateChanged(lane);
        }
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        waveActive = false;
        regenerationTicks = 0;
        transferHealingTicks = 0;
        rollbackIncompleteTransfers();
        removeData(TRANSFER_PROGRESS);
        resetRoundTransferBonuses(lane);
        if (isCoreTower()) {
            setData(STATE, EndTowerState.EGG);
            syncMaxHealth(effectBaseMaxHealth(), false);
        }
        super.resetForRound(lane);
    }

    void resetRoundTransferBonuses(PlayerLane lane) {
        absorption.resetRoundContributions();
        roundCompletedTransferCount = 0;
        refreshAbsorbedStats(lane);
    }

    @Override
    public void onRemoved(PlayerLane lane) {
        rollbackIncompleteTransfers();
        removeData(TRANSFER_PROGRESS);
        super.onRemoved(lane);
    }

    @Override
    public void onDeath(PlayerLane lane) {
        rollbackIncompleteTransfers();
        removeData(TRANSFER_PROGRESS);
        super.onDeath(lane);
    }

    @Override
    public void refreshType(TowerType type, PlayerLane lane) {
        if (type == null || !type().id().equals(type.id())) {
            return;
        }
        if (isCoreTower() && rollbackIncompleteTransfers()) {
            refreshAbsorbedStats(lane);
        }
        super.refreshType(type, lane);
        reconcileEvolutionState(lane);
    }

    @Override
    protected void refreshMaxHealthAfterTypeChange(PlayerLane lane) {
        if (!isHatched()) {
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
        if (waveActive && isHatched()) {
            int transferringTowerCount = absorbAlliedEndTowers(lane);
            reconcileEvolutionState(lane);
            tickTransferHealing(lane, transferringTowerCount);
            tickRegeneration(lane);
        }
        super.tick(lane);
    }

    @Override
    public double effectBaseMaxHealth() {
        return isHatched() ? previewHatchedMaxHealth() : super.effectBaseMaxHealth();
    }

    public double previewHatchedMaxHealth() {
        return type().maxHealth() + absorption.permanentHealthBonus() + roundHealthBonus;
    }

    public double previewHatchedAttackDamage() {
        return type().damage() + absorption.permanentDamageBonus() + roundDamageBonus;
    }

    public int previewHatchedAttackIntervalTicks() {
        int minimumInterval = Math.max(1, globalInt("minimumAttackIntervalTicks"));
        return reducedAttackInterval(type().attackIntervalTicks(), minimumInterval);
    }

    public double previewHatchedAttackRange() {
        return type().range() + attackRangeBonus() + dragonAttackRangeBonus();
    }

    @Override
    public double adjustAttackRange(double baseRange) {
        if (isEgg()) {
            return 0.0;
        }
        return baseRange + attackRangeBonus() + dragonAttackRangeBonus();
    }

    @Override
    public int adjustAttackInterval(int baseIntervalTicks) {
        if (!isHatched()) {
            return baseIntervalTicks;
        }
        int minimumInterval = Math.max(1, globalInt("minimumAttackIntervalTicks"));
        return reducedAttackInterval(baseIntervalTicks, minimumInterval);
    }

    @Override
    public double modifyAttackDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        if (!isHatched() || type().damage() <= 0.0) {
            return damageAmount;
        }
        double absorbedDamage = damageAmount * (
                1.0 + (absorption.permanentDamageBonus() + roundDamageBonus) / type().damage()
        );
        return isDragon() ? absorbedDamage * (1.0 + Math.max(0.0, global("dragonDamageBonus"))) : absorbedDamage;
    }

    @Override
    public double modifyOutgoingDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        if (!isCoreTower()) {
            return damageAmount;
        }
        return Math.min(attackDamageCap(), Math.max(0.0, damageAmount)
        );
    }

    @Override
    public double finalDamageBonus() {
        return isDragon() ? Math.max(0.0, global("dragonFinalDamageBonus")) : 0.0;
    }

    @Override
    public double incomeDebuffResistance() {
        return isDragon()
                ? Math.clamp(global("dragonIncomeDebuffResistance"), 0.0, 1.0)
                : 0.0;
    }

    @Override
    public double modifyIncomingDamage(SemionTowerEntity towerEntity, DamageSource damageSource, double damageAmount) {
        if (EndTowers.isShulkerLine(type())) {
            return damageAmount * Math.max(0.0, 1.0 - shulkerDamageReduction());
        }
        if (!isHatched()) {
            return damageAmount;
        }
        return damageAmount * Math.max(0.0, 1.0 - damageReduction());
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
        if (!isHatched() || towerEntity == null || target == null) {
            return;
        }
        applySplashDamage(towerEntity, target, resolvedOutgoingDamage);
        heal(towerEntity, dealtDamage * lifeStealRatio());
    }

    @Override
    public List<String> runtimeDetailLines() {
        if (!isCoreTower()) {
            ArrayList<String> lines = new ArrayList<>();
            if (waveActive) {
                lines.add("힘 전달 진행률 " + percent(transferProgress()));
            } else {
                lines.add("엔더 드래곤에게 힘 전달 대기 중");
            }
            if (EndTowers.isShulkerLine(type()) && shulkerDamageReduction() > 0.0) {
                lines.add("받는 피해 감소 " + percent(shulkerDamageReduction()));
            }
            return lines;
        }

        ArrayList<String> lines = new ArrayList<>();
        lines.add(switch (state()) {
            case EGG -> "<white>상태: <#B77DE8>드래곤 알</#B77DE8></white>";
            case PHANTOM -> "<white>상태: <#B77DE8>아기 드래곤</#B77DE8></white>";
            case DRAGON -> "<white>상태: <#B77DE8>엔더 드래곤</#B77DE8></white>";
        });

        double maxHealth = isEgg() ? previewHatchedMaxHealth() : currentMaxHealth();
        double additionalHealth = permanentHealthBonus();
        double additionalAttackDamage = permanentDamageBonus();
        int attackIntervalReductionTicks = Math.max(
                0,
                type().attackIntervalTicks() - previewHatchedAttackIntervalTicks()
        );
        int maximumAttackIntervalReductionTicks = maximumAttackIntervalReduction();
        double maximumAttackRange = type().range()
                + Math.max(0.0, global("attackRangeCap"))
                + (isDragon() ? Math.max(0.0, global("dragonAttackRangeBonus")) : 0.0);
        double maximumAttackDamage = attackDamageCap();
        double maximumSplashRadius = Math.max(0.0, global("splashRadiusCap"));
        double maximumLifeSteal = Math.max(0.0, global("lifeStealCap"));
        double maximumDamageReduction = Math.max(0.0, global("damageReductionCap"));
        double maximumRegeneration = Math.max(0.0, global("regenerationCap"));
        double currentAttackRange = previewHatchedAttackRange();
        double currentSplashRadius = splashRadius();
        double currentLifeSteal = lifeStealRatio();
        double currentDamageReduction = damageReduction();
        double currentRegeneration = regenerationPerSecond();

        lines.add("<#B77DE8>엔더 드래곤</#B77DE8><white> 능력치</white>");
        lines.add("<white>엔드 수정, 셜커 스택: " + absorbedEndCrystalCount + " / " + absorbedShulkerCount + "</white>");
        lines.add("<#D94343>피해량 상한(최종 피해 제외): " + compactOneDecimal(maximumAttackDamage) + "</#D94343>");
        lines.add("<#D94343>추가 공격력: " + oneDecimal(additionalAttackDamage) + "</#D94343><white> / </white>" +
                "<#D9B94F>사거리: " + oneDecimal(currentAttackRange) + "블록 / " + oneDecimal(maximumAttackRange) + "블록</#D9B94F>");
        lines.add("<#D9B94F>공격 속도: -" + attackIntervalReductionTicks + "틱 / -" + maximumAttackIntervalReductionTicks + "틱</#D9B94F><white> / " +
                "</white><#D9B94F>공격 범위: " + Math.round(currentSplashRadius) + "블록 / " + Math.round(maximumSplashRadius) + "블록</#D9B94F>");
        lines.add("<#E66F6F>추가 체력: " + oneDecimal(additionalHealth) +
                "</#E66F6F><white> / </white><#79C97B>재생: " + Math.round(currentRegeneration) + " / " + Math.round(maximumRegeneration) + "/초</#79C97B>");
        lines.add("<#D94343>생명력 흡수: " + percentInteger(currentLifeSteal) + " / " + percentInteger(maximumLifeSteal) + "</#D94343><white> / " +
                "</white><#72A9E6>피해 감소: " + percentInteger(currentDamageReduction) + " / " + percentInteger(maximumDamageReduction) + "</#72A9E6>");
        if ((isEgg() || isDragon()) && maxHealth >= dragonEvolutionMaxHealth()) {
            lines.add("<#D94343>최종 피해: +" + percentInteger(Math.max(0.0, global("dragonFinalDamageBonus"))) + "</#D94343><white> / " +
                    "</white><#C892E3>저항: +" + percentInteger(Math.clamp(global("dragonIncomeDebuffResistance"), 0.0, 1.0)) + "</#C892E3>");
            lines.add("<#D9B94F>추가 사거리: +" + oneDecimal(Math.max(0.0, global("dragonAttackRangeBonus"))) + "블록</#D9B94F><white> / " +
                    "</white><#D94343>공격력 증가율: +" + percentInteger(Math.max(0.0, global("dragonDamageBonus"))) + "</#D94343>");
        }
        return lines;
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (!(previousTower instanceof EndTower endTower)) {
            return;
        }
        waveActive = endTower.waveActive;
        if (!isCoreTower()) {
            removeData(TRANSFER_PROGRESS);
            return;
        }
        if (endTower.rollbackIncompleteTransfers()) {
            endTower.refreshAbsorbedStats(null);
        }
        absorption.copyBonusesFrom(endTower.absorption);
        roundHealthBonus = endTower.roundHealthBonus;
        roundDamageBonus = endTower.roundDamageBonus;
        syncedPermanentHealthBonus = endTower.syncedPermanentHealthBonus;
        syncedPermanentDamageBonus = endTower.syncedPermanentDamageBonus;
        absorbedEndCrystalCount = endTower.absorbedEndCrystalCount;
        absorbedShulkerCount = endTower.absorbedShulkerCount;
        roundCompletedTransferCount = endTower.roundCompletedTransferCount;
        regenerationTicks = endTower.regenerationTicks;
        transferHealingTicks = endTower.transferHealingTicks;
    }

    public int absorbedEndCrystalCount() {
        return absorbedEndCrystalCount;
    }

    public int absorbedShulkerCount() {
        return absorbedShulkerCount;
    }

    public int roundCompletedTransferCount() {
        return roundCompletedTransferCount;
    }

    public double absorbedHealthBonus() {
        return absorption.permanentHealthBonus() + roundHealthBonus;
    }

    public double absorbedDamageBonus() {
        return absorption.permanentDamageBonus() + roundDamageBonus;
    }

    public double permanentHealthBonus() {
        return absorption.permanentHealthBonus();
    }

    public double permanentDamageBonus() {
        return absorption.permanentDamageBonus();
    }

    public double roundHealthBonus() {
        return roundHealthBonus;
    }

    public double roundDamageBonus() {
        return roundDamageBonus;
    }

    private int absorbAlliedEndTowers(PlayerLane lane) {
        if (lane == null) {
            return 0;
        }
        absorption.beginSnapshot();
        for (Tower tower : lane.towers()) {
            absorption.markPresent(tower);
            if (isEligibleAbsorptionTarget(tower)) {
                absorption.progressFor(tower, this::newAbsorptionProgress);
            }
        }

        double absorptionHealing = 0.0;
        int transferringTowerCount = 0;
        boolean countsChanged = false;
        var progressIterator = absorption.progressEntries().iterator();
        while (progressIterator.hasNext()) {
            Map.Entry<Tower, EndAbsorptionState.Progress> entry = progressIterator.next();
            Tower source = entry.getKey();
            EndAbsorptionState.Progress progress = entry.getValue();
            if (!absorption.isPresent(source) || source.health() <= 0.0) {
                progressIterator.remove();
                source.removeData(TRANSFER_PROGRESS);
                rollbackTransferProgress(progress);
                continue;
            }
            progress.elapsedTicks++;
            absorption.apply(progress);
            source.setData(TRANSFER_PROGRESS, progress.appliedRatio);
            if (progress.elapsedTicks < progress.durationTicks) {
                transferringTowerCount++;
                if (shouldShowTransferParticles(source, progress.elapsedTicks)) {
                    showTransferParticles(lane, source);
                }
                continue;
            }

            progressIterator.remove();
            source.removeData(TRANSFER_PROGRESS);
            if (!absorption.isPresent(source)
                    || source.health() <= 0.0
                    || !lane.killTower(source)) {
                rollbackTransferProgress(progress);
                continue;
            }
            absorptionHealing += Math.max(0.0, global("absorptionHealAmount"));
            int tier = EndTowers.absorptionTier(source.type());
            roundCompletedTransferCount = saturatedAdd(roundCompletedTransferCount, 1);
            if (EndTowers.isEndCrystalLine(source.type())) {
                absorbedEndCrystalCount = saturatedAdd(absorbedEndCrystalCount, tier);
            } else {
                absorbedShulkerCount = saturatedAdd(absorbedShulkerCount, tier);
            }
            countsChanged = true;
        }
        refreshAbsorbedStats(lane);
        healTransferredHealth(lane, absorptionHealing);
        if (countsChanged) {
            towerEntity(lane).ifPresent(SemionTowerEntity::refreshCombatStats);
        }
        return transferringTowerCount;
    }

    private boolean refreshAbsorbedStats(PlayerLane lane) {
        double nextRoundHealthBonus = Math.max(0.0, absorption.roundHealthContribution());
        double nextRoundDamageBonus = Math.max(0.0, absorption.roundDamageContribution());
        if (Math.abs(nextRoundHealthBonus - roundHealthBonus) < 1.0E-9
                && Math.abs(nextRoundDamageBonus - roundDamageBonus) < 1.0E-9
                && Math.abs(absorption.permanentHealthBonus() - syncedPermanentHealthBonus) < 1.0E-9
                && Math.abs(absorption.permanentDamageBonus() - syncedPermanentDamageBonus) < 1.0E-9) {
            return false;
        }
        roundHealthBonus = nextRoundHealthBonus;
        roundDamageBonus = nextRoundDamageBonus;
        syncedPermanentHealthBonus = absorption.permanentHealthBonus();
        syncedPermanentDamageBonus = absorption.permanentDamageBonus();
        Optional<SemionTowerEntity> entity = towerEntity(lane);
        if (entity.isPresent()) {
            entity.get().refreshMaxHealthEffects(false);
        } else {
            syncMaxHealth(effectBaseMaxHealth(), false);
        }
        return true;
    }

    private boolean isEligibleAbsorptionTarget(Tower tower) {
        return tower != null
                && tower != this
                && Objects.equals(tower.ownerPlayer(), ownerPlayer())
                && tower.health() > 0.0
                && EndTowers.isAbsorbableTower(tower.type());
    }

    private EndAbsorptionState.Progress newAbsorptionProgress(Tower tower) {
        int durationTicks = Math.max(1, globalTicks("absorptionDurationTicks"));
        boolean endCrystalLine = EndTowers.isEndCrystalLine(tower.type());
        boolean shulkerLine = EndTowers.isShulkerLine(tower.type());
        // Transfer configured base stats, not temporary effects, to prevent
        // recursive buff multiplication and make the snapshot deterministic.
        double sourceConfiguredMaxHealth = tower.type().maxHealth();
        double sourceConfiguredAttackDamage = tower.type().damage();
        tower.setData(TRANSFER_PROGRESS, 0.0);
        return new EndAbsorptionState.Progress(
                durationTicks,
                shulkerLine
                        ? sourceConfiguredMaxHealth * Math.max(0.0, global("roundHealthRatio"))
                        : 0.0,
                endCrystalLine
                        ? sourceConfiguredAttackDamage * Math.max(0.0, global("roundDamageRatio"))
                        : 0.0,
                shulkerLine
                        ? sourceConfiguredMaxHealth * Math.max(0.0, global("permanentHealthRatio"))
                        : 0.0,
                endCrystalLine
                        ? sourceConfiguredAttackDamage * Math.max(0.0, global("permanentDamageRatio"))
                        : 0.0
        );
    }

    double transferProgress() {
        return Math.clamp(getDataOrDefault(TRANSFER_PROGRESS, 0.0), 0.0, 1.0);
    }

    private boolean rollbackIncompleteTransfers() {
        boolean changed = false;
        for (Map.Entry<Tower, EndAbsorptionState.Progress> entry : absorption.progressEntries()) {
            Tower source = entry.getKey();
            source.removeData(TRANSFER_PROGRESS);
            changed |= rollbackTransferProgress(entry.getValue());
        }
        absorption.clearProgress();
        return changed;
    }

    private boolean rollbackTransferProgress(EndAbsorptionState.Progress progress) {
        return absorption.rollback(progress);
    }

    private void healTransferredHealth(PlayerLane lane, double amount) {
        if (amount <= 0.0) {
            return;
        }
        Optional<SemionTowerEntity> entity = towerEntity(lane);
        if (entity.isPresent()) {
            entity.get().receiveHealing(amount);
        } else {
            syncHealth(health() + amount);
        }
    }

    private void tickTransferHealing(PlayerLane lane, int transferringTowerCount) {
        if (transferringTowerCount <= 0) {
            transferHealingTicks = 0;
            return;
        }
        int intervalTicks = Math.max(1, globalTicks("transferHealingIntervalTicks"));
        transferHealingTicks++;
        if (transferHealingTicks < intervalTicks) {
            return;
        }
        transferHealingTicks %= intervalTicks;
        double healingPerTower = Math.max(0.0, global("transferHealingPerTower"));
        healTransferredHealth(lane, transferringTowerCount * healingPerTower);
    }

    private void switchToPhantom(PlayerLane lane) {
        if (!isEgg()) {
            return;
        }
        setData(STATE, EndTowerState.PHANTOM);
        Optional<SemionTowerEntity> entity = towerEntity(lane);
        if (entity.isPresent()) {
            entity.get().refreshMaxHealthEffects();
        } else {
            syncMaxHealth(effectBaseMaxHealth(), true);
        }
        if (lane != null) {
            onStateChanged(lane);
        }
    }

    private void reconcileEvolutionState(PlayerLane lane) {
        if (!isHatched()) {
            return;
        }
        EndTowerState nextState = EndTowerState.evolvedState(
                currentMaxHealth(),
                dragonEvolutionMaxHealth()
        );
        if (state() == nextState) {
            return;
        }
        setData(STATE, nextState);
        if (lane != null) {
            onStateChanged(lane);
        }
    }

    private static boolean shouldShowTransferParticles(Tower source, int elapsedTicks) {
        return Math.floorMod(elapsedTicks + System.identityHashCode(source), TRANSFER_PARTICLE_INTERVAL_TICKS) == 0;
    }

    private void showTransferParticles(PlayerLane lane, Tower source) {
        ServerLevel level = lane.arenaWorld();
        if (level == null) {
            return;
        }
        Vec3 targetPosition = transferParticlePosition(this, TRANSFER_PARTICLE_TARGET_HEIGHT);
        Vec3 sourceOffset = transferParticlePosition(source, TRANSFER_PARTICLE_SOURCE_HEIGHT).subtract(targetPosition);
        level.sendParticles(
                ParticleTypes.ENCHANT,
                targetPosition.x,
                targetPosition.y,
                targetPosition.z,
                0,
                sourceOffset.x,
                sourceOffset.y,
                sourceOffset.z,
                1.0
        );
    }

    private static Vec3 transferParticlePosition(Tower tower, double height) {
        return new Vec3(
                tower.position().x() + 0.5,
                tower.position().y() + height,
                tower.position().z() + 0.5
        );
    }

    public double splashRadius() {
        int unlockedSteps = 0;
        for (String thresholdKey : SPLASH_THRESHOLD_KEYS) {
            int threshold = Math.max(1, globalInt(thresholdKey));
            if (absorbedEndCrystalCount >= threshold) {
                unlockedSteps++;
            }
        }
        double radiusPerThreshold = Math.max(0.0, global("splashRadiusPerThreshold"));
        return Math.min(Math.max(0.0, global("splashRadiusCap")), unlockedSteps * radiusPerThreshold);
    }

    private void applySplashDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        double radius = splashRadius();
        double splashDamageAmount = resolvedSplashDamage(damageAmount);
        if (radius <= 0.0 || splashDamageAmount <= 0.0) {
            return;
        }
        ResourceLocation vfxStyle = isDragon() ? AreaVfxStyles.DRAGON_BREATH : AreaVfxStyles.SPLASH;
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTarget(AreaEffectIds.tower(this, "splash"), towerEntity, target, radius, AreaVfxSpec.onTrigger(vfxStyle));
        TowerAreaDamage.applyResolved(
                this,
                towerEntity,
                request,
                ignored -> splashDamageAmount,
                true,
                (ignored, dealtSplashDamage, killed) -> heal(
                        towerEntity,
                        dealtSplashDamage * lifeStealRatio()
                )
        );
    }

    double resolvedSplashDamage(double resolvedOutgoingDamage) {
        if (!Double.isFinite(resolvedOutgoingDamage) || resolvedOutgoingDamage <= 0.0) {
            return 0.0;
        }
        return resolvedOutgoingDamage * Math.max(0.0, global("splashDamageRatio"));
    }

    private int attackIntervalReduction() {
        int every = Math.max(1, globalInt("endCrystalAttackIntervalEvery"));
        long permanentReduction = (absorbedEndCrystalCount / (long) every)
                * Math.max(0, globalInt("attackIntervalReductionPerStep"));
        return (int) Math.min(
                Math.max(0, globalInt("maxAttackIntervalReductionTicks")),
                permanentReduction
        );
    }

    private int maximumAttackIntervalReduction() {
        int minimumInterval = Math.max(1, globalInt("minimumAttackIntervalTicks"));
        int availableReduction = Math.max(0, type().attackIntervalTicks() - minimumInterval);
        return Math.min(
                availableReduction,
                Math.max(0, globalInt("maxAttackIntervalReductionTicks"))
        );
    }

    public double attackRangeBonus() {
        return cappedStackBonus(
                absorbedEndCrystalCount,
                "endCrystalAttackRangeEvery",
                "attackRangePerStep",
                "attackRangeCap"
        );
    }

    private double dragonAttackRangeBonus() {
        return isDragon() ? Math.max(0.0, global("dragonAttackRangeBonus")) : 0.0;
    }

    private int roundAttackIntervalReduction() {
        int every = Math.max(1, globalInt("roundAbsorptionAttackIntervalEvery"));
        int reductionPerStep = Math.max(0, globalInt("roundAbsorptionAttackIntervalReductionTicks"));
        long reduction = (roundCompletedTransferCount / (long) every) * reductionPerStep;
        return (int) Math.min(Integer.MAX_VALUE, reduction);
    }

    private int reducedAttackInterval(int baseIntervalTicks, int minimumInterval) {
        long totalReduction = (long) attackIntervalReduction() + roundAttackIntervalReduction();
        long reduced = (long) baseIntervalTicks - totalReduction;
        return (int) Math.max(minimumInterval, reduced);
    }

    private double lifeStealRatio() {
        return cappedStackBonus(
                absorbedShulkerCount,
                "shulkerLifeStealEvery",
                "lifeStealPerStep",
                "lifeStealCap"
        );
    }

    public double regenerationPerSecond() {
        return cappedStackBonus(
                absorbedShulkerCount,
                "shulkerRegenerationEvery",
                "regenerationPerStep",
                "regenerationCap"
        );
    }

    private void tickRegeneration(PlayerLane lane) {
        double regeneration = regenerationPerSecond();
        if (regeneration <= 0.0) {
            regenerationTicks = 0;
            return;
        }
        int intervalTicks = Math.max(1, globalTicks("regenerationIntervalTicks"));
        regenerationTicks++;
        if (regenerationTicks < intervalTicks) {
            return;
        }
        regenerationTicks %= intervalTicks;
        healTransferredHealth(lane, regeneration);
    }

    private double damageReduction() {
        return cappedStackBonus(
                absorbedShulkerCount,
                "shulkerReductionEvery",
                "damageReductionPerStep",
                "damageReductionCap"
        );
    }

    private double cappedStackBonus(int stackCount, String everyKey, String perStepKey, String capKey) {
        int every = Math.max(1, globalInt(everyKey));
        double value = stackCount / every * Math.max(0.0, global(perStepKey));
        return Math.min(Math.max(0.0, global(capKey)), value);
    }

    private double shulkerDamageReduction() {
        return Math.max(
                0.0,
                Math.min(1.0, balance.ability(type().id(), "damageReduction"))
        );
    }

    private double attackDamageCap() {
        return Math.max(0.0, global("attackDamageCap"));
    }

    private static String compactOneDecimal(double value) {
        String formatted = oneDecimal(value);
        return formatted.endsWith(".0") ? formatted.substring(0, formatted.length() - 2) : formatted;
    }

    private void heal(SemionTowerEntity towerEntity, double amount) {
        if (amount > 0.0) {
            towerEntity.receiveHealing(amount);
        }
    }

    private Optional<SemionTowerEntity> towerEntity(PlayerLane lane) {
        if (lane == null || lane.arenaWorld() == null || entityId().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(lane.arenaWorld().getEntity(entityId().getAsInt()))
                .filter(SemionTowerEntity.class::isInstance)
                .map(SemionTowerEntity.class::cast);
    }

    private boolean isDragon() {
        return isCoreTower() && state() == EndTowerState.DRAGON;
    }

    private boolean isHatched() {
        if (!isCoreTower()) {
            return false;
        }
        return state().hatched();
    }

    private boolean isEgg() {
        return isCoreTower() && state() == EndTowerState.EGG;
    }

    private boolean isCoreTower() {
        return EndTowers.isBaseEndTower(type());
    }

    @Override
    protected double entityAnchorYOffset() {
        return isHatched() ? 2.0 : 1.0;
    }

    private void initializeState() {
        if (isCoreTower()) {
            setData(STATE, EndTowerState.EGG);
        }
    }

    private double global(String key) {
        return balance.ability(key);
    }

    public double phantomScaleForMaxHealth(double maxHealth) {
        double baseScale = Math.max(0.0, global("phantomBaseScale"));
        double healthInterval = global("phantomScaleHealthInterval");
        double scalePerInterval = Math.max(0.0, global("phantomScalePerInterval"));
        double scaleCap = Math.max(0.0, global("phantomScaleCap"));
        double resolvedMaxHealth = Double.isFinite(maxHealth)
                ? Math.max(0.0, maxHealth)
                : 0.0;
        double growth = healthInterval > 0.0
                ? resolvedMaxHealth / healthInterval * scalePerInterval
                : 0.0;
        return Math.min(scaleCap, baseScale + growth);
    }

    private int globalInt(String key) {
        return balance.abilityInt(key);
    }

    private int globalTicks(String key) {
        return balance.abilityTicks(key);
    }

    private double dragonEvolutionMaxHealth() {
        return Math.max(0.0, global("dragonEvolutionMaxHealth"));
    }

    private static int saturatedAdd(int value, int increment) {
        if (increment <= 0 || value >= Integer.MAX_VALUE) {
            return value;
        }
        return value > Integer.MAX_VALUE - increment
                ? Integer.MAX_VALUE
                : value + increment;
    }

}
