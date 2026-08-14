package kim.biryeong.semiontd.tower.insect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.visual.EntityVisual;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.ProductionTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.TowerUpgradeOption;
import net.minecraft.world.damagesource.DamageSource;

public final class InsectUnitTower extends ProductionTower {
    private int deathsThisRound;
    private int reviveTicksRemaining = -1;
    private GridPosition revivePosition;
    private boolean freshPowerActive;
    private boolean waveActive;
    private boolean permanentDeath;

    public InsectUnitTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    @Override
    public void onWaveStarted(PlayerLane lane, int currentRound) {
        waveActive = true;
        permanentDeath = false;
        freshPowerActive = InsectTowers.tier(type()) == 1 && placedRound() == currentRound;
        syncMaxHealth(type().maxHealth() * (freshPowerActive ? InsectBalance.freshPowerMultiplier() : 1.0), true);
        syncHealth(currentMaxHealth());
        onStateChanged(lane);
    }

    @Override
    public EntityVisual visual() {
        EntityVisual visual = super.visual();
        return freshPowerActive ? visual.withScale(visual.scale() * InsectBalance.freshPowerScale()) : visual;
    }

    @Override
    public boolean meetsUpgradeRequirements(PlayerLane lane, TowerUpgradeOption option) {
        return waveStartedAfterPlacement();
    }

    @Override
    public double modifyAttackDamage(
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        return freshPowerActive ? damageAmount * InsectBalance.freshPowerMultiplier() : damageAmount;
    }

    @Override
    public double modifyIncomingDamage(
            SemionTowerEntity towerEntity,
            DamageSource damageSource,
            double damageAmount
    ) {
        double reduction = InsectTowers.line(type()) == InsectTowers.UnitLine.SPIDER
                ? InsectBalance.spiderDamageReduction(InsectTowers.tier(type()))
                : 0.0;
        return damageAmount * (1.0 - reduction)
                * (1.0 + deathsThisRound * InsectBalance.deathDamageTakenPerStack());
    }

    @Override
    public boolean isDestroyed(PlayerLane lane) {
        if (permanentDeath) {
            return true;
        }
        if (reviveTicksRemaining >= 0) {
            if (!hasLivingSpawner(lane, revivePosition)) {
                cancelRevival();
                permanentDeath = true;
                return true;
            }
            return false;
        }
        if (!super.isDestroyed(lane)) {
            return false;
        }
        GridPosition deathPosition = position();
        if (!waveActive || !hasLivingSpawner(lane, deathPosition)) {
            permanentDeath = true;
            return true;
        }
        deathsThisRound++;
        revivePosition = deathPosition;
        reviveTicksRemaining = InsectBalance.reviveBaseTicks()
                + (deathsThisRound - 1) * InsectBalance.reviveIncrementTicks();
        return false;
    }

    @Override
    public void tick(PlayerLane lane) {
        if (reviveTicksRemaining >= 0) {
            if (!hasLivingSpawner(lane, revivePosition)) {
                cancelRevival();
                permanentDeath = true;
                return;
            }
            if (reviveTicksRemaining > 0) {
                reviveTicksRemaining--;
                if (reviveTicksRemaining > 0) {
                    return;
                }
            }
            revive(lane);
            return;
        }
        super.tick(lane);
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        waveActive = false;
        freshPowerActive = false;
        deathsThisRound = 0;
        permanentDeath = false;
        cancelRevival();
        syncMaxHealth(type().maxHealth(), false);
        super.resetForRound(lane);
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (!(previousTower instanceof InsectUnitTower previous)) {
            return;
        }
        deathsThisRound = previous.deathsThisRound;
        reviveTicksRemaining = previous.reviveTicksRemaining;
        revivePosition = previous.revivePosition;
        freshPowerActive = previous.freshPowerActive;
        waveActive = previous.waveActive;
        permanentDeath = previous.permanentDeath;
    }

    @Override
    public List<String> runtimeDetailLines() {
        ArrayList<String> lines = new ArrayList<>();
        String freshStatus = freshPowerActive
                ? "<green>활성</green>"
                : freshPowerPending() ? "<yellow>첫 웨이브 대기</yellow>" : "<gray>종료</gray>";
        lines.add("<gold>첫 배치 강화</gold> " + freshStatus);
        lines.add("<red>이번 라운드 사망</red> <white>" + deathsThisRound + "회</white>");
        lines.add("<red>받는 피해 증가</red> <white>"
                + percent(deathsThisRound * InsectBalance.deathDamageTakenPerStack()) + "</white>");
        if (reviveTicksRemaining >= 0) {
            lines.add("<green>부활 대기</green> <white>" + oneDecimal(reviveTicksRemaining / 20.0) + "초</white>");
        }
        lines.add("<light_purple>스포너 연결</light_purple> "
                + (hasLivingSpawner(lastLaneForDetails, reviveTicksRemaining >= 0 ? revivePosition : position())
                ? "<green>연결됨</green>" : "<red>없음</red>"));
        return List.copyOf(lines);
    }

    private transient PlayerLane lastLaneForDetails;

    @Override
    public void onPlaced(PlayerLane lane) {
        lastLaneForDetails = lane;
        if (!waveActive && InsectTowers.tier(type()) == 1 && !waveStartedAfterPlacement()) {
            freshPowerActive = true;
            syncMaxHealth(type().maxHealth() * InsectBalance.freshPowerMultiplier(), true);
            syncHealth(currentMaxHealth());
        }
        super.onPlaced(lane);
    }

    @Override
    public void onRemoved(PlayerLane lane) {
        super.onRemoved(lane);
        if (lane != null) {
            lastLaneForDetails = lane;
        }
    }

    int deathsThisRound() {
        return deathsThisRound;
    }

    int reviveTicksRemaining() {
        return reviveTicksRemaining;
    }

    boolean freshPowerActive() {
        return freshPowerActive;
    }

    boolean freshPowerPending() {
        return !freshPowerActive && InsectTowers.tier(type()) == 1 && !waveStartedAfterPlacement();
    }

    private void revive(PlayerLane lane) {
        GridPosition destination = revivePosition;
        onRemoved(lane);
        syncPosition(destination);
        syncHealth(currentMaxHealth());
        reviveTicksRemaining = -1;
        revivePosition = null;
        onPlaced(lane);
    }

    private void cancelRevival() {
        reviveTicksRemaining = -1;
        revivePosition = null;
    }

    private boolean hasLivingSpawner(PlayerLane lane, GridPosition center) {
        if (lane == null || center == null) {
            return false;
        }
        double radiusSquared = InsectBalance.spawnerRadius() * InsectBalance.spawnerRadius();
        return lane.towers().stream()
                .filter(tower -> ownerPlayer().equals(tower.ownerPlayer()))
                .filter(tower -> InsectTowers.isSpawner(tower.type()))
                .filter(tower -> !tower.isDestroyed(lane))
                .anyMatch(tower -> distanceSquared(center, tower.position()) <= radiusSquared);
    }

    private static double distanceSquared(GridPosition first, GridPosition second) {
        double x = first.x() - second.x();
        double y = first.y() - second.y();
        double z = first.z() - second.z();
        return x * x + y * y + z * z;
    }
}
