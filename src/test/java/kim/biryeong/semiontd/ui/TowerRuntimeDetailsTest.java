package kim.biryeong.semiontd.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.adversary.AdversaryFoxTower;
import kim.biryeong.semiontd.tower.adversary.AdversaryTowers;
import kim.biryeong.semiontd.tower.adversary.FoxForm;
import kim.biryeong.semiontd.tower.animal.AnimalTowers;
import kim.biryeong.semiontd.tower.animal.PigTower;
import kim.biryeong.semiontd.tower.ancientcity.AncientCityTower;
import kim.biryeong.semiontd.tower.ancientcity.AncientCityTowers;
import kim.biryeong.semiontd.tower.resonance.ResonanceService;
import kim.biryeong.semiontd.tower.resonance.ResonanceTower;
import kim.biryeong.semiontd.tower.resonance.ResonanceTowers;
import kim.biryeong.semiontd.tower.undead.UndeadMeleeSkeletonTower;
import kim.biryeong.semiontd.tower.undead.UndeadTowers;
import kim.biryeong.semiontd.tower.villager.AntiTankerCatTower;
import kim.biryeong.semiontd.tower.villager.VillagerSplashTower;
import kim.biryeong.semiontd.tower.villager.VillagerThornTower;
import kim.biryeong.semiontd.tower.villager.VillagerTowers;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TowerRuntimeDetailsTest {
    private static final UUID OWNER = UUID.nameUUIDFromBytes("tower-runtime-details".getBytes(StandardCharsets.UTF_8));
    private static final GridPosition POSITION = new GridPosition(0, 0, 0);

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void applyDefaultBalance() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @AfterEach
    void resetTowerBalance() {
        TowerBalanceRuntime.apply(TowerBalanceConfig.defaultConfig());
    }

    @Test
    void resonanceTowerShowsEarnedMoobloomEffects() {
        ResonanceTower focus = resonance(ResonanceTowers.FOCUS_CORE, POSITION);
        ResonanceService.refresh(List.of(
                focus,
                resonance(ResonanceTowers.WAVE_CRYSTAL, new GridPosition(1, 0, 0)),
                resonance(ResonanceTowers.FROST_CRYSTAL, new GridPosition(-1, 0, 0)),
                resonance(ResonanceTowers.AMPLIFY_CRYSTAL, new GridPosition(0, 0, 1)),
                resonance(ResonanceTowers.WAVE_PRISM, new GridPosition(1, 0, -1)),
                resonance(ResonanceTowers.FROST_PRISM, new GridPosition(-1, 0, -1)),
                resonance(ResonanceTowers.AMPLIFY_CORE, new GridPosition(0, 0, -1))
        ));

        List<String> lines = SemionDialogService.towerRuntimeDetailLines(focus);

        assertContains(lines, "무블룸 공명");
        assertContains(lines, "Lv 3");
        assertContains(lines, "링크 6");
        assertContains(lines, "받는 오라");
        assertContains(lines, "공속 +50.0%");
    }

    @Test
    void animalTowerShowsHerdStackCount() throws Exception {
        PigTower pig = new PigTower(AnimalTowers.T2_PIG_TOWER, OWNER, TeamId.RED, 1, POSITION);
        setFieldFromHierarchy(pig, "currentStacks", 2);

        List<String> lines = SemionDialogService.towerRuntimeDetailLines(pig);

        assertContains(lines, "무리 스택");
        assertContains(lines, "2/2");
    }

    @Test
    void villagerTowersShowSurvivalAndDeathStacks() throws Exception {
        VillagerSplashTower librarian = new VillagerSplashTower(VillagerTowers.T2_LIBRARIAN_TOWER, OWNER, TeamId.RED, 1, POSITION);
        setFieldFromHierarchy(librarian, "survivalBouns", 2);
        AntiTankerCatTower cat = new AntiTankerCatTower(VillagerTowers.T2_ANTI_TANKER_CAT_TOWER, OWNER, TeamId.RED, 1, POSITION);
        cat.onNearbyMonsterDeath(null, null, new Vec3(0.5, 1.0, 0.5));
        cat.onNearbyMonsterDeath(null, null, new Vec3(0.5, 1.0, 0.5));
        VillagerThornTower golem = new VillagerThornTower(VillagerTowers.T2_GOLEM_TOWER, OWNER, TeamId.RED, 1, POSITION);
        setFieldFromHierarchy(golem, "survivalBonus", 1);

        assertContains(SemionDialogService.towerRuntimeDetailLines(librarian), "생존 스택 2/");
        assertContains(SemionDialogService.towerRuntimeDetailLines(cat), "사망 스택");
        assertContains(SemionDialogService.towerRuntimeDetailLines(cat), "공격력 +");
        assertContains(SemionDialogService.towerRuntimeDetailLines(golem), "생존 스택 1/");
    }

    @Test
    void undeadDeathStackTowerShowsDeathStackCountAndBonus() {
        UndeadMeleeSkeletonTower skeleton = new UndeadMeleeSkeletonTower(UndeadTowers.T2_MELEE_TOWER, OWNER, TeamId.RED, 1, POSITION);
        skeleton.onNearbyMonsterDeath(null, null, new Vec3(0.5, 1.0, 0.5));
        skeleton.onNearbyTowerDeath(null, new AntiTankerCatTower(VillagerTowers.T2_ANTI_TANKER_CAT_TOWER, OWNER, TeamId.RED, 1, POSITION));

        List<String> lines = SemionDialogService.towerRuntimeDetailLines(skeleton);

        assertContains(lines, "사망 스택 2/");
        assertContains(lines, "공격력 +");
        assertContains(lines, "체력 +");
    }

    @Test
    void towerDetailsConvertDividerTokensWithoutActionButtons() {
        List<DialogBody> bodies = SemionDialogService.actionDialogBodies(
                "\nStats\n<divider>\nOther owner's tower",
                () -> Component.literal("----------").withStyle(style -> style.withStrikethrough(true))
        );

        PlainMessage message = assertInstanceOf(PlainMessage.class, bodies.getFirst());
        assertFalse(message.contents().getString().contains("<divider>"));
        assertTrue(message.contents().getString().contains("Stats"));
        assertTrue(message.contents().getString().contains("Other owner's tower"));
        assertTrue(containsStrikethrough(message.contents()));
    }

    @Test
    void towerDetailsUsePrimaryDamageTypeFromPlacement() {
        PigTower physicalTower = new PigTower(AnimalTowers.T2_PIG_TOWER, OWNER, TeamId.RED, 1, POSITION);
        assertEquals(
                "<#ec8d34>🪓 피해</#ec8d34><white>: </white><#ec8d34>42</#ec8d34>",
                SemionDialogService.formatTowerDamage(physicalTower, 42.0)
        );

        AncientCityTower ancientCityTower = new AncientCityTower(
                AncientCityTowers.SENSOR_T1, OWNER, TeamId.RED, 1, POSITION
        );
        assertEquals(
                "<#796CFF>🔥 피해</#796CFF><white>: </white><#796CFF>42</#796CFF>",
                SemionDialogService.formatTowerDamage(ancientCityTower, 42.0)
        );

        AdversaryFoxTower sculkCore = new AdversaryFoxTower(
                AdversaryTowers.typeFor(FoxForm.SCULK_CORE), OWNER, TeamId.RED, 1, POSITION
        );
        assertEquals(
                "<#796CFF>🔥 피해</#796CFF><white>: </white><#796CFF>42</#796CFF>",
                SemionDialogService.formatTowerDamage(sculkCore, 42.0)
        );

        AdversaryFoxTower breeze = new AdversaryFoxTower(
                AdversaryTowers.typeFor(FoxForm.BREEZE), OWNER, TeamId.RED, 1, POSITION
        );
        assertEquals(
                "<#ec8d34>🪓 피해</#ec8d34><white>: </white><#ec8d34>42</#ec8d34>",
                SemionDialogService.formatTowerDamage(breeze, 42.0)
        );
    }

    @Test
    void placementAndUpgradeTooltipsUseTowerTypePrimaryDamageFormat() {
        assertEquals(
                "<#796CFF>🔥 피해</#796CFF><white>: </white><#796CFF>5</#796CFF>",
                SemionDialogService.formatTowerTypePrimaryDamage(AncientCityTowers.SENSOR_T1)
        );
        assertEquals(
                "<#796CFF>🔥 피해</#796CFF><white>: </white><#796CFF>800</#796CFF>",
                SemionDialogService.formatTowerTypePrimaryDamage(AdversaryTowers.typeFor(FoxForm.SCULK_CORE))
        );
        assertEquals(
                "<#ec8d34>🪓 피해</#ec8d34><white>: </white><#ec8d34>26</#ec8d34>",
                SemionDialogService.formatTowerTypePrimaryDamage(AdversaryTowers.typeFor(FoxForm.BREEZE))
        );
    }

    @Test
    void installedTowerDetailsUseTheSameBaseDamageAsTooltips() {
        AncientCityTower sensor = new AncientCityTower(
                AncientCityTowers.SENSOR_T1, OWNER, TeamId.RED, 1, POSITION
        );
        assertEquals(5.0, SemionDialogService.towerPrimaryDamage(sensor));
        assertEquals(5.0, SemionDialogService.currentTowerPrimaryDamage(sensor, null));
        assertEquals(
                SemionDialogService.formatTowerTypePrimaryDamage(AncientCityTowers.SENSOR_T1),
                SemionDialogService.formatTowerDamage(
                        sensor,
                        SemionDialogService.currentTowerPrimaryDamage(sensor, null)
                )
        );

        AdversaryFoxTower sculkCore = new AdversaryFoxTower(
                AdversaryTowers.typeFor(FoxForm.SCULK_CORE), OWNER, TeamId.RED, 1, POSITION
        );
        assertEquals(800.0, SemionDialogService.towerPrimaryDamage(sculkCore));
        assertEquals(800.0, SemionDialogService.currentTowerPrimaryDamage(sculkCore, null));
        assertEquals(
                SemionDialogService.formatTowerTypePrimaryDamage(AdversaryTowers.typeFor(FoxForm.SCULK_CORE)),
                SemionDialogService.formatTowerDamage(
                        sculkCore,
                        SemionDialogService.currentTowerPrimaryDamage(sculkCore, null)
                )
        );
    }

    private static ResonanceTower resonance(kim.biryeong.semiontd.tower.TowerType type, GridPosition position) {
        return new ResonanceTower(type, OWNER, TeamId.RED, 1, position, position);
    }

    private static void assertContains(List<String> lines, String expected) {
        assertTrue(lines.stream().anyMatch(line -> line.contains(expected)),
                () -> "Expected a runtime detail line containing '" + expected + "' but got " + lines);
    }

    private static boolean containsStrikethrough(Component component) {
        return component.getStyle().isStrikethrough()
                || component.getSiblings().stream().anyMatch(TowerRuntimeDetailsTest::containsStrikethrough);
    }

    private static void setFieldFromHierarchy(Tower tower, String name, Object value) throws Exception {
        Class<?> type = tower.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(tower, value);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
