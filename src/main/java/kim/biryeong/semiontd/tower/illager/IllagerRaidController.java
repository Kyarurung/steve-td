package kim.biryeong.semiontd.tower.illager;

import java.util.Map;
import java.util.UUID;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.SemionPlayer;
import kim.biryeong.semiontd.job.IllagerTowerJob;
import kim.biryeong.semiontd.tower.EntityBackedTower;
import kim.biryeong.semiontd.tower.Tower;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class IllagerRaidController {
    private static final IllagerRaidRules RULES = new IllagerRaidRules(IllagerConfig.RUNTIME);

    private IllagerRaidController() {
    }

    public static void onMatchStarted(UUID playerId) {
        IllagerRaidStates.clear(playerId);
    }

    public static void onRoundStarted(SemionGame game, SemionPlayer player) {
        if (game == null || player == null || !isIllagerBuilder(player)) {
            return;
        }
        int towerCount = game.playerLane(player.uuid())
                .map(IllagerRaidController::countAliveIllagerTowers)
                .orElse(0);
        IllagerRaidStates.state(player.uuid()).resetForRound(towerCount);
    }

    public static void onRoundEnded(UUID playerId) {
        IllagerRaidStates.clear(playerId);
    }

    public static void onEliminated(UUID playerId) {
        IllagerRaidStates.clear(playerId);
    }

    public static void onMatchClosed(UUID playerId) {
        IllagerRaidStates.clear(playerId);
    }

    public static boolean active(UUID playerId) {
        return IllagerRaidStates.get(playerId).map(IllagerRaidState::active).orElse(false);
    }

    public static double attackSpeedBonus(UUID playerId) {
        return IllagerRaidStates.get(playerId)
                .filter(IllagerRaidState::active)
                .map(state -> RULES.attackSpeedBonus(state.roundStartTowerCount()))
                .orElse(0.0);
    }

    public static double damageBonus(UUID playerId) {
        return IllagerRaidStates.get(playerId)
                .filter(IllagerRaidState::active)
                .map(state -> RULES.damageBonus(state.roundStartTowerCount()))
                .orElse(0.0);
    }

    public static int timedEffectTicks() {
        return RULES.timedEffectTicks();
    }

    public static int gaugeMax() {
        return RULES.gaugeMax();
    }

    public static void onMonsterKilled(Map<UUID, SemionPlayer> players, Monster monster) {
        if (players == null || monster == null || monster.lastHitPlayerId().isEmpty()) {
            return;
        }
        SemionPlayer player = players.get(monster.lastHitPlayerId().get());
        if (player == null || !isIllagerBuilder(player)) {
            return;
        }
        boolean marked = IllagerMarkDomain.activeMark(monster, player.uuid()).isPresent();
        addGauge(player.uuid(), RULES.killGauge(monster.ownerPlayer().isPresent(), marked));
    }

    public static void onTowerDeath(PlayerLane lane, Tower destroyedTower) {
        if (lane == null || destroyedTower == null || !IllagerTowers.isIllagerTower(destroyedTower.type())) {
            return;
        }
        IllagerRaidStates.get(destroyedTower.ownerPlayer()).ifPresent(state ->
                addGauge(destroyedTower.ownerPlayer(), RULES.towerDeathGauge())
        );
    }

    public static int playPendingActivationEffects(MinecraftServer server, PlayerLane lane) {
        if (server == null || lane == null) {
            return 0;
        }
        IllagerRaidState state = IllagerRaidStates.get(lane.ownerPlayer()).orElse(null);
        if (state == null || !state.consumePendingActivationEffects()) {
            return 0;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(lane.ownerPlayer());
        if (player != null) {
            player.playNotifySound(SoundEvents.APPLY_EFFECT_RAID_OMEN, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
        int affectedTowers = 0;
        for (Tower tower : lane.towers()) {
            if (!IllagerTowers.isIllagerTower(tower.type()) || tower.health() <= 0.0
                    || !(tower instanceof EntityBackedTower entityBackedTower) || entityBackedTower.entityId().isEmpty()) {
                continue;
            }
            if (lane.arenaWorld().getEntity(entityBackedTower.entityId().getAsInt()) instanceof SemionTowerEntity towerEntity
                    && towerEntity.isAlive() && !towerEntity.isRemoved()) {
                IllagerRaidVfx.showActivation(towerEntity);
                affectedTowers++;
            }
        }
        return affectedTowers;
    }

    private static void addGauge(UUID playerId, int amount) {
        IllagerRaidStates.state(playerId).addGauge(amount, RULES.gaugeMax());
    }

    private static int countAliveIllagerTowers(PlayerLane lane) {
        int count = 0;
        for (Tower tower : lane.towers()) {
            if (IllagerTowers.isIllagerTower(tower.type()) && tower.health() > 0) {
                count++;
            }
        }
        return count;
    }

    private static boolean isIllagerBuilder(SemionPlayer player) {
        return player.job().filter(job -> job instanceof IllagerTowerJob).isPresent();
    }
}
