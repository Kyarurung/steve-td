package kim.biryeong.semiontd.tower.warlock;

import java.util.List;

final class WarlockStats {
    private final WarlockCombat combat;

    WarlockStats(WarlockCombat combat) {
        this.combat = combat;
    }

    List<String> create(WarlockTower tower) {
        boolean showAwakening =
                tower.is(WarlockTowers.RANGED_WARLOCK_TOWER)
                        || tower.is(WarlockTowers.MELEE_WARLOCK_TOWER);

        return WarlockStatsView.core(
                new WarlockStatsView.CoreStats(
                        tower.totalSacrificeCount(),
                        tower.roundSacrificeCount(),
                        showAwakening,
                        tower.awakenedThisRound(),
                        tower.is(WarlockTowers.RANGED_WARLOCK_TOWER),
                        tower.is(WarlockTowers.MELEE_WARLOCK_TOWER),
                        new WarlockStatsView.CombatStats(
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
                )
        );
    }
}
