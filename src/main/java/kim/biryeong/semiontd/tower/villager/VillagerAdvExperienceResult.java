package kim.biryeong.semiontd.tower.villager;

import java.util.UUID;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.Tower;

record VillagerAdvExperienceResult(
        UUID ownerPlayer,
        PlayerLane lane,
        Tower tower,
        double nextExperience
) {
}
