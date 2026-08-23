package kim.biryeong.semiontd.tower.warlock;

import java.util.UUID;
import kim.biryeong.semiontd.api.SemionTdApi;
import kim.biryeong.semiontd.api.area.AreaEffectOutcome;
import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.AreaVfxStyles;
import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.area.AreaEffectIds;

public class WarlockSacrificeTower extends EntityBackedTower {
    private final WarlockConfig config = WarlockConfig.RUNTIME;

    public WarlockSacrificeTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public WarlockSacrificeTower(
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
    public void onPlaced(PlayerLane lane) {
        super.onPlaced(lane);
        WarlockTower.refreshWarlockCoreStats(lane);
    }

    @Override
    public void onRemoved(PlayerLane lane) {
        super.onRemoved(lane);
        WarlockTower.refreshWarlockCoreStats(lane);
    }

    @Override
    public void onDeath(PlayerLane lane) {
        WarlockRules.DeathEffectRule rule = config.deathEffect(type());
        TimedEffectType effectType = rule.path() == WarlockPath.MELEE
                ? TimedEffectType.MONSTER_TOWER_DAMAGE_TAKEN_BONUS
                : TimedEffectType.MONSTER_ATTACK_SPEED_REDUCTION;
        applyMonsterEffect(lane, effectType, rule);
        WarlockTower.refreshWarlockCoreStats(lane);
    }

    private void applyMonsterEffect(
            PlayerLane lane,
            TimedEffectType effectType,
            WarlockRules.DeathEffectRule rule
    ) {
        SemionTowerEntity towerEntity = towerEntity(lane);
        if (towerEntity == null || rule.magnitude() <= 0.0) {
            return;
        }
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTower(
                AreaEffectIds.tower(this, "death_debuff"),
                towerEntity,
                rule.radius(),
                AreaVfxSpec.onChange(AreaVfxStyles.DEBUFF)
        );
        SemionTdApi.areaEffects().applyToMonsters(request, monster -> {
            double previous = monster.activeTimedEffectMagnitude(effectType);
            monster.applyTimedEffect(effectType, rule.magnitude(), rule.durationTicks());
            return Double.compare(previous, monster.activeTimedEffectMagnitude(effectType)) != 0
                    ? AreaEffectOutcome.APPLIED
                    : AreaEffectOutcome.UNCHANGED;
        });
    }

    private SemionTowerEntity towerEntity(PlayerLane lane) {
        if (lane == null || entityId().isEmpty()) {
            return null;
        }
        return lane.arenaWorld().getEntity(entityId().getAsInt()) instanceof SemionTowerEntity towerEntity ? towerEntity : null;
    }

}
