package kim.biryeong.semiontd.tower.end;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import static kim.biryeong.semiontd.tower.description.TowerDescriptionTemplate.splashProgress;
import static kim.biryeong.semiontd.tower.description.TowerDescriptionTemplate.stackProgress;
import static kim.biryeong.semiontd.tower.description.TowerDescriptionTemplate.styledEndStat;
import static kim.biryeong.semiontd.tower.end.EndConfig.Ability.*;

final class EndStatsView {
    private EndStatsView() {
    }

    private static final String DAMAGE_COLOR = "#ec8d34";
    private static final String ATTACK_SPEED_COLOR = "#ffe78d";
    private static final String ATTACK_RANGE_COLOR = "#f0e6d2";
    private static final String HEALTH_COLOR = "#fc5454";
    private static final String REGENERATION_COLOR = "#20985d";
    private static final String LIFE_STEAL_COLOR = "#e32042";
    private static final String DAMAGE_REDUCTION_COLOR = "#f3ba59";

    static List<String> feeder(double damageReduction) {
        ArrayList<String> lines = new ArrayList<>();
        if (damageReduction > 0.0) {
            lines.add(styledEndStat(DAMAGE_REDUCTION_COLOR, "\uD83D\uDEE1", "피해 감소", "+" + Math.round(damageReduction * 100.0), "%", ""));
        }
        return lines;
    }

    static List<String> core(CoreStats stats) {
        CombatStats combat = stats.combat();
        DefenseStats defense = stats.defense();
        EvolutionStats evolution = stats.evolution();
        int attackSpeedStacks = Math.max(1, EndConfig.RUNTIME.integer(ATTACK_SPEED_STACKS));
        int attackRangeStacks = Math.max(1, EndConfig.RUNTIME.integer(ATTACK_RANGE_STACKS));
        int regenerationStacks = Math.max(1, EndConfig.RUNTIME.integer(REGENERATION_STACKS));
        int lifeStealStacks = Math.max(1, EndConfig.RUNTIME.integer(LIFE_STEAL_STACKS));
        int damageReductionStacks = Math.max(1, EndConfig.RUNTIME.integer(DAMAGE_REDUCTION_STACKS));
        int splash1 = EndConfig.RUNTIME.integer(SPLASH_1);
        int splash2 = EndConfig.RUNTIME.integer(SPLASH_2);
        int splash3 = EndConfig.RUNTIME.integer(SPLASH_3);
        int splash4 = EndConfig.RUNTIME.integer(SPLASH_4);
        ArrayList<String> lines = new ArrayList<>();
        lines.add(switch (stats.state()) {
            case EGG -> "<white>상태: <#cc00fa>드래곤 알</#cc00fa></white>";
            case PHANTOM -> "<white>상태: <#cc00fa>아기 드래곤</#cc00fa></white>";
            case DRAGON -> "<white>상태: <#cc00fa>엔더 드래곤</#cc00fa></white>";
        });
        lines.add("<white><#fc5454>셜커</#fc5454> 계열, <#ec8d34>엔드 수정</#ec8d34> 계열 누적 수: <#fc5454>" + stats.shulkerStacks() + "</#fc5454> <dark_gray>|</dark_gray> <#ec8d34>" + stats.endCrystalStacks() + "</#ec8d34></white>");
        lines.add(styledEndStat(DAMAGE_COLOR, "\u2694", "피해량 상한", compactOneDecimal(combat.maximumAttackDamage()), "", ""));
        lines.add(styledEndStat(HEALTH_COLOR, "\u2764", "영구 체력", "+" + oneDecimal(defense.additionalHealth()), "", ""));
        lines.add(styledEndStat(REGENERATION_COLOR, "➕", "재생", "+" + compactOneDecimal(defense.currentRegeneration()), " HP/s", stackProgress(stats.shulkerStacks(), regenerationStacks, defense.currentRegeneration(), defense.maximumRegeneration())));
        lines.add(styledEndStat(LIFE_STEAL_COLOR, "\uD83E\uDE78", "생명력 흡수", "+" + Math.round(defense.currentLifeSteal() * 100.0), "%", stackProgress(stats.shulkerStacks(), lifeStealStacks, defense.currentLifeSteal(), defense.maximumLifeSteal())));
        lines.add(styledEndStat(DAMAGE_REDUCTION_COLOR, "\uD83D\uDEE1", "피해 감소", "+" + Math.round(defense.currentDamageReduction() * 100.0), "%", stackProgress(stats.shulkerStacks(), damageReductionStacks, defense.currentDamageReduction(), defense.maximumDamageReduction())));
        lines.add(styledEndStat(DAMAGE_COLOR, "\uD83E\uDE93", "영구 피해", "+" + oneDecimal(combat.additionalAttackDamage()), "", ""));
        lines.add(styledEndStat(ATTACK_SPEED_COLOR, "\u26A1", "공격 속도", "-" + combat.attackIntervalReductionTicks(), "틱", stackProgress(stats.endCrystalStacks(), attackSpeedStacks, combat.attackIntervalReductionTicks(), combat.maximumAttackIntervalReductionTicks())));
        lines.add(styledEndStat(ATTACK_SPEED_COLOR, "⭕", "공격 범위", "+" + compactOneDecimal(combat.currentSplashRadius()), " 블록", splashProgress(stats.endCrystalStacks(), splash1, splash2, splash3, splash4)));
        lines.add(styledEndStat(ATTACK_RANGE_COLOR, "\uD83C\uDFF9", "사거리", "+" + oneDecimal(combat.currentAttackRange()), " 블록", stackProgress(stats.endCrystalStacks(), attackRangeStacks, combat.currentAttackRange(), combat.maximumAttackRange())));
        if (evolution.showBonuses()) {
            lines.add(styledEndStat(DAMAGE_COLOR, "\u2694", "최종 피해", "+" + Math.round(evolution.finalDamageBonus() * 100.0), "%", ""));
            lines.add(styledEndStat(ATTACK_RANGE_COLOR, "\uD83C\uDFF9", "추가 사거리", "+" + oneDecimal(evolution.dragonRangeBonus()), " 블록", ""));
        }

        return lines;
    }

    private static String oneDecimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String compactOneDecimal(double value) {
        String formatted = oneDecimal(value);
        return formatted.endsWith(".0") ? formatted.substring(0, formatted.length() - 2) : formatted;
    }

    record CoreStats(EndTowerState state, int shulkerStacks, int endCrystalStacks, DefenseStats defense, CombatStats combat, EvolutionStats evolution) {
    }

    record DefenseStats(double additionalHealth, double currentRegeneration, double maximumRegeneration, double currentLifeSteal, double maximumLifeSteal, double currentDamageReduction, double maximumDamageReduction) {
    }

    record CombatStats(double maximumAttackDamage, double additionalAttackDamage, double currentAttackRange, double maximumAttackRange, int attackIntervalReductionTicks, int maximumAttackIntervalReductionTicks, double currentSplashRadius, double maximumSplashRadius) {
    }

    record EvolutionStats(boolean showBonuses, double finalDamageBonus, double dragonRangeBonus) {
    }
}