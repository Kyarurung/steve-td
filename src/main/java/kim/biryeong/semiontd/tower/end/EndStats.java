package kim.biryeong.semiontd.tower.end;

import java.util.List;

final class EndStats {
    private final EndConfig config;
    private final EndCombat combat;
    private final EndTransferController transfers;

    EndStats(EndConfig config, EndCombat combat, EndTransferController transfers) {
        this.config = config;
        this.combat = combat;
        this.transfers = transfers;
    }

    List<String> create(EndTower tower, boolean waveActive) {
        if (!tower.isCoreTower()) {
            double reduction = EndTowers.isShulkerLine(tower.type()) ? combat.shulkerDamageReduction(tower.type()) : 0.0;
            return EndStatsView.feeder(waveActive, EndTransferController.progress(tower), reduction);
        }
        EndTowerState state = tower.state();
        EndTransferStats transfer = transfers.stats();
        double maxHealth = state == EndTowerState.EGG ? tower.previewHatchedMaxHealth() : tower.currentMaxHealth();
        int intervalReduction = Math.max(0, tower.type().attackIntervalTicks() - tower.previewHatchedAttackIntervalTicks());
        EndConfig.StackRule regeneration = config.regeneration();
        EndConfig.StackRule lifeSteal = config.lifeSteal();
        EndConfig.StackRule damageReduction = config.damageReduction();
        EndConfig.AttackSpeedRule attackSpeed = config.attackSpeed();
        EndConfig.SplashRule splash = config.splash();
        EndConfig.StackRule attackRange = config.attackRange();
        return EndStatsView.core(new EndStatsView.CoreStats(
                state,
                transfer.shulkerCount(),
                transfer.endCrystalCount(),
                new EndStatsView.DefenseStats(
                        transfer.permanentHealthBonus(),
                        combat.lifeStealRatio(),
                        combat.maximumLifeSteal(),
                        combat.damageReduction(),
                        combat.maximumDamageReduction(),
                        combat.regenerationPerSecond(),
                        combat.maximumRegeneration()
                ),
                new EndStatsView.CombatStats(
                        transfer.permanentDamageBonus(),
                        combat.splashRadius(state == EndTowerState.EGG ? EndTowerState.PHANTOM : state),
                        combat.maximumSplashRadius(),
                        intervalReduction,
                        combat.maximumAttackIntervalReduction(tower.type()),
                        tower.previewHatchedAttackRange(),
                        combat.maximumAttackRange(tower.type(), state)
                ),
                new EndStatsView.EvolutionStats(
                        state != EndTowerState.PHANTOM && maxHealth >= combat.dragonEvolutionHealth(),
                        combat.finalDamageBonus(EndTowerState.DRAGON),
                        combat.dragonRangeBonus(EndTowerState.DRAGON)
                ),
                new EndStatsView.ProgressionStats(
                        regeneration.stacksPerStep(),
                        lifeSteal.stacksPerStep(),
                        damageReduction.stacksPerStep(),
                        attackSpeed.stacksPerStep(),
                        splash.thresholds(),
                        attackRange.stacksPerStep()
                )
        ));
    }
}
