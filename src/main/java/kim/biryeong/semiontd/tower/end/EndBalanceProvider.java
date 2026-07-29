package kim.biryeong.semiontd.tower.end;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;

interface EndBalanceProvider {
    EndBalanceProvider RUNTIME = new EndBalanceProvider() {
        @Override
        public double ability(String key) {
            return TowerBalanceRuntime.ability(EndTowers.CONFIG_ID, key);
        }

        @Override
        public double ability(String configId, String key) {
            return TowerBalanceRuntime.ability(configId, key);
        }

        @Override
        public int abilityInt(String key) {
            return TowerBalanceRuntime.abilityInt(EndTowers.CONFIG_ID, key);
        }

        @Override
        public int abilityTicks(String key) {
            return TowerBalanceRuntime.abilityTicks(EndTowers.CONFIG_ID, key);
        }
    };

    double ability(String key);

    double ability(String configId, String key);

    int abilityInt(String key);

    int abilityTicks(String key);
}
