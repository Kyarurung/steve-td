package kim.biryeong.semiontd.tower.warlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static kim.biryeong.semiontd.tower.description.TowerDescriptionTemplate.*;
import static kim.biryeong.semiontd.tower.description.TowerDescriptionTemplate.formatPermanentHealth;
import static kim.biryeong.semiontd.tower.warlock.WarlockConfig.Ability.*;

final class WarlockStatsView {
    private WarlockStatsView() {
    }

    static List<String> core(CoreStats stats) {
        CombatStats combat = stats.combat();
        DefenseStats defense = stats.defense();
        ArrayList<String> lines = new ArrayList<>();
        boolean ranged = stats.ranged();
        boolean melee = stats.melee();
        int lifeStealEvery = ranged ? Math.max(1, WarlockConfig.RUNTIME.integer(RANGED_LIFE_EVERY)) : 1;
        int damageReductionEvery = ranged ? Math.max(1, WarlockConfig.RUNTIME.integer(RANGED_DEFENSE_THRESHOLD) + 1) : Math.max(1, WarlockConfig.RUNTIME.integer(MELEE_DEFENSE_EVERY));
        lines.add("<white>흡수한 타워: " + stats.totalSacrifices() + "기</white>");
        lines.add("<white>이번 라운드에 흡수한 타워: " + stats.roundSacrifices() + "기</white>");
        if (stats.showAwakening()) {
            lines.add(stats.awakened()
                    ? "<gray>각성 상태</gray><white>: </white><dark_purple>각성</dark_purple>"
                    : "<gray>각성 상태</gray><white>: </white><gray>미각성</gray>");
        }
        lines.add(formatPermanentHealth(defense.additionalHealth(), ""));
        if (defense.maximumRegenerationPerSecond() > 0.0) {
            lines.add(formatRegeneration(defense.regenerationPerSecond(), ""));
        }
        if (ranged || melee) {
            lines.add(formatLifeSteal(defense.lifeSteal(), stackProgress(ranged ? stats.totalSacrifices() : stats.roundSacrifices(), lifeStealEvery, defense.lifeSteal(), defense.maximumLifeSteal())));
            lines.add(formatDamageReduction(defense.damageReduction(), stackProgress(ranged ? stats.roundSacrifices() : stats.totalSacrifices(), damageReductionEvery, defense.damageReduction(), defense.maximumDamageReduction())));
        }
        lines.add(formatPermanentDamage(combat.additionalAttackDamage(), ""));
        if (melee) {
            lines.add(formatAttackSpeedReduction(combat.attackIntervalReductionTicks(), stackProgress(stats.roundSacrifices(), 1, combat.attackIntervalReductionTicks(), combat.maximumAttackIntervalReductionTicks())));
        } else {
            lines.add(formatAttackSpeedReduction(combat.attackIntervalReductionTicks(), maxOnlyProgress(combat.attackIntervalReductionTicks(), combat.maximumAttackIntervalReductionTicks())));
        }
        if (combat.showAttackRange()) {
            lines.add(formatSplashRange(combat.splashRadius(), stackProgress(ranged ? stats.totalSacrifices() : stats.roundSacrifices(), 1, combat.splashRadius(), combat.maximumSplashRadius())));
        }
        return lines;
    }

    private static String maxOnlyProgress(double currentValue, double maximumValue) {
        return maximumValue > 0.0 && currentValue >= maximumValue - 0.0001 ? "(MAX)" : "";
    }

    private static String oneDecimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String precise(double value) {
        String formatted = String.format(Locale.ROOT, "%.3f", value);
        return formatted.replaceFirst("\\.?0+$", "");
    }

    private static String percent(double value) {
        return oneDecimal(value * 100.0) + "%";
    }

    record CoreStats(
            int totalSacrifices,
            int roundSacrifices,
            boolean showAwakening,
            boolean awakened,
            boolean ranged,
            boolean melee,
            CombatStats combat,
            DefenseStats defense
    ) {
    }

    record CombatStats(
            double additionalAttackDamage,
            int attackIntervalReductionTicks,
            int maximumAttackIntervalReductionTicks,
            double splashRadius,
            double maximumSplashRadius,
            boolean showAttackRange
    ) {
    }

    record DefenseStats(
            double additionalHealth,
            double regenerationPerSecond,
            double maximumRegenerationPerSecond,
            double lifeSteal,
            double maximumLifeSteal,
            double damageReduction,
            double maximumDamageReduction
    ) {
    }
}
