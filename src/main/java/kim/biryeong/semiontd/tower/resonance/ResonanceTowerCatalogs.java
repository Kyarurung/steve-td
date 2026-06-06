package kim.biryeong.semiontd.tower.resonance;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.job.JobRegistry;
import kim.biryeong.semiontd.job.ResonanceTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.TowerType;

public final class ResonanceTowerCatalogs {
    private ResonanceTowerCatalogs() {
    }

    public static void register() {
        registerTower(ResonanceTowers.FOCUS_CRYSTAL, 1);
        registerTower(ResonanceTowers.FOCUS_PRISM, 2);
        registerTower(ResonanceTowers.FOCUS_CORE, 3);
        registerTower(ResonanceTowers.WAVE_CRYSTAL, 1);
        registerTower(ResonanceTowers.WAVE_PRISM, 2);
        registerTower(ResonanceTowers.WAVE_CORE, 3);
        registerTower(ResonanceTowers.FROST_CRYSTAL, 1);
        registerTower(ResonanceTowers.FROST_PRISM, 2);
        registerTower(ResonanceTowers.FROST_CORE, 3);
        registerTower(ResonanceTowers.AMPLIFY_CRYSTAL, 1);
        registerTower(ResonanceTowers.AMPLIFY_PRISM, 2);
        registerTower(ResonanceTowers.AMPLIFY_CORE, 3);

        link(ResonanceTowers.FOCUS_CRYSTAL, ResonanceTowers.FOCUS_PRISM.id(), "해바라기 집속 무블룸", ResonanceTowers.FOCUS_PRISM);
        link(ResonanceTowers.FOCUS_PRISM, ResonanceTowers.FOCUS_CORE.id(), "주황 튤립 초점 무블룸", ResonanceTowers.FOCUS_CORE);
        link(ResonanceTowers.WAVE_CRYSTAL, ResonanceTowers.WAVE_PRISM.id(), "파란 난초 진동 무블룸", ResonanceTowers.WAVE_PRISM);
        link(ResonanceTowers.WAVE_PRISM, ResonanceTowers.WAVE_CORE.id(), "푸른 들꽃 파동 무블룸", ResonanceTowers.WAVE_CORE);
        link(ResonanceTowers.FROST_CRYSTAL, ResonanceTowers.FROST_PRISM.id(), "하얀 튤립 서리 무블룸", ResonanceTowers.FROST_PRISM);
        link(ResonanceTowers.FROST_PRISM, ResonanceTowers.FROST_CORE.id(), "데이지 빙결 무블룸", ResonanceTowers.FROST_CORE);
        link(ResonanceTowers.AMPLIFY_CRYSTAL, ResonanceTowers.AMPLIFY_PRISM.id(), "라일락 정원 무블룸", ResonanceTowers.AMPLIFY_PRISM);
        link(ResonanceTowers.AMPLIFY_PRISM, ResonanceTowers.AMPLIFY_CORE.id(), "작약 공명 무블룸", ResonanceTowers.AMPLIFY_CORE);

        JobRegistry.registerIfAbsent(new ResonanceTowerJob());
    }

    private static void registerTower(TowerType type, int tier) {
        if (ProductionTowerCatalog.find(type.id()).isPresent()) {
            return;
        }
        TowerType resolvedType = TowerBalanceRuntime.resolve(type);
        if (tier == 1) {
            ProductionTowerCatalog.registerStarter(resolvedType, ResonanceTower::new);
            return;
        }
        ProductionTowerCatalog.register(resolvedType, ResonanceTower::new, tier);
    }

    private static void link(TowerType from, String id, String displayName, TowerType to) {
        if (ProductionTowerCatalog.upgrade(from, id).isPresent()) {
            return;
        }
        TowerType targetType = ProductionTowerCatalog.find(to.id()).map(ProductionTowerCatalog.CatalogEntry::type).orElse(to);
        ProductionTowerCatalog.linkUpgrade(from, id, displayName, targetType, TowerBalanceRuntime.upgradeCost(from, id));
    }
}
