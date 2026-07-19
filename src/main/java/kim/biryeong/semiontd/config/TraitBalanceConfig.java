package kim.biryeong.semiontd.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record TraitBalanceConfig(Map<String, Map<String, Double>> traits) {
    public TraitBalanceConfig {
        traits = copyTraits(traits);
    }

    public static TraitBalanceConfig defaultConfig() {
        LinkedHashMap<String, Map<String, Double>> traits = new LinkedHashMap<>();
        traits.put("mobilization_grant", Map.of(
                "startingDiamond", 150.0
        ));
        traits.put("clean_lane_bonus", Map.of(
                "incomeRatio", 0.15
        ));
        traits.put("rapid_deployment", Map.of(
                "refundRateAfterWave", 0.90
        ));
        traits.put("berserk_summons", Map.of(
                "attackDamageBonus", 0.20,
                "attackSpeedBonus", 0.10
        ));
        traits.put("interception_doctrine", Map.of(
                "damageBonus", 0.15
        ));
        traits.put("opening_salvo", Map.of(
                "attackSpeedBonus", 0.15,
                "durationSeconds", 15.0
        ));
        traits.put("wavebreaker_doctrine", Map.of(
                "damageBonus", 0.15
        ));
        traits.put("fortitude", Map.of(
                "maxHealthBonus", 0.20,
                "warlockCoreMaxHealthBonus", 0.10
        ));
        traits.put("double_edged_sword", Map.of(
                "outgoingDamageBonus", 0.25,
                "incomingDamageBonus", 0.25
        ));
        traits.put("strength_in_numbers", Map.of(
                "damageBonusPerTower", 0.02
        ));
        traits.put("diversity", Map.of(
                "damageBonusPerType", 0.02
        ));
        traits.put("supply_depot", Map.of(
                "towerLimitBonus", 4.0
        ));
        traits.put("transcendence", Map.of(
                "activationDelaySeconds", 20.0,
                "damageBonus", 0.30
        ));
        return new TraitBalanceConfig(traits);
    }

    public double value(String traitId, String key, double fallback) {
        Map<String, Double> values = traits.get(traitId);
        if (values == null) {
            return fallback;
        }
        Double value = values.get(key);
        return value == null || !Double.isFinite(value) ? fallback : value;
    }

    public TraitBalanceConfig withMissingDefaults(TraitBalanceConfig defaults) {
        if (defaults == null) {
            return this;
        }

        LinkedHashMap<String, Map<String, Double>> mergedTraits = new LinkedHashMap<>();
        traits.forEach((traitId, values) -> {
            LinkedHashMap<String, Double> mergedValues = new LinkedHashMap<>(values);
            Map<String, Double> defaultValues = defaults.traits.get(traitId);
            if (defaultValues != null) {
                defaultValues.forEach(mergedValues::putIfAbsent);
            }
            mergedTraits.put(traitId, mergedValues);
        });
        defaults.traits.forEach((traitId, values) -> mergedTraits.putIfAbsent(traitId, values));
        return new TraitBalanceConfig(mergedTraits);
    }

    private static Map<String, Map<String, Double>> copyTraits(Map<String, Map<String, Double>> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Map<String, Double>> copied = new LinkedHashMap<>();
        source.forEach((traitId, values) -> {
            if (traitId == null || values == null) {
                return;
            }
            LinkedHashMap<String, Double> copiedValues = new LinkedHashMap<>();
            values.forEach((key, value) -> {
                if (key != null && value != null && Double.isFinite(value)) {
                    copiedValues.put(key, value);
                }
            });
            copied.put(traitId, Collections.unmodifiableMap(copiedValues));
        });
        return Collections.unmodifiableMap(copied);
    }
}
