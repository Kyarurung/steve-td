package kim.biryeong.semiontd.tower.frost;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.api.SemionTdApi;
import kim.biryeong.semiontd.api.area.AreaEffectOutcome;
import kim.biryeong.semiontd.api.area.AreaVfxSpec;
import kim.biryeong.semiontd.api.area.MonsterAreaEffectRequest;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.SemionGameManager;
import kim.biryeong.semiontd.job.FrostTowerJob;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.ui.SemionText;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

/** 혹한 빌더의 웨이브별 생산라인 충전과 9번 슬롯 완전 가동 능력. */
public final class FrostFullOperationService {
    private static final int ACTIVATION_SLOT = 8;
    private static final int VFX_INTERVAL_TICKS = 5;
    private static final float FULL_OPERATION_SOUND_PITCH = 0.75F;
    private static final Component ACTIVATION_NAME = Component.literal("냉동창고 완전 가동")
            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
    private static final DustParticleOptions ACTIVE_PARTICLE = new DustParticleOptions(0x43C9FF, 1.45F);
    private static final ResourceLocation CHILL_PULSE_ID =
            ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "frost_full_operation_chill");
    private static final ResourceLocation DAMAGE_REDUCTION_ID =
            ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "frost_full_operation_damage_reduction");
    private static final ResourceLocation FULL_OPERATION_AMBIENT_SOUND_ID =
            ResourceLocation.withDefaultNamespace("ambient.soul_sand_valley.loop");
    private static final Map<UUID, PlayerState> STATES = new ConcurrentHashMap<>();

    private FrostFullOperationService() {
    }

    public static void register(SemionGameManager gameManager) {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide()
                    || hand != InteractionHand.MAIN_HAND
                    || !(player instanceof ServerPlayer serverPlayer)
                    || !isActivationItem(serverPlayer.getItemInHand(hand))) {
                return InteractionResult.PASS;
            }
            SemionGame game = gameManager.playableGame(serverPlayer.getUUID()).orElse(null);
            if (game == null || !game.isActiveParticipant(serverPlayer.getUUID())) {
                return InteractionResult.PASS;
            }
            var semionPlayer = game.players().get(serverPlayer.getUUID());
            if (semionPlayer == null
                    || semionPlayer.job().filter(job -> FrostTowerJob.ID.equals(job.id())).isEmpty()) {
                return InteractionResult.PASS;
            }
            PlayerLane lane = game.playerLane(serverPlayer.getUUID()).orElse(null);
            if (lane == null || !activate(lane, serverPlayer)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.SUCCESS;
        });
    }

    public static void beginWave(PlayerLane lane) {
        if (lane == null || lane.ownerPlayer() == null) {
            return;
        }
        PlayerState state = STATES.computeIfAbsent(lane.ownerPlayer(), ignored -> new PlayerState());
        state.beginWave();
        clearFullOperationEffects(lane);
        onlinePlayer(lane).ifPresent(player -> {
            clearActivationItem(player);
            stopFullOperationPresentation(player);
        });
    }

    public static void endWave(PlayerLane lane) {
        if (lane == null || lane.ownerPlayer() == null) {
            return;
        }
        PlayerState state = STATES.get(lane.ownerPlayer());
        if (state != null) {
            state.endWave();
        }
        clearFullOperationEffects(lane);
        onlinePlayer(lane).ifPresent(player -> {
            clearActivationItem(player);
            stopFullOperationPresentation(player);
        });
    }

    public static void clearPlayer(UUID playerId) {
        if (playerId != null) {
            STATES.remove(playerId);
        }
    }

    public static void cleanupPlayer(ServerPlayer player) {
        if (player != null) {
            clearActivationItem(player);
            stopFullOperationPresentation(player);
            clearPlayer(player.getUUID());
        }
    }

    public static void clearAll() {
        STATES.clear();
    }

    static void recordSpecialActivation(PlayerLane lane, TriggerFamily family) {
        if (lane == null || family == null || lane.arenaWorld() == null) {
            return;
        }
        PlayerState state = STATES.computeIfAbsent(lane.ownerPlayer(), ignored -> new PlayerState());
        if (!state.record(family, lane.arenaWorld().getGameTime())) {
            return;
        }
        updateReadiness(lane, state);
    }

    static void onEruptionChillChanged(PlayerLane lane) {
        if (lane == null) {
            return;
        }
        PlayerState state = STATES.computeIfAbsent(lane.ownerPlayer(), ignored -> new PlayerState());
        updateReadiness(lane, state);
    }

    public static void tick(PlayerLane lane) {
        if (lane == null || lane.arenaWorld() == null) {
            return;
        }
        PlayerState state = STATES.get(lane.ownerPlayer());
        if (state == null || !state.active) {
            return;
        }
        long gameTime = lane.arenaWorld().getGameTime();
        if (gameTime >= state.activeUntilTick) {
            state.active = false;
            clearFullOperationEffects(lane);
            onlinePlayer(lane).ifPresent(FrostFullOperationService::stopFullOperationPresentation);
            return;
        }
        refreshFullOperationEffects(lane, state, gameTime);
        onlinePlayer(lane).ifPresent(player -> player.setTicksFrozen(player.getTicksRequiredToFreeze()));
        if (gameTime >= state.nextChillPulseTick) {
            applyChillPulse(lane);
            state.nextChillPulseTick = gameTime + Math.max(1, FrostBalance.fullOperationChillIntervalTicks());
        }
        if (gameTime >= state.nextVfxTick) {
            showActiveParticles(lane);
            state.nextVfxTick = gameTime + VFX_INTERVAL_TICKS;
        }
    }

    public static double fixedOutgoingDamage(UUID ownerPlayer, long gameTime, double normalDamage) {
        return isActive(ownerPlayer, gameTime)
                ? Math.max(0.0, FrostBalance.fullOperationFixedAttackDamage())
                : normalDamage;
    }

    public static double fixedIncomingDamage(
            UUID ownerPlayer,
            long gameTime,
            double originalDamage,
            double normallyReducedDamage
    ) {
        if (!isActive(ownerPlayer, gameTime)) {
            return normallyReducedDamage;
        }
        return Math.max(0.0, originalDamage) * (1.0 - FrostBalance.fullOperationDamageReduction());
    }

    public static boolean isActivationItem(ItemStack stack) {
        if (stack == null || !stack.is(Items.ICE)) {
            return false;
        }
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        return name != null && ACTIVATION_NAME.getString().equals(name.getString());
    }

    static PlayerState stateForTest(UUID ownerPlayer) {
        return STATES.computeIfAbsent(ownerPlayer, ignored -> new PlayerState());
    }

    private static boolean activate(PlayerLane lane, ServerPlayer player) {
        PlayerState state = STATES.get(lane.ownerPlayer());
        if (state == null || !state.ready || state.usedThisWave || !hasBothDevices(lane)
                || eruptionChill(lane) + 1.0E-9 < FrostBalance.fullOperationEruptionChill()) {
            clearActivationItem(player);
            return false;
        }
        long gameTime = lane.arenaWorld().getGameTime();
        state.activate(gameTime);
        clearActivationItem(player);
        refreshFullOperationEffects(lane, state, gameTime);
        startFullOperationPresentation(player);
        showActiveParticles(lane);
        player.sendSystemMessage(SemionText.prefixedMini(
                "<aqua><bold>냉동창고 완전 가동!</bold></aqua> <gray>5초 동안 방어 체계가 작동합니다.</gray>"
        ));
        return true;
    }

    private static void startFullOperationPresentation(ServerPlayer player) {
        player.setTicksFrozen(player.getTicksRequiredToFreeze());
        player.playNotifySound(
                SoundEvents.ANVIL_USE,
                SoundSource.PLAYERS,
                1.25F,
                1.05F
        );
        player.playNotifySound(
                SoundEvents.BREEZE_WIND_CHARGE_BURST.value(),
                SoundSource.AMBIENT,
                1.0F,
                FULL_OPERATION_SOUND_PITCH
        );
        player.playNotifySound(
                SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP.value(),
                SoundSource.AMBIENT,
                1.0F,
                FULL_OPERATION_SOUND_PITCH
        );
    }

    private static void stopFullOperationPresentation(ServerPlayer player) {
        player.setTicksFrozen(0);
        player.connection.send(fullOperationAmbientStopPacket());
    }

    static ClientboundStopSoundPacket fullOperationAmbientStopPacket() {
        return new ClientboundStopSoundPacket(FULL_OPERATION_AMBIENT_SOUND_ID, SoundSource.AMBIENT);
    }

    static float fullOperationSoundPitch() {
        return FULL_OPERATION_SOUND_PITCH;
    }

    private static void updateReadiness(PlayerLane lane, PlayerState state) {
        if (!state.waveActive || state.ready || state.usedThisWave
                || state.totalActivations < FrostBalance.fullOperationRequiredActivations()
                || !hasBothDevices(lane)
                || eruptionChill(lane) + 1.0E-9 < FrostBalance.fullOperationEruptionChill()) {
            return;
        }
        state.ready = true;
        onlinePlayer(lane).ifPresent(player -> {
            player.getInventory().setItem(ACTIVATION_SLOT, activationItem());
            player.containerMenu.sendAllDataToRemote();
            player.sendSystemMessage(SemionText.prefixedMini(
                    "<aqua>완전 가동 준비 완료!</aqua> <gray>9번 슬롯의 얼음을 우클릭하세요.</gray>"
            ));
        });
    }

    private static boolean hasBothDevices(PlayerLane lane) {
        boolean emission = false;
        boolean eruption = false;
        for (Tower tower : lane.towers()) {
            if (!lane.ownerPlayer().equals(tower.ownerPlayer()) || tower.isDestroyed(lane)) {
                continue;
            }
            emission |= FrostTowers.isEmissionCoolingDevice(tower.type());
            eruption |= FrostTowers.isEruptionCoolingDevice(tower.type());
        }
        return emission && eruption;
    }

    private static double eruptionChill(PlayerLane lane) {
        return lane.towers().stream()
                .filter(tower -> lane.ownerPlayer().equals(tower.ownerPlayer()))
                .filter(FrostEruptionCoolingTower.class::isInstance)
                .map(FrostEruptionCoolingTower.class::cast)
                .mapToDouble(FrostEruptionCoolingTower::operationChill)
                .max()
                .orElse(0.0);
    }

    private static void applyChillPulse(PlayerLane lane) {
        SemionTowerEntity source = operationSource(lane);
        if (source == null) {
            return;
        }
        MonsterAreaEffectRequest request = MonsterAreaEffectRequest.aroundTower(
                CHILL_PULSE_ID,
                source,
                Math.max(0.01, FrostBalance.fullOperationAreaRadius()),
                AreaVfxSpec.none()
        );
        SemionTdApi.areaEffects().applyToMonsters(request, target -> {
            var result = FrostMonsterStates.applyChill(target, FrostBalance.fullOperationChillPerPulse());
            return result.currentChill() > result.previousChill() || result.becameRefrigerated()
                    ? AreaEffectOutcome.APPLIED
                    : AreaEffectOutcome.UNCHANGED;
        });
    }

    private static void showActiveParticles(PlayerLane lane) {
        ServerLevel level = lane.arenaWorld();
        if (level == null) {
            return;
        }
        for (Tower tower : lane.towers()) {
            if (!lane.ownerPlayer().equals(tower.ownerPlayer()) || tower.isDestroyed(lane)) {
                continue;
            }
            towerEntity(level, tower).ifPresent(entity -> level.sendParticles(
                    ACTIVE_PARTICLE,
                    entity.getX(),
                    entity.getY() + Math.max(0.5, entity.getBbHeight() * 0.6),
                    entity.getZ(),
                    12,
                    0.35,
                    0.45,
                    0.35,
                    0.02
            ));
        }
    }

    private static SemionTowerEntity operationSource(PlayerLane lane) {
        ServerLevel level = lane.arenaWorld();
        if (level == null) {
            return null;
        }
        return lane.towers().stream()
                .filter(tower -> lane.ownerPlayer().equals(tower.ownerPlayer()))
                .filter(tower -> FrostTowers.isEruptionCoolingDevice(tower.type())
                        || FrostTowers.isEmissionCoolingDevice(tower.type()))
                .map(tower -> towerEntity(level, tower).orElse(null))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static java.util.Optional<SemionTowerEntity> towerEntity(ServerLevel level, Tower tower) {
        if (!(tower instanceof EntityBackedTower entityBacked) || entityBacked.entityId().isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.ofNullable(level.getEntity(entityBacked.entityId().getAsInt()))
                .filter(SemionTowerEntity.class::isInstance)
                .map(SemionTowerEntity.class::cast);
    }

    public static boolean isActive(UUID ownerPlayer, long gameTime) {
        if (ownerPlayer == null) {
            return false;
        }
        PlayerState state = STATES.get(ownerPlayer);
        return state != null && state.active && gameTime < state.activeUntilTick;
    }

    public static double displayedDamageReduction(UUID ownerPlayer, long gameTime, double normalReduction) {
        return isActive(ownerPlayer, gameTime)
                ? Math.max(0.0, FrostBalance.fullOperationDamageReduction())
                : normalReduction;
    }

    private static ItemStack activationItem() {
        ItemStack stack = new ItemStack(Items.ICE);
        stack.set(DataComponents.CUSTOM_NAME, ACTIVATION_NAME);
        stack.set(DataComponents.LORE, new ItemLore(java.util.List.of(
                SemionText.mini("<gray>[5초간 지속, 5초간 자신의 모든 타워의 받는 피해 감소가 95%로 고정되며 매초 모든 적에게 100%의 한기가 부여됨.</gray>"),
                SemionText.mini("<gray>지속 시간 동안 본인의 모든 타워가 입히는 공격력 피해가 5로 고정됨. ]</gray>")
        )));
        return stack;
    }

    static ItemStack activationItemForTest() {
        return activationItem();
    }

    private static void refreshFullOperationEffects(PlayerLane lane, PlayerState state, long gameTime) {
        ServerLevel level = lane.arenaWorld();
        int remainingTicks = (int) Math.max(1L, state.activeUntilTick - gameTime);
        double reduction = Math.max(0.0, FrostBalance.fullOperationDamageReduction());
        for (Tower tower : lane.towers()) {
            if (!lane.ownerPlayer().equals(tower.ownerPlayer()) || tower.isDestroyed(lane)) {
                continue;
            }
            towerEntity(level, tower).ifPresent(entity -> entity.refreshTimedEffect(
                    TimedEffectType.TOWER_DAMAGE_REDUCTION,
                    DAMAGE_REDUCTION_ID,
                    reduction,
                    remainingTicks
            ));
        }
    }

    private static void clearFullOperationEffects(PlayerLane lane) {
        if (lane == null || lane.arenaWorld() == null) {
            return;
        }
        for (Tower tower : lane.towers()) {
            if (!lane.ownerPlayer().equals(tower.ownerPlayer())) {
                continue;
            }
            towerEntity(lane.arenaWorld(), tower).ifPresent(entity -> entity.refreshTimedEffect(
                    TimedEffectType.TOWER_DAMAGE_REDUCTION,
                    DAMAGE_REDUCTION_ID,
                    0.0,
                    1
            ));
        }
    }

    public static void clearActivationItem(ServerPlayer player) {
        ItemStack existing = player.getInventory().getItem(ACTIVATION_SLOT);
        if (isActivationItem(existing)) {
            player.getInventory().setItem(ACTIVATION_SLOT, ItemStack.EMPTY);
            player.containerMenu.sendAllDataToRemote();
        }
    }

    private static java.util.Optional<ServerPlayer> onlinePlayer(PlayerLane lane) {
        if (lane == null || lane.arenaWorld() == null || lane.arenaWorld().getServer() == null) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.ofNullable(
                lane.arenaWorld().getServer().getPlayerList().getPlayer(lane.ownerPlayer()));
    }

    enum TriggerFamily {
        DONGTAE,
        FROZEN_FOOD,
        ICEBOX
    }

    static final class PlayerState {
        private final EnumMap<TriggerFamily, Integer> familyActivations =
                new EnumMap<>(TriggerFamily.class);
        private final EnumMap<TriggerFamily, Long> lastActivationTick =
                new EnumMap<>(TriggerFamily.class);
        private int totalActivations;
        private boolean waveActive;
        private boolean ready;
        private boolean usedThisWave;
        private boolean active;
        private long activeUntilTick;
        private long nextChillPulseTick;
        private long nextVfxTick;

        void beginWave() {
            familyActivations.clear();
            lastActivationTick.clear();
            totalActivations = 0;
            waveActive = true;
            ready = false;
            usedThisWave = false;
            active = false;
            activeUntilTick = 0L;
            nextChillPulseTick = 0L;
            nextVfxTick = 0L;
        }

        void endWave() {
            waveActive = false;
            ready = false;
            active = false;
        }

        boolean record(TriggerFamily family, long gameTime) {
            if (!waveActive || lastActivationTick.getOrDefault(family, Long.MIN_VALUE) == gameTime) {
                return false;
            }
            int count = familyActivations.getOrDefault(family, 0);
            if (count >= FrostBalance.fullOperationMaxActivationsPerFamily()) {
                return false;
            }
            lastActivationTick.put(family, gameTime);
            familyActivations.put(family, count + 1);
            totalActivations++;
            return true;
        }

        void activate(long gameTime) {
            ready = false;
            usedThisWave = true;
            active = true;
            activeUntilTick = gameTime + Math.max(1, FrostBalance.fullOperationDurationTicks());
            nextChillPulseTick = gameTime;
            nextVfxTick = gameTime;
        }

        int totalActivations() {
            return totalActivations;
        }

        int familyActivations(TriggerFamily family) {
            return familyActivations.getOrDefault(family, 0);
        }
    }
}
