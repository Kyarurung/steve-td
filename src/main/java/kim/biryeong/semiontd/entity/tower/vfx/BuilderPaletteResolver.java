package kim.biryeong.semiontd.entity.tower.vfx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.adversary.AdversaryTowers;
import kim.biryeong.semiontd.tower.ancientcity.AncientCityTowers;
import kim.biryeong.semiontd.tower.animal.AnimalTowers;
import kim.biryeong.semiontd.tower.army.ArmyTowers;
import kim.biryeong.semiontd.tower.body.BodyTowers;
import kim.biryeong.semiontd.tower.demonlord.DemonLordTowers;
import kim.biryeong.semiontd.tower.developer.DeveloperTowers;
import kim.biryeong.semiontd.tower.end.EndTowers;
import kim.biryeong.semiontd.tower.engineer.EngineerTowers;
import kim.biryeong.semiontd.tower.frost.FrostTowers;
import kim.biryeong.semiontd.tower.futureagency.FutureAgencyTowers;
import kim.biryeong.semiontd.tower.gamble.GambleTowers;
import kim.biryeong.semiontd.tower.hero.HeroPartyTowers;
import kim.biryeong.semiontd.tower.illager.IllagerTowers;
import kim.biryeong.semiontd.tower.insect.InsectTowers;
import kim.biryeong.semiontd.tower.legion.LegionTowers;
import kim.biryeong.semiontd.tower.mage.MageTowers;
import kim.biryeong.semiontd.tower.nether.NetherTowers;
import kim.biryeong.semiontd.tower.ocean.OceanTowers;
import kim.biryeong.semiontd.tower.pet.PetTowers;
import kim.biryeong.semiontd.tower.plant.PlantTowers;
import kim.biryeong.semiontd.tower.queen.QueenTowers;
import kim.biryeong.semiontd.tower.resonance.ResonanceTowers;
import kim.biryeong.semiontd.tower.succubus.SuccubusTowers;
import kim.biryeong.semiontd.tower.thunder.ThunderTowers;
import kim.biryeong.semiontd.tower.undead.UndeadTowers;
import kim.biryeong.semiontd.tower.villager.VillagerTowers;
import kim.biryeong.semiontd.tower.warlock.WarlockTowers;

final class BuilderPaletteResolver {
    private final BuilderPalette fallback;
    private final List<Route> routes = new ArrayList<>();

    BuilderPaletteResolver(BuilderPalette fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
    }

    static BuilderPaletteResolver builtIn() {
        return new BuilderPaletteResolver(BuilderPalette.DEFAULT)
                .register(VillagerTowers::isAdvVillagerTower, BuilderPalette.VILLAGER_ADV)
                .register(VillagerTowers::isBaseVillagerTower, BuilderPalette.VILLAGER)
                .register(UndeadTowers::isUndeadTower, BuilderPalette.UNDEAD)
                .register(AnimalTowers::isAnimalTower, BuilderPalette.ANIMAL)
                .register(WarlockTowers::isWarlockTower, BuilderPalette.WARLOCK)
                .register(LegionTowers::isLegionTower, BuilderPalette.LEGION)
                .register(ResonanceTowers::isResonanceTower, BuilderPalette.RESONANCE)
                .register(IllagerTowers::isIllagerTower, BuilderPalette.ILLAGER)
                .register(NetherTowers::isNetherTower, BuilderPalette.NETHER)
                .register(EndTowers::isEndTower, BuilderPalette.END)
                .register(OceanTowers::isOceanTower, BuilderPalette.OCEAN)
                .register(AncientCityTowers::isAncientCityTower, BuilderPalette.ANCIENT_CITY)
                .register(AdversaryTowers::isAdversaryTower, BuilderPalette.ADVERSARY)
                .register(FutureAgencyTowers::isFutureAgencyTower, BuilderPalette.FUTURE_AGENCY)
                .register(QueenTowers::isQueenTower, BuilderPalette.QUEEN)
                .register(EngineerTowers::isEngineerTower, BuilderPalette.ENGINEER)
                .register(MageTowers::isMageTower, BuilderPalette.MAGE)
                .register(HeroPartyTowers::isHeroPartyTower, BuilderPalette.HERO_PARTY)
                .register(InsectTowers::isInsectTower, BuilderPalette.INSECT)
                .register(PlantTowers::isPlantTower, BuilderPalette.PLANT)
                .register(ArmyTowers::isArmyTower, BuilderPalette.ARMY)
                .register(ThunderTowers::isThunderTower, BuilderPalette.THUNDER)
                .register(DemonLordTowers::isDemonLordTower, BuilderPalette.DEMON_LORD)
                .register(GambleTowers::isGambleTower, BuilderPalette.GAMBLE)
                .register(DeveloperTowers::isDeveloperTower, BuilderPalette.DEVELOPER)
                .register(SuccubusTowers::isSuccubusTower, BuilderPalette.SUCCUBUS)
                .register(BodyTowers::isBodyTower, BuilderPalette.BODY)
                .register(FrostTowers::isFrostTower, BuilderPalette.FROST)
                .register(PetTowers::isPetTower, BuilderPalette.PET);
    }

    BuilderPaletteResolver register(Predicate<TowerType> matcher, BuilderPalette palette) {
        routes.add(new Route(
                Objects.requireNonNull(matcher, "matcher"),
                Objects.requireNonNull(palette, "palette")
        ));
        return this;
    }

    BuilderPalette resolve(TowerType type) {
        if (type == null) {
            return fallback;
        }
        return routes.stream()
                .filter(route -> route.matcher().test(type))
                .map(Route::palette)
                .findFirst()
                .orElse(fallback);
    }

    private record Route(Predicate<TowerType> matcher, BuilderPalette palette) {
    }
}
