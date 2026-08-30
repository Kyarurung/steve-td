package kim.biryeong.semiontd.tower.end;

import java.util.Objects;
import kim.biryeong.semiontd.tower.TowerType;

final class EndEvolutionController {
    private final EndConfig config;
    private double synchronizedProgressionMaxHealth;

    EndEvolutionController(EndConfig config, double initialProgressionMaxHealth) {
        this.config = Objects.requireNonNull(config, "config");
        synchronize(initialProgressionMaxHealth);
    }

    EndTowerState hatch(EndTowerState state) {
        return state == EndTowerState.EGG ? EndTowerState.PHANTOM : state;
    }

    EndTowerState reconcile(EndTowerState state, double projectedMaxHealth) {
        if (!state.hatched()) {
            return state;
        }
        return qualifiesForDragon(projectedMaxHealth) ? EndTowerState.DRAGON : EndTowerState.PHANTOM;
    }

    boolean qualifiesForDragon(double projectedMaxHealth) {
        return projectedMaxHealth >= evolutionHealth();
    }

    double projectedBaseMaxHealth(double currentBaseMaxHealth, double progressionMaxHealth) {
        double externalMaxHealth = currentBaseMaxHealth - synchronizedProgressionMaxHealth;
        return Math.max(0.0, progressionMaxHealth + externalMaxHealth);
    }

    double progressionMaxHealth(TowerType type, EndTransferSnapshot progression) {
        return type.maxHealth() + progression.totalHealthBonus(config.healthScaling());
    }

    void synchronize(double progressionMaxHealth) {
        synchronizedProgressionMaxHealth = Math.max(0.0, progressionMaxHealth);
    }

    double phantomScale(double maxHealth) {
        EndConfig.PhantomScaleRule rule = config.phantomScale();
        double resolvedMaxHealth = Double.isFinite(maxHealth) ? Math.max(0.0, maxHealth) : 0.0;
        double growth = rule.healthInterval() > 0.0
                ? resolvedMaxHealth / rule.healthInterval() * rule.step()
                : 0.0;
        return Math.min(rule.cap(), rule.base() + growth);
    }

    private double evolutionHealth() {
        return config.dragon().evolutionHealth();
    }
}
