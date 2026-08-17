package kim.biryeong.semiontd.tower.gamble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import kim.biryeong.semiontd.api.SemionTdApi;
import kim.biryeong.semiontd.api.area.AreaEffectOutcome;
import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.api.area.TowerAreaEffectRequest;
import kim.biryeong.semiontd.api.area.TowerAreaTargetMode;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.ProductionTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public final class GambleSupportTower extends ProductionTower {
    private final int[] lastRollCounts = new int[6];
    private final List<Tower> linkedTargets = new ArrayList<>();
    private int lastFace;
    private int affectedTargets;
    private boolean waveActive;
    private int rangeVfxTicks;

    public GambleSupportTower(
            TowerType type, UUID ownerPlayer, TeamId teamId, int laneId,
            GridPosition originalPosition, GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    @Override
    public boolean canChaseTargets() {
        return false;
    }

    /**
     * The configured range is a support radius, not a combat range. Keeping the
     * entity attack range at zero also prevents attack animations and zero-damage hits.
     */
    @Override
    public double adjustAttackRange(double baseRange) {
        return 0.0;
    }

    @Override
    public void onPlaced(PlayerLane lane) {
        super.onPlaced(lane);
        GambleRoundEffects.towerEntity(this, lane).ifPresent(this::showRange);
        rangeVfxTicks = GambleBalance.supportVfxIntervalTicks();
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        waveActive = false;
        linkedTargets.clear();
        lastFace = 0;
        super.resetForRound(lane);
    }

    @Override
    public void tick(PlayerLane lane) {
        super.tick(lane);
        GambleRollLabels.sync(lane, ownerPlayer(), this);
        if (isDestroyed(lane) || --rangeVfxTicks > 0) {
            return;
        }
        GambleRoundEffects.towerEntity(this, lane).ifPresent(source -> {
            if (waveActive) {
                showPersistentVfx(source, lane);
            } else {
                showRange(source);
            }
        });
        rangeVfxTicks = GambleBalance.supportVfxIntervalTicks();
    }

    @Override
    public void onWaveStarted(PlayerLane lane, int currentRound) {
        waveActive = true;
        Arrays.fill(lastRollCounts, 0);
        linkedTargets.clear();
        lastFace = 0;
        affectedTargets = 0;
        SemionTowerEntity source = GambleRoundEffects.towerEntity(this, lane).orElse(null);
        if (source == null || isDestroyed(lane)) {
            return;
        }

        ResourceLocation sourceId = GambleRoundEffects.sourceId(this);
        GambleRoundEffects.rememberSource(lane, ownerPlayer(), sourceId);
        GambleRoundEffects.clearSource(lane, ownerPlayer(), sourceId);
        int minimum = GambleBalance.minimumRoll(type());
        double positiveMultiplier = GambleBalance.positiveMultiplier(type());
        int face = minimum + source.getRandom().nextInt(7 - minimum);
        TimedEffectType effect = GambleBalance.supportEffectType(face);
        double magnitude = GambleBalance.supportMagnitude(face, positiveMultiplier);
        lastFace = face;
        lastRollCounts[face - 1] = 1;
        GambleRollLabels.show(lane, ownerPlayer(), this, sourceId, face);
        List<Vec3> positiveHits = new ArrayList<>();
        List<Vec3> negativeHits = new ArrayList<>();
        TowerAreaEffectRequest request = TowerAreaEffectRequest.aroundTower(
                AreaEffectIds.tower(this, "round_roll"), source, type().range(),
                TowerAreaTargetMode.REGISTERED, AreaVfxSpec.none()
        ).withFilter(target -> target.tower() != this
                && ownerPlayer().equals(target.tower().ownerPlayer())
                && acceptsTarget(target.tower())
                && target.entity().isPresent());

        SemionTdApi.areaEffects().applyToTowers(request, target -> {
            boolean changed = target.entity().orElseThrow().setPersistentEffect(effect, sourceId, magnitude);
            linkedTargets.add(target.tower());
            affectedTargets++;
            Vec3 hit = target.entity().orElseThrow().position().add(0.0, 0.7, 0.0);
            (face <= 2 ? negativeHits : positiveHits).add(hit);
            lane.arenaWorld().sendParticles(
                    face <= 2 ? ParticleTypes.WITCH : ParticleTypes.HAPPY_VILLAGER,
                    hit.x, hit.y, hit.z, Math.max(1, face), 0.18, 0.24, 0.18, 0.02
            );
            return changed ? AreaEffectOutcome.APPLIED : AreaEffectOutcome.UNCHANGED;
        });

        showRollVfx(source, positiveHits, negativeHits);
        if (positiveHits.isEmpty() && negativeHits.isEmpty()) {
            showRange(source);
        }
        rangeVfxTicks = GambleBalance.supportVfxIntervalTicks();
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (previousTower instanceof GambleSupportTower previous) {
            System.arraycopy(previous.lastRollCounts, 0, lastRollCounts, 0, lastRollCounts.length);
            linkedTargets.addAll(previous.linkedTargets);
            lastFace = previous.lastFace;
            affectedTargets = previous.affectedTargets;
            waveActive = previous.waveActive;
            rangeVfxTicks = previous.rangeVfxTicks;
        }
    }

    @Override
    public List<String> runtimeDetailLines() {
        return List.of(
                "이번 라운드 대상: " + affectedTargets + "기",
                "이번 라운드 눈: " + (lastFace == 0 ? "아직 굴리지 않음" : Integer.toString(lastFace)),
                "지원 범위: " + oneDecimal(type().range()) + "칸"
        );
    }

    int[] lastRollCounts() {
        return lastRollCounts.clone();
    }

    int affectedTargets() {
        return affectedTargets;
    }

    int linkedTargets() {
        return linkedTargets.size();
    }

    private boolean acceptsTarget(Tower target) {
        return !GambleTowers.isSpectator(type())
                || target instanceof GamblerTower
                || target.type().id().equals(GambleTowers.GAMBLER.id());
    }

    private void showPersistentVfx(SemionTowerEntity source, PlayerLane lane) {
        List<Vec3> positiveHits = new ArrayList<>();
        List<Vec3> negativeHits = new ArrayList<>();
        linkedTargets.forEach(target -> GambleRoundEffects.towerEntity(target, lane).ifPresent(entity -> {
            Vec3 hit = entity.position().add(0.0, 0.7, 0.0);
            (lastFace <= 2 ? negativeHits : positiveHits).add(hit);
        }));
        if (positiveHits.isEmpty() && negativeHits.isEmpty()) {
            showRange(source);
            return;
        }
        showConnectionVfx(source, positiveHits, negativeHits);
    }

    private void showRollVfx(SemionTowerEntity source, List<Vec3> positiveHits, List<Vec3> negativeHits) {
        showConnectionVfx(source, positiveHits, negativeHits);
        int faceParticles = IntStream.range(0, lastRollCounts.length)
                .map(index -> (index + 1) * lastRollCounts[index]).sum();
        if (source.level() instanceof net.minecraft.server.level.ServerLevel level) {
            level.sendParticles(ParticleTypes.END_ROD, source.getX(), source.getY() + 1.1, source.getZ(),
                    Math.min(36, faceParticles), 0.25, 0.25, 0.25, 0.01);
        }
    }

    private void showConnectionVfx(
            SemionTowerEntity source, List<Vec3> positiveHits, List<Vec3> negativeHits
    ) {
        if (!positiveHits.isEmpty()) {
            TowerVfxService.showAreaEffect(source, AreaEffectIds.tower(this, "positive_rolls"),
                    AreaVfxStyles.BUFF, source.position(), type().range(), positiveHits,
                    affectedTargets, positiveHits.size(), 0);
        }
        if (!negativeHits.isEmpty()) {
            TowerVfxService.showAreaEffect(source, AreaEffectIds.tower(this, "negative_rolls"),
                    AreaVfxStyles.DEBUFF, source.position(), type().range(), negativeHits,
                    affectedTargets, negativeHits.size(), 0);
        }
    }

    private void showRange(SemionTowerEntity source) {
        TowerVfxService.showAreaEffect(source, AreaEffectIds.tower(this, "support_range"),
                AreaVfxStyles.BUFF, source.position(), type().range(), List.of(), 0, 0, 0);
    }

}
