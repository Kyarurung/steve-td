package kim.biryeong.semiontd.summon;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class SummonRegistry {
    private static final Map<String, SummonMonsterType> SUMMONS = new LinkedHashMap<>();

    private SummonRegistry() {
    }

    public static SummonMonsterType register(SummonMonsterType summonType) {
        Objects.requireNonNull(summonType, "summonType");
        SummonMonsterType previous = SUMMONS.putIfAbsent(summonType.id(), summonType);
        if (previous != null) {
            throw new IllegalArgumentException("Duplicate summon id: " + summonType.id());
        }
        return summonType;
    }

    public static Optional<SummonMonsterType> find(String id) {
        return Optional.ofNullable(SUMMONS.get(id));
    }

    public static Collection<SummonMonsterType> all() {
        return java.util.List.copyOf(SUMMONS.values());
    }
}
