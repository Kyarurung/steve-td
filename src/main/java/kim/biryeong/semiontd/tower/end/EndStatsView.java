package kim.biryeong.semiontd.tower.end;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class EndStatsView {
    private EndStatsView() {
    }

    static List<String> feeder(boolean waveActive, double transferProgress, double damageReduction) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add(waveActive ? "힘 전달 진행률 " + percent(transferProgress) : "엔더 드래곤에게 힘 전달 대기 중");
        if (damageReduction > 0.0) {lines.add("받는 피해 감소 " + percent(damageReduction));}
        return lines;
    }

    static List<String> core(CoreStats stats) {
        CombatStats combat = stats.combat();
        DefenseStats defense = stats.defense();
        EvolutionStats evolution = stats.evolution();
        ArrayList<String> lines = new ArrayList<>();
        lines.add(switch (stats.state()) {
            case EGG -> "<white>상태: <#B77DE8>드래곤 알</#B77DE8></white>";
            case PHANTOM -> "<white>상태: <#B77DE8>아기 드래곤</#B77DE8></white>";
            case DRAGON -> "<white>상태: <#B77DE8>엔더 드래곤</#B77DE8></white>";
        });
        lines.add("<#B77DE8>엔더 드래곤</#B77DE8><white> 능력치</white>");
        lines.add("<white>엔드 수정, 셜커 스택: " + stats.endCrystalStacks() + " / " + stats.shulkerStacks() + "</white>");
        lines.add("<#D94343>피해·공격력 상한: " + compactOneDecimal(combat.maximumAttackDamage()) + "</#D94343>");
        lines.add("<#D94343>영구 공격력: " + oneDecimal(combat.additionalAttackDamage()) + "</#D94343><white> / </white><#D9B94F>사거리: " + oneDecimal(combat.currentAttackRange()) + " 블록 / " + oneDecimal(combat.maximumAttackRange()) + " 블록</#D9B94F>");
        lines.add("<#D9B94F>공격 속도: -" + combat.attackIntervalReductionTicks() + "틱 / -" + combat.maximumAttackIntervalReductionTicks() + "틱</#D9B94F><white> / </white><#D9B94F>공격 범위: " + Math.round(combat.currentSplashRadius()) + " 블록 / " + Math.round(combat.maximumSplashRadius()) + " 블록</#D9B94F>");
        lines.add("<#E66F6F>영구 체력: " + oneDecimal(defense.additionalHealth()) + "</#E66F6F><white> / </white><#79C97B>재생: " + Math.round(defense.currentRegeneration()) + " / " + Math.round(defense.maximumRegeneration()) + " HP/s</#79C97B>");
        lines.add("<#D94343>생명력 흡수: " + percent(defense.currentLifeSteal()) + " / " + percent(defense.maximumLifeSteal()) + "</#D94343><white> / </white><#72A9E6>피해 감소: " + percentInteger(defense.currentDamageReduction()) + " / " + percentInteger(defense.maximumDamageReduction()) + "</#72A9E6>");
        if (evolution.showBonuses()) {lines.add("<#D94343>최종 피해: +" + percentInteger(evolution.finalDamageBonus()) + "</#D94343><white> / </white><#D9B94F>추가 사거리: +" + oneDecimal(evolution.dragonRangeBonus()) + " 블록</#D9B94F>");}
        return lines;
    }

    private static String oneDecimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String compactOneDecimal(double value) {
        String formatted = oneDecimal(value);
        return formatted.endsWith(".0") ? formatted.substring(0, formatted.length() - 2) : formatted;
    }

    private static String percent(double value) {
        return oneDecimal(value * 100.0) + "%";
    }

    private static String percentInteger(double value) {
        return Math.round(value * 100.0) + "%";
    }

    record CoreStats(EndTowerState state, int endCrystalStacks, int shulkerStacks, CombatStats combat, DefenseStats defense, EvolutionStats evolution) {
    }

    record CombatStats(double maximumAttackDamage, double additionalAttackDamage, double currentAttackRange, double maximumAttackRange, int attackIntervalReductionTicks, int maximumAttackIntervalReductionTicks, double currentSplashRadius, double maximumSplashRadius) {
    }

    record DefenseStats(double additionalHealth, double currentRegeneration, double maximumRegeneration, double currentLifeSteal, double maximumLifeSteal, double currentDamageReduction, double maximumDamageReduction) {
    }

    record EvolutionStats(boolean showBonuses, double finalDamageBonus, double dragonRangeBonus) {
    }
}
