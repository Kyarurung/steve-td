package kim.biryeong.semiontd.tower.end;

import java.util.Objects;
import kim.biryeong.semiontd.tower.LogarithmicScaling;

record EndTransferSnapshot(
        EndTransferStacks stacks,
        double roundHealthContribution,
        double permanentHealthContribution,
        double roundDamageContribution,
        double permanentDamageContribution
) {
    EndTransferSnapshot {
        stacks = Objects.requireNonNull(stacks, "stacks");
    }

    EndTransferStats resolve(
            EndConfig.ScalingRule healthScaling,
            EndConfig.ScalingRule damageScaling
    ) {
        double permanentHealth = scale(permanentHealthContribution, healthScaling);
        double totalHealth = totalHealthBonus(healthScaling);
        double permanentDamage = scale(permanentDamageContribution, damageScaling);
        double totalDamage = totalDamageBonus(damageScaling);
        return new EndTransferStats(
                stacks.shulkerCount(),
                stacks.endCrystalCount(),
                stacks.roundCompletedCount(),
                permanentHealth,
                Math.max(0.0, totalHealth - permanentHealth),
                permanentDamage,
                Math.max(0.0, totalDamage - permanentDamage)
        );
    }

    double totalHealthBonus(EndConfig.ScalingRule rule) {
        return scale(permanentHealthContribution + roundHealthContribution, rule);
    }

    double totalDamageBonus(EndConfig.ScalingRule rule) {
        return scale(permanentDamageContribution + roundDamageContribution, rule);
    }

    private static double scale(double raw, EndConfig.ScalingRule rule) {
        return LogarithmicScaling.logarithmicBonus(raw, rule.threshold(), rule.scale());
    }
}
