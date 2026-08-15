package kim.biryeong.semiontd;

import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;
import kim.biryeong.semiontd.cosmetic.CosmeticItemSupport;
import kim.biryeong.semiontd.cosmetic.CosmeticService;
import kim.biryeong.semiontd.game.SemionGameManager;
import kim.biryeong.semiontd.game.SemionPlayerProtectionService;
import kim.biryeong.semiontd.skybox.SemionSkyboxService;
import kim.biryeong.semiontd.tip.SemionTipService;
import kim.biryeong.semiontd.tower.demonlord.DemonLordBinding;
import kim.biryeong.semiontd.tower.demonlord.DemonLordService;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerC2SPacketEvent;
import xyz.nucleoid.stimuli.event.player.PlayerConsumeHungerEvent;
import xyz.nucleoid.stimuli.event.player.PlayerSwapWithOffhandEvent;

public final class Events {

    public static void initialize(
            SemionGameManager gameManager,
            SemionSkyboxService skyboxService,
            SemionTipService tipService,
            CosmeticService cosmeticService
    ) {
        SemionPlayerProtectionService.register(gameManager);
        // 보호 서비스 뒤에 등록해야 마왕만 예외로 피해를 받고 평타를 넣을 수 있습니다.
        DemonLordService.register(gameManager);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            gameManager.tick(server);
            gameManager.tickStartupLobbyLoad(server);
            skyboxService.tick(server);
            tipService.tick(server);
            TowerVfxService.endServerTick(server);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            cosmeticService.load(server);
            gameManager.scheduleStartupLobbyLoad(server);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            gameManager.restoreCombatTickRate(server);
            TowerVfxService.shutdown();
            skyboxService.shutdown();
            tipService.shutdown();
            gameManager.shutdown();
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            gameManager.handlePlayerJoin(handler.getPlayer());
            tipService.handlePlayerJoin(handler.getPlayer());
            cosmeticService.syncPlayer(handler.getPlayer());
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> cosmeticService.syncPlayer(newPlayer));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            // The disconnect callback may run from Netty's channel thread. Entity
            // cleanup must be deferred to the server thread (C2ME enforces this).
            var player = handler.getPlayer();
            server.execute(() -> {
                skyboxService.handlePlayerDisconnect(player);
                tipService.handlePlayerDisconnect(player);
                gameManager.handlePlayerDisconnect(player);
                // 보스바는 명시적으로 지워야 사라집니다. 재접속 시 유령 바가 남지 않게 합니다.
                DemonLordService.clearBossBar(player.getUUID());
            });
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            skyboxService.handlePlayerWorldChanged(player);
            gameManager.handlePlayerWorldChanged(player);
            // 경기가 끝나 로비로 돌아갈 때 보스바를 걷습니다. 아직 전투 중이면 다음 틱에
            // syncBossBar 가 다시 만들어 주므로 무조건 지워도 안전합니다.
            DemonLordService.clearBossBar(player.getUUID());
        });

        Stimuli.global().listen(PlayerConsumeHungerEvent.EVENT, ((serverPlayer, i, v, v1) -> EventResult.DENY));
        // F 키. 마왕이 다섯 번째 스킬로 쓰므로 오프핸드 교체보다 먼저 가로챕니다.
        Stimuli.global().listen(PlayerSwapWithOffhandEvent.EVENT, player -> {
            if (DemonLordService.handleKeyBinding(gameManager, player, DemonLordBinding.OFFHAND)) {
                return EventResult.DENY;
            }
            return CosmeticItemSupport.isLockedOffhandCosmetic(player.getOffhandItem())
                    ? EventResult.DENY
                    : EventResult.PASS;
        });

        // Q 키. 드롭 패킷을 가로채 여섯 번째 스킬로 씁니다.
        Stimuli.global().listen(PlayerC2SPacketEvent.EVENT, (player, packet) -> {
            if (!(packet instanceof ServerboundPlayerActionPacket action)) {
                return EventResult.PASS;
            }
            ServerboundPlayerActionPacket.Action kind = action.getAction();
            if (kind != ServerboundPlayerActionPacket.Action.DROP_ITEM
                    && kind != ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS) {
                return EventResult.PASS;
            }
            return DemonLordService.handleKeyBinding(gameManager, player, DemonLordBinding.DROP)
                    ? EventResult.DENY
                    : EventResult.PASS;
        });
    }

    private Events() throws IllegalAccessException {
        throw new IllegalAccessException("Utility Class");
    }
}
