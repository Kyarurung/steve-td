package kim.biryeong.semiontd.tower.ocean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class OceanWaterTower extends EntityBackedTower {
    private final OceanConfig config = OceanConfig.RUNTIME;
    private final OceanResourceState resourceState = new OceanResourceState(0.0);
    private final OceanWaterSupplyController supply = new OceanWaterSupplyController(config, resourceState);

    public OceanWaterTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public OceanWaterTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    public static BlockPos waterBlockPos(GridPosition position) {
        return OceanWaterSupplyController.waterBlockPos(position);
    }

    public static BlockState waterMarker() {
        return OceanWaterSupplyController.waterMarker();
    }

    public static boolean canPlaceAt(PlayerLane lane, GridPosition position) {
        return OceanWaterSupplyController.canPlaceAt(lane, position);
    }

    @Override
    public void onPlaced(PlayerLane lane) {
        super.onPlaced(lane);
        supply.onPlaced(this, lane);
    }

    @Override
    protected void configureEntityAfterSpawn(SemionTowerEntity entity, PlayerLane lane) {
        entity.setNoAi(true);
    }

    @Override
    public void onRemoved(PlayerLane lane) {
        supply.onRemoved(lane);
        super.onRemoved(lane);
    }

    @Override
    public void onDeath(PlayerLane lane) {
        supply.onRemoved(lane);
    }

    @Override
    public void onStateChanged(PlayerLane lane) {
        super.onStateChanged(lane);
        supply.onStateChanged(this, lane);
    }

    @Override
    public void onWaveStarted(PlayerLane lane, int currentRound) {
        supply.onWaveStarted(this, lane);
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        resourceState.resetRound();
        super.resetForRound(lane);
    }

    @Override
    protected boolean execute(PlayerLane lane) {
        return supply.execute(this, lane);
    }

    @Override
    protected int cooldownTicksAfterExecute(PlayerLane lane) {
        return Math.max(1, config.ticks(type(), OceanAbilityKey.SUPPLY_INTERVAL_TICKS));
    }

    @Override
    public List<String> runtimeDetailLines() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("공급 반경 " + oneDecimal(config.value(type(), OceanAbilityKey.SUPPLY_RADIUS)) + "블록");
        lines.add("웨이브 시작 물 +" + oneDecimal(config.value(type(), OceanAbilityKey.WAVE_START_WATER)));
        lines.add("초당 물 +" + oneDecimal(config.value(type(), OceanAbilityKey.WATER_PER_SUPPLY) * 20.0
                / Math.max(1, config.ticks(type(), OceanAbilityKey.SUPPLY_INTERVAL_TICKS))));
        lines.add("중첩 체감 계수 " + percent(config.global(OceanAbilityKey.WATER_SUPPLY_STACK_DECAY))
                + " (같은 대상 연결 수 증가 시 타워당 공급 효율 감소)");
        lines.add("물 " + oneDecimal(config.global(OceanAbilityKey.WATER_SOFT_CAP)) + "부터 공급 감소, "
                + oneDecimal(config.global(OceanAbilityKey.WATER_SUPPLY_STOP_THRESHOLD)) + " 이상 공급 중단");
        return lines;
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (previousTower instanceof OceanWaterTower waterTower) {
            resourceState.restore(waterTower.resourceState.snapshot());
        }
    }

    static double stackedSupplyMultiplier(int sourceCount, double decay) {
        return OceanRules.stackedSupplyMultiplier(sourceCount, decay);
    }
}
