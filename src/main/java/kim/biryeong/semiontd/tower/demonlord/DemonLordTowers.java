package kim.biryeong.semiontd.tower.demonlord;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.visual.BlockDisplayVisual;
import kim.biryeong.semiontd.entity.visual.EntityVisual;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.catalog.ProductionTowerDefinitions;
import kim.biryeong.semiontd.tower.description.TowerDescriptionRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Tower types of the demon lord builder: five skills, four tiers each.
 *
 * <p>Every one of these towers has zero damage, zero range and zero aggro. They are altars, not
 * guns - the whole point of the builder is that the player fights in person and the towers only
 * decide which skills are in the hotbar. Because they never fight, they are also invulnerable and
 * invisible to monster targeting (see {@link DemonLordSkillTower}).
 *
 * <p>Only one tower per skill can exist at a time; the shop hides a skill once its altar is up.
 * Tiers raise skill power and shave one second off the cooldown each step.
 */
public final class DemonLordTowers {
    public static final String GLOBAL_CONFIG_ID = "demon_lord_global";

    /** Must stay above the tower tables: the factory below fills them during class init. */
    private static final Map<String, Definition> DEFINITIONS = new HashMap<>();

    private static final String NO_COMBAT_LINE =
            "<red>이 타워는 공격도, 방어도, 어그로도 없습니다. 마왕 본인이 싸웁니다.</red>";

    private static final Map<DemonLordSkill, List<TowerType>> TOWERS = new EnumMap<>(DemonLordSkill.class);

    static {
        register(
                DemonLordSkill.WAVE_OF_MALICE,
                Blocks.CRYING_OBSIDIAN,
                new long[] {55, 130, 240, 380},
                new double[] {100, 200, 350, 550},
                List.of(
                        "<gray>전방 <aqua>{ability.coneDegrees:number}도</aqua> 부채꼴을 쓸어버립니다.</gray>",
                        "<green>범위 안의 모든 적에게 피해를 주고 뒤로 밀어냅니다.</green>",
                        "<yellow>넓게 퍼진 무리를 한 번에 정리하는 기본기입니다.</yellow>"
                )
        );
        register(
                DemonLordSkill.DEMON_WINGS,
                Blocks.SOUL_LANTERN,
                new long[] {40, 100, 190, 300},
                new double[] {90, 180, 320, 500},
                List.of(
                        "<gray>바라보는 방향으로 도약합니다.</gray>",
                        "<green>착지 시 주위 적에게 피해를 주고 밀어내며, 체력을 회복합니다.</green>",
                        "<yellow>포위를 빠져나오면서 회복까지 챙기는 생존기입니다.</yellow>"
                )
        );
        register(
                DemonLordSkill.SKY_BREAKER,
                Blocks.RESPAWN_ANCHOR,
                new long[] {75, 170, 300, 460},
                new double[] {110, 220, 380, 600},
                List.of(
                        "<gray>전방으로 길게 돌진합니다.</gray>",
                        "<green>부딪힌 적을 하늘로 띄우고 큰 피해를 줍니다.</green>",
                        "<green>적중한 적은 <aqua>{ability.stunTicks:seconds}</aqua>간 기절해 이동도 공격도 못 합니다.</green>",
                        "<yellow>단일 대상에게 가장 강한 한 방입니다.</yellow>"
                )
        );
        register(
                DemonLordSkill.ARCANE_BOMBARDMENT,
                Blocks.MAGMA_BLOCK,
                new long[] {75, 170, 300, 460},
                new double[] {105, 210, 360, 570},
                List.of(
                        "<gray>높이 뛰어오른 뒤 바라보는 방향으로 마력탄을 쏩니다.</gray>",
                        "<green>착탄 지점 반경 <aqua>{ability.blastRadius:blocks}</aqua>에 원형 광역 피해를 줍니다.</green>",
                        "<yellow>거리를 두고 뭉친 무리를 때리는 유일한 원거리 기술입니다.</yellow>"
                )
        );
        register(
                DemonLordSkill.DEMON_BARRIER,
                Blocks.OBSIDIAN,
                new long[] {60, 140, 250, 390},
                new double[] {120, 240, 420, 660},
                List.of(
                        "<gray>최대 체력의 <aqua>{ability.shieldRatio:percent}</aqua>만큼 방어막을 두릅니다.</gray>",
                        "<green>방어막이 남아 있는 동안 받는 피해를 대신 흡수합니다.</green>",
                        "<yellow>쿨타임이 가장 길어 위험한 순간을 골라 써야 합니다.</yellow>"
                )
        );
    }

    private DemonLordTowers() {
    }

    /** Every demon lord tower, tier 1 first within each skill. */
    public static List<TowerType> all() {
        List<TowerType> all = new ArrayList<>();
        for (DemonLordSkill skill : DemonLordSkill.values()) {
            all.addAll(TOWERS.get(skill));
        }
        return List.copyOf(all);
    }

    public static TowerType tower(DemonLordSkill skill, int tier) {
        List<TowerType> tiers = TOWERS.get(skill);
        if (tiers == null || tier < 1 || tier > tiers.size()) {
            throw new IllegalArgumentException("No demon lord tower for " + skill + " tier " + tier);
        }
        return tiers.get(tier - 1);
    }

    public static boolean isDemonLordTower(TowerType type) {
        return type != null && DEFINITIONS.containsKey(type.id());
    }

    public static DemonLordSkill skillOf(TowerType type) {
        Definition definition = definition(type);
        return definition == null ? null : definition.skill();
    }

    public static int tierOf(TowerType type) {
        Definition definition = definition(type);
        return definition == null ? 0 : definition.tier();
    }

    /**
     * Cooldown in ticks for a placed skill tower.
     *
     * <p>Defaults to the tier-adjusted value from {@link DemonLordSkill}, but a live config can
     * override any single tier through {@code cooldownTicks} without a rebuild.
     */
    public static int cooldownTicks(TowerType type) {
        Definition definition = definition(type);
        if (definition == null) {
            return 0;
        }
        int fallback = definition.skill().cooldownSecondsForTier(definition.tier()) * 20;
        return Math.max(1, TowerBalanceRuntime.abilityInt(type.id(), "cooldownTicks", fallback));
    }

    private static Definition definition(TowerType type) {
        return type == null ? null : DEFINITIONS.get(type.id());
    }

    private static void register(
            DemonLordSkill skill,
            Block altarBlock,
            long[] mineralCosts,
            double[] maxHealths,
            List<String> flavour
    ) {
        List<TowerType> tiers = new ArrayList<>(DemonLordSkill.MAX_TIER);
        for (int tier = 1; tier <= DemonLordSkill.MAX_TIER; tier++) {
            List<String> lines = new ArrayList<>();
            lines.add("<gray>마왕에게 <yellow>" + skill.displayName() + "</yellow> 스킬을 부여합니다.</gray>");
            lines.addAll(flavour);
            lines.add("<green>쿨타임 <aqua>{ability.cooldownTicks:seconds}</aqua> "
                    + "<dark_gray>|</dark_gray> 코스트 <aqua>" + skill.slotCost() + "</aqua></green>");
            lines.add(NO_COMBAT_LINE);

            String id = skill.towerId(tier);
            TowerType type = ProductionTowerDefinitions.tower(
                    id,
                    tierName(skill, tier),
                    mineralCosts[tier - 1],
                    maxHealths[tier - 1],
                    0.0,
                    0.0,
                    20,
                    0,
                    altarVisual(altarBlock, tier),
                    List.copyOf(lines)
            );
            DEFINITIONS.put(id, new Definition(skill, tier));
            TowerDescriptionRegistry.registerTemplate(type, type.description());
            tiers.add(type);
        }
        TOWERS.put(skill, List.copyOf(tiers));
    }

    /** Tier 1 keeps the bare skill name so the shop reads cleanly; upgrades get a rank suffix. */
    private static String tierName(DemonLordSkill skill, int tier) {
        return switch (tier) {
            case 1 -> skill.displayName();
            case 2 -> skill.displayName() + " II";
            case 3 -> skill.displayName() + " III";
            default -> skill.displayName() + " IV";
        };
    }

    private static EntityVisual altarVisual(Block block, int tier) {
        double scale = 0.7 + (tier - 1) * 0.15;
        return BlockDisplayVisual.builder(block.defaultBlockState()).scale(scale).build();
    }

    private record Definition(DemonLordSkill skill, int tier) {
    }
}
