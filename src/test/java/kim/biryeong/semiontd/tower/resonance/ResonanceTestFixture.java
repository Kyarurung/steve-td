package kim.biryeong.semiontd.tower.resonance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;

final class ResonanceTestFixture {
    static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private ResonanceTestFixture() {
    }

    static ResonanceTower tower(TowerType type, int x, int z) {
        GridPosition position = new GridPosition(x, 64, z);
        return new ResonanceTower(type, OWNER, TeamId.RED, 1, position, position);
    }

    static List<ResonanceTower> differentAspects() {
        return List.of(
                tower(ResonanceTowers.WAVE_CRYSTAL, 1, 0),
                tower(ResonanceTowers.FROST_CRYSTAL, -1, 0),
                tower(ResonanceTowers.AMPLIFY_CRYSTAL, 0, 1),
                tower(ResonanceTowers.WAVE_PRISM, 1, -1),
                tower(ResonanceTowers.FROST_PRISM, -1, -1),
                tower(ResonanceTowers.AMPLIFY_PRISM, 0, -1)
        );
    }

    static List<Tower> with(ResonanceTower tower, List<ResonanceTower> links) {
        ArrayList<Tower> towers = new ArrayList<>();
        towers.add(tower);
        towers.addAll(links);
        return towers;
    }
}
