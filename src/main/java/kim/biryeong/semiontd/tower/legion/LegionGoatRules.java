package kim.biryeong.semiontd.tower.legion;

import java.util.List;
import java.util.OptionalInt;
import kim.biryeong.semiontd.game.GridPosition;

final class LegionGoatRules {
    private LegionGoatRules() {
    }

    static int maxStacks(int configured, int absoluteMaximum) {
        return Math.max(1, Math.min(absoluteMaximum, configured));
    }

    static boolean withinRange(GridPosition source, GridPosition target, double radius) {
        double nonNegativeRadius = Math.max(0.0, radius);
        double dx = target.x() - source.x();
        double dy = target.y() - source.y();
        double dz = target.z() - source.z();
        return dx * dx + dy * dy + dz * dz <= nonNegativeRadius * nonNegativeRadius;
    }

    static <T> OptionalInt providerIndex(List<T> orderedProviders, T provider, int maximum) {
        int size = Math.min(Math.max(0, maximum), orderedProviders.size());
        for (int index = 0; index < size; index++) {
            if (orderedProviders.get(index) == provider) {
                return OptionalInt.of(index);
            }
        }
        return OptionalInt.empty();
    }
}
