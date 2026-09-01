package kim.biryeong.semiontd.tower.resonance;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.Tower;

public final class ResonanceController {
    private static final ResonanceConfig CONFIG = ResonanceConfig.RUNTIME;

    private ResonanceController() {
    }

    public static void captureWaveStart(PlayerLane lane) {
        if (lane != null) {
            refresh(lane.towers());
        }
    }

    public static void refresh(Collection<Tower> towers) {
        if (towers == null || towers.isEmpty()) {
            return;
        }
        List<ResonanceTower> resonanceTowers = towers.stream()
                .filter(ResonanceTower.class::isInstance)
                .map(ResonanceTower.class::cast)
                .filter(tower -> tower.health() > 0.0)
                .toList();
        resonanceTowers.forEach(tower -> updateResonance(tower, resonanceTowers));
        resonanceTowers.forEach(tower -> updateAuras(tower, resonanceTowers));
    }

    private static void updateResonance(ResonanceTower tower, List<ResonanceTower> towers) {
        int maximumLinks = Math.max(0, CONFIG.integer(tower.type(), ResonanceAbilityKey.MAX_LINKS_PER_TOWER));
        if (maximumLinks == 0) {
            tower.updateResonanceState(0, 0);
            return;
        }
        int links = (int) towers.stream()
                .filter(candidate -> candidate != tower)
                .filter(candidate -> sameOwnerLane(tower, candidate))
                .filter(candidate -> ResonanceRules.distance(tower.position(), candidate.position())
                        <= CONFIG.value(tower.type(), ResonanceAbilityKey.LINK_RANGE))
                .filter(candidate -> candidate.aspect() != tower.aspect())
                .limit(maximumLinks)
                .count();
        int level = ResonanceRules.level(
                links,
                Math.max(0, CONFIG.integer(tower.type(), ResonanceAbilityKey.MAX_RESONANCE_LEVEL)),
                CONFIG.integer(tower.type(), ResonanceAbilityKey.LEVEL_1_REQUIRED_LINKS),
                CONFIG.integer(tower.type(), ResonanceAbilityKey.LEVEL_2_REQUIRED_LINKS),
                CONFIG.integer(tower.type(), ResonanceAbilityKey.LEVEL_3_REQUIRED_LINKS)
        );
        tower.updateResonanceState(level, links);
    }

    private static void updateAuras(ResonanceTower tower, List<ResonanceTower> towers) {
        double attackSpeed = towers.stream()
                .filter(candidate -> candidate != tower)
                .filter(candidate -> candidate.aspect() == ResonanceAspect.AMPLIFY)
                .filter(candidate -> candidate.resonanceLevel() >= 2)
                .filter(candidate -> sameOwnerLane(tower, candidate))
                .filter(candidate -> ResonanceRules.distance(tower.position(), candidate.position())
                        <= CONFIG.value(candidate.type(), ResonanceAbilityKey.BLOOM_AURA_RANGE))
                .mapToDouble(ResonanceController::bloomAttackSpeedAura)
                .max()
                .orElse(0.0);
        double slowedDamage = towers.stream()
                .filter(candidate -> candidate != tower)
                .filter(candidate -> candidate.aspect() == ResonanceAspect.FROST)
                .filter(candidate -> candidate.resonanceLevel() >= 2)
                .filter(candidate -> sameOwnerLane(tower, candidate))
                .filter(candidate -> ResonanceRules.distance(tower.position(), candidate.position())
                        <= CONFIG.value(candidate.type(), ResonanceAbilityKey.FROST_AURA_RANGE))
                .mapToDouble(ResonanceController::frostDamageAura)
                .max()
                .orElse(0.0);
        tower.updateResonanceAuras(attackSpeed, slowedDamage);
    }

    private static double bloomAttackSpeedAura(ResonanceTower tower) {
        ResonanceAbilityKey key = tower.resonanceLevel() >= 3
                ? ResonanceAbilityKey.BLOOM_LEVEL_3_AURA_ATTACK_SPEED_BONUS
                : ResonanceAbilityKey.BLOOM_LEVEL_2_AURA_ATTACK_SPEED_BONUS;
        return CONFIG.value(tower.type(), key);
    }

    private static double frostDamageAura(ResonanceTower tower) {
        ResonanceAbilityKey key = tower.resonanceLevel() >= 3
                ? ResonanceAbilityKey.FROST_LEVEL_3_AURA_DAMAGE_BONUS
                : ResonanceAbilityKey.FROST_LEVEL_2_AURA_DAMAGE_BONUS;
        return CONFIG.value(tower.type(), key);
    }

    private static boolean sameOwnerLane(ResonanceTower tower, ResonanceTower candidate) {
        return Objects.equals(tower.ownerPlayer(), candidate.ownerPlayer())
                && tower.teamId() == candidate.teamId()
                && tower.laneId() == candidate.laneId();
    }
}
