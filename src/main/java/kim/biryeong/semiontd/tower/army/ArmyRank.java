package kim.biryeong.semiontd.tower.army;

/**
 * The one number the 군대 builder runs on.
 *
 * <p>Rank is earned by surviving waves, never by spending minerals, which makes it orthogonal to
 * tier: a T3 이등병 is a fresh heavy hitter and a T3 병장 is a commander that no longer fires at all.
 *
 * <p>The attack multipliers fall to zero on purpose. A tower that keeps some damage at the top rank
 * would let a player stack 고참 without paying for it, and the whole builder is the trade between
 * shooting now and making everyone else shoot harder.
 *
 * <p>The live balance config owns thresholds and multipliers so reloading the server cannot leave
 * rank behavior out of sync with player-facing descriptions.
 */
public enum ArmyRank {
    /** Fresh. Full damage, gives nothing. */
    PRIVATE,
    CORPORAL,
    SERGEANT,
    /** Stops firing entirely and carries the family's real output on its buff. */
    STAFF_SERGEANT;

    public int requiredService() {
        return switch (this) {
            case PRIVATE -> 0;
            case CORPORAL -> ArmyBalance.corporalService();
            case SERGEANT -> ArmyBalance.sergeantService();
            case STAFF_SERGEANT -> ArmyBalance.staffSergeantService();
        };
    }

    /** What fraction of its listed damage a tower of this rank still deals. */
    public double attackMultiplier() {
        return switch (this) {
            case PRIVATE -> 1.0;
            case CORPORAL -> ArmyBalance.corporalAttackMultiplier();
            case SERGEANT -> ArmyBalance.sergeantAttackMultiplier();
            case STAFF_SERGEANT -> 0.0;
        };
    }

    /** Damage bonus this rank grants to each lower-ranked ally in range. */
    public double damageBuff() {
        return switch (this) {
            case PRIVATE -> 0.0;
            case CORPORAL -> ArmyBalance.corporalDamageBuff();
            case SERGEANT -> ArmyBalance.sergeantDamageBuff();
            case STAFF_SERGEANT -> ArmyBalance.staffSergeantDamageBuff();
        };
    }

    public double attackSpeedBuff() {
        return this == STAFF_SERGEANT ? ArmyBalance.staffSergeantAttackSpeedBuff() : 0.0;
    }

    public boolean isSuperiorTo(ArmyRank other) {
        return other != null && ordinal() > other.ordinal();
    }

    /** Rank for a given service length, clamped to the top rank. */
    public static ArmyRank of(int service) {
        ArmyRank resolved = PRIVATE;
        for (ArmyRank rank : values()) {
            if (service >= rank.requiredService()) {
                resolved = rank;
            }
        }
        return resolved;
    }

    /** Waves until the next promotion, or {@code -1} once the top rank is reached. */
    public static int wavesUntilPromotion(int service) {
        for (ArmyRank rank : values()) {
            if (service < rank.requiredService()) {
                return rank.requiredService() - service;
            }
        }
        return -1;
    }

    /** Waves until automatic discharge, floored at 0. */
    public static int wavesUntilDischarge(int service) {
        return Math.max(0, ArmyBalance.dischargeService() - service);
    }
}
