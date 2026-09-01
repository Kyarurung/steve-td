package kim.biryeong.semiontd.tower.illager;

final class IllagerRaidRules {
    private final IllagerConfig config;

    IllagerRaidRules(IllagerConfig config) {
        this.config = config;
    }

    int gaugeMax() {
        return Math.max(1, config.globalInt(IllagerAbilityKey.GAUGE_MAX));
    }

    int killGauge(boolean incomeMonster, boolean marked) {
        int amount = config.globalInt(incomeMonster
                ? IllagerAbilityKey.INCOME_KILL_GAUGE
                : IllagerAbilityKey.WAVE_KILL_GAUGE);
        if (marked) {
            amount += config.globalInt(IllagerAbilityKey.MARKED_KILL_BONUS_GAUGE);
        }
        return amount;
    }

    int towerDeathGauge() {
        return config.globalInt(IllagerAbilityKey.ILLAGER_TOWER_DEATH_GAUGE);
    }

    int timedEffectTicks() {
        return Math.max(1, config.globalTicks(IllagerAbilityKey.TIMED_EFFECT_DURATION_TICKS));
    }

    double attackSpeedBonus(int towerCount) {
        return cappedTowerBonus(
                towerCount,
                config.global(IllagerAbilityKey.ATTACK_SPEED_PERCENT_PER_TOWER),
                config.global(IllagerAbilityKey.ATTACK_SPEED_BONUS_CAP)
        );
    }

    double damageBonus(int towerCount) {
        return cappedTowerBonus(
                towerCount,
                config.global(IllagerAbilityKey.DAMAGE_PERCENT_PER_TOWER),
                config.global(IllagerAbilityKey.DAMAGE_BONUS_CAP)
        );
    }

    static double cappedTowerBonus(int towerCount, double bonusPerTower, double cap) {
        return Math.min(cap, Math.max(0, towerCount) * bonusPerTower);
    }
}
