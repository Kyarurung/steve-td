package kim.biryeong.semiontd.tower.warlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class WarlockStatsView {
    private WarlockStatsView() {
    }

    static List<String> core(CoreStats stats) {
        CombatStats combat = stats.combat();
        DefenseStats defense = stats.defense();
        ArrayList<String> lines = new ArrayList<>();
        lines.add("<white>흡수한 타워: " + stats.totalSacrifices() + "기</white>");
        if (combat.maximumAttackDamage() > 0.0) {
            lines.add("<#D94343>피해량 상한: "
                    + compactOneDecimal(combat.maximumAttackDamage()) + "</#D94343>");
        }
        lines.add("<#D94343>추가 공격력: " + oneDecimal(combat.additionalAttackDamage())
                + "</#D94343><white> / </white><#E66F6F>추가 체력: "
                + oneDecimal(defense.additionalHealth()) + "</#E66F6F>");
        String attackStats = "<#D9B94F>공격 속도: -" + combat.attackIntervalReductionTicks()
                + "틱 / -" + combat.maximumAttackIntervalReductionTicks() + "틱</#D9B94F>";
        if (combat.showAttackRange()) {
            attackStats += "<white> / </white><#D9B94F>공격 범위: "
                    + precise(combat.splashRadius()) + " 블록 / "
                    + precise(combat.maximumSplashRadius()) + " 블록</#D9B94F>";
        }
        lines.add(attackStats);
        if (defense.maximumRegenerationPerSecond() > 0.0) {
            String maximumRegeneration = Double.isInfinite(defense.maximumRegenerationPerSecond())
                    ? ""
                    : " / " + Math.round(defense.maximumRegenerationPerSecond());
            lines.add("<#79C97B>재생: " + Math.round(defense.regenerationPerSecond())
                    + maximumRegeneration + " HP/s</#79C97B>");
        }
        lines.add("<#D94343>생명력 흡수: " + percent(defense.lifeSteal())
                + " / " + percent(defense.maximumLifeSteal())
                + "</#D94343><white> / </white><#72A9E6>피해 감소: "
                + percent(defense.damageReduction()) + " / "
                + percent(defense.maximumDamageReduction()) + "</#72A9E6>");
        return lines;
    }

    private static String oneDecimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String compactOneDecimal(double value) {
        String formatted = oneDecimal(value);
        return formatted.endsWith(".0") ? formatted.substring(0, formatted.length() - 2) : formatted;
    }

    private static String precise(double value) {
        String formatted = String.format(Locale.ROOT, "%.3f", value);
        return formatted.replaceFirst("\\.?0+$", "");
    }

    private static String percent(double value) {
        return oneDecimal(value * 100.0) + "%";
    }

    record CoreStats(int totalSacrifices, CombatStats combat, DefenseStats defense) {
    }

    record CombatStats(
            double maximumAttackDamage,
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
