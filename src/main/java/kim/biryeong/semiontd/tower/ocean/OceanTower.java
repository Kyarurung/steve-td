package kim.biryeong.semiontd.tower.ocean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.damagesource.DamageSource;

public final class OceanTower extends EntityBackedTower {
    public static final String CONFIG_ID = OceanConfig.GLOBAL_ID;
    private final OceanConfig config = OceanConfig.RUNTIME;
    private final OceanResourceState resourceState;
    private final OceanCombat combat;
    private final OceanAbilityController abilities;

    public OceanTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
        resourceState = new OceanResourceState(config.global(OceanAbilityKey.INITIAL_WATER));
        combat = new OceanCombat(config, resourceState);
        abilities = new OceanAbilityController(config, resourceState);
    }

    public OceanTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
        resourceState = new OceanResourceState(config.global(OceanAbilityKey.INITIAL_WATER));
        combat = new OceanCombat(config, resourceState);
        abilities = new OceanAbilityController(config, resourceState);
    }

    public double water() {
        return resourceState.water();
    }

    public void addWater(double amount) {
        resourceState.addWater(amount);
    }

    public boolean spendWater(double amount) {
        return resourceState.spendWater(amount);
    }

    public double waterDamageMultiplier() {
        return combat.waterDamageMultiplier(this);
    }

    @Override
    public void onPlaced(PlayerLane lane) {
        abilities.attach(lane);
        super.onPlaced(lane);
    }

    @Override
    public void onWaveStarted(PlayerLane lane, int currentRound) {
        resourceState.startWave();
    }

    @Override
    public void resetForRound(PlayerLane lane) {
        resourceState.resetRound();
        abilities.attach(lane);
        super.resetForRound(lane);
    }

    @Override
    public void tick(PlayerLane lane) {
        abilities.attach(lane);
        super.tick(lane);
        abilities.tick(this, lane);
    }

    @Override
    protected boolean execute(PlayerLane lane) {
        return abilities.execute(this, lane);
    }

    @Override
    protected int cooldownTicksAfterExecute(PlayerLane lane) {
        return abilities.cooldownTicks(this, super.cooldownTicksAfterExecute(lane));
    }

    @Override
    public Optional<SemionMonsterEntity> selectAttackTarget(
            SemionTowerEntity towerEntity,
            List<SemionMonsterEntity> candidates
    ) {
        return combat.selectAttackTarget(this, candidates);
    }

    @Override
    public double modifyAttackDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        return combat.modifyAttackDamage(this, target, damageAmount);
    }

    @Override
    public int adjustAttackInterval(int baseIntervalTicks) {
        return combat.adjustAttackInterval(this, baseIntervalTicks);
    }

    @Override
    public double modifyIncomingDamage(SemionTowerEntity towerEntity, DamageSource damageSource, double damageAmount) {
        return combat.modifyIncomingDamage(this, damageSource, damageAmount);
    }

    @Override
    public void onDamaged(
            SemionTowerEntity towerEntity,
            DamageSource damageSource,
            double damageAmount,
            double previousHealth,
            double currentHealth
    ) {
        abilities.onDamaged(this, towerEntity, previousHealth, currentHealth);
    }

    @Override
    public void onAttack(
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount,
            boolean killedTarget
    ) {
        combat.onAttack(this, towerEntity, target, damageAmount);
    }

    @Override
    public List<String> runtimeDetailLines() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("물 " + oneDecimal(water()));
        if (type().damage() > 0.0) {
            lines.add("물 공격력 " + percent(waterDamageMultiplier() - 1.0));
            lines.add("물 " + oneDecimal(config.global(OceanAbilityKey.WATER_SOFT_CAP)) + " 초과분은 공격력에 완만하게 반영");
            lines.add("공격당 물 -" + oneDecimal(config.value(type(), OceanAbilityKey.ATTACK_WATER_COST)));
        }
        if (OceanTowers.isSupport(type()) || OceanTowers.isHealer(type())) {
            lines.add("능력당 물 -" + oneDecimal(config.value(type(), OceanAbilityKey.ABILITY_WATER_COST)));
            lines.add("물 " + oneDecimal(config.global(OceanAbilityKey.EMPOWERED_ABILITY_WATER_THRESHOLD))
                    + " 이상: 소모 " + oneDecimal(config.global(OceanAbilityKey.EMPOWERED_ABILITY_WATER_COST_MULTIPLIER))
                    + "배, 효과 " + oneDecimal(config.global(OceanAbilityKey.EMPOWERED_ABILITY_EFFECT_MULTIPLIER)) + "배");
        }
        if (OceanTowers.isTank(type())) {
            lines.add("물 분배 최대 " + oneDecimal(config.value(type(), OceanAbilityKey.TRANSFER_CAP))
                    + " / " + oneDecimal(config.value(type(), OceanAbilityKey.TRANSFER_COOLDOWN_TICKS) / 20.0) + "초");
        }
        if (water() <= 0.0) {
            lines.add("탈수: 능력 정지, 공격력·공격 속도 감소");
        }
        return lines;
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (previousTower instanceof OceanTower oceanTower) {
            resourceState.restore(oceanTower.resourceState.snapshot());
        }
    }

    double incomeWaterMultiplier() {
        return combat.incomeWaterMultiplier(this);
    }

    Optional<SemionTowerEntity> runtimeEntity(OceanTower target, PlayerLane lane) {
        if (target == null || lane == null || target.entityId().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(lane.arenaWorld().getEntity(target.entityId().getAsInt()))
                .filter(SemionTowerEntity.class::isInstance)
                .map(SemionTowerEntity.class::cast);
    }

    Optional<SemionTowerEntity> ownRuntimeEntity(PlayerLane lane) {
        return super.runtimeEntity(lane);
    }

    boolean healRuntimeTarget(SemionTowerEntity target, double amount) {
        return healTarget(target, amount);
    }
}
