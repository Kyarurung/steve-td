package kim.biryeong.semiontd.entity.tower;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class TowerAttackVfxGameTest {
    @GameTest
    public void attackRangeChoosesExpectedHitParticle(GameTestHelper context) {
        if (TowerAttackVfxService.visualKind(3.0) != TowerAttackVfxService.AttackVisualKind.MELEE) {
            throw new AssertionError("Expected range 3.0 to use melee sweep_attack hit VFX");
        }
        if (TowerAttackVfxService.visualKind(3.01) != TowerAttackVfxService.AttackVisualKind.RANGED) {
            throw new AssertionError("Expected range above 3.0 to use ranged crit hit VFX");
        }
        context.succeed();
    }
}
