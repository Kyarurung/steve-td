package kim.biryeong.semiontd.tower.description;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

public final class TowerDescriptionTemplate {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^{}]+)}");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final DecimalFormat PRECISE_NUMBER_FORMAT = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final String ATTACK_DAMAGE_COLOR = "#ec8d34";
    private static final String HEALTH_COLOR = "#fc5454";
    private static final String AGGRO_PRIORITY_COLOR = "#a80000";
    private static final String ATTACK_RANGE_COLOR = "#f0e6d2";
    private static final String ATTACK_SPEED_COLOR = "#ffe78d";
    private static final String DIAMOND_GRADIENT = "<gradient:#ffffff:#d5fff6:#a1fbe8:#4aedd9:#20c5b5:#1aaaa7:#11727a:#145e53>";
    private static final String GRADIENT_CLOSE = "</gradient>";

    private TowerDescriptionTemplate() {
    }

    public static TowerDescriptionFactory of(List<String> template) {
        List<String> lines = template == null ? List.of() : List.copyOf(template);
        return type -> render(lines, type);
    }

    public static List<String> render(List<String> template, TowerType type) {
        if (template == null || template.isEmpty()) {
            return List.of();
        }
        List<String> rendered = new ArrayList<>(template.size());
        for (String line : template) {
            rendered.add(renderLine(line, type));
        }
        return List.copyOf(rendered);
    }

    private static String renderLine(String line, TowerType type) {
        if (line == null || line.isEmpty()) {
            return "";
        }
        Matcher matcher = PLACEHOLDER.matcher(line);
        StringBuilder rendered = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(rendered, Matcher.quoteReplacement(renderPlaceholder(matcher.group(1), type)));
        }
        matcher.appendTail(rendered);
        return rendered.toString();
    }

    private static String renderPlaceholder(String placeholder, TowerType type) {
        int formatSeparator = placeholder.lastIndexOf(':');
        String expression = formatSeparator < 0 ? placeholder.trim() : placeholder.substring(0, formatSeparator).trim();
        String format = formatSeparator < 0 ? "number" : placeholder.substring(formatSeparator + 1).trim();
        try {
            return format(evaluate(expression, type), format);
        } catch (IllegalArgumentException exception) {
            return "{" + placeholder + "}";
        }
    }

    private static double evaluate(String expression, TowerType type) {
        if (expression.isBlank()) {
            throw new IllegalArgumentException("Blank tower description expression.");
        }
        double result = 0.0;
        char operator = '+';
        int tokenStart = 0;
        for (int index = 0; index <= expression.length(); index++) {
            if (index < expression.length() && expression.charAt(index) != '*' && expression.charAt(index) != '/') {
                continue;
            }
            String token = expression.substring(tokenStart, index).trim();
            double value = value(token, type);
            if (operator == '*') {
                result *= value;
            } else if (operator == '/') {
                result = value == 0.0 ? 0.0 : result / value;
            } else {
                result = value;
            }
            if (index < expression.length()) {
                operator = expression.charAt(index);
                tokenStart = index + 1;
            }
        }
        return result;
    }

    private static double value(String token, TowerType type) {
        if (token.startsWith("ability.")) {
            String abilityKey = token.substring("ability.".length());
            int idSeparator = abilityKey.lastIndexOf('.');
            if (idSeparator > 0 && idSeparator < abilityKey.length() - 1) {
                double configured = TowerBalanceRuntime.ability(
                        abilityKey.substring(0, idSeparator),
                        abilityKey.substring(idSeparator + 1),
                        Double.NaN
                );
                if (!Double.isNaN(configured)) {
                    return configured;
                }
            }
            return TowerBalanceRuntime.ability(type.id(), abilityKey);
        }
        if (token.startsWith("stat.")) {
            return stat(type, token.substring("stat.".length()));
        }
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Unknown tower description token: " + token, exception);
        }
    }

    private static double stat(TowerType type, String key) {
        return switch (key) {
            case "mineralCost" -> type.mineralCost();
            case "maxHealth" -> type.maxHealth();
            case "range" -> type.range();
            case "damage" -> type.damage();
            case "attackIntervalTicks" -> type.attackIntervalTicks();
            case "attackIntervalSeconds" -> type.attackIntervalTicks() / 20.0;
            case "attacksPerSecond" -> 20.0 / Math.max(1, type.attackIntervalTicks());
            case "aggroPriority" -> type.aggroPriority();
            default -> throw new IllegalArgumentException("Unknown tower stat token: " + key);
        };
    }

    public static String format(double value, String format) {
        return switch (format) {
            case "integer", "int" -> Long.toString(Math.round(value));
            case "percent" -> formatNumber(value * 100.0) + "%";
            case "percent_integer" -> Math.round(value * 100.0) + "%";
            case "seconds", "second" -> formatNumber(value / 20.0) + "초";
            case "blocks", "block" -> formatNumber(value) + "블록";
            case "precise_blocks", "precise_block" -> formatPreciseNumber(value) + "블록";
            case "attack_damage", "ad" -> formatAttackDamage(value);
            case "health", "hp" -> formatHealth(value);
            case "aggro", "priority" -> formatAggroPriority(value);
            case "attack_range", "range" -> formatAttackRange(value);
            case "attack_speed", "as" -> formatAttackSpeed(value);
            case "sell_price", "sell" -> formatSellPrice(value);
            case "number", "num", "" -> formatNumber(value);
            default -> throw new IllegalArgumentException("Unknown tower description format: " + format);
        };
    }

    public static String formatIncrease(double baseValue, double currentValue, String format) {
        if (!Double.isFinite(baseValue) || !Double.isFinite(currentValue) || baseValue <= 0.0 || currentValue <= baseValue) {
            return "";
        }
        long increasePercent = Math.round(((currentValue - baseValue) / baseValue) * 100.0);
        if (increasePercent <= 0L) {
            return "";
        }
        return " <white>(</white><green>+" + increasePercent + "%</green><white>)</white>";
    }

    private static String styledStat(String color, String icon, String label, String formattedValue) {
        return "<" + color + ">" + icon + " " + label + "</" + color + "><white>: </white><" + color + ">" + formattedValue + "</" + color + ">";
    }

    public static String styledEndStat(String color, String icon, String label, String value, String unit, String progress) {
        return "<" + color + ">" + icon + " " + label + "</" + color + "><white>: </white><" + color + ">" + value + unit + "</" + color + ">" + (progress.isEmpty() ? "" : "<white> " + progress + "</white>");
    }

    public static String stackProgress(int currentStacks, int stacksPerStep, double currentValue, double maximumValue) {
        if (stacksPerStep <= 0 || currentValue >= maximumValue - 0.0001) {return "(MAX)";}
        int nextStacks = (currentStacks / stacksPerStep + 1) * stacksPerStep;
        return "(" + nextStacks + ")";
    }

    public static String splashProgress(int currentStacks, int... thresholds) {
        for (int threshold : thresholds) {if (currentStacks < threshold) {return "(" + threshold + ")";}}
        return "(MAX)";
    }

    public static String tooltipStat(String color, String icon, String label, String value) {
        return "<" + color + ">" + icon + " " + label + "</" + color + ">" + "<white>: </white>" + "<" + color + ">" + value + "</" + color + ">";
    }

    public static String tooltipAttackSpeedTicks(int ticks) {
        return "<dark_gray>(" + ticks + "틱)</dark_gray>";
    }

    public static String formatNumber(double value) {
        synchronized (NUMBER_FORMAT) {
            return NUMBER_FORMAT.format(value);
        }
    }

    private static String formatPreciseNumber(double value) {
        synchronized (PRECISE_NUMBER_FORMAT) {
            return PRECISE_NUMBER_FORMAT.format(value);
        }
    }

    private static String formatAttackDamage(double value) {
        return styledStat(ATTACK_DAMAGE_COLOR, "\uD83E\uDE93", "피해", formatNumber(value));
    }

    private static String formatHealth(double value) {
        return styledStat(HEALTH_COLOR, "\u2764", "체력", formatNumber(value));
    }

    private static String formatAttackSpeed(double value) {
        return styledStat(ATTACK_SPEED_COLOR, "\u26A1", "공격 속도", formatNumber(value) + "회/초");
    }

    public static String formatAttackSpeedTicks(int ticks) {
        return "<white>(</white><" + ATTACK_SPEED_COLOR + ">" + ticks + "틱</" + ATTACK_SPEED_COLOR + "><white>)</white>";
    }

    private static String formatAttackRange(double value) {
        return styledStat(ATTACK_RANGE_COLOR, "\uD83C\uDFF9", "사거리", formatNumber(value) + " 블록");
    }

    private static String formatAggroPriority(double value) {
        return styledStat(AGGRO_PRIORITY_COLOR, "\uD83D\uDCA2", "어그로", Long.toString(Math.round(value)));
    }

    private static String formatSellPrice(double value) {
        return DIAMOND_GRADIENT + "\uD83D\uDC8E 판매가<white>: </white>" + Math.round(value) + " 다이아" + GRADIENT_CLOSE;
    }
}
