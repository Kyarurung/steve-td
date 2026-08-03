package kim.biryeong.semiontd.tower.warlock;

import java.util.List;

final class WarlockStats {
    private final WarlockCombat combat;

    WarlockStats(WarlockCombat combat) {
        this.combat = combat;
    }

    List<String> create(WarlockTower tower) {
        return WarlockStatsView.core(new WarlockStatsView.CoreStats(
                tower.totalSacrificeCount(),
                new WarlockStatsView.CombatStats(
                        combat.damageCap(tower.type()),
                        tower.additionalAttackDamage(),
                        tower.attackIntervalReduction(),
                        tower.maximumAttackIntervalReduction(),
                        tower.splashRadius(),
                        combat.maximumSplashRadius(tower),
                        combat.maximumSplashRadius(tower) > 0.0
                ),
                new WarlockStatsView.DefenseStats(
                        tower.additionalHealth(),
                        tower.regenerationPerSecond(),
                        tower.maximumRegenerationPerSecond(),
                        combat.lifeStealRatio(tower),
                        combat.maximumLifeSteal(tower),
                        tower.damageReduction(),
                        tower.maximumDamageReduction()
                )
        ));
    }
}
