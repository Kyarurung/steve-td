package kim.biryeong.semiontd.tower.adversary;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.TowerType;

public final class AdversaryTowerCatalogs {
    private AdversaryTowerCatalogs() {
    }

    public static void register() {
        synchronized (ProductionTowerCatalog.class) {
            registerAll();
        }
    }

    private static void registerAll() {
        for (FoxForm form : FoxForm.values()) {
            registerFox(form);
        }
        for (RivalKind kind : RivalKind.values()) {
            registerRival(AdversaryTowers.baseRival(kind), 1);
            registerRival(AdversaryTowers.enhancedRival(kind), 2);
        }
        for (RivalKind kind : RivalKind.values()) {
            linkEnhancement(kind);
        }
        for (FoxRoute route : FoxRoute.values()) {
            FoxForm intermediate = FoxForm.intermediateFor(route);
            linkFoxEvolution(FoxForm.BASE, intermediate);
            for (FoxForm finalForm : FoxForm.finalsFor(route)) {
                linkFoxEvolution(intermediate, finalForm);
            }
        }
    }

    private static void registerFox(FoxForm form) {
        TowerType type = AdversaryTowers.resolvedTypeFor(form);
        if (ProductionTowerCatalog.find(type.id()).isEmpty()) {
            TowerType resolved = form == FoxForm.BASE ? TowerBalanceRuntime.resolve(type) : type;
            if (form == FoxForm.BASE) {
                ProductionTowerCatalog.registerStarter(resolved, AdversaryFoxTower::new);
            } else {
                ProductionTowerCatalog.register(resolved, AdversaryFoxTower::new, form.stage() + 1);
            }
        }
    }

    private static void linkFoxEvolution(FoxForm fromForm, FoxForm toForm) {
        TowerType from = registeredFoxType(fromForm);
        TowerType to = registeredFoxType(toForm);
        if (ProductionTowerCatalog.upgrade(from, to.id()).isPresent()) {
            return;
        }
        ProductionTowerCatalog.linkUpgrade(
                from,
                to.id(),
                toForm.displayName() + " 전직",
                to,
                TowerBalanceRuntime.upgradeCost(from, to.id())
        );
    }

    private static TowerType registeredFoxType(FoxForm form) {
        return ProductionTowerCatalog.find(AdversaryTowers.typeFor(form).id())
                .map(ProductionTowerCatalog.CatalogEntry::type)
                .orElseThrow();
    }

    private static void registerRival(TowerType type, int tier) {
        if (ProductionTowerCatalog.find(type.id()).isPresent()) {
            return;
        }
        TowerType resolved = TowerBalanceRuntime.resolve(type);
        if (tier == 1) {
            ProductionTowerCatalog.registerStarter(resolved, AdversaryRivalTower::new);
            return;
        }
        ProductionTowerCatalog.register(resolved, AdversaryRivalTower::new, tier);
    }

    private static void linkEnhancement(RivalKind kind) {
        TowerType from = AdversaryTowers.baseRival(kind);
        TowerType to = AdversaryTowers.enhancedRival(kind);
        if (ProductionTowerCatalog.upgrade(from, to.id()).isPresent()) {
            return;
        }
        TowerType registeredTarget = ProductionTowerCatalog.find(to.id())
                .map(ProductionTowerCatalog.CatalogEntry::type)
                .orElse(to);
        ProductionTowerCatalog.linkUpgrade(
                from,
                to.id(),
                "강화된 " + kind.displayName() + " 숙적",
                registeredTarget,
                TowerBalanceRuntime.upgradeCost(from, to.id())
        );
    }
}
