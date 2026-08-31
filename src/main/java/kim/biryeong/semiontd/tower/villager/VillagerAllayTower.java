package kim.biryeong.semiontd.tower.villager;

import java.util.UUID;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.SupportTower;
import kim.biryeong.semiontd.tower.TowerType;

/** Tower hook for Villager support towers. */
public class VillagerAllayTower extends SupportTower {
    private final VillagerSupportController supportController = new VillagerSupportController(this);

    public VillagerAllayTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition position
    ) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public VillagerAllayTower(
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
    protected boolean execute(PlayerLane lane) {
        return supportController.execute(lane);
    }

    @Override
    protected int cooldownTicksAfterExecute(PlayerLane lane) {
        return supportController.cooldownTicksAfterExecute(
                lane,
                super.cooldownTicksAfterExecute(lane)
        );
    }

    boolean isType(TowerType expected) {
        return VillagerTowers.matches(type(), expected);
    }

    boolean healEntity(SemionTowerEntity entity, double amount) {
        return healTarget(entity, amount);
    }

    void recordSupportHealing(double amount) {
        recordHealingDone(amount);
    }
}
