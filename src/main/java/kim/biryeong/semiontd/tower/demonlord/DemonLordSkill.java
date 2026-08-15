package kim.biryeong.semiontd.tower.demonlord;

import java.util.Locale;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * The five demon lord skills.
 *
 * <p>Unlike every other builder, a demon lord tower never fights. It exists only to hand its skill
 * to the owning player, who is the actual weapon. Each skill therefore owns a fixed hotbar slot:
 * slots 0-2 stay with the shared match tools, slots 3-7 hold whichever skills the player bought,
 * and slot {@link #BLADE_SLOT} always holds the 마검 the hand snaps back to after a cast.
 *
 * <p>{@link #slotCost()} is the builder's "코스트" and feeds the existing
 * {@code towerSlotCost} capacity system, so the round tower limit decides how many skills can be
 * live at once. All five together cost 16, which is far more than an early limit allows.
 */
public enum DemonLordSkill {
    WAVE_OF_MALICE("wave_of_malice", "악의 파동", 3, 8, 3, Items.BREEZE_ROD),
    DEMON_WINGS("demon_wings", "악마의 날개", 2, 6, 4, Items.PHANTOM_MEMBRANE),
    SKY_BREAKER("sky_breaker", "하늘 부수기", 4, 10, 5, Items.MACE),
    ARCANE_BOMBARDMENT("arcane_bombardment", "마도 폭격", 4, 10, 6, Items.FIRE_CHARGE),
    DEMON_BARRIER("demon_barrier", "악마 배리어", 3, 20, 7, Items.SHIELD);

    /** Hotbar slot the hand is forced back to after a cast. Holds the 마검. */
    public static final int BLADE_SLOT = 8;

    /** Lowest hotbar slot a skill can occupy; 0-2 belong to the shared match tools. */
    public static final int FIRST_SKILL_SLOT = 3;

    /** Highest hotbar slot a skill can occupy. */
    public static final int LAST_SKILL_SLOT = 7;

    /** Number of upgrade tiers. Every tier past the first shaves one second off the cooldown. */
    public static final int MAX_TIER = 4;

    private final String key;
    private final String displayName;
    private final int slotCost;
    private final int baseCooldownSeconds;
    private final int hotbarSlot;
    private final Item item;

    DemonLordSkill(String key, String displayName, int slotCost, int baseCooldownSeconds, int hotbarSlot, Item item) {
        this.key = key;
        this.displayName = displayName;
        this.slotCost = slotCost;
        this.baseCooldownSeconds = baseCooldownSeconds;
        this.hotbarSlot = hotbarSlot;
        this.item = item;
    }

    public String key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    /** The builder's 코스트: how much of the round tower limit this skill occupies. */
    public int slotCost() {
        return slotCost;
    }

    public int baseCooldownSeconds() {
        return baseCooldownSeconds;
    }

    public int hotbarSlot() {
        return hotbarSlot;
    }

    public Item item() {
        return item;
    }

    /** Config bucket for values shared by every tier of this skill. */
    public String configId() {
        return "demon_lord_" + key;
    }

    /** Tower id for a given tier, e.g. {@code t1_wave_of_malice_tower}. */
    public String towerId(int tier) {
        return "t" + tier + "_" + key + "_tower";
    }

    /**
     * Cooldown before per-tier reduction. Tier 1 pays the full price and each upgrade removes one
     * second, so a tier 4 악의 파동 fires every 5 seconds instead of 8.
     */
    public int cooldownSecondsForTier(int tier) {
        return Math.max(1, baseCooldownSeconds - (Math.max(1, tier) - 1));
    }

    public static DemonLordSkill fromKey(String key) {
        if (key == null) {
            return null;
        }
        String normalized = key.toLowerCase(Locale.ROOT);
        for (DemonLordSkill skill : values()) {
            if (skill.key.equals(normalized)) {
                return skill;
            }
        }
        return null;
    }

    /** The skill bound to a hotbar slot, or {@code null} when that slot holds something else. */
    public static DemonLordSkill fromHotbarSlot(int slot) {
        for (DemonLordSkill skill : values()) {
            if (skill.hotbarSlot == slot) {
                return skill;
            }
        }
        return null;
    }
}
