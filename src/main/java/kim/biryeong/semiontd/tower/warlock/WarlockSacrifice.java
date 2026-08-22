package kim.biryeong.semiontd.tower.warlock;

import java.util.Comparator;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import net.minecraft.world.phys.Vec3;

final class WarlockSacrifice {
    private final WarlockConfig config;
    private final WarlockState state;

    WarlockSacrifice(WarlockConfig config, WarlockState state) {
        this.config = config;
        this.state = state;
    }

    boolean absorbNearest(
            WarlockTower warlock,
            SemionTowerEntity towerEntity,
            PlayerLane lane,
            Comparator<Tower> priority
    ) {
        if (warlock == null
                || towerEntity == null
                || towerEntity.runtimeTower() != warlock
                || lane == null
                || priority == null) {
            return false;
        }
        WarlockConfig.SacrificeRule rule = config.path(warlock.path()).sacrifice();
        Tower target = lane.towers().stream()
                .filter(tower -> isEligibleTarget(warlock, tower, rule.radius()))
                .min(priority)
                .orElse(null);
        if (target == null) {
            return false;
        }

        double sacrificedHealth = target.currentMaxHealth();
        double sacrificedDamage = target.modifyAttackDamage(null, null, target.type().damage());
        int sacrificedInterval = target.type().attackIntervalTicks();
        Vec3 center = sacrificedCenter(lane, target);
        if (!lane.killTower(target)) {
            return false;
        }

        TowerVfxService.showWarlockSacrifice(towerEntity, center);
        double previousMaxHealth = warlock.currentMaxHealth();
        absorbStats(warlock.path(), warlock.type().attackIntervalTicks(), sacrificedHealth, sacrificedDamage, sacrificedInterval);
        double increasedMaxHealth = Math.max(0.0, warlock.currentMaxHealth() - previousMaxHealth);
        warlock.refreshAfterSacrifice(lane, towerEntity, increasedMaxHealth + rule.completionHealing());
        return true;
    }

    double passiveHealthBonus(WarlockTower warlock, PlayerLane lane) {
        return config.path(warlock.path()).passive().healthBonus(passiveStackCount(warlock, lane));
    }

    double passiveDamageBonus(WarlockTower warlock, PlayerLane lane) {
        return config.path(warlock.path()).passive().damageBonus(passiveStackCount(warlock, lane));
    }

    double damageReduction(WarlockPath path) {
        int sacrifices = path == WarlockPath.RANGED
                ? state.roundSacrificeCount()
                : state.totalSacrificeCount();
        return config.path(path).defense().value(sacrifices);
    }

    double maximumDamageReduction(WarlockPath path) {
        return config.path(path).defense().maximum();
    }

    private void absorbStats(
            WarlockPath path,
            int baseAttackIntervalTicks,
            double sacrificedHealth,
            double sacrificedDamage,
            int sacrificedIntervalTicks
    ) {
        WarlockConfig.PathRule rule = config.path(path);
        WarlockConfig.AbsorptionRule absorption = rule.absorption();
        if (path == WarlockPath.BASE) {
            state.absorbBasePermanently(
                    sacrificedHealth,
                    sacrificedDamage,
                    absorption.permanentHealthRatio(),
                    absorption.permanentDamageRatio()
            );
            return;
        }

        state.absorbForRound(sacrificedHealth, sacrificedDamage, absorption.roundStatRatio());
        state.absorbPermanently(
                sacrificedHealth,
                sacrificedDamage,
                absorption.permanentHealthRatio(),
                absorption.permanentDamageRatio()
        );
        if (path == WarlockPath.RANGED) {
            state.absorbAttackInterval(
                    baseAttackIntervalTicks,
                    sacrificedIntervalTicks,
                    config.combat().maximumIntervalReductionTicks()
            );
        }
    }

    private int passiveStackCount(WarlockTower warlock, PlayerLane lane) {
        if (lane == null || warlock.path() == WarlockPath.BASE) {
            return 0;
        }
        return (int) lane.towers().stream()
                .filter(tower -> tower != warlock)
                .filter(tower -> tower.health() > 0.0)
                .filter(tower -> sameOwner(warlock, tower))
                .filter(tower -> warlock.path().acceptsSacrificeTower(tower.type()))
                .count();
    }

    private static boolean sameOwner(WarlockTower warlock, Tower tower) {
        return tower != null && warlock.ownerPlayer().equals(tower.ownerPlayer());
    }

    static boolean isEligibleTarget(WarlockTower warlock, Tower tower, double radius) {
        return warlock != null
                && tower != null
                && tower != warlock
                && tower.health() > 0.0
                && !WarlockTowers.isWarlockCore(tower.type())
                && sameOwner(warlock, tower)
                && withinRadius(warlock, tower, radius);
    }

    private static boolean withinRadius(WarlockTower warlock, Tower tower, double radius) {
        if (tower == null || radius <= 0.0) {
            return tower != null;
        }
        double dx = tower.position().x() - warlock.position().x();
        double dy = tower.position().y() - warlock.position().y();
        double dz = tower.position().z() - warlock.position().z();
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    private static Vec3 sacrificedCenter(PlayerLane lane, Tower target) {
        if (target instanceof EntityBackedTower entityBacked && entityBacked.entityId().isPresent()) {
            var entity = lane.arenaWorld().getEntity(entityBacked.entityId().getAsInt());
            if (entity instanceof SemionTowerEntity towerEntity) {
                return new Vec3(
                        towerEntity.getX(),
                        towerEntity.getY() + Math.max(0.35, towerEntity.getBbHeight() * 0.65),
                        towerEntity.getZ()
                );
            }
        }
        return new Vec3(
                target.position().x() + 0.5,
                target.position().y() + 1.5,
                target.position().z() + 0.5
        );
    }
}
