package kim.biryeong.semiontd.entity.tower.vfx;

import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.config.VfxConfig;
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
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

final class BuilderTowerVfxRenderer {
    private static final DustParticleOptions NETHER_TRANSITION_PARTICLE = new DustParticleOptions(0xFF6D00, 1.2F);
    private static final DustParticleOptions ZOMBIE_TRANSITION_PARTICLE = new DustParticleOptions(0x6D8B3D, 1.0F);
    private static final DustParticleOptions ILLAGER_RAID_POWER_PARTICLE = new DustParticleOptions(0xE53935, 1.2F);
    private static final DustParticleOptions ILLAGER_RAID_ARMOR_PARTICLE = new DustParticleOptions(0xB0BEC5, 1.0F);

    private BuilderTowerVfxRenderer() {
    }

    static BuilderPalette paletteFor(TowerType type) {
        if (VillagerTowers.isAdvVillagerTower(type)) return BuilderPalette.VILLAGER_ADV;
        if (VillagerTowers.isBaseVillagerTower(type)) return BuilderPalette.VILLAGER;
        if (UndeadTowers.isUndeadTower(type)) return BuilderPalette.UNDEAD;
        if (AnimalTowers.isAnimalTower(type)) return BuilderPalette.ANIMAL;
        if (WarlockTowers.isWarlockTower(type)) return BuilderPalette.WARLOCK;
        if (LegionTowers.isLegionTower(type)) return BuilderPalette.LEGION;
        if (ResonanceTowers.isResonanceTower(type)) return BuilderPalette.RESONANCE;
        if (IllagerTowers.isIllagerTower(type)) return BuilderPalette.ILLAGER;
        if (NetherTowers.isNetherTower(type)) return BuilderPalette.NETHER;
        if (EndTowers.isEndTower(type)) return BuilderPalette.END;
        if (OceanTowers.isOceanTower(type)) return BuilderPalette.OCEAN;
        if (AncientCityTowers.isAncientCityTower(type)) return BuilderPalette.ANCIENT_CITY;
        if (AdversaryTowers.isAdversaryTower(type)) return BuilderPalette.ADVERSARY;
        if (FutureAgencyTowers.isFutureAgencyTower(type)) return BuilderPalette.FUTURE_AGENCY;
        if (QueenTowers.isQueenTower(type)) return BuilderPalette.QUEEN;
        if (EngineerTowers.isEngineerTower(type)) return BuilderPalette.ENGINEER;
        if (MageTowers.isMageTower(type)) return BuilderPalette.MAGE;
        if (HeroPartyTowers.isHeroPartyTower(type)) return BuilderPalette.HERO_PARTY;
        if (InsectTowers.isInsectTower(type)) return BuilderPalette.INSECT;
        if (PlantTowers.isPlantTower(type)) return BuilderPalette.PLANT;
        if (ArmyTowers.isArmyTower(type)) return BuilderPalette.ARMY;
        if (ThunderTowers.isThunderTower(type)) return BuilderPalette.THUNDER;
        if (DemonLordTowers.isDemonLordTower(type)) return BuilderPalette.DEMON_LORD;
        if (GambleTowers.isGambleTower(type)) return BuilderPalette.GAMBLE;
        if (DeveloperTowers.isDeveloperTower(type)) return BuilderPalette.DEVELOPER;
        if (SuccubusTowers.isSuccubusTower(type)) return BuilderPalette.SUCCUBUS;
        if (BodyTowers.isBodyTower(type)) return BuilderPalette.BODY;
        if (FrostTowers.isFrostTower(type)) return BuilderPalette.FROST;
        if (PetTowers.isPetTower(type)) return BuilderPalette.PET;
        return BuilderPalette.DEFAULT;
    }

    static void renderNetherTransition(
            TowerVfxService.EventContext context,
            Vec3 center,
            long gameTime,
            VfxConfig config,
            Map<UUID, Integer> packetCounts,
            Map<VfxLaneKey, Integer> shapeCounts
    ) {
        int points = TowerVfxService.claimVanillaPoints(context.lane(), gameTime, config, 160, 64, true);
        int orangeSpherePoints = points * 22 / 100;
        int greenSpherePoints = points * 26 / 100;
        int groundRingPoints = points * 14 / 100;
        int middleRingPoints = points * 14 / 100;
        int trailPoints = Math.max(4, (points - orangeSpherePoints - greenSpherePoints
                - groundRingPoints - middleRingPoints) / 4);

        TowerVfxService.sendSphere(context, NETHER_TRANSITION_PARTICLE, "minecraft:flame", center, 1.15,
                orangeSpherePoints, true, config, packetCounts, shapeCounts);
        TowerVfxService.sendSphere(context, ZOMBIE_TRANSITION_PARTICLE, "minecraft:smoke", center.add(0.0, 0.05, 0.0),
                0.78, greenSpherePoints, true, config, packetCounts, shapeCounts);
        TowerVfxService.sendCircle(context, NETHER_TRANSITION_PARTICLE, "minecraft:flame", center.add(0.0, -0.68, 0.0),
                1.05, groundRingPoints, true, config, packetCounts, shapeCounts);
        TowerVfxService.sendCircle(context, ZOMBIE_TRANSITION_PARTICLE, "minecraft:smoke", center.add(0.0, -0.08, 0.0),
                0.72, middleRingPoints, true, config, packetCounts, shapeCounts);

        for (int index = 0; index < 4; index++) {
            double angle = Math.PI * 2.0 * index / 4.0;
            Vec3 direction = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            Vec3 start = center.add(direction.scale(0.72)).add(0.0, -0.48, 0.0);
            Vec3 control = center.add(direction.scale(1.05)).add(0.0, 0.48, 0.0);
            Vec3 end = center.add(direction.scale(0.28)).add(0.0, 1.42, 0.0);
            TowerVfxService.sendTrail(context, ZOMBIE_TRANSITION_PARTICLE, "minecraft:smoke", start, control, end,
                    trailPoints, true, config, packetCounts, shapeCounts);
        }

        int smokePoints = TowerVfxService.claimVanillaPoints(context.lane(), gameTime, config, 18, 0, false);
        if (smokePoints > 0) {
            TowerVfxService.sendSphere(context, ParticleTypes.LARGE_SMOKE, "minecraft:large_smoke", center, 0.58,
                    smokePoints, false, config, packetCounts, shapeCounts);
        }
    }

    static void renderIllagerRaidActivation(
            TowerVfxService.EventContext context,
            Vec3 center,
            double radius,
            double height,
            long gameTime,
            VfxConfig config,
            Map<UUID, Integer> packetCounts,
            Map<VfxLaneKey, Integer> shapeCounts
    ) {
        int points = TowerVfxService.claimVanillaPoints(context.lane(), gameTime, config, 180, 72, true);
        int armorSpherePoints = points * 22 / 100;
        int powerSpherePoints = points * 28 / 100;
        int baseRingPoints = points * 16 / 100;
        int upperRingPoints = points * 12 / 100;
        int trailPoints = Math.max(4, (points - armorSpherePoints - powerSpherePoints
                - baseRingPoints - upperRingPoints) / 6);
        Vec3 base = center.add(0.0, -height * 0.5, 0.0);

        TowerVfxService.sendSphere(context, ILLAGER_RAID_ARMOR_PARTICLE, "minecraft:ash", center, radius,
                armorSpherePoints, true, config, packetCounts, shapeCounts);
        TowerVfxService.sendSphere(context, ILLAGER_RAID_POWER_PARTICLE, "minecraft:damage_indicator", center,
                radius * 0.68, powerSpherePoints, true, config, packetCounts, shapeCounts);
        TowerVfxService.sendCircle(context, ILLAGER_RAID_POWER_PARTICLE, "minecraft:damage_indicator", base, radius,
                baseRingPoints, true, config, packetCounts, shapeCounts);
        TowerVfxService.sendCircle(context, ILLAGER_RAID_ARMOR_PARTICLE, "minecraft:ash",
                center.add(0.0, height * 0.12, 0.0), radius * 0.62, upperRingPoints,
                true, config, packetCounts, shapeCounts);

        for (int index = 0; index < 6; index++) {
            double angle = Math.PI * 2.0 * index / 6.0;
            Vec3 direction = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            Vec3 start = base.add(direction.scale(radius * 0.82));
            Vec3 control = center.add(direction.scale(radius * 1.05));
            Vec3 end = center.add(direction.scale(radius * 0.24)).add(0.0, height * 0.5, 0.0);
            TowerVfxService.sendTrail(context, ILLAGER_RAID_POWER_PARTICLE, "minecraft:damage_indicator", start,
                    control, end, trailPoints, true, config, packetCounts, shapeCounts);
        }

        int sparkPoints = TowerVfxService.claimVanillaPoints(context.lane(), gameTime, config, 24, 0, false);
        if (sparkPoints > 0) {
            TowerVfxService.sendSphere(context, ParticleTypes.ELECTRIC_SPARK, "minecraft:electric_spark", center,
                    radius * 0.58, sparkPoints, false, config, packetCounts, shapeCounts);
        }
        int angerPoints = TowerVfxService.claimVanillaPoints(context.lane(), gameTime, config, 8, 0, false);
        if (angerPoints > 0) {
            TowerVfxService.sendSphere(context, ParticleTypes.ANGRY_VILLAGER, "minecraft:angry_villager",
                    center.add(0.0, height * 0.32, 0.0), radius * 0.48,
                    angerPoints, false, config, packetCounts, shapeCounts);
        }
    }
}
