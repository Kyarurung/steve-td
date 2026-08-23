package kim.biryeong.semiontd.tower.warlock;

import kim.biryeong.semiontd.tower.TowerType;

public final class WarlockConfig {
    public static final WarlockConfig RUNTIME = new WarlockConfig(
            new WarlockRuleFactory(new WarlockConfigReader())
    );

    private final WarlockRuleFactory rules;

    private WarlockConfig(WarlockRuleFactory rules) {
        this.rules = rules;
    }

    WarlockRules.PathRule path(WarlockPath path) {
        return rules.path(path);
    }

    WarlockRules.CombatRule combat() {
        return rules.combat();
    }

    WarlockRules.AwakeningRule awakening(WarlockPath path) {
        return rules.awakening(path);
    }

    public int requiredAwakeningKills() {
        return rules.requiredAwakeningKills();
    }

    WarlockRules.DeathEffectRule deathEffect(TowerType type) {
        return rules.deathEffect(type);
    }
}
