package kim.biryeong.semiontd.tower.animal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceConfig.TowerStats;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AnimalConfigMergeTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void missingLeaderDefaultsMergeWithoutOverwritingExistingValues() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        Map<String, TowerStats> towers = new LinkedHashMap<>();
        towers.put(AnimalTowers.T3_PIG_TOWER.id(), new TowerStats(999L, null, null, null, null, null));
        Map<String, Long> costs = Map.of("t2_pig_tower->t3_pig_tower", 777L);
        Map<String, Map<String, Double>> abilities = Map.of(
                AnimalTowers.T3_PIG_TOWER.id(), Map.of("damagePerStack", 123.0)
        );

        TowerBalanceConfig merged = new TowerBalanceConfig(towers, costs, abilities)
                .withMissingDefaults(defaults);

        assertEquals(999L, merged.towers().get(AnimalTowers.T3_PIG_TOWER.id()).mineralCost());
        assertEquals(400.0, merged.towers().get(AnimalTowers.T3_PIG_TOWER.id()).maxHealth());
        assertEquals(777L, merged.upgradeCost("t2_pig_tower", "t3_pig_tower", -1));
        assertEquals(123.0, merged.ability(AnimalTowers.T3_PIG_TOWER.id(), "damagePerStack", -1.0));
        assertEquals(
                defaults.towers().get(AnimalTowers.T4_PIG_LEADER_TOWER.id()),
                merged.towers().get(AnimalTowers.T4_PIG_LEADER_TOWER.id())
        );
        assertEquals(350L, merged.upgradeCost("t3_pig_tower", "t4_pig_leader_tower", -1));
        assertEquals(0.15, merged.ability(
                AnimalTowers.T4_PIG_LEADER_TOWER.id(), "leaderMaxHealthBonus", -1.0
        ));
    }
}
