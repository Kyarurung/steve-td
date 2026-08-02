package kim.biryeong.semiontd.tower.animal;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.TowerUpgradeOption;
import net.minecraft.world.phys.Vec3;

abstract class AnimalStackTower extends EntityBackedTower {
    private int currentStacks;
    private boolean leaderAuraActive;
    private boolean livingLeaderExists;

    protected AnimalStackTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    protected AnimalStackTower(
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
    public void onPlaced(PlayerLane lane) {
        super.onPlaced(lane);
        refreshAnimalStacks(lane);
    }

    @Override
    public void onRemoved(PlayerLane lane) {
        super.onRemoved(lane);
        refreshAnimalStacks(lane);
    }

    @Override
    public void tick(PlayerLane lane) {
        refreshAnimalStacks(lane);
        super.tick(lane);
    }

    protected final int currentStacks() {
        return currentStacks;
    }

    protected final boolean atMaxStacks() {
        return currentStacks >= maxStacks();
    }

    protected final boolean hasLeaderAura() {
        return leaderAuraActive;
    }

    protected final boolean isLeader() {
        return type().id().equals(leaderType().id());
    }

    protected final double leaderValue(String key) {
        return TowerBalanceRuntime.ability(leaderType().id(), key);
    }

    @Override
    public final boolean meetsUpgradeRequirements(PlayerLane lane, TowerUpgradeOption option) {
        if (option == null || !option.targetType().id().equals(leaderType().id())) {
            return true;
        }
        return type().id().equals(leaderBaseType().id()) && atMaxStacks() && !hasOtherLivingLeader(lane);
    }

    @Override
    public java.util.List<String> runtimeDetailLines() {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        lines.add("무리 스택 " + currentStacks + "/" + maxStacks());
        if (type().id().equals(leaderBaseType().id())) {
            lines.add("우두머리 승급: 최대 무리 " + (atMaxStacks() ? "충족" : "미충족")
                    + ", 계열 우두머리 " + (livingLeaderExists ? "존재" : "없음"));
        }
        if (isLeader()) {
            lines.add("우두머리 오라 " + (health() > 0.0 && atMaxStacks() ? "활성" : "비활성"));
        } else if (leaderAuraActive) {
            lines.add("우두머리 오라 적용 중");
        }
        return lines;
    }

    protected final int refreshStacks(PlayerLane lane) {
        int previousStacks = currentStacks;
        currentStacks = countMatchingTowers(lane);
        onStacksChanged(lane, previousStacks, currentStacks);
        return currentStacks;
    }

    protected void onStacksChanged(PlayerLane lane, int previousStacks, int currentStacks) {
    }

    protected void onLeaderAuraChanged(PlayerLane lane, boolean previousActive, boolean currentActive) {
    }

    protected abstract boolean isStackFamily(Tower tower);

    protected abstract int maxStacks();

    protected abstract TowerType leaderBaseType();

    protected abstract TowerType leaderType();

    private int countMatchingTowers(PlayerLane lane) {
        if (lane == null) {
            return 0;
        }
        long count = lane.towers().stream()
                .filter(tower -> tower != this)
                .filter(tower -> ownerPlayer().equals(tower.ownerPlayer()))
                .filter(this::isStackFamily)
                .count();
        return Math.min(maxStacks(), (int) count);
    }

    static void refreshAnimalStacks(PlayerLane lane) {
        if (lane == null) {
            return;
        }
        for (Tower tower : lane.towers()) {
            if (tower instanceof AnimalStackTower animalTower) {
                animalTower.refreshStacks(lane);
            }
        }
        for (Tower tower : lane.towers()) {
            if (tower instanceof AnimalStackTower animalTower) {
                animalTower.refreshLeaderState(lane);
            }
        }
    }

    private void refreshLeaderState(PlayerLane lane) {
        boolean previousLeaderExists = livingLeaderExists;
        boolean previousAuraActive = leaderAuraActive;
        livingLeaderExists = hasOtherLivingLeader(lane);
        leaderAuraActive = !isLeader() && health() > 0.0 && findActiveLeader(lane) != null;
        if (previousAuraActive != leaderAuraActive) {
            onLeaderAuraChanged(lane, previousAuraActive, leaderAuraActive);
        }
        if (previousLeaderExists != livingLeaderExists || previousAuraActive != leaderAuraActive) {
            onStateChanged(lane);
        }
    }

    private boolean hasOtherLivingLeader(PlayerLane lane) {
        return lane != null && lane.towers().stream()
                .filter(tower -> tower != this)
                .filter(tower -> ownerPlayer().equals(tower.ownerPlayer()))
                .anyMatch(tower -> tower.health() > 0.0 && tower.type().id().equals(leaderType().id()));
    }

    private AnimalStackTower findActiveLeader(PlayerLane lane) {
        if (lane == null) {
            return null;
        }
        return lane.towers().stream()
                .filter(tower -> tower != this)
                .filter(tower -> ownerPlayer().equals(tower.ownerPlayer()))
                .filter(tower -> tower instanceof AnimalStackTower)
                .map(tower -> (AnimalStackTower) tower)
                .filter(tower -> tower.health() > 0.0)
                .filter(tower -> tower.type().id().equals(leaderType().id()))
                .filter(AnimalStackTower::atMaxStacks)
                .filter(tower -> withinLeaderAura(lane, tower))
                .findFirst()
                .orElse(null);
    }

    private boolean withinLeaderAura(PlayerLane lane, AnimalStackTower leader) {
        double radius = Math.max(0.0, leaderValue("leaderAuraRadius"));
        return center(lane).distanceToSqr(leader.center(lane)) <= radius * radius;
    }

    private Vec3 center(PlayerLane lane) {
        if (lane != null && lane.arenaWorld() != null) {
            var currentEntity = entityId().isPresent() ? lane.arenaWorld().getEntity(entityId().getAsInt()) : null;
            if (currentEntity instanceof SemionTowerEntity towerEntity) {
                return towerEntity.position().add(0.0, towerEntity.getBbHeight() * 0.5, 0.0);
            }
        }
        return new Vec3(position().x() + 0.5, position().y() + 1.0, position().z() + 0.5);
    }
}
