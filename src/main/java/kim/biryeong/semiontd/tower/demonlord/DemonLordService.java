package kim.biryeong.semiontd.tower.demonlord;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.entity.monster.DamageType;
import kim.biryeong.semiontd.entity.monster.KillSourceKind;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionPlayer;
import kim.biryeong.semiontd.job.DemonLordTowerJob;
import kim.biryeong.semiontd.tower.Tower;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Drives everything the demon lord player does: the boss bar, the combat lock, the hotbar and the
 * melee swing.
 *
 * <p>Health never touches the vanilla player. Incoming damage is intercepted and routed into
 * {@link DemonLordState}, so the player cannot actually die, respawn or drop anything - emptying the
 * pool simply flips them to 전투 제외 until the next round.
 */
public final class DemonLordService {
    private static final Map<UUID, ServerBossEvent> BOSS_BARS = new ConcurrentHashMap<>();

    private static final Component BLADE_NAME =
            Component.literal("마검").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);

    private DemonLordService() {
    }

    /**
     * Hooks damage and melee. Must run after {@code SemionPlayerProtectionService}, which already
     * stops protecting a demon lord while they are in combat.
     */
    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayer player)) {
                return true;
            }
            DemonLordState state = DemonLordStates.get(player.getUUID());
            if (state == null || !state.inCombat()) {
                return true;
            }
            // 체력은 보스바 풀에서만 관리합니다. 바닐라 체력은 건드리지 않습니다.
            state.expireShieldIfNeeded(player.level().getGameTime());
            boolean knockedOut = state.applyDamage(amount);
            // 바닐라 피해를 막으면 연출도 같이 사라지므로 피격 패킷을 직접 보냅니다.
            sendHitFeedback(player, source, amount);
            if (knockedOut) {
                knockOutOfCombat(player, state);
            }
            return false;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND || !(player instanceof ServerPlayer attacker)) {
                return InteractionResult.PASS;
            }
            DemonLordState state = DemonLordStates.get(attacker.getUUID());
            if (state == null || !state.inCombat() || !(target instanceof SemionMonsterEntity monsterEntity)) {
                return InteractionResult.PASS;
            }
            // 마검 평타. 바닐라 피해 대신 런타임 피해로 넣어야 몹의 방어/저항이 정상 적용됩니다.
            dealDamage(attacker, monsterEntity, state.bladeDamage(), DamageType.PHYSICAL);
            return InteractionResult.SUCCESS;
        });
    }

    /** Called once per lane tick from {@code PlayerLane}. */
    public static void tick(PlayerLane lane, Map<UUID, SemionPlayer> players) {
        if (lane == null || lane.arenaWorld() == null || players == null) {
            return;
        }
        UUID owner = lane.ownerPlayer();
        SemionPlayer semionPlayer = owner == null ? null : players.get(owner);
        if (semionPlayer == null || !isDemonLord(semionPlayer)) {
            return;
        }
        ServerPlayer player = lane.arenaWorld().getServer().getPlayerList().getPlayer(owner);
        if (player == null) {
            return;
        }
        DemonLordState state = DemonLordStates.getOrCreate(owner);
        state.setLaneId(lane.laneId());
        long gameTime = lane.arenaWorld().getGameTime();

        // 초당 한 번 강제로 다시 깔아, 인벤토리에서 스킬이나 마검을 옮겨도 제자리로 돌아옵니다.
        if (gameTime % 20 == 0) {
            state.markLoadoutDirty();
        }
        if (state.loadoutDirty()) {
            syncHotbar(player, lane, state);
            state.clearLoadoutDirty();
        }
        syncBossBar(player, state);

        if (!state.inCombat()) {
            restoreFlight(player);
            return;
        }

        if (state.consumePendingSpawn()) {
            moveToLaneCentre(player, lane);
        }
        state.expireShieldIfNeeded(gameTime);
        lockFlight(player);
        leashToLane(player, lane);
        detectSkillCast(player, lane, state, gameTime);
    }

    /** Round start: pull the demon lord to the middle of their own lane. */
    private static void moveToLaneCentre(ServerPlayer player, PlayerLane lane) {
        if (lane.laneLayout() == null) {
            return;
        }
        Vec3 centre = lane.laneLayout().positionAt(0.5);
        player.teleportTo(centre.x, centre.y, centre.z);
        player.getInventory().setSelectedSlot(DemonLordSkill.BLADE_SLOT);
    }

    public static void clearBossBar(UUID playerId) {
        ServerBossEvent bar = BOSS_BARS.remove(playerId);
        if (bar != null) {
            bar.removeAllPlayers();
        }
    }

    // ------------------------------------------------------------- internals

    private static boolean isDemonLord(SemionPlayer player) {
        return player.job().map(job -> DemonLordTowerJob.ID.equals(job.id())).orElse(false);
    }

    private static void knockOutOfCombat(ServerPlayer player, DemonLordState state) {
        state.leaveCombat();
        restoreFlight(player);
        player.getInventory().setSelectedSlot(DemonLordSkill.BLADE_SLOT);
        player.displayClientMessage(
                Component.literal("전투에서 제외되었습니다. 다음 라운드에 부활합니다.")
                        .withStyle(ChatFormatting.DARK_RED),
                false
        );
    }

    /**
     * Rebuilds the hit feedback that blocking vanilla damage throws away.
     *
     * <p>{@link ClientboundHurtAnimationPacket} is what makes the screen flash red and tilts the
     * camera away from the attacker - the same packet vanilla sends on a normal hit. Without it the
     * demon lord takes damage with no on-screen sign at all beyond the boss bar sliding.
     */
    private static void sendHitFeedback(ServerPlayer player, DamageSource source, double amount) {
        float hurtDirection = 0.0f;
        Entity attacker = source == null ? null : source.getEntity();
        if (attacker != null) {
            hurtDirection = (float) (Mth.atan2(attacker.getZ() - player.getZ(), attacker.getX() - player.getX())
                    * (180.0 / Math.PI) - player.getYRot());
        }
        player.connection.send(new ClientboundHurtAnimationPacket(player.getId(), hurtDirection));

        // 큰 피해일수록 강하게: 최대 체력의 5% 를 넘는 타격에만 낮은 신음을 겹칩니다.
        DemonLordState state = DemonLordStates.get(player.getUUID());
        boolean heavy = state != null && amount >= state.maxHealth() * 0.05;
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(heavy ? SoundEvents.WARDEN_HEARTBEAT : SoundEvents.PLAYER_HURT),
                SoundSource.PLAYERS,
                player.getX(),
                player.getY(),
                player.getZ(),
                heavy ? 1.0f : 0.6f,
                heavy ? 0.7f : 1.0f,
                player.level().getRandom().nextLong()
        ));
    }

    private static void syncBossBar(ServerPlayer player, DemonLordState state) {
        ServerBossEvent bar = BOSS_BARS.computeIfAbsent(player.getUUID(), id -> {
            ServerBossEvent created = new ServerBossEvent(
                    Component.empty(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
            created.addPlayer(player);
            return created;
        });
        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
        if (state.inCombat()) {
            bar.setName(Component.literal(
                            "마왕 Lv." + state.level() + "  " + Math.round(state.health()) + " / " + Math.round(state.maxHealth())
                                    + (state.shield() > 0.0 ? "  (+" + Math.round(state.shield()) + ")" : ""))
                    .withStyle(ChatFormatting.RED));
            bar.setColor(BossEvent.BossBarColor.RED);
            bar.setProgress((float) state.healthRatio());
        } else {
            bar.setName(Component.literal("마왕 Lv." + state.level() + "  [전투 제외]")
                    .withStyle(ChatFormatting.DARK_GRAY));
            bar.setColor(BossEvent.BossBarColor.WHITE);
            bar.setProgress(0.0f);
        }
    }

    private static void lockFlight(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.getAbilities().mayfly || player.getAbilities().flying) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    private static void restoreFlight(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    /**
     * Keeps the demon lord inside their own lane. Rather than a hard wall we snap them back onto the
     * lane path at whatever progress they had reached, which reads as being yanked back by the lane.
     */
    private static void leashToLane(ServerPlayer player, PlayerLane lane) {
        // 레인을 다 정리했으면 중앙 방어 웨이브로 넘어가야 하므로 묶어두지 않습니다.
        if (lane.laneLayout() == null || lane.clearedThisRound()) {
            return;
        }
        double leash = TowerBalanceRuntime.ability(DemonLordTowers.GLOBAL_CONFIG_ID, "laneLeashRadius", 24.0);
        Vec3 position = player.position();
        Vec3 onPath = lane.laneLayout().positionAt(lane.laneLayout().progressAt(position));
        if (position.distanceTo(onPath) <= leash) {
            return;
        }
        player.teleportTo(onPath.x, onPath.y, onPath.z);
    }

    /**
     * Casting is "move your hand to the slot", so we poll the selected slot instead of listening for
     * a use packet. A cast always bounces the hand back to the 마검 slot, which also means holding a
     * skill slot cannot re-trigger it every tick.
     */
    private static void detectSkillCast(ServerPlayer player, PlayerLane lane, DemonLordState state, long gameTime) {
        int selected = player.getInventory().getSelectedSlot();
        if (selected == state.lastSelectedSlot()) {
            return;
        }
        state.setLastSelectedSlot(selected);

        DemonLordSkill skill = DemonLordSkill.fromHotbarSlot(selected);
        if (skill == null) {
            return;
        }
        DemonLordSkillTower altar = findAltar(lane, player.getUUID(), skill);
        if (altar == null) {
            return;
        }
        if (!state.isSkillReady(skill, gameTime)) {
            player.getInventory().setSelectedSlot(DemonLordSkill.BLADE_SLOT);
            state.setLastSelectedSlot(DemonLordSkill.BLADE_SLOT);
            return;
        }

        DemonLordSkills.cast(player, lane, state, skill, altar.type());
        int cooldown = altar.cooldownTicks();
        state.startCooldown(skill, gameTime, cooldown);
        player.getCooldowns().addCooldown(new ItemStack(skill.item()), cooldown);

        player.getInventory().setSelectedSlot(DemonLordSkill.BLADE_SLOT);
        state.setLastSelectedSlot(DemonLordSkill.BLADE_SLOT);
    }

    private static DemonLordSkillTower findAltar(PlayerLane lane, UUID owner, DemonLordSkill skill) {
        for (Tower tower : List.copyOf(lane.towers())) {
            if (tower instanceof DemonLordSkillTower altar
                    && owner.equals(altar.ownerPlayer())
                    && altar.skill() == skill) {
                return altar;
            }
        }
        return null;
    }

    /**
     * Rebuilds slots 3-7 from the altars that are actually standing, and keeps the 마검 in slot 8.
     * Slots without an altar are emptied so a sold skill disappears from the bar immediately.
     */
    private static void syncHotbar(ServerPlayer player, PlayerLane lane, DemonLordState state) {
        for (DemonLordSkill skill : DemonLordSkill.values()) {
            DemonLordSkillTower altar = findAltar(lane, player.getUUID(), skill);
            if (altar == null) {
                player.getInventory().setItem(skill.hotbarSlot(), ItemStack.EMPTY);
                continue;
            }
            ItemStack stack = new ItemStack(skill.item());
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(skill.displayName())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            player.getInventory().setItem(skill.hotbarSlot(), stack);
        }
        ItemStack blade = new ItemStack(Items.NETHERITE_SWORD);
        blade.set(DataComponents.CUSTOM_NAME, BLADE_NAME);
        player.getInventory().setItem(DemonLordSkill.BLADE_SLOT, blade);
    }

    /** Shared damage entry point for the blade and every skill. */
    static void dealDamage(ServerPlayer attacker, SemionMonsterEntity monsterEntity, double amount, DamageType type) {
        if (amount <= 0.0 || monsterEntity == null || monsterEntity.isRemoved()) {
            return;
        }
        Monster monster = monsterEntity.runtimeMonster();
        if (monster == null || !monster.isAlive()) {
            return;
        }
        double before = monster.health();
        monsterEntity.applyRuntimeDamage(attacker.damageSources().playerAttack(attacker), amount, type);
        if (monster.health() < before) {
            monster.recordLastHit(attacker.getUUID(), KillSourceKind.TOWER);
        }
    }
}
