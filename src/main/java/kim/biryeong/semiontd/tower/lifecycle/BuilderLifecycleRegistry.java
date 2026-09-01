package kim.biryeong.semiontd.tower.lifecycle;

import java.util.List;
import java.util.Objects;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.SemionPlayer;
import kim.biryeong.semiontd.tower.ancientcity.AncientCityTerritoryController;
import kim.biryeong.semiontd.tower.illager.IllagerRaidController;
import kim.biryeong.semiontd.tower.villager.VillagerAdvProgressionController;
import kim.biryeong.semiontd.tower.warlock.WarlockAwakeningStates;

public final class BuilderLifecycleRegistry {
    private static final BuilderLifecycleRegistry RUNTIME = new BuilderLifecycleRegistry(List.of(
            new WarlockLifecycleListener(),
            new IllagerLifecycleListener(),
            new VillagerAdvLifecycleListener(),
            new AncientCityLifecycleListener()
    ));

    private final List<BuilderLifecycleListener> listeners;

    BuilderLifecycleRegistry(List<BuilderLifecycleListener> listeners) {
        this.listeners = List.copyOf(Objects.requireNonNull(listeners, "listeners"));
    }

    public static BuilderLifecycleRegistry runtime() {
        return RUNTIME;
    }

    public void onMatchStarted(SemionGame game, SemionPlayer player) {
        listeners.forEach(listener -> listener.onMatchStarted(game, player));
    }

    public void onRoundStarted(SemionGame game, SemionPlayer player, int round) {
        listeners.forEach(listener -> listener.onRoundStarted(game, player, round));
    }

    public void onRoundEnded(SemionGame game, SemionPlayer player, int round) {
        listeners.forEach(listener -> listener.onRoundEnded(game, player, round));
    }

    public void onEliminated(SemionGame game, SemionPlayer player) {
        listeners.forEach(listener -> listener.onEliminated(game, player));
    }

    public void onMatchClosed(SemionGame game, SemionPlayer player) {
        listeners.forEach(listener -> listener.onMatchClosed(game, player));
    }

    public void onWaveStarted(SemionGame game, int round) {
        listeners.forEach(listener -> listener.onWaveStarted(game, round));
    }

    public void onWaveCleared(SemionGame game, int round) {
        listeners.forEach(listener -> listener.onWaveCleared(game, round));
    }

    private static final class WarlockLifecycleListener implements BuilderLifecycleListener {
        @Override
        public void onMatchStarted(SemionGame game, SemionPlayer player) {
            WarlockAwakeningStates.clear(player.uuid());
        }

        @Override
        public void onEliminated(SemionGame game, SemionPlayer player) {
            WarlockAwakeningStates.clear(player.uuid());
        }

        @Override
        public void onMatchClosed(SemionGame game, SemionPlayer player) {
            WarlockAwakeningStates.clear(player.uuid());
        }
    }

    private static final class IllagerLifecycleListener implements BuilderLifecycleListener {
        @Override
        public void onMatchStarted(SemionGame game, SemionPlayer player) {
            IllagerRaidController.onMatchStarted(player.uuid());
        }

        @Override
        public void onRoundStarted(SemionGame game, SemionPlayer player, int round) {
            IllagerRaidController.onRoundStarted(game, player);
        }

        @Override
        public void onRoundEnded(SemionGame game, SemionPlayer player, int round) {
            IllagerRaidController.onRoundEnded(player.uuid());
        }

        @Override
        public void onEliminated(SemionGame game, SemionPlayer player) {
            IllagerRaidController.onEliminated(player.uuid());
        }

        @Override
        public void onMatchClosed(SemionGame game, SemionPlayer player) {
            IllagerRaidController.onMatchClosed(player.uuid());
        }
    }

    private static final class VillagerAdvLifecycleListener implements BuilderLifecycleListener {
        @Override
        public void onMatchStarted(SemionGame game, SemionPlayer player) {
            VillagerAdvProgressionController.onMatchStarted(player.uuid());
        }

        @Override
        public void onEliminated(SemionGame game, SemionPlayer player) {
            VillagerAdvProgressionController.onEliminated(player.uuid());
        }

        @Override
        public void onMatchClosed(SemionGame game, SemionPlayer player) {
            VillagerAdvProgressionController.onMatchClosed(player.uuid());
        }

        @Override
        public void onWaveStarted(SemionGame game, int round) {
            VillagerAdvProgressionController.onWaveStarted(game, round);
        }

        @Override
        public void onWaveCleared(SemionGame game, int round) {
            VillagerAdvProgressionController.onWaveCleared(game, round);
        }
    }

    private static final class AncientCityLifecycleListener implements BuilderLifecycleListener {
        @Override
        public void onMatchStarted(SemionGame game, SemionPlayer player) {
            AncientCityTerritoryController.onMatchStarted(player.uuid());
        }

        @Override
        public void onRoundStarted(SemionGame game, SemionPlayer player, int round) {
            AncientCityTerritoryController.onRoundStarted(player, round);
        }

        @Override
        public void onEliminated(SemionGame game, SemionPlayer player) {
            AncientCityTerritoryController.onEliminated(player.uuid());
        }

        @Override
        public void onMatchClosed(SemionGame game, SemionPlayer player) {
            AncientCityTerritoryController.onMatchClosed(player.uuid());
        }
    }
}
