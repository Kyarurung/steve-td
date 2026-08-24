package kim.biryeong.semiontd.tower.end;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import kim.biryeong.semiontd.entity.visual.BlockDisplayVisual;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class EndEvolutionTest extends EndTestFixture {
    @Test
    void oneTowerIsAnEggDuringPreparationAndSwitchesToPhantomAtWaveStart() {
        EndTower tower = core();
        assertEquals(EndTowerState.EGG, tower.state());
        assertTrue(BlockDisplayVisual.matches(tower.visual()));
        tower.onWaveStarted(null, 1);
        assertEquals(EndTowerState.PHANTOM, tower.state());
        assertEquals(EndTowers.BASE_END_TOWER, tower.type());
        assertEquals("minecraft:phantom", tower.visual().entityTypeId());
        assertTrue(tower.visual().blockbenchModel().isEmpty());
    }

    @Test
    void phantomScaleStartsAtOneAndGrowsByPointTwoPerHundredMaxHealth() {
        EndTower tower = core();
        assertEquals(1.2, tower.phantomScaleForMaxHealth(100.0), 0.0001);
        assertEquals(1.3, tower.phantomScaleForMaxHealth(150.0), 0.0001);
        assertEquals(1.4, tower.phantomScaleForMaxHealth(200.0), 0.0001);
        assertEquals(1.6, tower.phantomScaleForMaxHealth(300.0), 0.0001);
        assertEquals(5.0, tower.phantomScaleForMaxHealth(5000.0), 0.0001);
        assertEquals(1.0, tower.phantomScaleForMaxHealth(Double.NaN), 0.0001);
    }

    @Test
    void phantomScaleFormulaComesFromEndGlobalConfig() {
        applyEndAbilities(Map.of(
                "phantomScaleHealth", 50.0,
                "phantomScaleStep", 0.25,
                "phantomScaleBase", 0.5,
                "phantomScaleCap", 1.25
        ));
        EndTower tower = core();
        assertEquals(1.0, tower.phantomScaleForMaxHealth(100.0), 0.0001);
        assertEquals(1.25, tower.phantomScaleForMaxHealth(1000.0), 0.0001);
    }

    @Test
    void phantomBecomesVanillaDragonWhenMaxHealthReachesThreshold() {
        double baseMaxHealth = EndTowers.BASE_END_TOWER.maxHealth();
        applyStateConfig(baseMaxHealth + 0.01);
        EndTower tower = core();
        tower.onWaveStarted(null, 1);
        tower.tick(null);
        assertEquals(EndTowerState.PHANTOM, tower.state());
        applyStateConfig(baseMaxHealth);
        tower.tick(null);
        assertEquals(EndTowerState.DRAGON, tower.state());
        assertEquals("minecraft:ender_dragon", tower.visual().entityTypeId());
        assertTrue(tower.visual().blockbenchModel().isEmpty());
        assertEquals(1.0, tower.visual().scale(), 0.0001);
        assertEquals(EndTowers.BASE_END_TOWER.range() + EndConfig.RUNTIME.dragon().rangeBonus(), tower.adjustAttackRange(EndTowers.BASE_END_TOWER.range()), 0.0001);
        assertEquals(EndTowers.BASE_END_TOWER.damage(), tower.modifyAttackDamage(null, null, EndTowers.BASE_END_TOWER.damage()), 0.0001);
        assertEquals(EndConfig.RUNTIME.dragon().finalDamageBonus(), tower.finalDamageBonus(), 0.0001);
    }

    @Test
    void dragonEggAndHatchedPhantomAreStatesOfOneTowerType() {
        applyTransferDuration(1);
        EndTower tower = tower(EndTowers.BASE_END_TOWER, 0);
        assertEquals(EndTowerState.EGG, tower.state());
        assertEquals(1.0, tower.entityAnchorYOffset(), 0.0001);
        assertTrue(BlockDisplayVisual.matches(tower.visual()));
        assertEquals(
                Blocks.DRAGON_EGG.defaultBlockState(),
                BlockDisplayVisual.blockState(tower.visual())
        );
        tower.onWaveStarted(null, 1);
        tower.tick(null);
        assertEquals(EndTowerState.PHANTOM, tower.state());
        assertTrue(tower.stopsBeforeFriendlyTowers());
        assertEquals(2.0, tower.entityAnchorYOffset(), 0.0001);
        assertEquals(EndTowers.BASE_END_TOWER, tower.type());
        assertEquals("minecraft:phantom", tower.visual().entityTypeId());
        assertTrue(tower.visual().blockbenchModel().isEmpty());
        assertEquals(0.0, tower.finalDamageBonus(), 0.0001);
        double dragonEvolution = EndConfig.RUNTIME.dragon().evolutionHealth();
        tower.syncMaxHealth(dragonEvolution, true);
        tower.tick(null);
        assertEquals(EndTowerState.DRAGON, tower.state());
        assertFalse(tower.stopsBeforeFriendlyTowers());
        assertEquals(2.0, tower.entityAnchorYOffset(), 0.0001);
        double dragonFinalDamage = EndConfig.RUNTIME.dragon().finalDamageBonus();
        double dragonRangeBonus = EndConfig.RUNTIME.dragon().rangeBonus();
        assertEquals(dragonFinalDamage, tower.finalDamageBonus(), 0.0001);
        assertEquals(
                EndTowers.BASE_END_TOWER.range() + dragonRangeBonus,
                tower.adjustAttackRange(EndTowers.BASE_END_TOWER.range()),
                0.0001
        );
        tower.resetForRound(null);
        assertEquals(EndTowerState.EGG, tower.state());
        assertEquals(1.0, tower.entityAnchorYOffset(), 0.0001);
        assertTrue(BlockDisplayVisual.matches(tower.visual()));
        assertEquals(EndTowers.BASE_END_TOWER.maxHealth(), tower.currentMaxHealth(), 0.0001);
    }

    private static EndTower core() {
        return tower(EndTowers.BASE_END_TOWER, 0);
    }

    private static void applyStateConfig(double evolutionMaxHealth) {
        applyEndAbilities(Map.of("dragonEvolution", evolutionMaxHealth));
    }
}
