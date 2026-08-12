package kim.biryeong.semiontd.tower.warlock;

import java.util.ArrayList;
import java.util.List;

import static kim.biryeong.semiontd.tower.description.TowerDescriptionTemplate.*;
import static kim.biryeong.semiontd.tower.warlock.WarlockConfig.Ability.*;
import static kim.biryeong.semiontd.tower.warlock.WarlockFormatting.warlockText;

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
        lines.add(sacrificeLine("흡수한 타워", stats.totalSacrifices()));
        lines.add(sacrificeLine("이번 라운드에 흡수한 타워", stats.roundSacrifices()));
        if (stats.showAwakening()) {
            lines.add(awakeningLine(stats.awakened()));
        }
        lines.add(formatPermanentHealth(defense.additionalHealth(), scalingProgress(defense.rawAbsorbedHealth(), defense.effectiveAbsorbedHealth())));
        if (defense.maximumRegenerationPerSecond() > 0.0) {
            lines.add(formatRegeneration(defense.regenerationPerSecond(), ""));
        } if (ranged || melee) {
            lines.add(formatLifeSteal(defense.lifeSteal(), stackProgress(ranged ? stats.totalSacrifices() : stats.roundSacrifices(), lifeStealEvery, defense.lifeSteal(), defense.maximumLifeSteal())));
            lines.add(formatDamageReduction(defense.damageReduction(), stackProgress(ranged ? stats.roundSacrifices() : stats.totalSacrifices(), damageReductionEvery, defense.damageReduction(), defense.maximumDamageReduction())));
        } lines.add(formatPermanentDamage(
                combat.effectiveAttackDamage(),
                damageProgress(combat.rawAttackDamage(), combat.effectiveAttackDamage())
        ));
        if (melee) {
            lines.add(formatAttackSpeedReduction(combat.attackIntervalReductionTicks(), stackProgress(stats.roundSacrifices(), 1, combat.attackIntervalReductionTicks(), combat.maximumAttackIntervalReductionTicks())));
        } else {
            lines.add(formatAttackSpeedReduction(combat.attackIntervalReductionTicks(), maxOnlyProgress(combat.attackIntervalReductionTicks(), combat.maximumAttackIntervalReductionTicks())));
        } if (combat.showAttackRange()) {
            lines.add(formatSplashRange(combat.splashRadius(), stackProgress(ranged ? stats.totalSacrifices() : stats.roundSacrifices(), 1, combat.splashRadius(), combat.maximumSplashRadius())));
        }
        lines.add("<gray>능력치는 높아질 수록 증가 효율이 감소합니다.</gray>");
        return lines;
    }

    private static String sacrificeLine(String label, int sacrifices) {
        return "<white>" + label + ": " + warlockText(sacrifices + "기") + "</white>";
    }

    private static String awakeningLine(boolean awakened) {
        return "<white>각성 상태: " + (awakened ? warlockText("각성") : "<gray>미각성</gray>") + "</white>";
    }

    private static String maxOnlyProgress(double currentValue, double maximumValue) {
        return maximumValue > 0.0 && currentValue >= maximumValue - 0.0001 ? "(MAX)" : "";
    }

    private static String damageProgress(double rawDamage, double effectiveDamage) {
        return scalingProgress(rawDamage, effectiveDamage);
    }

    private static String scalingProgress(double rawValue, double effectiveValue) {
        return rawValue > effectiveValue + 0.0001
                ? "(누적 " + formatNumber(rawValue) + ")"
                : "";
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
            double rawAttackDamage,
            double effectiveAttackDamage,
            int attackIntervalReductionTicks,
            int maximumAttackIntervalReductionTicks,
            double splashRadius,
            double maximumSplashRadius,
            boolean showAttackRange
    ) {
    }

    record DefenseStats(
            double additionalHealth,
            double rawAbsorbedHealth,
            double effectiveAbsorbedHealth,
            double regenerationPerSecond,
            double maximumRegenerationPerSecond,
            double lifeSteal,
            double maximumLifeSteal,
            double damageReduction,
            double maximumDamageReduction
    ) {
    }
}
