package kim.biryeong.semiontd.tower.legion;

import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.Tower;

final class LegionGoatProviderResolver {
    private static final Comparator<LegionGoatTower> STACK_ORDER = Comparator
            .comparingInt((LegionGoatTower tower) -> tower.originalPosition().x())
            .thenComparingInt(tower -> tower.originalPosition().y())
            .thenComparingInt(tower -> tower.originalPosition().z())
            .thenComparing(tower -> tower.type().id());

    private final LegionGoatTower owner;
    private final List<LegionGoatTower> providers;
    private final int maxStacks;
    private final BiPredicate<LegionGoatTower, Tower> eligibility;
    private final Map<Tower, OptionalInt> stackIndices = new IdentityHashMap<>();

    private LegionGoatProviderResolver(
            LegionGoatTower owner,
            List<LegionGoatTower> providers,
            int maxStacks,
            BiPredicate<LegionGoatTower, Tower> eligibility
    ) {
        this.owner = owner;
        this.providers = providers;
        this.maxStacks = maxStacks;
        this.eligibility = eligibility;
    }

    static LegionGoatProviderResolver capture(
            PlayerLane lane,
            LegionGoatTower owner,
            int maxStacks,
            BiPredicate<LegionGoatTower, Tower> eligibility
    ) {
        List<LegionGoatTower> providers = lane.towers().stream()
                .filter(LegionGoatTower.class::isInstance)
                .map(LegionGoatTower.class::cast)
                .filter(goat -> goat.health() > 0.0)
                .sorted(STACK_ORDER)
                .toList();
        return new LegionGoatProviderResolver(owner, providers, maxStacks, eligibility);
    }

    OptionalInt stackIndex(Tower target) {
        return stackIndices.computeIfAbsent(target, ignored -> LegionGoatRules.providerIndex(providers.stream()
                .filter(goat -> eligibility.test(goat, target))
                .toList(), owner, maxStacks));
    }
}
