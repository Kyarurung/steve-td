package kim.biryeong.semiontd.tower.animal;

import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.TowerUpgradeOption;
import net.minecraft.world.phys.Vec3;

/** Recomputes the two-phase pack/leader transition from lane state. */
final class AnimalPackController {
    private final AnimalPackTower tower;
    private int stacks;
    private boolean leaderAuraActive;
    private boolean livingLeaderExists;

    AnimalPackController(AnimalPackTower tower) {
        this.tower = tower;
    }

    int stacks() { return stacks; }

    boolean atMaximum() { return stacks() >= tower.maxStacks(); }

    boolean leaderAuraActive() { return leaderAuraActive; }

    boolean livingLeaderExists() { return livingLeaderExists; }

    boolean meetsUpgradeRequirements(PlayerLane lane, TowerUpgradeOption option) {
        if (option == null || !option.targetType().id().equals(tower.leaderType().id())) {
            return true;
        }
        return AnimalPackRules.canUpgradeToLeader(
                tower.type().id().equals(tower.leaderBaseType().id()),
                stacks(),
                tower.maxStacks(),
                hasOtherLivingLeader(lane)
        );
    }

    void refreshStacks(PlayerLane lane) {
        int previous = stacks();
        long matching = lane == null ? 0L : lane.towers().stream()
                .filter(candidate -> candidate != tower)
                .filter(candidate -> tower.ownerPlayer().equals(candidate.ownerPlayer()))
                .filter(tower::isStackFamily)
                .count();
        stacks = AnimalPackRules.cappedStacks(matching, tower.maxStacks());
        tower.onStacksChanged(lane, previous, stacks());
    }

    void refreshLeaderState(PlayerLane lane) {
        boolean previousLeaderExists = livingLeaderExists;
        boolean previousAuraActive = leaderAuraActive;
        livingLeaderExists = hasOtherLivingLeader(lane);
        leaderAuraActive = !tower.isLeader() && tower.health() > 0.0 && findActiveLeader(lane) != null;
        if (previousAuraActive != leaderAuraActive) {
            tower.onLeaderAuraChanged(lane, previousAuraActive, leaderAuraActive);
        }
        if (previousLeaderExists != livingLeaderExists || previousAuraActive != leaderAuraActive) {
            tower.onStateChanged(lane);
        }
    }

    private boolean hasOtherLivingLeader(PlayerLane lane) {
        return lane != null && lane.towers().stream()
                .filter(candidate -> candidate != tower)
                .filter(candidate -> tower.ownerPlayer().equals(candidate.ownerPlayer()))
                .anyMatch(candidate -> candidate.health() > 0.0
                        && candidate.type().id().equals(tower.leaderType().id()));
    }

    private AnimalPackTower findActiveLeader(PlayerLane lane) {
        if (lane == null) {
            return null;
        }
        return lane.towers().stream()
                .filter(candidate -> candidate != tower)
                .filter(candidate -> tower.ownerPlayer().equals(candidate.ownerPlayer()))
                .filter(AnimalPackTower.class::isInstance)
                .map(AnimalPackTower.class::cast)
                .filter(candidate -> candidate.health() > 0.0)
                .filter(candidate -> candidate.type().id().equals(tower.leaderType().id()))
                .filter(AnimalPackTower::atMaxStacks)
                .filter(candidate -> withinLeaderAura(lane, candidate))
                .findFirst()
                .orElse(null);
    }

    private boolean withinLeaderAura(PlayerLane lane, AnimalPackTower leader) {
        return AnimalPackRules.withinAura(
                center(lane, tower).distanceToSqr(center(lane, leader)),
                tower.leaderValue(AnimalAbilityKey.LEADER_AURA_RADIUS)
        );
    }

    private static Vec3 center(PlayerLane lane, AnimalPackTower animalTower) {
        if (lane != null && lane.arenaWorld() != null) {
            var currentEntity = animalTower.entityId().isPresent()
                    ? lane.arenaWorld().getEntity(animalTower.entityId().getAsInt())
                    : null;
            if (currentEntity instanceof SemionTowerEntity towerEntity) {
                return towerEntity.position().add(0.0, towerEntity.getBbHeight() * 0.5, 0.0);
            }
        }
        return new Vec3(
                animalTower.position().x() + 0.5,
                animalTower.position().y() + 1.0,
                animalTower.position().z() + 0.5
        );
    }
}
