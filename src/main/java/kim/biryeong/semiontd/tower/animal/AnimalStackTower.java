package kim.biryeong.semiontd.tower.animal;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.TowerUpgradeOption;

abstract class AnimalStackTower extends EntityBackedTower {
    private final AnimalPackController pack = new AnimalPackController(this);

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

    protected final int currentStacks() { return pack.stacks(); }

    protected final boolean atMaxStacks() { return pack.atMaximum(); }

    protected final boolean hasLeaderAura() { return pack.leaderAuraActive(); }

    protected final boolean isLeader() { return type().id().equals(leaderType().id()); }

    protected final double leaderValue(String key) {
        return TowerBalanceRuntime.ability(leaderType().id(), key);
    }

    @Override
    public final boolean meetsUpgradeRequirements(PlayerLane lane, TowerUpgradeOption option) {
        return pack.meetsUpgradeRequirements(lane, option);
    }

    @Override
    public java.util.List<String> runtimeDetailLines() {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        lines.add("무리 스택 " + currentStacks() + "/" + maxStacks());
        if (type().id().equals(leaderBaseType().id())) {
            lines.add("우두머리 승급: 최대 무리 " + (atMaxStacks() ? "충족" : "미충족")
                    + ", 계열 우두머리 " + (pack.livingLeaderExists() ? "존재" : "없음"));
        }
        if (isLeader()) {
            lines.add("우두머리 오라 " + (health() > 0.0 && atMaxStacks() ? "활성" : "비활성"));
        } else if (hasLeaderAura()) {
            lines.add("우두머리 오라 적용 중");
        }
        return lines;
    }

    protected final int refreshStacks(PlayerLane lane) {
        pack.refreshStacks(lane);
        return currentStacks();
    }

    protected void onStacksChanged(PlayerLane lane, int previousStacks, int currentStacks) {
    }

    protected void onLeaderAuraChanged(PlayerLane lane, boolean previousActive, boolean currentActive) {
    }

    protected abstract boolean isStackFamily(Tower tower);

    protected abstract int maxStacks();

    protected abstract TowerType leaderBaseType();

    protected abstract TowerType leaderType();

    static void refreshAnimalStacks(PlayerLane lane) {
        if (lane == null) {
            return;
        }
        for (Tower tower : lane.towers()) {
            if (tower instanceof AnimalStackTower animalTower) {
                animalTower.pack.refreshStacks(lane);
            }
        }
        for (Tower tower : lane.towers()) {
            if (tower instanceof AnimalStackTower animalTower) {
                animalTower.pack.refreshLeaderState(lane);
            }
        }
    }
}
