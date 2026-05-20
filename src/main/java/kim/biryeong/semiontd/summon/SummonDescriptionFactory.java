package kim.biryeong.semiontd.summon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import kim.biryeong.semiontd.config.AttackKind;
import kim.biryeong.semiontd.config.SummonConfig;
import kim.biryeong.semiontd.entity.monster.DamageType;

public final class SummonDescriptionFactory {
    private SummonDescriptionFactory() {
    }

    public static List<String> describe(SummonConfig.SummonDefinition definition) {
        ArrayList<String> lines = new ArrayList<>();
        if (definition.description() != null) {
            for (String line : definition.description()) {
                addLine(lines, sanitize(line));
            }
        }
        addLine(lines, roleLine(definition));
        addLine(lines, economyLine(definition));
        addLine(lines, attackLine(definition));
        addAbilityLines(lines, definition);
        return List.copyOf(lines);
    }

    private static String roleLine(SummonConfig.SummonDefinition definition) {
        SummonRole primaryRole = primaryRole(definition.roles());
        return switch (primaryRole) {
            case SWARM -> "역할: 낮은 비용 또는 큰 충돌 수로 타워 공격 대상을 분산시키는 물량 유닛입니다.";
            case RUSH -> "역할: 빠르게 라인을 밀어 타워가 충분히 정리하기 전에 압박을 넣는 러시 유닛입니다.";
            case TANK -> "역할: 높은 체력과 방어로 타워 화력을 오래 받아내는 탱커 유닛입니다.";
            case SIEGE -> "역할: 라인 후반부나 보스 압박에서 방어 대상에게 직접 부담을 주는 공성 유닛입니다.";
            case SUPPORT -> "역할: 주변 아군 인컴 유닛을 회복하거나 강화해 라인 유지력을 올리는 지원 유닛입니다.";
            case DISRUPTOR -> "역할: 상대 타워의 공격 속도나 사거리를 낮춰 방어 효율을 흔드는 교란 유닛입니다.";
        };
    }

    private static String economyLine(SummonConfig.SummonDefinition definition) {
        double ratio = definition.emeraldCost() == 0 ? 0.0 : (double) definition.incomeGain() / definition.emeraldCost() * 100.0;
        return "경제: 비용 " + definition.emeraldCost() + " 에메랄드, 수입 +" + definition.incomeGain()
                + ", 처치 보상 " + definition.diamondReward() + " 다이아, 수입 효율 " + number(ratio) + "%입니다.";
    }

    private static String attackLine(SummonConfig.SummonDefinition definition) {
        String kind = definition.attackKind() == AttackKind.RANGED ? "원거리" : "근접";
        String damage = definition.damageType() == DamageType.MAGIC ? "마법" : "물리";
        return "전투: " + kind + " " + damage + " 공격으로 " + number(definition.attackDamage())
                + " 피해를 주며, 체력 " + number(definition.maxHealth())
                + ", 방어 " + number(definition.armor())
                + ", 저항 " + number(definition.resistance()) + "을 가집니다.";
    }

    private static void addAbilityLines(ArrayList<String> lines, SummonConfig.SummonDefinition definition) {
        switch (definition.id()) {
            case "wolf", "cave_spider", "stray", "goat", "bogged", "breeze", "vindicator" ->
                    addTowerDebuffLine(lines, definition, "공격 속도", "magnitude");
            case "horse", "shulker" ->
                    addTowerDebuffLine(lines, definition, "사거리", "magnitude");
            case "elder_guardian" -> {
                addLine(lines, "능력: 반경 " + blocks(value(definition, "radius", 8.0))
                        + " 내 가까운 타워 최대 " + integer(definition, "maxTargets", 3)
                        + "기의 공격 속도를 " + percent(value(definition, "attackSpeedMagnitude", 0.30))
                        + ", 사거리를 " + percent(value(definition, "rangeMagnitude", 0.20))
                        + " 감소시킵니다.");
                addLine(lines, "능력 시간: " + seconds(definition, "durationTicks", 100)
                        + " 지속, 쿨타임 " + seconds(definition, "cooldownTicks", 80) + "입니다.");
            }
            case "turtle" ->
                    addAllyBuffLine(lines, definition, "받는 피해", "magnitude", "감소");
            case "allay" -> {
                addLine(lines, "능력: 반경 " + blocks(value(definition, "radius", 6.0))
                        + " 내 아군 인컴 유닛 최대 " + integer(definition, "maxTargets", 6)
                        + "기를 " + number(value(definition, "healAmount", 8.0)) + " 회복합니다.");
                addLine(lines, "능력 시간: 쿨타임 " + seconds(definition, "cooldownTicks", 120) + "입니다.");
            }
            case "fox" ->
                    addAllyBuffLine(lines, definition, "공격력", "magnitude", "증가");
            case "ocelot" ->
                    addAllyBuffLine(lines, definition, "이동 속도", "magnitude", "증가");
            case "witch" -> {
                addLine(lines, "능력: 반경 " + blocks(value(definition, "radius", 7.0))
                        + " 내 아군 인컴 유닛 최대 " + integer(definition, "maxTargets", 8)
                        + "기의 이동 속도 " + percent(value(definition, "moveMagnitude", 0.30))
                        + ", 공격력 " + percent(value(definition, "attackMagnitude", 0.25))
                        + ", 공격 속도 " + percent(value(definition, "attackSpeedMagnitude", 0.25))
                        + "를 증가시킵니다.");
                addLine(lines, "능력 시간: " + seconds(definition, "durationTicks", 80)
                        + " 지속, 쿨타임 " + seconds(definition, "cooldownTicks", 60) + "입니다.");
            }
            case "evoker" -> {
                addLine(lines, "능력: 반경 " + blocks(value(definition, "radius", 8.0))
                        + " 내 아군 인컴 유닛 최대 " + integer(definition, "maxTargets", 10)
                        + "기의 공격력을 " + percent(value(definition, "attackMagnitude", 0.25))
                        + " 증가시키고 받는 피해를 " + percent(value(definition, "damageReductionMagnitude", 0.25))
                        + " 감소시킵니다.");
                addLine(lines, "능력 시간: " + seconds(definition, "durationTicks", 80)
                        + " 지속, 쿨타임 " + seconds(definition, "cooldownTicks", 60) + "입니다.");
            }
            case "guardian", "blaze", "ghast", "wither_skeleton", "warden" ->
                    addSiegeLine(lines, definition);
            default ->
                    addLine(lines, "능력: 별도 액티브 능력 없이 기본 스탯과 역할 특성으로 압박합니다.");
        }
    }

    private static void addTowerDebuffLine(
            ArrayList<String> lines,
            SummonConfig.SummonDefinition definition,
            String statName,
            String magnitudeKey
    ) {
        addLine(lines, "능력: 반경 " + blocks(value(definition, "radius", 6.0))
                + " 내 가까운 타워 최대 " + integer(definition, "maxTargets", 1)
                + "기의 " + statName + "를 " + percent(value(definition, magnitudeKey, 0.10))
                + " 감소시킵니다.");
        addLine(lines, "능력 시간: " + seconds(definition, "durationTicks", 80)
                + " 지속, 쿨타임 " + seconds(definition, "cooldownTicks", 80) + "입니다.");
    }

    private static void addAllyBuffLine(
            ArrayList<String> lines,
            SummonConfig.SummonDefinition definition,
            String statName,
            String magnitudeKey,
            String verb
    ) {
        addLine(lines, "능력: 반경 " + blocks(value(definition, "radius", 6.0))
                + " 내 자신 포함 아군 인컴 유닛 최대 " + integer(definition, "maxTargets", 8)
                + "기의 " + statName + "를 " + percent(value(definition, magnitudeKey, 0.15))
                + " " + verb + "시킵니다.");
        addLine(lines, "능력 시간: " + seconds(definition, "durationTicks", 80)
                + " 지속, 쿨타임 " + seconds(definition, "cooldownTicks", 60) + "입니다.");
    }

    private static void addSiegeLine(ArrayList<String> lines, SummonConfig.SummonDefinition definition) {
        addLine(lines, "능력: 라인 진행도 " + percent(value(definition, "progressThreshold", 0.70))
                + " 이상이거나 보스를 공격할 때 방어 대상에게 추가 고정 피해 "
                + number(value(definition, "bonusDamage", 20.0)) + "를 줍니다.");
        addLine(lines, "능력 시간: 쿨타임 " + seconds(definition, "cooldownTicks", 80) + "입니다.");
    }

    private static SummonRole primaryRole(List<SummonRole> roles) {
        return roles.stream()
                .max(java.util.Comparator.comparingInt(SummonRole::targetPriority))
                .orElse(SummonRole.RUSH);
    }

    private static double value(SummonConfig.SummonDefinition definition, String key, double fallback) {
        return definition.abilityValues().getOrDefault(key, fallback);
    }

    private static int integer(SummonConfig.SummonDefinition definition, String key, int fallback) {
        return Math.max(0, (int) Math.round(value(definition, key, fallback)));
    }

    private static String seconds(SummonConfig.SummonDefinition definition, String key, int fallbackTicks) {
        return number(integer(definition, key, fallbackTicks) / 20.0) + "초";
    }

    private static String blocks(double value) {
        return number(value) + "블록";
    }

    private static String percent(double value) {
        return number(value * 100.0) + "%";
    }

    private static String number(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) {
            return String.format(Locale.ROOT, "%.0f", value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String sanitize(String line) {
        if (line == null) {
            return "";
        }
        return line
                .replace("T1 ", "")
                .replace("T2 ", "")
                .replace("T3 ", "")
                .replace("T4 ", "")
                .replace("T5 ", "");
    }

    private static void addLine(ArrayList<String> lines, String line) {
        if (line == null || line.isBlank() || lines.contains(line)) {
            return;
        }
        lines.add(line);
    }
}
