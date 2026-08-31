package kim.biryeong.semiontd.tower.legion;

import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.TowerType;

/** Shared runtime for the chicken and penguin illusion lines, whose combat behavior is identical. */
public final class LegionSplashIllusionTower extends LegionIllusionSummonerTower {
    public LegionSplashIllusionTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition position
    ) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public LegionSplashIllusionTower(
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
    public void onAttack(
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount,
            boolean killedTarget
    ) {
        LegionCombat.splash(this, towerEntity, target, damageAmount);
    }
}
