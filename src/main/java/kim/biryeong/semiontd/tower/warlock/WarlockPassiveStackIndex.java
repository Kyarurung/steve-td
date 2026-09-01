package kim.biryeong.semiontd.tower.warlock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.tower.Tower;

final class WarlockPassiveStackIndex {
    private final Map<Key, Integer> counts;

    private WarlockPassiveStackIndex(Map<Key, Integer> counts) {
        this.counts = Map.copyOf(counts);
    }

    static WarlockPassiveStackIndex capture(Collection<? extends Tower> towers) {
        HashMap<Key, Integer> counts = new HashMap<>();
        if (towers == null) {
            return new WarlockPassiveStackIndex(counts);
        }
        for (Tower tower : towers) {
            if (tower == null || tower.health() <= 0.0 || WarlockTowers.isWarlockCore(tower.type())) {
                continue;
            }
            WarlockPath path = sacrificePath(tower);
            if (path != WarlockPath.BASE) {
                counts.merge(new Key(tower.ownerPlayer(), path), 1, Integer::sum);
            }
        }
        return new WarlockPassiveStackIndex(counts);
    }

    int count(WarlockTower tower) {
        if (tower == null || tower.path() == WarlockPath.BASE) {
            return 0;
        }
        return counts.getOrDefault(new Key(tower.ownerPlayer(), tower.path()), 0);
    }

    private static WarlockPath sacrificePath(Tower tower) {
        if (WarlockTowers.isRangedSlave(tower.type())) {
            return WarlockPath.RANGED;
        }
        if (WarlockTowers.isMeleeSlave(tower.type())) {
            return WarlockPath.MELEE;
        }
        return WarlockPath.BASE;
    }

    private record Key(UUID ownerPlayer, WarlockPath path) {
    }
}
