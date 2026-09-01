package kim.biryeong.semiontd.tower.villager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.SemionPlayer;
import kim.biryeong.semiontd.job.VillagerAdvTowerJob;
import kim.biryeong.semiontd.tower.Tower;

public final class VillagerAdvProgressionController {
    private static final ExecutorService EXPERIENCE_EXECUTOR = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "SemionTD Villager ADV Experience");
        thread.setDaemon(true);
        return thread;
    });

    private VillagerAdvProgressionController() {
    }

    public static void onMatchStarted(UUID playerId) {
        VillagerAdvStates.clear(playerId);
    }

    public static void onEliminated(UUID playerId) {
        VillagerAdvStates.clear(playerId);
    }

    public static void onMatchClosed(UUID playerId) {
        VillagerAdvStates.clear(playerId);
    }

    public static void onWaveStarted(SemionGame game, int round) {
        if (game == null) {
            return;
        }
        TowerBalanceConfig.VillagerAdvConfig config = VillagerConfig.RUNTIME.advanced();
        List<VillagerAdvExperienceSnapshot> snapshots = new ArrayList<>();
        for (SemionPlayer player : game.players().values()) {
            if (!isAdvPlayer(player)) {
                continue;
            }
            game.playerLane(player.uuid()).ifPresent(lane -> {
                for (Tower tower : lane.towers()) {
                    if (VillagerTowers.isVillagerTower(tower.type())) {
                        snapshots.add(new VillagerAdvExperienceSnapshot(
                                player.uuid(),
                                lane,
                                tower,
                                VillagerAdvTowerRoles.tier(tower),
                                VillagerAdvStates.experience(tower)
                        ));
                        VillagerAdvEffectController.refresh(player, lane, tower);
                    }
                }
            });
        }
        if (!snapshots.isEmpty()) {
            CompletableFuture
                    .supplyAsync(() -> calculateExperienceGains(List.copyOf(snapshots), config), EXPERIENCE_EXECUTOR)
                    .thenAccept(VillagerAdvStates::enqueue);
        }
    }

    public static void applyPending(SemionGame game) {
        if (game == null) {
            return;
        }
        for (var entry : game.players().entrySet()) {
            for (VillagerAdvExperienceResult result : VillagerAdvStates.drain(entry.getKey())) {
                SemionPlayer player = game.players().get(result.ownerPlayer());
                if (!isAdvPlayer(player) || !result.lane().towers().contains(result.tower())) {
                    continue;
                }
                result.tower().setData(VillagerAdvStates.EXPERIENCE, result.nextExperience());
                VillagerAdvEffectController.refresh(player, result.lane(), result.tower());
            }
        }
    }

    public static void onWaveCleared(SemionGame game, int round) {
        if (game == null) {
            return;
        }
        TowerBalanceConfig.VillagerAdvConfig config = VillagerConfig.RUNTIME.advanced();
        for (SemionPlayer player : game.players().values()) {
            if (!isAdvPlayer(player)) {
                continue;
            }
            game.playerLane(player.uuid())
                    .filter(lane -> !lane.leakedThisRound())
                    .ifPresent(lane -> {
                        VillagerAdvStates.addReputation(
                                player.uuid(),
                                Math.max(0, round) * config.resolvedReputationGainRoundMultiplier(),
                                config
                        );
                        lane.towers().forEach(tower -> VillagerAdvEffectController.refresh(player, lane, tower));
                    });
        }
    }

    public static void onLaneLeak(SemionPlayer laneOwner, PlayerLane lane) {
        if (!isAdvPlayer(laneOwner)) {
            return;
        }
        TowerBalanceConfig.VillagerAdvConfig config = VillagerConfig.RUNTIME.advanced();
        VillagerAdvStates.addReputation(laneOwner.uuid(), -config.resolvedReputationLossPerLeak(), config);
        if (lane != null) {
            lane.towers().forEach(tower -> VillagerAdvEffectController.refresh(laneOwner, lane, tower));
        }
    }

    static boolean isAdvPlayer(SemionPlayer player) {
        return player != null && player.job()
                .map(job -> VillagerAdvTowerJob.ID.equals(job.id()))
                .orElse(false);
    }

    static List<VillagerAdvExperienceResult> calculateExperienceGains(
            List<VillagerAdvExperienceSnapshot> snapshots,
            TowerBalanceConfig.VillagerAdvConfig config
    ) {
        return snapshots.stream()
                .map(snapshot -> new VillagerAdvExperienceResult(
                        snapshot.ownerPlayer(),
                        snapshot.lane(),
                        snapshot.tower(),
                        VillagerAdvRules.nextExperience(snapshot.currentExperience(), snapshot.tier(), config)
                ))
                .toList();
    }
}
