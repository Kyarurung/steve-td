package kim.biryeong.semiontd.tower.illager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.raid.Raid;

public class IllagerTower extends EntityBackedTower {
    private final IllagerCombat combat;

    public IllagerTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        this(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition, IllagerTargetPolicy.DEFAULT);
    }

    public IllagerTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition,
            IllagerTargetPolicy targetPolicy
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
        this.combat = new IllagerCombat(IllagerConfig.RUNTIME, targetPolicy);
    }

    @Override
    protected void configureEntityAfterSpawn(SemionTowerEntity entity, PlayerLane lane) {
        if (!IllagerTowers.isCaptainTower(type())) {
            return;
        }
        var patterns = entity.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
        entity.setItemSlot(EquipmentSlot.HEAD, Raid.getOminousBannerInstance(patterns));
    }

    @Override
    public Optional<SemionMonsterEntity> selectAttackTarget(
            SemionTowerEntity towerEntity,
            List<SemionMonsterEntity> candidates
    ) {
        return combat.selectAttackTarget(this, candidates);
    }

    @Override
    public boolean supportsForcedAttackTargeting() {
        return true;
    }

    @Override
    public Optional<SemionMonsterEntity> selectForcedAttackTarget(
            SemionTowerEntity towerEntity,
            List<SemionMonsterEntity> candidates
    ) {
        return combat.selectForcedAttackTarget(this, candidates);
    }

    @Override
    public double modifyAttackDamage(
            SemionTowerEntity towerEntity,
            SemionMonsterEntity target,
            double damageAmount
    ) {
        return combat.modifyAttackDamage(this, target, damageAmount);
    }

    @Override
    public void tick(PlayerLane lane) {
        combat.refreshRaidTimedEffects(this, lane);
        super.tick(lane);
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
}
