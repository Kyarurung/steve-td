package kim.biryeong.semiontd.tower.villager;

import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.DirectTower;
import kim.biryeong.semiontd.tower.SupportTower;
import kim.biryeong.semiontd.tower.TowerType;

import java.util.UUID;

public class AllayTower extends SupportTower {
    public AllayTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    @Override
    protected boolean execute(PlayerLane lane) {

        return false;
    }

}
