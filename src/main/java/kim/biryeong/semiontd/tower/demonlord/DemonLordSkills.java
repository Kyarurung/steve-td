package kim.biryeong.semiontd.tower.demonlord;

import java.util.ArrayList;
import java.util.List;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.monster.DamageType;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The five demon lord skills.
 *
 * <p>Every skill reads its numbers from the altar's tower id, so a live config can retune any single
 * tier. Damage is always multiplied by {@link DemonLordState#damageMultiplier()} - levels are the
 * builder's only scaling, since none of its towers ever deal damage.
 */
public final class DemonLordSkills {
    private DemonLordSkills() {
    }

    public static void cast(
            ServerPlayer player,
            PlayerLane lane,
            DemonLordState state,
            DemonLordSkill skill,
            TowerType altarType
    ) {
        switch (skill) {
            case WAVE_OF_MALICE -> castWaveOfMalice(player, lane, state, altarType);
            case DEMON_WINGS -> castDemonWings(player, lane, state, altarType);
            case SKY_BREAKER -> castSkyBreaker(player, lane, state, altarType);
            case ARCANE_BOMBARDMENT -> castArcaneBombardment(player, lane, state, altarType);
            case DEMON_BARRIER -> castDemonBarrier(player, state, altarType);
            default -> {
            }
        }
    }

    /** 전방 부채꼴을 쓸어 피해를 주고 뒤로 밀어냅니다. */
    private static void castWaveOfMalice(ServerPlayer player, PlayerLane lane, DemonLordState state, TowerType altar) {
        double range = ability(altar, "range", 6.0);
        double halfAngleCos = Math.cos(Math.toRadians(ability(altar, "coneDegrees", 60.0) / 2.0));
        double damage = ability(altar, "damage", 45.0) * state.damageMultiplier();
        double knockback = ability(altar, "knockback", 0.8);

        Vec3 origin = player.position();
        Vec3 look = horizontal(player.getLookAngle());
        for (SemionMonsterEntity monster : monstersNear(lane, origin, range)) {
            Vec3 toMonster = horizontal(monster.position().subtract(origin));
            if (toMonster.lengthSqr() > 1.0e-4 && look.dot(toMonster.normalize()) < halfAngleCos) {
                continue;
            }
            DemonLordService.dealDamage(player, monster, damage, DamageType.MAGIC);
            push(monster, toMonster, knockback, 0.35);
        }
        effect(player, ParticleTypes.SOUL_FIRE_FLAME, origin.add(look.scale(2.0)), 40, 1.5);
        sound(player, SoundEvents.WARDEN_SONIC_BOOM, 0.7f, 1.4f);
    }

    /** 도약하며 주위를 밀어내고 체력을 회복합니다. */
    private static void castDemonWings(ServerPlayer player, PlayerLane lane, DemonLordState state, TowerType altar) {
        double radius = ability(altar, "radius", 4.0);
        double damage = ability(altar, "damage", 30.0) * state.damageMultiplier();
        double knockback = ability(altar, "knockback", 0.7);
        double leapPower = ability(altar, "leapPower", 1.0);

        Vec3 origin = player.position();
        for (SemionMonsterEntity monster : monstersNear(lane, origin, radius)) {
            DemonLordService.dealDamage(player, monster, damage, DamageType.MAGIC);
            push(monster, horizontal(monster.position().subtract(origin)), knockback, 0.4);
        }
        state.heal(state.maxHealth() * ability(altar, "healRatio", 0.10));

        Vec3 look = horizontal(player.getLookAngle());
        player.setDeltaMovement(look.x * leapPower, 0.62, look.z * leapPower);
        player.hurtMarked = true;
        player.resetFallDistance();

        effect(player, ParticleTypes.SOUL, origin, 30, 1.2);
        sound(player, SoundEvents.ENDER_DRAGON_FLAP, 1.0f, 0.8f);
    }

    /**
     * 전방으로 돌진해 부딪힌 적을 띄우고 기절시킵니다.
     *
     * <p>돌진은 텔레포트로 처리합니다. 속도로 밀면 서버 틱 동안 충돌 판정이 새기 때문에, 경로를
     * 샘플링해 맞은 적을 모두 잡아낸 뒤 끝점으로 옮기는 쪽이 결과가 일정합니다.
     */
    private static void castSkyBreaker(ServerPlayer player, PlayerLane lane, DemonLordState state, TowerType altar) {
        double distance = ability(altar, "dashDistance", 8.0);
        double hitRadius = ability(altar, "hitRadius", 2.0);
        double damage = ability(altar, "damage", 90.0) * state.damageMultiplier();
        double lift = ability(altar, "liftPower", 0.8);
        int stunTicks = (int) Math.max(1.0, ability(altar, "stunTicks", 40.0));

        Vec3 start = player.position();
        Vec3 look = horizontal(player.getLookAngle());
        Vec3 end = start.add(look.scale(distance));

        List<SemionMonsterEntity> hit = new ArrayList<>();
        int samples = Math.max(2, (int) Math.ceil(distance));
        for (int i = 0; i <= samples; i++) {
            Vec3 point = start.add(look.scale(distance * i / samples));
            for (SemionMonsterEntity monster : monstersNear(lane, point, hitRadius)) {
                if (!hit.contains(monster)) {
                    hit.add(monster);
                }
            }
        }
        for (SemionMonsterEntity monster : hit) {
            DemonLordService.dealDamage(player, monster, damage, DamageType.PHYSICAL);
            monster.setDeltaMovement(monster.getDeltaMovement().x, lift, monster.getDeltaMovement().z);
            monster.hurtMarked = true;
            // 기절: 이동·공격 속도·공격력을 모두 100% 깎아 아무것도 못 하게 만듭니다.
            monster.applyTimedEffect(TimedEffectType.MONSTER_MOVE_SPEED_REDUCTION, 1.0, stunTicks);
            monster.applyTimedEffect(TimedEffectType.MONSTER_ATTACK_SPEED_REDUCTION, 1.0, stunTicks);
            monster.applyTimedEffect(TimedEffectType.MONSTER_ATTACK_DAMAGE_REDUCTION, 1.0, stunTicks);
        }

        player.teleportTo(end.x, end.y, end.z);
        player.resetFallDistance();
        effect(player, ParticleTypes.EXPLOSION, end, 6, 1.0);
        sound(player, SoundEvents.RAVAGER_ROAR, 1.0f, 0.9f);
    }

    /**
     * 높이 뛴 뒤 바라보는 방향 착탄 지점에 원형 광역 피해를 줍니다.
     *
     * <p>실제 투사체 엔티티 대신 시선 방향으로 즉시 레이캐스트해 착탄 지점을 구합니다. 서버 입장에서
     * 결과는 같고, 라운드마다 수십 발이 날아다니는 엔티티를 만들지 않아도 됩니다.
     */
    private static void castArcaneBombardment(ServerPlayer player, PlayerLane lane, DemonLordState state, TowerType altar) {
        double blastRadius = ability(altar, "blastRadius", 4.0);
        double damage = ability(altar, "damage", 70.0) * state.damageMultiplier();
        double range = ability(altar, "projectileRange", 18.0);

        Vec3 eye = player.getEyePosition();
        Vec3 target = eye.add(player.getLookAngle().scale(range));
        HitResult clip = player.level().clip(new ClipContext(
                eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 impact = clip.getType() == HitResult.Type.MISS ? target : clip.getLocation();

        for (SemionMonsterEntity monster : monstersNear(lane, impact, blastRadius)) {
            DemonLordService.dealDamage(player, monster, damage, DamageType.MAGIC);
        }

        player.setDeltaMovement(player.getDeltaMovement().x, ability(altar, "jumpPower", 0.9), player.getDeltaMovement().z);
        player.hurtMarked = true;
        player.resetFallDistance();

        effect(player, ParticleTypes.EXPLOSION_EMITTER, impact, 1, 0.0);
        sound(player, SoundEvents.GENERIC_EXPLODE.value(), 1.0f, 1.1f);
    }

    /** 최대 체력 비례 방어막을 두릅니다. */
    private static void castDemonBarrier(ServerPlayer player, DemonLordState state, TowerType altar) {
        double shield = state.maxHealth() * ability(altar, "shieldRatio", 0.25);
        int duration = (int) Math.max(1.0, ability(altar, "shieldDurationTicks", 160.0));
        state.grantShield(shield, player.level().getGameTime() + duration);
        effect(player, ParticleTypes.SCULK_SOUL, player.position(), 30, 1.0);
        sound(player, SoundEvents.TOTEM_USE, 0.8f, 1.2f);
    }

    // ------------------------------------------------------------- internals

    private static double ability(TowerType altar, String key, double fallback) {
        return TowerBalanceRuntime.ability(altar.id(), key, fallback);
    }

    /** Live monsters of this lane whose entity sits within {@code radius} of {@code center}. */
    private static List<SemionMonsterEntity> monstersNear(PlayerLane lane, Vec3 center, double radius) {
        List<SemionMonsterEntity> found = new ArrayList<>();
        double radiusSqr = radius * radius;
        for (Monster monster : List.copyOf(lane.activeMonsters())) {
            if (monster == null || !monster.isAlive() || !monster.hasMinecraftEntity()) {
                continue;
            }
            if (!(lane.arenaWorld().getEntity(monster.minecraftEntityId()) instanceof SemionMonsterEntity entity)
                    || entity.isRemoved()) {
                continue;
            }
            if (entity.position().distanceToSqr(center) <= radiusSqr) {
                found.add(entity);
            }
        }
        return found;
    }

    private static Vec3 horizontal(Vec3 vector) {
        Vec3 flat = new Vec3(vector.x, 0.0, vector.z);
        return flat.lengthSqr() < 1.0e-6 ? new Vec3(0.0, 0.0, 1.0) : flat.normalize();
    }

    private static void push(SemionMonsterEntity monster, Vec3 direction, double strength, double lift) {
        Vec3 away = horizontal(direction).scale(strength);
        monster.setDeltaMovement(away.x, lift, away.z);
        monster.hurtMarked = true;
    }

    private static void effect(ServerPlayer player, net.minecraft.core.particles.ParticleOptions particle,
            Vec3 at, int count, double spread) {
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(particle, at.x, at.y + 1.0, at.z, count, spread, spread, spread, 0.02);
        }
    }

    private static void sound(ServerPlayer player, net.minecraft.sounds.SoundEvent event, float volume, float pitch) {
        if (player.level() instanceof ServerLevel level) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), event, SoundSource.PLAYERS, volume, pitch);
        }
    }
}
