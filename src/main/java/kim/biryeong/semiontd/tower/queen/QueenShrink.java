package kim.biryeong.semiontd.tower.queen;

import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.entity.monster.MonsterDataKey;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import net.minecraft.resources.ResourceLocation;

public final class QueenShrink {
    private static final MonsterDataKey<Double> POINTS = new MonsterDataKey<>(
            ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "queen_shrink_points"), Double.class);

    private QueenShrink() {}

    public static boolean apply(SemionMonsterEntity target, double points) {
        if (target == null || target.runtimeMonster() == null || !target.isAlive()
                || !Double.isFinite(points) || points <= 0.0) return false;
        double factor = Math.pow(QueenBalance.shrinkFactorPerPoint(), points);
        target.applyPermanentStatScale(factor, QueenBalance.minimumVisualScale());
        target.runtimeMonster().setData(POINTS, points(target) + points);
        return true;
    }

    public static double points(SemionMonsterEntity target) {
        return target == null || target.runtimeMonster() == null
                ? 0.0 : target.runtimeMonster().getData(POINTS).orElse(0.0);
    }
}
