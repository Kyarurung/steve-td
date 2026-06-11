package kim.biryeong.semiontd.trait;

import java.util.Objects;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.SemionPlayer;

public record TraitContext(SemionGame game, SemionPlayer player) {
    public TraitContext {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(player, "player");
    }
}
