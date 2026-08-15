package kim.biryeong.semiontd.tower.demonlord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.job.DemonLordTowerJob;
import kim.biryeong.semiontd.tower.ProductionTowerCatalog;
import kim.biryeong.semiontd.tower.ProductionTowerCatalogs;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerCapacity;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class DemonLordTowerCatalogTest {
    private static final double EPSILON = 0.0001;

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void resetCatalogs() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        TowerBalanceRuntime.apply(defaults);
        ProductionTowerCatalogs.reloadBuiltIns(defaults);
        DemonLordStates.clearAllForTesting();
    }

    @Test
    void jobExposesEveryDemonLordTowerAndNothingElse() {
        DemonLordTowerJob job = new DemonLordTowerJob();
        assertEquals("semion-td:demon_lord_towers", job.id().toString());
        for (TowerType type : DemonLordTowers.all()) {
            assertTrue(job.canUseTower(null, type), "Job should allow " + type.id());
        }
        assertFalse(job.canUseTower(null, ProductionTowerCatalog.all().stream()
                .map(ProductionTowerCatalog.CatalogEntry::type)
                .filter(type -> !DemonLordTowers.isDemonLordTower(type))
                .findFirst()
                .orElseThrow()));
    }

    @Test
    void catalogHasFiveStartersAndFourTiersEach() {
        assertEquals(DemonLordSkill.values().length * DemonLordSkill.MAX_TIER, DemonLordTowers.all().size());
        long starters = ProductionTowerCatalog.all().stream()
                .filter(ProductionTowerCatalog.CatalogEntry::starter)
                .filter(entry -> DemonLordTowers.isDemonLordTower(entry.type()))
                .count();
        assertEquals(DemonLordSkill.values().length, starters, "Only tier 1 altars belong in the shop.");

        for (DemonLordSkill skill : DemonLordSkill.values()) {
            for (int tier = 1; tier < DemonLordSkill.MAX_TIER; tier++) {
                TowerType from = DemonLordTowers.tower(skill, tier);
                TowerType to = DemonLordTowers.tower(skill, tier + 1);
                assertTrue(ProductionTowerCatalog.upgrade(from, to.id()).isPresent(),
                        skill + " T" + tier + " should upgrade into T" + (tier + 1));
            }
        }
    }

    /** 사용자가 지정한 코스트: 파동 3 / 날개 2 / 하늘 부수기 4 / 폭격 4 / 배리어 3. */
    @Test
    void skillCostsMatchTheDesignedValues() {
        assertEquals(3, DemonLordSkill.WAVE_OF_MALICE.slotCost());
        assertEquals(2, DemonLordSkill.DEMON_WINGS.slotCost());
        assertEquals(4, DemonLordSkill.SKY_BREAKER.slotCost());
        assertEquals(4, DemonLordSkill.ARCANE_BOMBARDMENT.slotCost());
        assertEquals(3, DemonLordSkill.DEMON_BARRIER.slotCost());

        // 코스트는 티어가 올라도 그대로입니다. 업그레이드는 다이아만 먹습니다.
        for (DemonLordSkill skill : DemonLordSkill.values()) {
            for (int tier = 1; tier <= DemonLordSkill.MAX_TIER; tier++) {
                assertEquals(skill.slotCost(), TowerCapacity.slotCost(DemonLordTowers.tower(skill, tier)),
                        skill + " T" + tier + " slot cost");
            }
        }
    }

    /** 스킬 5종을 전부 열면 코스트 16. 초반 타워 한도로는 절대 못 여는 값이어야 선택이 생깁니다. */
    @Test
    void openingEverySkillCostsMoreThanAnEarlyTowerLimit() {
        int total = 0;
        for (DemonLordSkill skill : DemonLordSkill.values()) {
            total += skill.slotCost();
        }
        assertEquals(16, total);
    }

    /** 업글마다 쿨타임 -1초. 4티어면 -3초입니다. */
    @Test
    void cooldownDropsOneSecondPerTier() {
        for (DemonLordSkill skill : DemonLordSkill.values()) {
            for (int tier = 1; tier <= DemonLordSkill.MAX_TIER; tier++) {
                int expectedSeconds = skill.baseCooldownSeconds() - (tier - 1);
                assertEquals(expectedSeconds, skill.cooldownSecondsForTier(tier), skill + " T" + tier);
                assertEquals(expectedSeconds * 20, DemonLordTowers.cooldownTicks(DemonLordTowers.tower(skill, tier)),
                        skill + " T" + tier + " cooldown ticks");
            }
        }
        assertEquals(8, DemonLordSkill.WAVE_OF_MALICE.cooldownSecondsForTier(1));
        assertEquals(5, DemonLordSkill.WAVE_OF_MALICE.cooldownSecondsForTier(4));
        assertEquals(20, DemonLordSkill.DEMON_BARRIER.cooldownSecondsForTier(1));
        assertEquals(17, DemonLordSkill.DEMON_BARRIER.cooldownSecondsForTier(4));
    }

    /** 빌더의 정체성: 타워는 공격도 방어도 어그로도 없습니다. */
    @Test
    void everyAltarIsInertAndUnkillable() {
        for (TowerType type : DemonLordTowers.all()) {
            assertEquals(0.0, type.damage(), EPSILON, type.id() + " must not deal damage");
            assertEquals(0.0, type.range(), EPSILON, type.id() + " must have no range");
            assertEquals(0, type.aggroPriority(), type.id() + " must not draw aggro");

            Tower tower = ProductionTowerCatalog.find(type.id())
                    .orElseThrow()
                    .create(UUID.randomUUID(), TeamId.RED, 1, new GridPosition(0, 0, 0));
            DemonLordSkillTower altar = assertInstanceOf(DemonLordSkillTower.class, tower);
            assertFalse(altar.canChaseTargets(), type.id() + " must not chase");
            assertFalse(altar.drawsAggro(), type.id() + " must not draw aggro");
            assertTrue(altar.invulnerable(), type.id() + " must be invulnerable");
            assertNotNull(altar.skill());
        }
    }

    /** 슬롯 0~2 는 기존 도구가 쓰고, 8 번은 마검 자리입니다. */
    @Test
    void skillsOccupyDistinctHotbarSlotsAwayFromTheMatchTools() {
        Set<Integer> slots = new HashSet<>();
        for (DemonLordSkill skill : DemonLordSkill.values()) {
            assertTrue(slots.add(skill.hotbarSlot()), "Duplicate hotbar slot for " + skill);
            assertTrue(skill.hotbarSlot() >= DemonLordSkill.FIRST_SKILL_SLOT
                            && skill.hotbarSlot() <= DemonLordSkill.LAST_SKILL_SLOT,
                    skill + " must sit between slots 3 and 7");
            assertFalse(skill.hotbarSlot() == DemonLordSkill.BLADE_SLOT, skill + " must not take the blade slot");
        }
        assertEquals(DemonLordSkill.values().length, slots.size());
        assertEquals(8, DemonLordSkill.BLADE_SLOT);
    }

    /** 스킬 하나만 열어도 150 다이아 안에서 시작할 수 있어야 합니다. */
    @Test
    void openingSkillIsAffordableOnTheStartingBudget() {
        long cheapest = DemonLordTowers.all().stream()
                .filter(type -> DemonLordTowers.tierOf(type) == 1)
                .mapToLong(TowerType::mineralCost)
                .min()
                .orElseThrow();
        assertTrue(cheapest <= 150, "Cheapest opening altar costs " + cheapest);

        long wingsAndWave = DemonLordTowers.tower(DemonLordSkill.DEMON_WINGS, 1).mineralCost()
                + DemonLordTowers.tower(DemonLordSkill.WAVE_OF_MALICE, 1).mineralCost();
        assertTrue(wingsAndWave <= 150, "Wings + wave opening costs " + wingsAndWave);
    }

    @Test
    void levellingRaisesHealthAndDamageTogether() {
        DemonLordState state = new DemonLordState(UUID.randomUUID());
        assertEquals(1, state.level());
        assertEquals(600.0, state.maxHealth(), EPSILON);
        assertEquals(1.0, state.damageMultiplier(), EPSILON);

        state.enterCombat();
        int gained = state.addExperience(1000.0);
        assertTrue(gained > 0, "A large experience dump should level the demon lord up");
        assertTrue(state.maxHealth() > 600.0);
        assertTrue(state.damageMultiplier() > 1.0);
        assertTrue(state.level() <= state.maxLevel());
    }

    /** 레벨업으로 늘어난 체력은 즉시 채워져야 전투 중 레벨업이 의미가 있습니다. */
    @Test
    void levelUpGrantsTheNewHeadroomImmediately() {
        DemonLordState state = new DemonLordState(UUID.randomUUID());
        state.enterCombat();
        double before = state.health();
        state.addExperience(state.experienceForNextLevel());
        assertEquals(2, state.level());
        assertTrue(state.health() > before, "Gained max health should be granted, not left empty");
        assertEquals(state.maxHealth(), state.health(), EPSILON);
    }

    @Test
    void barrierAbsorbsBeforeHealthAndExpires() {
        DemonLordState state = new DemonLordState(UUID.randomUUID());
        state.enterCombat();
        double full = state.health();

        state.grantShield(100.0, 100L);
        assertFalse(state.applyDamage(60.0));
        assertEquals(40.0, state.shield(), EPSILON);
        assertEquals(full, state.health(), EPSILON, "Shield should soak the whole hit");

        state.expireShieldIfNeeded(100L);
        assertEquals(0.0, state.shield(), EPSILON);
        assertFalse(state.applyDamage(50.0));
        assertEquals(full - 50.0, state.health(), EPSILON);
    }

    @Test
    void emptyingThePoolLeavesCombatAndBlocksSkills() {
        DemonLordState state = new DemonLordState(UUID.randomUUID());
        state.enterCombat();
        assertTrue(state.inCombat());

        assertTrue(state.applyDamage(state.maxHealth() + 1.0), "Overkill should report the knockout");
        state.leaveCombat();
        assertFalse(state.inCombat());
        assertEquals(0.0, state.health(), EPSILON);
        // 전투 제외 상태에서는 추가 피해가 들어가지 않습니다.
        assertFalse(state.applyDamage(10.0));
    }

    @Test
    void cooldownsBlockRecastingUntilTheyExpire() {
        DemonLordState state = new DemonLordState(UUID.randomUUID());
        state.enterCombat();
        assertTrue(state.isSkillReady(DemonLordSkill.SKY_BREAKER, 0L));

        state.startCooldown(DemonLordSkill.SKY_BREAKER, 0L, 200);
        assertFalse(state.isSkillReady(DemonLordSkill.SKY_BREAKER, 199L));
        assertEquals(1, state.remainingCooldownTicks(DemonLordSkill.SKY_BREAKER, 199L));
        assertTrue(state.isSkillReady(DemonLordSkill.SKY_BREAKER, 200L));
        // 다른 스킬은 영향을 받지 않습니다.
        assertTrue(state.isSkillReady(DemonLordSkill.DEMON_WINGS, 0L));
    }

    /** 라운드가 새로 시작되면 전투 제외 상태에서도 부활합니다. */
    @Test
    void enteringCombatRevivesAndClearsCooldowns() {
        DemonLordState state = new DemonLordState(UUID.randomUUID());
        state.enterCombat();
        state.startCooldown(DemonLordSkill.WAVE_OF_MALICE, 0L, 500);
        state.applyDamage(state.maxHealth());
        state.leaveCombat();

        state.enterCombat();
        assertTrue(state.inCombat());
        assertEquals(state.maxHealth(), state.health(), EPSILON);
        assertTrue(state.isSkillReady(DemonLordSkill.WAVE_OF_MALICE, 0L));
        assertTrue(state.consumePendingSpawn(), "Round start should queue the teleport to lane centre");
        assertFalse(state.consumePendingSpawn(), "The teleport request is one-shot");
    }

    @Test
    void balanceConfigCarriesEverySkillNumber() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        for (DemonLordSkill skill : DemonLordSkill.values()) {
            for (int tier = 1; tier <= DemonLordSkill.MAX_TIER; tier++) {
                String id = skill.towerId(tier);
                assertEquals(skill.slotCost(), (int) defaults.ability(id, TowerCapacity.CONFIG_KEY, -1),
                        id + " must publish its cost");
                assertTrue(defaults.ability(id, "cooldownTicks", -1) > 0, id + " must publish its cooldown");
            }
        }
        assertEquals(60.0, defaults.ability(DemonLordSkill.WAVE_OF_MALICE.towerId(1), "coneDegrees", -1), EPSILON);
        assertEquals(0.25, defaults.ability(DemonLordSkill.DEMON_BARRIER.towerId(1), "shieldRatio", -1), EPSILON);
        assertEquals(0.50, defaults.ability(DemonLordSkill.DEMON_BARRIER.towerId(4), "shieldRatio", -1), EPSILON);
        assertEquals(600.0, defaults.ability(DemonLordTowers.GLOBAL_CONFIG_ID, "baseMaxHealth", -1), EPSILON);
    }

    /**
     * 번들 리소스 {@code tower_balance.json} 은 코드 기본값과 <b>병합되지 않고 통째로 대체</b>합니다.
     * Java 에만 값을 넣으면 런타임에서 업그레이드 비용이 0 이 되고 ability 가 폴백으로 떨어지는데,
     * 컴파일로는 절대 잡히지 않습니다. 두 곳이 어긋나면 여기서 깨집니다.
     */
    @Test
    void bundledResourceCarriesEveryDemonLordEntryThatCodeDefines() {
        TowerBalanceConfig code = TowerBalanceConfig.codeDefaults();
        TowerBalanceConfig bundled = TowerBalanceConfig.defaultConfig();

        for (DemonLordSkill skill : DemonLordSkill.values()) {
            for (int tier = 1; tier <= DemonLordSkill.MAX_TIER; tier++) {
                String id = skill.towerId(tier);
                assertNotNull(bundled.towers().get(id), id + " missing from the bundled resource");
                assertEquals(code.ability(id, TowerCapacity.CONFIG_KEY, -1),
                        bundled.ability(id, TowerCapacity.CONFIG_KEY, -2), EPSILON,
                        id + " cost drifted between code and the bundled resource");
                assertEquals(code.ability(id, "cooldownTicks", -1),
                        bundled.ability(id, "cooldownTicks", -2), EPSILON,
                        id + " cooldown drifted between code and the bundled resource");

                if (tier < DemonLordSkill.MAX_TIER) {
                    String next = skill.towerId(tier + 1);
                    assertTrue(bundled.upgradeCosts().getOrDefault(id + "->" + next, 0L) > 0L,
                            id + " upgrade cost missing from the bundled resource");
                }
            }
        }
        assertEquals(code.ability(DemonLordTowers.GLOBAL_CONFIG_ID, "baseMaxHealth", -1),
                bundled.ability(DemonLordTowers.GLOBAL_CONFIG_ID, "baseMaxHealth", -2), EPSILON);
    }

    /** 스킬 피해는 티어가 오를수록 반드시 강해져야 업그레이드가 의미를 가집니다. */
    @Test
    void skillDamageGrowsWithEveryTier() {
        TowerBalanceConfig defaults = TowerBalanceConfig.defaultConfig();
        List<DemonLordSkill> damaging = List.of(
                DemonLordSkill.WAVE_OF_MALICE,
                DemonLordSkill.DEMON_WINGS,
                DemonLordSkill.SKY_BREAKER,
                DemonLordSkill.ARCANE_BOMBARDMENT
        );
        for (DemonLordSkill skill : damaging) {
            double previous = 0.0;
            for (int tier = 1; tier <= DemonLordSkill.MAX_TIER; tier++) {
                double damage = defaults.ability(skill.towerId(tier), "damage", -1);
                assertTrue(damage > previous, skill + " T" + tier + " should out-damage the tier below");
                previous = damage;
            }
        }
    }
}
