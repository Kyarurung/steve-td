package kim.biryeong.semiontd.entity.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import kim.biryeong.semiontd.config.AttackKind;
import kim.biryeong.semiontd.config.MonsterScalingConfig;
import kim.biryeong.semiontd.game.TeamId;
import org.junit.jupiter.api.Test;

final class MonsterSurvivalScalingTest {
    @Test
    void roundDelayScalingIncreasesHealthAndAttackDamageByConfiguredPercent() {
        Monster monster = new Monster(
                "wave",
                TeamId.RED,
                1,
                Optional.empty(),
                Optional.empty(),
                100.0,
                0.0,
                10.0,
                AttackKind.MELEE,
                "minecraft:zombie",
                0
        );
        MonsterScalingConfig config = new MonsterScalingConfig(true, 0, 600, 1, 3.0, 3.0, true, true);

        boolean scaled = monster.tickSurvivalScaling(config, 0);

        assertEquals(true, scaled);
        assertEquals(103.0, monster.maxHealth(), 0.0001);
        assertEquals(103.0, monster.health(), 0.0001);
        assertEquals(10.3, monster.attackDamage(), 0.0001);
        assertEquals(1, monster.survivalScalingStacks());
    }

    @Test
    void laneBreachDelayCanTriggerBeforeRoundDelay() {
        Monster monster = new Monster(
                "income",
                TeamId.BLUE,
                1,
                Optional.of(java.util.UUID.randomUUID()),
                Optional.of(TeamId.RED),
                200.0,
                0.0,
                20.0,
                AttackKind.MELEE,
                "minecraft:zombie",
                0
        );
        MonsterScalingConfig config = new MonsterScalingConfig(true, 0, 0, 1, 5.0, 2.0, true, true);
        monster.syncLaneProgress(1.0);

        boolean scaled = monster.tickSurvivalScaling(config, 0);

        assertEquals(true, scaled);
        assertEquals(210.0, monster.maxHealth(), 0.0001);
        assertEquals(210.0, monster.health(), 0.0001);
        assertEquals(20.4, monster.attackDamage(), 0.0001);
        assertEquals(1, monster.laneBreachTicks());
    }

    @Test
    void sourceTogglesCanExcludeIncomeMonsters() {
        Monster monster = new Monster(
                "income",
                TeamId.BLUE,
                1,
                Optional.of(java.util.UUID.randomUUID()),
                Optional.of(TeamId.RED),
                200.0,
                0.0,
                20.0,
                AttackKind.MELEE,
                "minecraft:zombie",
                0
        );
        MonsterScalingConfig config = new MonsterScalingConfig(true, 0, 0, 1, 5.0, 5.0, true, false);

        boolean scaled = monster.tickSurvivalScaling(config, 0);

        assertEquals(false, scaled);
        assertEquals(200.0, monster.maxHealth(), 0.0001);
        assertEquals(20.0, monster.attackDamage(), 0.0001);
    }
}
