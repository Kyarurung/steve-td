package kim.biryeong.semiontd.tower.villager;

import java.util.UUID;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.Tower;

record VillagerAdvExperienceSnapshot(
        UUID ownerPlayer,
        PlayerLane lane,
        Tower tower,
        int tier,
        double currentExperience
) {
}
