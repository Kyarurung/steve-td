package kim.biryeong.semiontd.tower.nether;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.job.JobRegistry;
import kim.biryeong.semiontd.job.NetherTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.TowerType;

public final class NetherTowerCatalogs {
    private NetherTowerCatalogs() {
    }

    public static void register() {
        registerTower(NetherTowers.T1_STRIDER, 1);
        registerTower(NetherTowers.T2_PIGLIN, 2);
        registerTower(NetherTowers.T3_PIGLIN_BRUTE, 3);
        registerTower(NetherTowers.T1_HOGLIN, 1);
        registerTower(NetherTowers.T2_ZOGLIN, 2);
        registerTower(NetherTowers.T3_ZOMBIFIED_PIGLIN, 3);
        registerTower(NetherTowers.T1_MAGMA_CUBE, 1);
        registerTower(NetherTowers.T2_BLAZE, 2);
        registerTower(NetherTowers.T3_GHAST, 3);
        registerTower(NetherTowers.T1_SKELETON, 1);
        registerTower(NetherTowers.T2_WITHER_SKELETON, 2);
        registerTower(NetherTowers.T3_WITHER, 3);

        link(NetherTowers.T1_STRIDER, NetherTowers.T2_PIGLIN.id(), "피글린", NetherTowers.T2_PIGLIN);
        link(NetherTowers.T2_PIGLIN, NetherTowers.T3_PIGLIN_BRUTE.id(), "피글린 야수", NetherTowers.T3_PIGLIN_BRUTE);
        link(NetherTowers.T1_HOGLIN, NetherTowers.T2_ZOGLIN.id(), "조글린", NetherTowers.T2_ZOGLIN);
        link(NetherTowers.T2_ZOGLIN, NetherTowers.T3_ZOMBIFIED_PIGLIN.id(), "좀비화 피글린", NetherTowers.T3_ZOMBIFIED_PIGLIN);
        link(NetherTowers.T1_MAGMA_CUBE, NetherTowers.T2_BLAZE.id(), "블레이즈", NetherTowers.T2_BLAZE);
        link(NetherTowers.T2_BLAZE, NetherTowers.T3_GHAST.id(), "가스트", NetherTowers.T3_GHAST);
        link(NetherTowers.T1_SKELETON, NetherTowers.T2_WITHER_SKELETON.id(), "위더 스켈레톤", NetherTowers.T2_WITHER_SKELETON);
        link(NetherTowers.T2_WITHER_SKELETON, NetherTowers.T3_WITHER.id(), "위더", NetherTowers.T3_WITHER);

        if (JobRegistry.find(NetherTowerJob.ID).isEmpty()) {
            JobRegistry.registerIfAbsent(new NetherTowerJob());
        }
    }

    private static void registerTower(TowerType type, int tier) {
        if (ProductionTowerCatalog.find(type.id()).isPresent()) {
            return;
        }
        TowerType resolvedType = TowerBalanceRuntime.resolve(type);
        if (tier == 1) {
            ProductionTowerCatalog.registerStarter(resolvedType, NetherTower::new);
            return;
        }
        ProductionTowerCatalog.register(resolvedType, NetherTower::new, tier);
    }

    private static void link(TowerType from, String id, String displayName, TowerType to) {
        if (ProductionTowerCatalog.upgrade(from, id).isPresent()) {
            return;
        }
        TowerType targetType = ProductionTowerCatalog.find(to.id()).map(ProductionTowerCatalog.CatalogEntry::type).orElse(to);
        ProductionTowerCatalog.linkUpgrade(from, id, displayName, targetType, TowerBalanceRuntime.upgradeCost(from, id));
    }
}
