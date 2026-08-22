package kim.biryeong.semiontd.tower.warlock;

import java.util.List;

final class WarlockStats {
    private final WarlockConfig config;
    private final WarlockCombat combat;

    WarlockStats(WarlockConfig config, WarlockCombat combat) {
        this.config = config;
        this.combat = combat;
    }

    List<String> create(WarlockTower tower) {
        WarlockPath path = tower.path();
        WarlockConfig.PathRule rule = config.path(path);
        WarlockAwakening.Snapshot awakeningProgress = tower.awakeningSnapshot();
        int lifeStealSacrifices = path == WarlockPath.MELEE
                ? tower.roundSacrificeCount()
                : tower.totalSacrificeCount();
        int damageReductionSacrifices = path == WarlockPath.RANGED
                ? tower.roundSacrificeCount()
                : tower.totalSacrificeCount();
        int splashSacrifices = path == WarlockPath.RANGED
                ? tower.totalSacrificeCount()
                : tower.roundSacrificeCount();
        boolean specialized = path.specialized();

        return WarlockStatsView.core(new WarlockStatsView.CoreStats(
                tower.totalSacrificeCount(),
                tower.roundSacrificeCount(),
                true,
                tower.awakenedThisRound(),
                new WarlockStatsView.AwakeningStats(
                        awakeningProgress.kills(),
                        awakeningProgress.requiredKills(),
                        awakeningProgress.unlocked(),
                        specialized,
                        tower.currentHealthRatio(),
                        tower.awakeningHealthThreshold(),
                        tower.isLastSurvivingTower(),
                        tower.regenerationPerSecond(),
                        tower.awakeningDamageBonus(),
                        tower.awakeningMovementSpeedBonus()
                ),
                new WarlockStatsView.CombatStats(
                        tower.effectiveDamageBonus(),
                        tower.attackIntervalReduction(),
                        tower.maximumAttackIntervalReduction(),
                        tower.splashRadius(),
                        combat.maximumSplashRadius(path),
                        rule.splash().maximumRadius() > 0.0
                ),
                new WarlockStatsView.DefenseStats(
                        tower.additionalHealth(),
                        combat.lifeStealRatio(tower),
                        combat.maximumLifeSteal(path),
                        tower.damageReduction(),
                        tower.maximumDamageReduction(),
                        tower.incomeDebuffResistance()
                ),
                new WarlockStatsView.ProgressionStats(
                        specialized,
                        lifeStealSacrifices,
                        rule.lifeSteal().sacrificesPerStep(),
                        damageReductionSacrifices,
                        rule.defense().sacrificesPerStep(),
                        specialized,
                        path == WarlockPath.RANGED,
                        tower.roundSacrificeCount(),
                        1,
                        splashSacrifices,
                        rule.splash().sacrificesPerStep(),
                        specialized
                )
        ));
    }
}
