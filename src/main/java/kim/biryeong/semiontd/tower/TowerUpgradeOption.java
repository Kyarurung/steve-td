package kim.biryeong.semiontd.tower;

public record TowerUpgradeOption(
        String id,
        String displayName,
        TowerType targetType,
        long mineralCost
) {
    public TowerUpgradeOption {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Tower upgrade id cannot be blank.");
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = id;
        }
        if (targetType == null) {
            throw new IllegalArgumentException("Tower upgrade target type cannot be null.");
        }
        if (mineralCost < 0) {
            throw new IllegalArgumentException("Tower upgrade mineral cost cannot be negative.");
        }
    }
}
