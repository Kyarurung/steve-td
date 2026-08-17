package kim.biryeong.semiontd.tower.demonlord;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.monster.DamageType;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.world.phys.Vec3;

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
    private boolean combatKitGranted;
    private boolean loadoutDirty = true;
    private int lastSelectedSlot = -1;
    private int laneId = -1;
    private long lastBladeAttackTick = Long.MIN_VALUE;
    private TowerType pendingBombardment;
    private long pendingBombardmentTick;
    private HellfireZone zone;
    private double roundPhysicalDamageDealt;
    private double roundMagicDamageDealt;

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

    public double maxHealth() {
        double base = global("baseMaxHealth", 450.0);
        double perLevel = global("maxHealthPerLevel", 52.5);
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
        return global("bladeDamage", 19.0) * damageMultiplier();
    }

    public void recordDamageDealt(double amount, DamageType damageType) {
        if (!Double.isFinite(amount) || amount <= 0.0) {
            return;
        }
        if (damageType == DamageType.MAGIC) {
            roundMagicDamageDealt += amount;
        } else {
            roundPhysicalDamageDealt += amount;
        }
    }

    public double roundPhysicalDamageDealt() {
        return roundPhysicalDamageDealt;
    }

    public double roundMagicDamageDealt() {
        return roundMagicDamageDealt;
    }

    /**
     * Vanilla-style swing charge, so mashing the button is worse than timing swings.
     *
     * <p>Returns the fraction of the attack interval that has elapsed since the last swing, clamped
     * to 1. Callers turn it into a damage multiplier; a fully charged swing is 1.0.
     */
    public double bladeChargeScale(long gameTime, int intervalTicks) {
        if (intervalTicks <= 0 || lastBladeAttackTick == Long.MIN_VALUE) {
            return 1.0;
        }
        double elapsed = gameTime - lastBladeAttackTick;
        return Math.max(0.0, Math.min(1.0, elapsed / intervalTicks));
    }

    /** Every swing resets the charge, including the weak ones. */
    public void recordBladeAttack(long gameTime) {
        lastBladeAttackTick = gameTime;
    }

    // ---------------------------------------------------- delayed 마도 폭격

    /**
     * 마도 폭격은 먼저 솟아오른 뒤 정점에서 쏩니다. 시전 시점이 아니라 발사 시점의 시선으로
     * 조준해야 하므로, 발사를 예약해 두고 서비스 틱이 처리합니다.
     */
    public void queueBombardment(TowerType altar, long fireTick) {
        pendingBombardment = altar;
        pendingBombardmentTick = fireTick;
    }

    public boolean bombardmentReady(long gameTime) {
        return pendingBombardment != null && gameTime >= pendingBombardmentTick;
    }

    public TowerType consumeBombardment() {
        TowerType altar = pendingBombardment;
        pendingBombardment = null;
        return altar;
    }

    public void clearPendingSkills() {
        pendingBombardment = null;
        zone = null;
    }

    // -------------------------------------------------- 지옥불 낙인 장판

    /**
     * 지옥불 낙인이 남긴 장판.
     *
     * <p>한 번에 하나만 유지합니다. 다시 시전하면 새 장판이 이전 것을 대체하므로, 여러 장을 겹쳐
     * 깔아 피해를 중첩시킬 수 없습니다.
     */
    public record HellfireZone(
            TowerType altarType,
            Vec3 centre,
            double radius,
            double damage,
            double damageTakenBonus,
            int tickIntervalTicks,
            long expiryTick,
            long nextPulseTick
    ) {
    }

    public void placeZone(HellfireZone newZone) {
        zone = newZone;
    }

    public HellfireZone zone() {
        return zone;
    }

    public void clearZone() {
        zone = null;
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
        clearPendingSkills();
        cooldownReadyTick.clear();
        roundPhysicalDamageDealt = 0.0;
        roundMagicDamageDealt = 0.0;
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
        clearPendingSkills();
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

    /** True while the hotbar is holding the combat kit instead of the normal match tools. */
    public boolean combatKitGranted() {
        return combatKitGranted;
    }

    public void setCombatKitGranted(boolean granted) {
        combatKitGranted = granted;
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
