package kim.biryeong.semiontd.tower.ocean;

import java.util.Set;
import java.util.UUID;

public record OceanResourceSnapshot(
        double water,
        boolean waveActive,
        int dehydrationTicks,
        int transferCooldownTicks,
        Set<UUID> supplyTargetIds
) {
    public OceanResourceSnapshot {
        water = Double.isFinite(water) ? Math.max(0.0, water) : 0.0;
        dehydrationTicks = Math.max(0, dehydrationTicks);
        transferCooldownTicks = Math.max(0, transferCooldownTicks);
        supplyTargetIds = supplyTargetIds == null ? Set.of() : Set.copyOf(supplyTargetIds);
    }
}
