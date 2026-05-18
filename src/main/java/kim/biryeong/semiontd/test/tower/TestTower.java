package kim.biryeong.semiontd.test.tower;

import java.util.UUID;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.TowerType;

public class TestTower extends EntityBackedTower {
    public TestTower(UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        this(TestTowerTypes.TEST_DIRECT, ownerPlayer, teamId, laneId, position);
    }

    public TestTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public TestTower(
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
