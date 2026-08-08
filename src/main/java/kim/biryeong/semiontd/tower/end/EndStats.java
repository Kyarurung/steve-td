package kim.biryeong.semiontd.tower.end;

import java.util.List;

final class EndStats {
    private final EndCombat combat;

    EndStats(EndCombat combat) {
        this.combat = combat;
    }

    List<String> create(EndTower tower) {
        if (!tower.isCoreTower()) {
            double reduction = EndTowers.isShulkerLine(tower.type()) ? combat.shulkerDamageReduction(tower.type()) : 0.0;
            return EndStatsView.feeder(reduction);
        }
        double maxHealth = tower.isEgg() ? tower.previewHatchedMaxHealth() : tower.currentMaxHealth();
        int intervalReduction = Math.max(0, tower.type().attackIntervalTicks() - tower.previewHatchedAttackIntervalTicks());
        return EndStatsView.core(new EndStatsView.CoreStats(
                tower.state(),
                tower.shulkerCount(),
                tower.endCrystalCount(),
                new EndStatsView.DefenseStats(
                        tower.permanentHealthBonus(),
                        combat.regenerationPerSecond(),
                        combat.maximumRegeneration(),
                        combat.lifeStealRatio(),
                        combat.maximumLifeSteal(),
                        combat.damageReduction(),
                        combat.maximumDamageReduction()
                ),
                new EndStatsView.CombatStats(
                        tower.permanentDamageBonus(),
                        tower.previewHatchedAttackRange(),
                        combat.maximumAttackRange(tower.type(), tower.isDragon()),
                        intervalReduction,
                        combat.maximumAttackIntervalReduction(tower.type()),
                        combat.splashRadius(true),
                        combat.maximumSplashRadius()
                ),
                new EndStatsView.EvolutionStats(
                        (tower.isEgg() || tower.isDragon()) && maxHealth >= combat.dragonEvolutionHealth(),
                        combat.finalDamageBonus(true),
                        combat.dragonRangeBonus(true)
                )
        ));
    }
}
