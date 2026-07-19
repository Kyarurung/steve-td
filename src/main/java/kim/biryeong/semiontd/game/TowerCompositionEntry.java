package kim.biryeong.semiontd.game;

import java.util.Objects;

public record TowerCompositionEntry(String towerTypeId, int tier, int count) {
    public TowerCompositionEntry {
        Objects.requireNonNull(towerTypeId, "towerTypeId");
        if (towerTypeId.isBlank()) {
            throw new IllegalArgumentException("towerTypeId cannot be blank");
        }
        tier = Math.max(0, tier);
        count = Math.max(1, count);
    }
}
