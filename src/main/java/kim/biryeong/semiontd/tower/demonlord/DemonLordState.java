package kim.biryeong.semiontd.tower.demonlord;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;

/**
 * Everything the demon lord player carries between ticks: the boss-bar health pool, the kill-fed
 * level, the active barrier and the per-skill cooldowns.
 *
 * <p>Health lives here rather than on the vanilla player. The match already blocks vanilla damage
 * to participants ({@code SemionPlayerProtectionService}), and routing demon lord damage into a
 * separate pool keeps that guarantee intact - the player never actually dies, respawns or drops
 * items; they simply fall out of combat when the pool empties.
 */
public final class DemonLordState {
    private final UUID playerId;
    private final Map<DemonLordSkill, Long> cooldownReadyTick = new EnumMap<>(DemonLordSkill.class);

    private int level = 1;
    private double experience;
    private double health;
    private double shield;
    private long shieldExpiryTick;
    private boolean inCombat;
    private boolean pendingSpawn;
    private boolean loadoutDirty = true;
    private int lastSelectedSlot = -1;
    private int laneId = -1;

    public DemonLordState(UUID playerId) {
        this.playerId = playerId;
        this.health = maxHealth();
    }

    public UUID playerId() {
        return playerId;
    }

    // ---------------------------------------------------------------- health

    public double health() {
        return health;
    }

    /**
     * 600 at level 1 sits just under the toughest tower in the game (780), which is deliberate: the
     * base value only really matters for the first round or two, because levels carry over between
     * rounds and quickly dominate. Starting low keeps the kill-fed growth curve meaningful instead
     * of front-loading it.
     */
    public double maxHealth() {
        double base = global("baseMaxHealth", 600.0);
        double perLevel = global("maxHealthPerLevel", 70.0);
        return Math.max(1.0, base + perLevel * (level - 1));
    }

    public double shield() {
        return shield;
    }

    public double healthRatio() {
        double max = maxHealth();
        return max <= 0.0 ? 0.0 : Math.min(1.0, health / max);
    }

    /**
     * Applies incoming damage to the barrier first, then to the health pool.
     *
     * @return {@code true} when this hit emptied the pool and the player drops out of combat
     */
    public boolean applyDamage(double amount) {
        if (amount <= 0.0 || !inCombat) {
            return false;
        }
        double remaining = amount;
        if (shield > 0.0) {
            double absorbed = Math.min(shield, remaining);
            shield -= absorbed;
            remaining -= absorbed;
        }
        if (remaining <= 0.0) {
            return false;
        }
        health = Math.max(0.0, health - remaining);
        return health <= 0.0;
    }

    public void heal(double amount) {
        if (amount <= 0.0) {
            return;
        }
        health = Math.min(maxHealth(), health + amount);
    }

    /** Barriers do not stack; recasting refreshes to the larger of the two shields. */
    public void grantShield(double amount, long expiryTick) {
        if (amount >= shield) {
            shield = amount;
            shieldExpiryTick = expiryTick;
        }
    }

    /** Drops an expired barrier. Called once per tick before damage is applied. */
    public void expireShieldIfNeeded(long gameTime) {
        if (shield > 0.0 && gameTime >= shieldExpiryTick) {
            shield = 0.0;
        }
    }

    public void clearShield() {
        shield = 0.0;
        shieldExpiryTick = 0L;
    }

    // ----------------------------------------------------------------- level

    public int level() {
        return level;
    }

    public double experience() {
        return experience;
    }

    public int maxLevel() {
        return Math.max(1, (int) global("maxLevel", 30.0));
    }

    /** Experience needed to move from {@code level} to the next one. */
    public double experienceForNextLevel() {
        double base = global("experienceBase", 12.0);
        double growth = Math.max(1.0, global("experienceGrowth", 1.25));
        return base * Math.pow(growth, level - 1);
    }

    /**
     * Feeds a kill into the level curve.
     *
     * <p>Levelling raises the health ceiling, and the gained headroom is granted immediately so a
     * level-up in the middle of a fight actually helps instead of only mattering next round.
     *
     * @return the number of levels gained
     */
    public int addExperience(double amount) {
        if (amount <= 0.0 || level >= maxLevel()) {
            return 0;
        }
        experience += amount;
        int gained = 0;
        while (level < maxLevel() && experience >= experienceForNextLevel()) {
            experience -= experienceForNextLevel();
            double previousMax = maxHealth();
            level++;
            gained++;
            health += Math.max(0.0, maxHealth() - previousMax);
        }
        if (level >= maxLevel()) {
            experience = 0.0;
        }
        return gained;
    }

    /** Scales every skill and blade hit. Levels are the builder's only damage growth. */
    public double damageMultiplier() {
        return 1.0 + global("damagePerLevel", 0.05) * (level - 1);
    }

    public double bladeDamage() {
        return global("bladeDamage", 30.0) * damageMultiplier();
    }

    // ---------------------------------------------------------------- combat

    public boolean inCombat() {
        return inCombat;
    }

    /**
     * Called at round start: full health, no barrier, every cooldown cleared.
     *
     * <p>The actual teleport to lane centre is deferred to {@code pendingSpawn}, because jobs run
     * without a {@code ServerPlayer} handle and the service tick has one.
     */
    public void enterCombat() {
        inCombat = true;
        pendingSpawn = true;
        health = maxHealth();
        clearShield();
        cooldownReadyTick.clear();
        loadoutDirty = true;
    }

    public boolean consumePendingSpawn() {
        boolean pending = pendingSpawn;
        pendingSpawn = false;
        return pending;
    }

    /** Called when the pool empties. Skills stop working and monsters stop caring. */
    public void leaveCombat() {
        inCombat = false;
        health = 0.0;
        clearShield();
        loadoutDirty = true;
    }

    // ------------------------------------------------------------- cooldowns

    public boolean isSkillReady(DemonLordSkill skill, long gameTime) {
        Long ready = cooldownReadyTick.get(skill);
        return ready == null || gameTime >= ready;
    }

    public void startCooldown(DemonLordSkill skill, long gameTime, int cooldownTicks) {
        cooldownReadyTick.put(skill, gameTime + Math.max(1, cooldownTicks));
    }

    public int remainingCooldownTicks(DemonLordSkill skill, long gameTime) {
        Long ready = cooldownReadyTick.get(skill);
        if (ready == null) {
            return 0;
        }
        return (int) Math.max(0L, ready - gameTime);
    }

    // ---------------------------------------------------------------- hotbar

    public boolean loadoutDirty() {
        return loadoutDirty;
    }

    public void markLoadoutDirty() {
        loadoutDirty = true;
    }

    public void clearLoadoutDirty() {
        loadoutDirty = false;
    }

    /** Lane the demon lord defends. Kept here so monster goals can match without a game lookup. */
    public int laneId() {
        return laneId;
    }

    public void setLaneId(int laneId) {
        this.laneId = laneId;
    }

    public int lastSelectedSlot() {
        return lastSelectedSlot;
    }

    public void setLastSelectedSlot(int slot) {
        lastSelectedSlot = slot;
    }

    private double global(String key, double fallback) {
        return TowerBalanceRuntime.ability(DemonLordTowers.GLOBAL_CONFIG_ID, key, fallback);
    }
}
