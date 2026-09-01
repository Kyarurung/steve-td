package kim.biryeong.semiontd.tower.lifecycle;

import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.SemionPlayer;

public interface BuilderLifecycleListener {
    default void onMatchStarted(SemionGame game, SemionPlayer player) {
    }

    default void onRoundStarted(SemionGame game, SemionPlayer player, int round) {
    }

    default void onRoundEnded(SemionGame game, SemionPlayer player, int round) {
    }

    default void onEliminated(SemionGame game, SemionPlayer player) {
    }

    default void onMatchClosed(SemionGame game, SemionPlayer player) {
    }

    default void onWaveStarted(SemionGame game, int round) {
    }

    default void onWaveCleared(SemionGame game, int round) {
    }
}
