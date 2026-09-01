package kim.biryeong.semiontd.tower.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BuilderLifecycleRegistryTest {
    @Test
    void dispatchesEveryLifecycleEventInRegistrationOrder() {
        List<String> events = new ArrayList<>();
        BuilderLifecycleRegistry registry = new BuilderLifecycleRegistry(List.of(
                listener("first", events),
                listener("second", events)
        ));

        registry.onMatchStarted(null, null);
        registry.onRoundStarted(null, null, 3);
        registry.onRoundEnded(null, null, 3);
        registry.onEliminated(null, null);
        registry.onMatchClosed(null, null);
        registry.onWaveStarted(null, 3);
        registry.onWaveCleared(null, 3);

        assertEquals(List.of(
                "first:matchStarted", "second:matchStarted",
                "first:roundStarted:3", "second:roundStarted:3",
                "first:roundEnded:3", "second:roundEnded:3",
                "first:eliminated", "second:eliminated",
                "first:matchClosed", "second:matchClosed",
                "first:waveStarted:3", "second:waveStarted:3",
                "first:waveCleared:3", "second:waveCleared:3"
        ), events);
    }

    private static BuilderLifecycleListener listener(String name, List<String> events) {
        return new BuilderLifecycleListener() {
            @Override
            public void onMatchStarted(kim.biryeong.semiontd.game.SemionGame game,
                                       kim.biryeong.semiontd.game.SemionPlayer player) {
                events.add(name + ":matchStarted");
            }

            @Override
            public void onRoundStarted(kim.biryeong.semiontd.game.SemionGame game,
                                       kim.biryeong.semiontd.game.SemionPlayer player, int round) {
                events.add(name + ":roundStarted:" + round);
            }

            @Override
            public void onRoundEnded(kim.biryeong.semiontd.game.SemionGame game,
                                     kim.biryeong.semiontd.game.SemionPlayer player, int round) {
                events.add(name + ":roundEnded:" + round);
            }

            @Override
            public void onEliminated(kim.biryeong.semiontd.game.SemionGame game,
                                     kim.biryeong.semiontd.game.SemionPlayer player) {
                events.add(name + ":eliminated");
            }

            @Override
            public void onMatchClosed(kim.biryeong.semiontd.game.SemionGame game,
                                      kim.biryeong.semiontd.game.SemionPlayer player) {
                events.add(name + ":matchClosed");
            }

            @Override
            public void onWaveStarted(kim.biryeong.semiontd.game.SemionGame game, int round) {
                events.add(name + ":waveStarted:" + round);
            }

            @Override
            public void onWaveCleared(kim.biryeong.semiontd.game.SemionGame game, int round) {
                events.add(name + ":waveCleared:" + round);
            }
        };
    }
}
