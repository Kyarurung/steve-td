package kim.biryeong.semiontd.tower.villager;

import java.util.Optional;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionPlayer;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import net.minecraft.resources.ResourceLocation;

public final class VillagerAdvEffectController {
    private static final ResourceLocation DAMAGE_SOURCE = source("damage");
    private static final ResourceLocation ATTACK_SPEED_SOURCE = source("attack_speed");
    private static final ResourceLocation DAMAGE_REDUCTION_SOURCE = source("damage_reduction");
    private static final ResourceLocation MAX_HEALTH_SOURCE = source("max_health");
    private static final ResourceLocation INCOME_DAMAGE_SOURCE = source("income_damage");
    private static final ResourceLocation WAVE_DAMAGE_SOURCE = source("wave_damage");
    private static final ResourceLocation HEAL_AMOUNT_SOURCE = source("heal_amount");
    private static final ResourceLocation ABILITY_INTERVAL_SOURCE = source("ability_interval");

    private VillagerAdvEffectController() {
    }

    public static void refresh(SemionPlayer player, PlayerLane lane, Tower tower) {
        if (!VillagerAdvProgressionController.isAdvPlayer(player)
                || lane == null || tower == null || !VillagerTowers.isVillagerTower(tower.type())) {
            return;
        }
        tower.setData(VillagerAdvStates.ADV_TOWER, true);
        towerEntity(tower, lane).ifPresent(entity -> refresh(player, tower, entity));
    }

    private static void refresh(SemionPlayer player, Tower tower, SemionTowerEntity entity) {
        TowerBalanceConfig.VillagerAdvConfig config = VillagerConfig.RUNTIME.advanced();
        double experience = VillagerAdvStates.experience(tower);
        double reputation = VillagerAdvStates.reputation(player.uuid());
        String towerId = tower.type().id();
        double damage = reputationBonus(config, towerId, reputation, "reputationDamagePerPoint");
        double attackSpeed = reputationBonus(config, towerId, reputation, "reputationAttackSpeedPerPoint");
        double maxHealth = reputationBonus(config, towerId, reputation, "reputationHealthPerPoint");
        double damageReduction = reputationBonus(config, towerId, reputation, "reputationDamageReductionPerPoint");
        double incomeDamage = 0.0;
        double waveDamage = 0.0;
        double healAmount = 0.0;
        double abilityInterval = 0.0;

        if (VillagerAdvTowerRoles.isGolem(tower)) {
            maxHealth += experienceBonus(config, towerId, experience, "golemHealthPerExperience");
            damageReduction += experienceBonus(config, towerId, experience, "golemDamageReductionPerExperience");
        } else if (VillagerAdvTowerRoles.isRanged(tower)) {
            damage += experienceBonus(config, towerId, experience, "rangedDamagePerExperience");
            attackSpeed += experienceBonus(config, towerId, experience, "rangedAttackSpeedPerExperience");
        } else if (VillagerAdvTowerRoles.isCat(tower)) {
            damage += experienceBonus(config, towerId, experience, "catDamagePerExperience");
            attackSpeed += experienceBonus(config, towerId, experience, "catAttackSpeedPerExperience");
            if (VillagerAdvTowerRoles.isAntiTankerCat(tower)) {
                incomeDamage += experienceBonus(config, towerId, experience, "catIncomeDamagePerExperience");
            }
            if (VillagerAdvTowerRoles.isLaneClearCat(tower)) {
                waveDamage += experienceBonus(config, towerId, experience, "catWaveDamagePerExperience");
            }
        } else if (VillagerAdvTowerRoles.isAllayLine(tower)) {
            healAmount += experienceBonus(config, towerId, experience, "allayHealAmountPerExperience");
            abilityInterval += experienceBonus(config, towerId, experience, "allayIntervalReductionPerExperience");
        }

        int duration = config.resolvedEffectDurationTicks();
        refresh(entity, TimedEffectType.TOWER_DAMAGE_BONUS, DAMAGE_SOURCE, damage, duration);
        refresh(entity, TimedEffectType.TOWER_ATTACK_SPEED_BONUS, ATTACK_SPEED_SOURCE, attackSpeed, duration);
        refresh(entity, TimedEffectType.TOWER_MAX_HEALTH_BONUS, MAX_HEALTH_SOURCE, maxHealth, duration);
        refresh(entity, TimedEffectType.TOWER_DAMAGE_REDUCTION, DAMAGE_REDUCTION_SOURCE, damageReduction, duration);
        refresh(entity, TimedEffectType.TOWER_INCOME_DAMAGE_BONUS, INCOME_DAMAGE_SOURCE, incomeDamage, duration);
        refresh(entity, TimedEffectType.TOWER_WAVE_DAMAGE_BONUS, WAVE_DAMAGE_SOURCE, waveDamage, duration);
        refresh(entity, TimedEffectType.TOWER_HEAL_AMOUNT_BONUS, HEAL_AMOUNT_SOURCE, healAmount, duration);
        refresh(entity, TimedEffectType.TOWER_ABILITY_INTERVAL_REDUCTION, ABILITY_INTERVAL_SOURCE, abilityInterval, duration);
    }

    private static Optional<SemionTowerEntity> towerEntity(Tower tower, PlayerLane lane) {
        if (!(tower instanceof EntityBackedTower entityBackedTower) || entityBackedTower.entityId().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(lane.arenaWorld().getEntity(entityBackedTower.entityId().getAsInt()))
                .filter(SemionTowerEntity.class::isInstance)
                .map(SemionTowerEntity.class::cast);
    }

    private static void refresh(
            SemionTowerEntity entity,
            TimedEffectType type,
            ResourceLocation source,
            double magnitude,
            int durationTicks
    ) {
        entity.refreshTimedEffect(type, source, magnitude, durationTicks);
    }

    private static double experienceBonus(
            TowerBalanceConfig.VillagerAdvConfig config,
            String towerId,
            double experience,
            String key
    ) {
        return VillagerAdvRules.buff(
                experience,
                config.buffInterval(towerId, key),
                config.buff(towerId, key),
                config.resolvedExperienceBuffCap()
        );
    }

    private static double reputationBonus(
            TowerBalanceConfig.VillagerAdvConfig config,
            String towerId,
            double reputation,
            String key
    ) {
        return VillagerAdvRules.buff(
                reputation,
                config.buffInterval(towerId, key),
                config.buff(towerId, key),
                config.resolvedReputationBuffCap()
        );
    }

    private static ResourceLocation source(String path) {
        return ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "villager_adv/effect/" + path);
    }
}
