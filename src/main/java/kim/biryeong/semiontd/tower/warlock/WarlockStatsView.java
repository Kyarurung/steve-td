package kim.biryeong.semiontd.tower.warlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import static kim.biryeong.semiontd.tower.description.TowerDescriptionTemplate.stackProgress;
import static kim.biryeong.semiontd.tower.description.TowerDescriptionTemplate.styledEndStat;
import static kim.biryeong.semiontd.tower.warlock.WarlockConfig.Ability.*;

final class WarlockStatsView {
    private WarlockStatsView() {
    }

    private static final String DAMAGE_COLOR = "#ec8d34";
    private static final String ATTACK_SPEED_COLOR = "#ffe78d";
    private static final String ATTACK_RANGE_COLOR = "#f0e6d2";
    private static final String HEALTH_COLOR = "#fc5454";
    private static final String REGENERATION_COLOR = "#20985d";
    private static final String LIFE_STEAL_COLOR = "#e32042";
    private static final String DAMAGE_REDUCTION_COLOR = "#f3ba59";

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
        lines.add(styledEndStat(
                DAMAGE_COLOR,
                "\uD83E\uDE93",
                "현재 추가 피해",
                "+" + oneDecimal(combat.effectiveAttackDamage()),
                "",
                damageProgress(combat.rawAttackDamage(), combat.effectiveAttackDamage())
        ));
        lines.add(styledEndStat(HEALTH_COLOR, "\u2764", "영구 체력", "+" + oneDecimal(defense.additionalHealth()), "", ""));
        if (melee) {
            lines.add(styledEndStat(ATTACK_SPEED_COLOR, "\u26A1", "공격 속도", "-" + combat.attackIntervalReductionTicks(), "틱",
                    stackProgress(stats.roundSacrifices(), 1, combat.attackIntervalReductionTicks(), combat.maximumAttackIntervalReductionTicks())));
        } else {
            lines.add(styledEndStat(ATTACK_SPEED_COLOR, "\u26A1", "공격 속도", "-" + combat.attackIntervalReductionTicks(), "틱",
                    maxOnlyProgress(combat.attackIntervalReductionTicks(), combat.maximumAttackIntervalReductionTicks())));
        }
        if (combat.showAttackRange()) {
            lines.add(styledEndStat(ATTACK_RANGE_COLOR, "⭕", "공격 범위", "+" + precise(combat.splashRadius()), " 블록",
                    stackProgress(ranged ? stats.totalSacrifices() : stats.roundSacrifices(), 1, combat.splashRadius(), combat.maximumSplashRadius())));
        }
        if (defense.maximumRegenerationPerSecond() > 0.0) {
            lines.add(styledEndStat(REGENERATION_COLOR, "➕", "재생", "+" + Math.round(defense.regenerationPerSecond()), " HP/s", ""));
        }
        if (ranged || melee) {
            lines.add(styledEndStat(LIFE_STEAL_COLOR, "\uD83E\uDE78", "생명력 흡수", "+" + percent(defense.lifeSteal()), "",
                    stackProgress(ranged ? stats.totalSacrifices() : stats.roundSacrifices(), lifeStealEvery, defense.lifeSteal(), defense.maximumLifeSteal())));
            lines.add(styledEndStat(DAMAGE_REDUCTION_COLOR, "\uD83D\uDEE1", "피해 감소", "+" + percent(defense.damageReduction()), "",
                    stackProgress(ranged ? stats.roundSacrifices() : stats.totalSacrifices(), damageReductionEvery, defense.damageReduction(), defense.maximumDamageReduction())));
        }
        return lines;
    }

    private static String maxOnlyProgress(double currentValue, double maximumValue) {
        return maximumValue > 0.0 && currentValue >= maximumValue - 0.0001 ? "(MAX)" : "";
    }

    private static String damageProgress(double rawDamage, double effectiveDamage) {
        return rawDamage > effectiveDamage + 0.0001
                ? "(누적 " + oneDecimal(rawDamage) + ")"
                : "";
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
            double regenerationPerSecond,
            double maximumRegenerationPerSecond,
            double lifeSteal,
            double maximumLifeSteal,
            double damageReduction,
            double maximumDamageReduction
    ) {
    }
}
