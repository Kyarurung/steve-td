package kim.biryeong.semiontd.tower.frost;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.TowerType;

/** 혹한 빌더 타워 등록과 얼음 전위 업그레이드 그래프. */
public final class FrostTowerCatalogs {
    private FrostTowerCatalogs() {
    }

    public static void register() {
        registerVanguard(FrostTowers.ICE_VANGUARD, 1);
        registerVanguard(FrostTowers.STURDY_ICE_VANGUARD, 2);
        registerVanguard(FrostTowers.DONGTAE, 3);
        registerSplashFamily(
                FrostTowers.ICE_BREAKER_T1,
                FrostTowers.ICE_BREAKER_T2,
                FrostTowers.ICE_BREAKER_T3
        );
        registerSplashFamily(
                FrostTowers.FROZEN_DUMPLING_T1,
                FrostTowers.FROZEN_DUMPLING_T2,
                FrostTowers.FROZEN_DUMPLING_T3
        );
        registerHealingFamily();
        // Keep both cooling devices at the end of the Frost placement menu.
        ProductionTowerCatalog.registerStarter(
                TowerBalanceRuntime.resolve(FrostTowers.EMISSION_COOLING_DEVICE),
                FrostCoolingTower::new
        );
        ProductionTowerCatalog.register(
                TowerBalanceRuntime.resolve(FrostTowers.EMISSION_COOLING_DEVICE_EXPANDED),
                FrostCoolingTower::new,
                2
        );
        ProductionTowerCatalog.registerStarter(
                TowerBalanceRuntime.resolve(FrostTowers.ERUPTION_COOLING_DEVICE),
                FrostEruptionCoolingTower::new
        );
        ProductionTowerCatalog.register(
                TowerBalanceRuntime.resolve(FrostTowers.ERUPTION_COOLING_DEVICE_EXPANDED),
                FrostEruptionCoolingTower::new,
                2
        );

        link(FrostTowers.ICE_VANGUARD, FrostTowers.STURDY_ICE_VANGUARD, 90);
        link(FrostTowers.STURDY_ICE_VANGUARD, FrostTowers.DONGTAE, 170);
        link(FrostTowers.EMISSION_COOLING_DEVICE, FrostTowers.EMISSION_COOLING_DEVICE_EXPANDED, 500);
        link(FrostTowers.ERUPTION_COOLING_DEVICE, FrostTowers.ERUPTION_COOLING_DEVICE_EXPANDED, 1000);
        link(FrostTowers.ICE_BREAKER_T1, FrostTowers.ICE_BREAKER_T2, 100);
        link(FrostTowers.ICE_BREAKER_T2, FrostTowers.ICE_BREAKER_T3, 200);
        link(FrostTowers.FROZEN_DUMPLING_T1, FrostTowers.FROZEN_DUMPLING_T2, 100);
        link(FrostTowers.FROZEN_DUMPLING_T2, FrostTowers.FROZEN_DUMPLING_T3, 200);
        link(FrostTowers.ICEBOX_T1, FrostTowers.ICEBOX_T2, 145);
        link(FrostTowers.ICEBOX_T2, FrostTowers.ICEBOX_T3, 225);
    }

    private static void registerVanguard(TowerType type, int tier) {
        TowerType resolved = TowerBalanceRuntime.resolve(type);
        if (tier == 1) {
            ProductionTowerCatalog.registerStarter(resolved, FrostVanguardTower::new);
        } else {
            ProductionTowerCatalog.register(resolved, FrostVanguardTower::new, tier);
        }
    }

    private static void registerSplashFamily(TowerType tierOne, TowerType tierTwo, TowerType tierThree) {
        ProductionTowerCatalog.registerStarter(TowerBalanceRuntime.resolve(tierOne), FrostSplashTower::new);
        ProductionTowerCatalog.register(TowerBalanceRuntime.resolve(tierTwo), FrostSplashTower::new, 2);
        ProductionTowerCatalog.register(TowerBalanceRuntime.resolve(tierThree), FrostSplashTower::new, 3);
    }

    private static void registerHealingFamily() {
        ProductionTowerCatalog.registerStarter(
                TowerBalanceRuntime.resolve(FrostTowers.ICEBOX_T1),
                FrostHealingTower::new
        );
        ProductionTowerCatalog.register(
                TowerBalanceRuntime.resolve(FrostTowers.ICEBOX_T2),
                FrostHealingTower::new,
                2
        );
        ProductionTowerCatalog.register(
                TowerBalanceRuntime.resolve(FrostTowers.ICEBOX_T3),
                FrostHealingTower::new,
                3
        );
    }

    private static void link(TowerType from, TowerType to, long fallbackCost) {
        TowerType target = ProductionTowerCatalog.find(to.id())
                .map(ProductionTowerCatalog.CatalogEntry::type)
                .orElse(to);
        ProductionTowerCatalog.linkUpgrade(
                from,
                to.id(),
                to.displayName(),
                target,
                TowerBalanceRuntime.upgradeCost(from, to.id(), fallbackCost)
        );
    }
}
