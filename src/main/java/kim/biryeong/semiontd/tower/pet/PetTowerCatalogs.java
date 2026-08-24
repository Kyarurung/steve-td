package kim.biryeong.semiontd.tower.pet;

import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.TowerType;

public final class PetTowerCatalogs {
    private PetTowerCatalogs() {
    }

    public static void register() {
        PetTowers.all().forEach(type -> register(type, PetTowers.tier(type)));
        link(PetTowers.BUTLER_T1, PetTowers.BUTLER_T2);
        link(PetTowers.TRAINER_T1, PetTowers.TRAINER_T2);
        link(PetTowers.KEEPER_T1, PetTowers.KEEPER_T2);
        link(PetTowers.DOG_T1, PetTowers.DOG_T2);
        link(PetTowers.DOG_T2, PetTowers.DOG_T3);
        link(PetTowers.CAT_T1, PetTowers.CAT_T2);
        link(PetTowers.CAT_T2, PetTowers.CAT_T3);
        link(PetTowers.BIRD_T1, PetTowers.BIRD_T2);
        link(PetTowers.BIRD_T2, PetTowers.BIRD_T3);
    }

    private static void register(TowerType type, int tier) {
        TowerType resolved = TowerBalanceRuntime.resolve(type);
        if (tier == 1) {
            ProductionTowerCatalog.registerStarter(resolved, PetTower::new);
        } else {
            ProductionTowerCatalog.register(resolved, PetTower::new, tier);
        }
    }

    private static void link(TowerType from, TowerType to) {
        TowerType target = ProductionTowerCatalog.find(to.id()).orElseThrow().type();
        ProductionTowerCatalog.linkUpgrade(from, to.id(), to.displayName(), target,
                TowerBalanceRuntime.upgradeCost(from, to.id()));
    }
}
