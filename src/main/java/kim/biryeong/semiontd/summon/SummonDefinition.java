package kim.biryeong.semiontd.summon;

import java.util.List;
import kim.biryeong.semiontd.config.AttackKind;
import kim.biryeong.semiontd.entity.monster.DamageType;
import kim.biryeong.semiontd.entity.monster.MonsterDimensions;

record SummonDefinition(
        String id,
        String displayName,
        long gasCost,
        long incomeGain,
        double maxHealth,
        double armor,
        double attackDamage,
        AttackKind attackKind,
        String entityTypeId,
        String blockbenchModelId,
        MonsterDimensions dimensions,
        DamageType damageType,
        double resistance,
        SummonTier tier,
        List<SummonRole> roles,
        List<SummonAbilityActivation> abilityActivations,
        List<String> description,
        long mineralReward
) {
    static Builder summon(String id, String displayName) {
        return new Builder(id, displayName);
    }

    static final class Builder {
        private final String id;
        private final String displayName;
        private long gasCost;
        private long incomeGain;
        private long mineralReward;
        private double maxHealth = 1.0;
        private double armor;
        private double attackDamage;
        private AttackKind attackKind = AttackKind.MELEE;
        private String entityTypeId = "minecraft:zombie";
        private String blockbenchModelId;
        private MonsterDimensions dimensions = MonsterDimensions.DEFAULT;
        private DamageType damageType = DamageType.PHYSICAL;
        private double resistance;
        private SummonTier tier = SummonTier.T1;
        private List<SummonRole> roles = List.of(SummonRole.RUSH);
        private List<SummonAbilityActivation> abilityActivations = List.of(SummonAbilityActivation.PASSIVE);
        private List<String> description = List.of();

        private Builder(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        Builder economy(long gasCost, long incomeGain, long mineralReward) {
            this.gasCost = gasCost;
            this.incomeGain = incomeGain;
            this.mineralReward = mineralReward;
            return this;
        }

        Builder combat(
                double maxHealth,
                double armor,
                double attackDamage,
                AttackKind attackKind,
                DamageType damageType,
                double resistance
        ) {
            this.maxHealth = maxHealth;
            this.armor = armor;
            this.attackDamage = attackDamage;
            this.attackKind = attackKind;
            this.damageType = damageType;
            this.resistance = resistance;
            return this;
        }

        Builder visual(String entityTypeId, String blockbenchModelId) {
            this.entityTypeId = entityTypeId;
            this.blockbenchModelId = blockbenchModelId;
            return this;
        }

        Builder dimensions(MonsterDimensions dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        Builder tier(SummonTier tier) {
            this.tier = tier;
            return this;
        }

        Builder roles(SummonRole... roles) {
            this.roles = List.of(roles);
            return this;
        }

        Builder abilities(SummonAbilityActivation... abilityActivations) {
            this.abilityActivations = List.of(abilityActivations);
            return this;
        }

        Builder description(String... description) {
            this.description = List.of(description);
            return this;
        }

        SummonDefinition build() {
            return new SummonDefinition(
                    id,
                    displayName,
                    gasCost,
                    incomeGain,
                    maxHealth,
                    armor,
                    attackDamage,
                    attackKind,
                    entityTypeId,
                    blockbenchModelId,
                    dimensions,
                    damageType,
                    resistance,
                    tier,
                    roles,
                    abilityActivations,
                    description,
                    mineralReward
            );
        }
    }
}
