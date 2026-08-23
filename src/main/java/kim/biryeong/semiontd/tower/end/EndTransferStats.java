package kim.biryeong.semiontd.tower.end;

public record EndTransferStats(
        int shulkerCount,
        int endCrystalCount,
        int roundCompletedCount,
        double permanentHealthBonus,
        double roundHealthBonus,
        double permanentDamageBonus,
        double roundDamageBonus
) {
    public double totalHealthBonus() {
        return permanentHealthBonus + roundHealthBonus;
    }
    public double totalDamageBonus() {
        return permanentDamageBonus + roundDamageBonus;
    }
}
