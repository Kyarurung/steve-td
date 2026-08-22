package kim.biryeong.semiontd.tower.body;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.TowerType;

/** 신체 빌더 능력값을 tower_balance.json과 연결합니다. */
public final class BodyBalance {
    private BodyBalance() {
    }

    public static double ability(TowerType type, String key, double fallback) {
        return TowerBalanceRuntime.ability(type.id(), key, fallback);
    }

    public static int abilityInt(TowerType type, String key, int fallback) {
        return TowerBalanceRuntime.abilityInt(type.id(), key, fallback);
    }

    public static int heartMaxDeathStacks(TowerType type) {
        return abilityInt(type, "maxDeathStacks", switch (BodyTowers.tier(type)) {
            case 2 -> 60;
            case 3 -> 120;
            default -> 0;
        });
    }

    public static int heartStacksPerIntervalReduction(TowerType type) {
        return abilityInt(type, "stacksPerIntervalReduction", 15);
    }

    public static double brainSplashRadius(TowerType type) {
        return ability(type, "splashRadius", switch (BodyTowers.tier(type)) {
            case 2 -> 3.5;
            case 3 -> 4.5;
            default -> 2.5;
        });
    }

    public static double brainDamageTaken(TowerType type) {
        return ability(type, "damageTaken", switch (BodyTowers.tier(type)) {
            case 2 -> 0.15;
            case 3 -> 0.20;
            default -> 0.10;
        });
    }

    public static double brainAttackReduction(TowerType type) {
        return ability(type, "attackReduction", switch (BodyTowers.tier(type)) {
            case 2 -> 0.12;
            case 3 -> 0.16;
            default -> 0.08;
        });
    }

    public static int brainDebuffTicks(TowerType type) {
        return abilityInt(type, "debuffTicks", 100);
    }

    public static double skinReductionPerStack(TowerType type) {
        return ability(type, "damageReductionPerStack", BodyTowers.tier(type) >= 3 ? 0.11 : 0.06);
    }

    public static int skinReductionTicks(TowerType type) {
        return abilityInt(type, "damageReductionTicks", 80);
    }

    public static double eyeWidth(TowerType type) {
        return ability(type, "lineWidth", switch (BodyTowers.tier(type)) {
            case 2 -> 1.6;
            case 3 -> 2.0;
            default -> 1.25;
        });
    }

    public static int genitalExtraTargets(TowerType type) {
        return abilityInt(type, "extraTargets", Math.max(0, BodyTowers.tier(type) - 1));
    }

    public static double genitalExtraTargetRadius(TowerType type) {
        return ability(type, "extraTargetRadius", 4.0);
    }

    public static double genitalMagicDamage(TowerType type) {
        return ability(type, "magicProcDamage", switch (BodyTowers.tier(type)) {
            case 2 -> 14.0;
            case 3 -> 27.0;
            default -> 5.0;
        });
    }

    public static double genitalSlow(TowerType type) {
        return ability(type, "slow", 0.35);
    }

    public static int genitalSlowTicks(TowerType type) {
        return abilityInt(type, "slowTicks", 40);
    }
}
