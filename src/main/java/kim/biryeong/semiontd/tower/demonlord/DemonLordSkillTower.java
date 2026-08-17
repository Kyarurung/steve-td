package kim.biryeong.semiontd.tower.demonlord;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.tower.ProductionTower;
import kim.biryeong.semiontd.tower.TowerType;

/**
 * A demon lord altar. It grants its skill to the owning player and does nothing else.
 *
 * <p>All three combat hooks are switched off: it never chases, never draws aggro and never takes
 * damage. Leaving it killable would be a trap, because the builder has no defensive tower to
 * protect it with and losing an altar mid-round would silently delete a skill the player paid for.
 */
public class DemonLordSkillTower extends ProductionTower {
    private DemonLordBinding binding;

    public DemonLordSkillTower(TowerType type, UUID ownerPlayer, TeamId teamId, int laneId, GridPosition position) {
        super(type, ownerPlayer, teamId, laneId, position);
    }

    public DemonLordSkillTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    public DemonLordSkill skill() {
        return DemonLordTowers.skillOf(type());
    }

    public int tier() {
        return DemonLordTowers.tierOf(type());
    }

    public int cooldownTicks() {
        return DemonLordTowers.cooldownTicks(type());
    }

    SemionTowerEntity entity(PlayerLane lane) {
        if (lane == null || entityId().isEmpty()) {
            return null;
        }
        return lane.arenaWorld().getEntity(entityId().getAsInt()) instanceof SemionTowerEntity entity
                ? entity
                : null;
    }

    /**
     * Key this altar answers to, refreshed by {@code DemonLordService} whenever the bar is rebuilt.
     *
     * <p>Cached on the tower purely so the tower info panel can show it - the binding itself is
     * always derived from build order, never stored as the source of truth.
     */
    public DemonLordBinding binding() {
        return binding;
    }

    void setBinding(DemonLordBinding binding) {
        this.binding = binding;
    }

    @Override
    public boolean canChaseTargets() {
        return false;
    }

    @Override
    public boolean invulnerable() {
        return true;
    }

    @Override
    public boolean drawsAggro() {
        return false;
    }

    /**
     * Placing or upgrading an altar changes which items belong in the hotbar, so the loadout is
     * marked dirty and the next service tick rebuilds slots 3-7.
     */
    @Override
    public void onPlaced(PlayerLane lane) {
        super.onPlaced(lane);
        DemonLordStates.markLoadoutDirty(ownerPlayer());
    }

    @Override
    public void onRemoved(PlayerLane lane) {
        DemonLordStates.markLoadoutDirty(ownerPlayer());
        super.onRemoved(lane);
    }

    @Override
    public List<String> runtimeDetailLines() {
        DemonLordSkill skill = skill();
        if (skill == null) {
            return List.of();
        }
        return List.of(
                skill.displayName() + " · 쿨타임 " + String.format("%.1f", cooldownTicks() / 20.0) + "초",
                "코스트 " + skill.slotCost()
                        + (binding == null ? " · 키 없음" : " · [" + binding.label() + "] 키")
        );
    }
}
