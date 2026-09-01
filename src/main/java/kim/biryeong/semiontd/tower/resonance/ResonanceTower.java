package kim.biryeong.semiontd.tower.resonance;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.DamageType;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.damagesource.DamageSource;

public final class ResonanceTower extends EntityBackedTower {
    private final ResonanceState resonance = new ResonanceState();
    private final ResonanceCombat combat = new ResonanceCombat(ResonanceConfig.RUNTIME);

    public ResonanceTower(
            TowerType type,
            UUID ownerPlayer,
            TeamId teamId,
            int laneId,
            GridPosition originalPosition,
            GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    public ResonanceAspect aspect() {
        return ResonanceTowers.aspectOf(type());
    }

    public int resonanceLevel() {
        return resonance.level();
    }

    public int resonanceLinks() {
        return resonance.links();
    }

    public double auraAttackSpeedBonus() {
        return resonance.auraAttackSpeedBonus();
    }

    public double auraDamageVsSlowedBonus() {
        return resonance.auraDamageVsSlowedBonus();
    }

    ResonanceSnapshot resonanceSnapshot() {
        return resonance.snapshot();
    }

    void updateResonanceState(int level, int links) {
        resonance.updateResonance(level, links);
    }

    void updateResonanceAuras(double attackSpeedBonus, double damageVsSlowedBonus) {
        resonance.updateAuras(attackSpeedBonus, damageVsSlowedBonus);
    }

    boolean chargeReady(int every) {
        return resonance.chargeReady(every);
    }

    boolean dealMagicDamage(SemionTowerEntity source, SemionMonsterEntity target, double damage) {
        return damageTarget(source, target, damage, DamageType.MAGIC);
    }

    void recordMagicKill(SemionTowerEntity source, SemionMonsterEntity target, double damage) {
        onKill(source, target, damage);
    }

    boolean healResonanceTarget(SemionTowerEntity target, double amount) {
        return healTarget(target, amount);
    }

    @Override
    public List<String> runtimeDetailLines() {
        ResonanceSnapshot state = resonance.snapshot();
        return List.of("무블룸 공명 Lv " + state.level()
                + " (링크 " + state.links() + ") / 받는 오라 공속 +" + percent(state.auraAttackSpeedBonus())
                + " / 둔화 대상 피해 +" + percent(state.auraDamageVsSlowedBonus()));
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        if (previousTower instanceof ResonanceTower previous) {
            resonance.restore(previous.resonance.snapshot());
        }
    }

    @Override
    public int adjustAttackInterval(int baseIntervalTicks) {
        return combat.adjustAttackInterval(this, baseIntervalTicks);
    }

    @Override
    public double modifyAttackDamage(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount) {
        return combat.modifyAttackDamage(this, target, damageAmount);
    }

    @Override
    public double modifyIncomingDamage(SemionTowerEntity towerEntity, DamageSource damageSource, double damageAmount) {
        return combat.modifyIncomingDamage(this, damageSource, damageAmount);
    }

    @Override
    public void onAttack(SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount, boolean killedTarget) {
        combat.resolveAttack(this, towerEntity, target, damageAmount);
    }
}
