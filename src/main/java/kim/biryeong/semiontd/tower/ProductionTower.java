package kim.biryeong.semiontd.tower;

import java.util.UUID;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;

public class ProductionTower extends EntityBackedTower {
    public ProductionTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition position
    ) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public ProductionTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }
}
