package kim.biryeong.semiontd.tower;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.UUID;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import org.junit.jupiter.api.Test;

class TowerDamagePipelineTest {
    @Test
    void towerFinalDamageIsIncludedInResolvedDamageCap() {
        double baseDamage = 250.0;
        double damageWithPrimaryDoubleEdgedSword = baseDamage * 1.25;
        double damageAfterFinalBonus = Tower.applyOutgoingDamageStages(damageWithPrimaryDoubleEdgedSword, damage -> damage, damage -> damage * 1.10);
        double outgoingDamage = Math.min(250.0, damageAfterFinalBonus);
        assertEquals(250.0, outgoingDamage, 0.0001);
    }

    @Test
    void roundDamageStatsResetAtWaveStartAndSurviveUpgrade() {
        TowerType tierOneType = type("round_stats_t1", "Round Stats T1");
        TowerType tierTwoType = type("round_stats_t2", "Round Stats T2");
        ProductionTower tierOne = tower(tierOneType);
        tierOne.markWaveStarted(3);
        tierOne.syncHealth(80.0);
        assertEquals(0.0, tierOne.roundDamageTaken(), 0.0001);
        tierOne.recordDamageDealt(125.5);
        tierOne.recordIgniteDamageDealt(12.5);
        tierOne.recordDamageTaken(40.25);
        tierOne.recordDamageDealt(Double.NaN);
        tierOne.recordDamageTaken(-5.0);

        ProductionTower tierTwo = tower(tierTwoType);
        tierTwo.copyFrom(tierOne, 50);

        assertEquals(125.5, tierTwo.roundDamageDealt(), 0.0001);
        assertEquals(12.5, tierTwo.roundIgniteDamageDealt(), 0.0001);
        assertEquals(40.25, tierTwo.roundDamageTaken(), 0.0001);
        assertSame(tierOneType, tierTwo.roundCombatType());

        tierTwo.markWaveStarted(4);

        assertEquals(0.0, tierTwo.roundDamageDealt(), 0.0001);
        assertEquals(0.0, tierTwo.roundIgniteDamageDealt(), 0.0001);
        assertEquals(0.0, tierTwo.roundDamageTaken(), 0.0001);
        assertSame(tierTwoType, tierTwo.roundCombatType());
    }

    private static ProductionTower tower(TowerType type) {
        return new ProductionTower(
                type,
                UUID.fromString("00000000-0000-0000-0000-000000000201"),
                TeamId.RED,
                1,
                new GridPosition(0, 0, 0)
        );
    }

    private static TowerType type(String id, String name) {
        return new TowerType(id, name, TowerCategory.DIRECT, 10, 100.0, 5.0, 10.0, 20, 0);
    }
}
