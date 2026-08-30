package kim.biryeong.semiontd.tower.end;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import kim.biryeong.semiontd.game.PlayerLane;
import org.junit.jupiter.api.Test;

class EndTransferHealingTest extends EndTestFixture {
    @Test
    void shulkerStacksGrantCappedRegenerationThatHealsOncePerSecond() {
        applyEndAbilities(Map.of(
                "transferTicks", 1.0,
                "transferHeal", 0.0,
                "roundHealthRatio", 0.0,
                "permanentHealthRatio", 0.0,
                "regenerationStacks", 1.0,
                "regenerationStep", 2.0,
                "regenerationCap", 3.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        lane.addTower(tower(EndTowers.T1_SHULKER_TOWER, 1));
        lane.addTower(tower(EndTowers.T1_SHULKER_TOWER, 2));
        dragon.tick(lane);
        assertEquals(3.0, regenerationPerSecond(dragon), 0.0001);
        dragon.syncHealth(10.0);
        tick(dragon, lane, 18);
        assertEquals(10.0, dragon.health(), 0.0001);
        dragon.tick(lane);
        assertEquals(13.0, dragon.health(), 0.0001);
    }

    @Test
    void activeEndCrystalLineTransfersDoNotGrantPeriodicHealing() {
        applyEndAbilities(Map.of(
                "transferTicks", 40.0,
                "transferHeal", 0.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        dragon.syncHealth(10.0);
        lane.addTower(tower(EndTowers.T1_ENDERMITE_TOWER, 1));
        lane.addTower(tower(EndTowers.T1_ENDERMITE_TOWER, 2));
        tick(dragon, lane, 19);
        assertEquals(10.0, dragon.health(), 0.0001);
        dragon.tick(lane);
        assertEquals(10.0, dragon.health(), 0.0001);
        tick(dragon, lane, 20);
        assertEquals(10.0, dragon.health(), 0.0001);
        assertEquals(2, dragon.transferStats().roundCompletedCount());
    }

    @Test
    void activeShulkerTransfersHealFivePercentOfTraitFreeBaseMaxHealthPerSecond() {
        applyEndAbilities(Map.ofEntries(
                Map.entry("transferTicks", 40.0),
                Map.entry("transferHeal", 0.0),
                Map.entry("roundHealthRatio", 0.0),
                Map.entry("permanentHealthRatio", 0.0),
                Map.entry("transferHealRatio", 0.05)
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        dragon.syncHealth(10.0);
        lane.addTower(tower(EndTowers.T1_SHULKER_TOWER, 1));
        lane.addTower(tower(EndTowers.T2_SHULKER_TOWER, 2));
        lane.addTower(tower(EndTowers.T3_SHULKER_TOWER, 3));
        tick(dragon, lane, 19);
        assertEquals(10.0, dragon.health(), 0.0001);
        dragon.tick(lane);
        assertEquals(32.5, dragon.health(), 0.0001);
    }

    @Test
    void completedTransferDoesNotReceiveAnExtraPeriodicTransferHeal() {
        applyEndAbilities(Map.of(
                "transferTicks", 1.0,
                "transferHeal", 0.0
        ));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.syncHealth(10.0);
        lane.addTower(tower(EndTowers.T1_ENDERMITE_TOWER, 1));
        dragon.tick(lane);
        assertEquals(10.0, dragon.health(), 0.0001);
        assertEquals(1, dragon.transferStats().roundCompletedCount());
    }

    @Test
    void activeTransferKeepsItsCompletionHealWhenBalanceReloads() {
        applyEndAbilities(Map.of("transferTicks", 4.0, "transferHeal", 30.0));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower feeder = tower(EndTowers.T1_ENDERMITE_TOWER, 1);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        dragon.syncHealth(10.0);
        lane.addTower(feeder);
        tick(dragon, lane, 2);
        applyEndAbilities(Map.of("transferHeal", 999.0));
        tick(dragon, lane, 2);
        assertEquals(40.0, dragon.health(), 0.0001);
    }

    @Test
    void activeTransferKeepsItsPeriodicHealingRatioWhenBalanceReloads() {
        applyEndAbilities(Map.of("transferTicks", 60.0, "transferHealRatio", 0.05));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower shulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.tick(lane);
        dragon.syncHealth(10.0);
        lane.addTower(shulker);
        tick(dragon, lane, 20);
        assertEquals(15.0, dragon.health(), 0.0001);
        applyEndAbilities(Map.of("transferHealRatio", 1.0));
        tick(dragon, lane, 20);
        assertEquals(20.0, dragon.health(), 0.0001);
    }

    @Test
    void staggeredShulkerTransfersHealOnTheirOwnIntervals() {
        applyEndAbilities(Map.of("transferTicks", 100.0, "transferHealRatio", 0.05));
        PlayerLane lane = lane();
        EndTower dragon = tower(EndTowers.BASE_END_TOWER, 0);
        EndTower firstShulker = tower(EndTowers.T1_SHULKER_TOWER, 1);
        EndTower secondShulker = tower(EndTowers.T1_SHULKER_TOWER, 2);
        lane.addTower(dragon);
        dragon.onWaveStarted(lane, 1);
        dragon.syncHealth(10.0);

        lane.addTower(firstShulker);
        tick(dragon, lane, 10);
        lane.addTower(secondShulker);
        tick(dragon, lane, 10);
        assertEquals(15.0, dragon.health(), 0.0001);

        tick(dragon, lane, 9);
        assertEquals(15.0, dragon.health(), 0.0001);
        dragon.tick(lane);
        assertEquals(20.0, dragon.health(), 0.0001);
    }

}
