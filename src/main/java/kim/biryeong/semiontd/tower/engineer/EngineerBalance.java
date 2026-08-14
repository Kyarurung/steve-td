package kim.biryeong.semiontd.tower.engineer;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;

public final class EngineerBalance {
    public static final String GLOBAL_ID = "engineer_global";

    public static final int ACTIVE_TICKS = 60;
    public static final int PLATE_COOLDOWN_TICKS = 100;
    public static final double GOLEM_MOVE_SPEED = 0.18;
    public static final int PISTON_IMMUNITY_TICKS = 200;
    public static final int DOOR_RETARGET_TICKS = 10;
    public static final int TNT_FUSE_TICKS = 60;
    public static final int MAX_REDSTONE = 35;
    public static final int MAX_PLATES = 4;
    public static final int MAX_PISTONS = 4;
    public static final double DISPENSER_DAMAGE_PER_PLATE_BLOCK = 0.20;

    private EngineerBalance() {
    }

    public static int activeTicks() {
        return TowerBalanceRuntime.abilityTicks(GLOBAL_ID, "activeTicks", ACTIVE_TICKS);
    }

    public static int plateCooldownTicks() {
        return TowerBalanceRuntime.abilityTicks(GLOBAL_ID, "plateCooldownTicks", PLATE_COOLDOWN_TICKS);
    }

    public static double golemMoveSpeed() {
        return TowerBalanceRuntime.ability(GLOBAL_ID, "golemMoveSpeed", GOLEM_MOVE_SPEED);
    }

    public static int pistonImmunityTicks() {
        return TowerBalanceRuntime.abilityTicks(GLOBAL_ID, "pistonImmunityTicks", PISTON_IMMUNITY_TICKS);
    }

    public static int doorRetargetTicks() {
        return TowerBalanceRuntime.abilityTicks(GLOBAL_ID, "doorRetargetTicks", DOOR_RETARGET_TICKS);
    }

    public static int tntFuseTicks() {
        return TowerBalanceRuntime.abilityTicks(GLOBAL_ID, "tntFuseTicks", TNT_FUSE_TICKS);
    }

    public static int maxRedstone() {
        return TowerBalanceRuntime.abilityInt(GLOBAL_ID, "maxRedstone", MAX_REDSTONE);
    }

    public static int maxPlates() {
        return TowerBalanceRuntime.abilityInt(GLOBAL_ID, "maxPlates", MAX_PLATES);
    }

    public static int maxPistons() {
        return TowerBalanceRuntime.abilityInt(GLOBAL_ID, "maxPistons", MAX_PISTONS);
    }

    public static double dispenserDamageMultiplier(int plateDistance) {
        return 1.0 + Math.max(0, plateDistance)
                * TowerBalanceRuntime.ability(
                        GLOBAL_ID,
                        "dispenserDamagePerPlateBlock",
                        DISPENSER_DAMAGE_PER_PLATE_BLOCK
                );
    }
}
