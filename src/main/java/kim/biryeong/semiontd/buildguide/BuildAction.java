package kim.biryeong.semiontd.buildguide;

import kim.biryeong.semiontd.game.GridPosition;

public record BuildAction(
        int round,
        BuildActionType type,
        String subjectId,
        GridPosition position,
        long cost,
        long incomeGain,
        int scheduledRound,
        String targetTeam,
        int targetLaneId,
        String positionMode
) {
    public static final String POSITION_ABSOLUTE = "absolute";
    public static final String POSITION_LANE_RELATIVE = "lane_relative";

    public BuildAction {
        round = Math.max(1, round);
        if (type == null) {
            throw new IllegalArgumentException("Build action type cannot be null.");
        }
        subjectId = subjectId == null ? "" : subjectId;
        cost = Math.max(0L, cost);
        incomeGain = Math.max(0L, incomeGain);
        scheduledRound = Math.max(0, scheduledRound);
        targetTeam = targetTeam == null ? "" : targetTeam;
        targetLaneId = Math.max(0, targetLaneId);
        positionMode = positionMode == null || positionMode.isBlank() ? POSITION_ABSOLUTE : positionMode;
    }

    public static BuildAction towerPlace(int round, String towerId, GridPosition position, long cost) {
        return new BuildAction(round, BuildActionType.TOWER_PLACE, towerId, position, cost, 0L, 0, "", 0, POSITION_ABSOLUTE);
    }

    public static BuildAction towerPlaceRelative(int round, String towerId, GridPosition position, long cost) {
        return new BuildAction(round, BuildActionType.TOWER_PLACE, towerId, position, cost, 0L, 0, "", 0, POSITION_LANE_RELATIVE);
    }

    public static BuildAction towerUpgrade(int round, String upgradeId, GridPosition position, long cost) {
        return new BuildAction(round, BuildActionType.TOWER_UPGRADE, upgradeId, position, cost, 0L, 0, "", 0, POSITION_ABSOLUTE);
    }

    public static BuildAction towerUpgradeRelative(int round, String upgradeId, GridPosition position, long cost) {
        return new BuildAction(round, BuildActionType.TOWER_UPGRADE, upgradeId, position, cost, 0L, 0, "", 0, POSITION_LANE_RELATIVE);
    }

    public static BuildAction summon(int round, String summonId, long cost, long incomeGain, int scheduledRound, String targetTeam, int targetLaneId) {
        return new BuildAction(round, BuildActionType.SUMMON, summonId, null, cost, incomeGain, scheduledRound, targetTeam, targetLaneId, POSITION_ABSOLUTE);
    }

    public static BuildAction emeraldProductionUpgrade(int round, int upgradeCount, long cost, long incomeGain) {
        return new BuildAction(round, BuildActionType.EMERALD_PRODUCTION_UPGRADE, Integer.toString(upgradeCount), null, cost, incomeGain, 0, "", 0, POSITION_ABSOLUTE);
    }

    public boolean hasLaneRelativePosition() {
        return POSITION_LANE_RELATIVE.equals(positionMode);
    }
}
