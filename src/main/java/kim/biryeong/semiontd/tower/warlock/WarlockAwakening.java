package kim.biryeong.semiontd.tower.warlock;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;
import kim.biryeong.semiontd.game.PlayerLane;

public final class WarlockAwakening {
    private static final Map<UUID, Long> KILLS = new ConcurrentHashMap<>();

    private final WarlockConfig config;
    private final WarlockState state;
    private final WarlockPath path;
    private final UUID ownerPlayer;
    private int regenerationTicks;
    private int vfxTicks;

    WarlockAwakening(WarlockConfig config, WarlockState state, WarlockPath path, UUID ownerPlayer) {
        this.config = config;
        this.state = state;
        this.path = path;
        this.ownerPlayer = ownerPlayer;
    }

    boolean tryActivate(WarlockTower tower, PlayerLane lane, SemionTowerEntity towerEntity) {
        if (towerEntity == null || !path.specialized() || state.awakenedThisRound()) {
            return false;
        }
        Snapshot progress = snapshot(ownerPlayer);
        WarlockConfig.AwakeningRule rule = config.awakening(path);
        tower.syncFromEntityHealth(towerEntity.getHealth());
        if (!meetsActivationConditions(
                progress.unlocked(),
                tower.currentHealthRatio(),
                rule.healthThreshold(),
                tower.isLastSurvivingTower(lane)
        ) || !state.awaken()) {
            return false;
        }

        regenerationTicks = 0;
        vfxTicks = 0;
        towerEntity.setGlowingTag(true);
        TowerVfxService.showWarlockAwakening(towerEntity);
        tower.heal(towerEntity, rule.bonus().healing());
        tower.onStateChanged(lane);
        return true;
    }

    void tick(WarlockTower tower, PlayerLane lane) {
        tickRegeneration(tower, lane);
        tickVfx(tower, lane);
    }

    void resetRound(WarlockTower tower, PlayerLane lane) {
        setGlow(tower, lane, false);
        regenerationTicks = 0;
        vfxTicks = 0;
    }

    Snapshot snapshot() {
        return snapshot(ownerPlayer);
    }

    boolean awakenedThisRound() {
        return state.awakenedThisRound();
    }

    double regenerationPerSecond() {
        return awakenedThisRound() ? config.awakening(path).bonus().regenerationPerSecond() : 0.0;
    }

    double attackDamageBonus() {
        return awakenedThisRound() ? config.awakening(path).bonus().attackDamage() : 0.0;
    }

    double movementSpeedBonus() {
        return awakenedThisRound() ? config.awakening(path).bonus().movementSpeed() : 0.0;
    }

    private void tickRegeneration(WarlockTower tower, PlayerLane lane) {
        double amount = regenerationPerSecond();
        if (tower.health() <= 0.0 || amount <= 0.0 || tower.health() >= tower.currentMaxHealth()) {
            regenerationTicks = 0;
            return;
        }
        int intervalTicks = config.awakening(path).bonus().regenerationIntervalTicks();
        regenerationTicks++;
        if (regenerationTicks < intervalTicks) {
            return;
        }
        regenerationTicks %= intervalTicks;
        tower.applyRegeneration(lane, amount);
    }

    private void tickVfx(WarlockTower tower, PlayerLane lane) {
        if (!awakenedThisRound()) {
            vfxTicks = 0;
            return;
        }
        if (lane == null || lane.arenaWorld() == null) {
            return;
        }
        vfxTicks++;
        tower.entityId().ifPresent(id -> {
            var entity = lane.arenaWorld().getEntity(id);
            if (!(entity instanceof SemionTowerEntity towerEntity) || !towerEntity.isAlive()) {
                return;
            }
            if (vfxTicks % 2 == 0) {
                TowerVfxService.showWarlockAwakeningAura(towerEntity);
            }
            if (vfxTicks % 10 == 0) {
                TowerVfxService.showWarlockAwakeningSparkBurst(towerEntity);
            }
        });
    }

    private static void setGlow(WarlockTower tower, PlayerLane lane, boolean glowing) {
        if (lane == null || lane.arenaWorld() == null) {
            return;
        }
        tower.entityId().ifPresent(id -> {
            var entity = lane.arenaWorld().getEntity(id);
            if (entity instanceof SemionTowerEntity towerEntity) {
                towerEntity.setGlowingTag(glowing);
            }
        });
    }

    static boolean meetsActivationConditions(
            boolean awakeningUnlocked,
            double currentHealthRatio,
            double healthThreshold,
            boolean lastSurvivingTower
    ) {
        if (!Double.isFinite(currentHealthRatio) || !Double.isFinite(healthThreshold)) {
            return false;
        }
        return awakeningUnlocked
                && lastSurvivingTower
                && currentHealthRatio > 0.0
                && currentHealthRatio <= Math.max(0.0, healthThreshold);
    }

    public static boolean recordKill(UUID ownerPlayer) {
        if (ownerPlayer == null) {
            return false;
        }
        boolean previouslyUnlocked = snapshot(ownerPlayer).unlocked();
        KILLS.compute(ownerPlayer, (ignored, kills) -> saturatedIncrement(kills == null ? 0L : kills));
        return !previouslyUnlocked && snapshot(ownerPlayer).unlocked();
    }

    public static Snapshot snapshot(UUID ownerPlayer) {
        long kills = ownerPlayer == null ? 0L : KILLS.getOrDefault(ownerPlayer, 0L);
        long requiredKills = WarlockConfig.RUNTIME.requiredAwakeningKills();
        return new Snapshot(kills, requiredKills, kills >= requiredKills);
    }

    public static void clear(UUID ownerPlayer) {
        if (ownerPlayer != null) {
            KILLS.remove(ownerPlayer);
        }
    }

    public static void clearAllForTesting() {
        KILLS.clear();
    }

    private static long saturatedIncrement(long value) {
        return value == Long.MAX_VALUE ? value : value + 1L;
    }

    public record Snapshot(long kills, long requiredKills, boolean unlocked) {
    }
}
