package kim.biryeong.semiontd.tower.legion;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.SupportTower;
import kim.biryeong.semiontd.tower.TowerType;

public class LegionGoatTower extends SupportTower {
    private final LegionGoatSupportController support = new LegionGoatSupportController(this);

    public LegionGoatTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public LegionGoatTower(
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
        return support.execute(lane);
    }

    double radius() {
        return value("radius");
    }

    double value(String key) {
        return TowerBalanceRuntime.ability(type().id(), key);
    }

    int ticks(String key) {
        return TowerBalanceRuntime.abilityTicks(type().id(), key);
    }

    int configuredMaxStacks() {
        return TowerBalanceRuntime.abilityInt(type().id(), "maxStacks");
    }
}
