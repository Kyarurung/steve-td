package kim.biryeong.semiontd.tower.ocean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import kim.biryeong.semiontd.game.PlayerLane;

final class OceanSupplySnapshot {
    private final List<OceanTower> combatTowers;
    private final Map<OceanWaterTower, List<OceanTower>> targetsBySource;
    private final Map<OceanTower, Integer> sourceCounts;

    private OceanSupplySnapshot(
            List<OceanTower> combatTowers,
            Map<OceanWaterTower, List<OceanTower>> targetsBySource,
            Map<OceanTower, Integer> sourceCounts
    ) {
        this.combatTowers = combatTowers;
        this.targetsBySource = targetsBySource;
        this.sourceCounts = sourceCounts;
    }

    static OceanSupplySnapshot capture(PlayerLane lane, OceanConfig config) {
        if (lane == null) {
            return empty();
        }
        ArrayList<OceanTower> combatTowers = new ArrayList<>();
        ArrayList<OceanWaterTower> waterSources = new ArrayList<>();
        lane.towers().forEach(tower -> {
            if (tower instanceof OceanTower combatTower && combatTower.health() > 0.0) {
                combatTowers.add(combatTower);
            } else if (tower instanceof OceanWaterTower waterSource && waterSource.health() > 0.0
                    && !waterSource.deployedAtFinalDefense()) {
                waterSources.add(waterSource);
            }
        });

        IdentityHashMap<OceanWaterTower, List<OceanTower>> targetsBySource = new IdentityHashMap<>();
        IdentityHashMap<OceanTower, Integer> sourceCounts = new IdentityHashMap<>();
        for (OceanWaterTower source : waterSources) {
            double radius = config.value(source.type(), OceanAbilityKey.SUPPLY_RADIUS);
            double radiusSqr = radius * radius;
            ArrayList<OceanTower> targets = new ArrayList<>();
            for (OceanTower target : combatTowers) {
                if (distanceSqr(source, target) <= radiusSqr) {
                    targets.add(target);
                    sourceCounts.merge(target, 1, Integer::sum);
                }
            }
            targetsBySource.put(source, List.copyOf(targets));
        }
        return new OceanSupplySnapshot(
                List.copyOf(combatTowers),
                Collections.unmodifiableMap(targetsBySource),
                Collections.unmodifiableMap(sourceCounts)
        );
    }

    List<OceanTower> combatTowers() {
        return combatTowers;
    }

    List<OceanTower> targetsFor(OceanWaterTower source) {
        return targetsBySource.getOrDefault(source, List.of());
    }

    int sourceCountFor(OceanTower target) {
        return sourceCounts.getOrDefault(target, 0);
    }

    private static OceanSupplySnapshot empty() {
        return new OceanSupplySnapshot(List.of(), Map.of(), Map.of());
    }

    private static double distanceSqr(OceanWaterTower source, OceanTower target) {
        return OceanRules.distanceSquared(
                source.originalPosition().x(), source.originalPosition().y(), source.originalPosition().z(),
                target.originalPosition().x(), target.originalPosition().y(), target.originalPosition().z()
        );
    }
}
