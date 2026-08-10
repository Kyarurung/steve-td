package kim.biryeong.semiontd.tower.warlock;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
final class WarlockConfig {
    static final WarlockConfig RUNTIME = new WarlockConfig();
    static final boolean AWAKENING_ENABLED = false;

    private WarlockConfig() {
    }

    double value(Ability ability) {
        return TowerBalanceRuntime.ability(ability.configId(), ability.key());
    }

    int integer(Ability ability) {
        return TowerBalanceRuntime.abilityInt(ability.configId(), ability.key());
    }

    enum Ability {
        DAMAGE_SOFT_CAP(Scope.GLOBAL, "damageSoftCap"),
        SACRIFICE_RADIUS(Scope.GLOBAL, "sacrificeRadius"),
        MIN_INTERVAL(Scope.GLOBAL, "minInterval"),
        SPEED_CAP(Scope.GLOBAL, "speedCap"),
        AWAKENING_ABSORPTIONS(Scope.GLOBAL, "awakeningAbsorptions"),
        AWAKENING_THRESHOLD(Scope.GLOBAL, "awakeningThreshold"),
        BASE_RADIUS(Scope.BASE, "sacrificeRadius"),
        BASE_HEAL(Scope.BASE, "fatalHeal"),
        BASE_PERMANENT_HEALTH(Scope.BASE, "permanentHealth"),
        BASE_PERMANENT_DAMAGE(Scope.BASE, "permanentDamage"),
        RANGED_THRESHOLD(Scope.RANGED, "threshold"),
        RANGED_ROUND_STAT(Scope.RANGED, "roundStat"),
        RANGED_PERMANENT_HEALTH(Scope.RANGED, "permanentHealth"),
        RANGED_PERMANENT_DAMAGE(Scope.RANGED, "permanentDamage"),
        RANGED_LIFE_EVERY(Scope.RANGED, "lifeEvery"),
        RANGED_LIFE_STEP(Scope.RANGED, "lifeStep"),
        RANGED_LIFE_CAP(Scope.RANGED, "lifeCap"),
        RANGED_SPLASH_STEP(Scope.RANGED, "splashStep"),
        RANGED_SPLASH_CAP(Scope.RANGED, "splashCap"),
        RANGED_SPLASH_DAMAGE(Scope.RANGED, "splashDamage"),
        RANGED_DEFENSE_THRESHOLD(Scope.RANGED, "defenseThreshold"),
        RANGED_DEFENSE(Scope.RANGED, "defense"),
        RANGED_PET_HEALTH(Scope.RANGED, "petHealth"),
        RANGED_PET_HEALTH_CAP(Scope.RANGED, "petHealthCap"),
        RANGED_PET_DAMAGE(Scope.RANGED, "petDamage"),
        RANGED_PET_DAMAGE_CAP(Scope.RANGED, "petDamageCap"),
        RANGED_AWAKENING_HEAL(Scope.RANGED, "awakeningHeal"),
        RANGED_AWAKENING_REGENERATION(Scope.RANGED, "awakeningRegeneration"),
        RANGED_AWAKENING_REGENERATION_TICKS(Scope.RANGED, "awakeningRegenerationTicks"),
        MELEE_THRESHOLD(Scope.MELEE, "threshold"),
        MELEE_ROUND_STAT(Scope.MELEE, "roundStat"),
        MELEE_PERMANENT_HEALTH(Scope.MELEE, "permanentHealth"),
        MELEE_PERMANENT_DAMAGE(Scope.MELEE, "permanentDamage"),
        MELEE_LIFE_STEP(Scope.MELEE, "lifeStep"),
        MELEE_LIFE_CAP(Scope.MELEE, "lifeCap"),
        MELEE_SPEED_STEP(Scope.MELEE, "speedStep"),
        MELEE_SPLASH_STEP(Scope.MELEE, "splashStep"),
        MELEE_SPLASH_CAP(Scope.MELEE, "splashCap"),
        MELEE_SPLASH_DAMAGE(Scope.MELEE, "splashDamage"),
        MELEE_DEFENSE_EVERY(Scope.MELEE, "defenseEvery"),
        MELEE_DEFENSE_STEP(Scope.MELEE, "defenseStep"),
        MELEE_DEFENSE_CAP(Scope.MELEE, "defenseCap"),
        MELEE_PET_HEALTH(Scope.MELEE, "petHealth"),
        MELEE_PET_HEALTH_CAP(Scope.MELEE, "petHealthCap"),
        MELEE_PET_DAMAGE(Scope.MELEE, "petDamage"),
        MELEE_PET_DAMAGE_CAP(Scope.MELEE, "petDamageCap"),
        MELEE_AWAKENING_DAMAGE(Scope.MELEE, "awakeningDamage"),
        MELEE_AWAKENING_MOVE_SPEED(Scope.MELEE, "awakeningMoveSpeed");

        private final Scope scope;
        private final String key;

        Ability(Scope scope, String key) {
            this.scope = scope;
            this.key = key;
        }

        String configId() {
            return switch (scope) {
                case GLOBAL -> WarlockTowers.CONFIG_ID;
                case BASE -> WarlockTowers.BASE_WARLOCK_TOWER.id();
                case RANGED -> WarlockTowers.RANGED_WARLOCK_TOWER.id();
                case MELEE -> WarlockTowers.MELEE_WARLOCK_TOWER.id();
            };
        }

        String key() {
            return key;
        }
    }

    private enum Scope {
        GLOBAL,
        BASE,
        RANGED,
        MELEE
    }
}
