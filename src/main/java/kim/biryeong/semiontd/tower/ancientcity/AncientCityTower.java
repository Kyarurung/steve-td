package kim.biryeong.semiontd.tower.ancientcity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.DamageType;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.damagesource.DamageSource;

public final class AncientCityTower extends EntityBackedTower {
    private final AncientCityConfig config = AncientCityConfig.RUNTIME;
    private final AncientCityCombatState combatState = new AncientCityCombatState();
    private final AncientCityCombat combat = new AncientCityCombat(config, combatState);

    public AncientCityTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public AncientCityTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    public AncientCityRole role() {
        return AncientCityTowers.roleOf(type());
    }

    @Override
    public DamageType primaryDamageType() {
        return DamageType.MAGIC;
    }

    @Override
    public void onPlaced(PlayerLane lane) {
        super.onPlaced(lane);
        AncientCityTerritoryController.ensureSeeded(this, lane);
    }

    @Override
    public void onStateChanged(PlayerLane lane) {
        super.onStateChanged(lane);
        AncientCityTerritoryController.ensureFinalDefenseSeeded(this, lane);
    }

    @Override
    public void onWaveStarted(PlayerLane lane, int currentRound) {
        AncientCityTerritoryController.onWaveStarted(this, lane, currentRound);
    }

    @Override
    public void tick(PlayerLane lane) {
        combat.tick();
        super.tick(lane);
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
        combat.onDamaged(this, towerEntity, damageAmount, currentHealth);
    }

    @Override
    protected boolean execute(PlayerLane lane) {
        return combat.execute(this, lane);
    }

    @Override
    protected int cooldownTicksAfterExecute(PlayerLane lane) {
        return Math.max(1, config.ticks(type(), AncientCityAbilityKey.MAGIC_COOLDOWN_TICKS));
    }

    @Override
    public List<String> runtimeDetailLines() {
        int count = AncientCityTerritoryStates.territoryCount(ownerPlayer());
        int maxSculk = Math.max(1, config.globalInt(AncientCityAbilityKey.MAX_SCULK));
        int fullAt = Math.min(maxSculk, Math.max(1, config.globalInt(AncientCityAbilityKey.RESONANCE_FULL_AT)));
        double bonus = AncientCityTerritoryController.resonanceBonus(this);
        return List.of(
                "스컬크 영토 " + count + "/" + maxSculk,
                "스컬크 공명 " + Math.min(count, fullAt) + "/" + fullAt + " · +" + percent(bonus)
                        + " (" + (AncientCityTerritoryController.resonanceActive(this) ? "활성" : "비활성") + ")"
        );
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (previousTower instanceof AncientCityTower previous) {
            combatState.copyFrom(previous.combatState);
        }
    }

    double magicDamage(SemionMonsterEntity target, double baseDamage, boolean includeMark) {
        return combat.magicDamage(this, target, baseDamage, includeMark);
    }

    static double combinedMagicBonus(double bonus) {
        return AncientCityRules.combinedMagicBonus(
                bonus,
                AncientCityConfig.RUNTIME.global(AncientCityAbilityKey.MAX_COMBINED_DAMAGE_BONUS)
        );
    }

    static double incomeAdjustedMagicDamage(double damage, boolean incomeTarget) {
        return AncientCityRules.incomeAdjustedMagicDamage(
                damage,
                incomeTarget,
                AncientCityConfig.RUNTIME.global(AncientCityAbilityKey.INCOME_MAGIC_DAMAGE_MULTIPLIER)
        );
    }

    Optional<SemionTowerEntity> ownRuntimeEntity(PlayerLane lane) {
        return super.runtimeEntity(lane);
    }
}
