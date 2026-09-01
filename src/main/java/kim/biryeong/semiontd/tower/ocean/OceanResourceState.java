package kim.biryeong.semiontd.tower.ocean;

import java.util.Set;
import java.util.UUID;

public final class OceanResourceState {
    private static final double EPSILON = 1.0E-9;
    private double water;
    private boolean waveActive;
    private int dehydrationTicks;
    private int transferCooldownTicks;
    private Set<UUID> supplyTargetIds = Set.of();

    public OceanResourceState(double initialWater) {
        water = Double.isFinite(initialWater) ? Math.max(0.0, initialWater) : 0.0;
    }

    public double water() {
        return water;
    }

    public boolean waveActive() {
        return waveActive;
    }

    public int dehydrationTicks() {
        return dehydrationTicks;
    }

    public int transferCooldownTicks() {
        return transferCooldownTicks;
    }

    public Set<UUID> supplyTargetIds() {
        return supplyTargetIds;
    }

    public void addWater(double amount) {
        if (Double.isFinite(amount) && amount > 0.0) {
            water += amount;
        }
    }

    public boolean spendWater(double amount) {
        if (!Double.isFinite(amount) || amount <= 0.0) {
            return true;
        }
        if (water + EPSILON < amount) {
            return false;
        }
        water = Math.max(0.0, water - amount);
        return true;
    }

    public void drainWater(double amount) {
        if (Double.isFinite(amount) && amount > 0.0) {
            water = Math.max(0.0, water - amount);
        }
    }

    public void startWave() {
        waveActive = true;
    }

    public void resetRound() {
        waveActive = false;
        dehydrationTicks = 0;
        transferCooldownTicks = 0;
    }

    public boolean tickTransferCooldown() {
        if (transferCooldownTicks <= 0) {
            return false;
        }
        transferCooldownTicks--;
        return true;
    }

    public void startTransferCooldown(int ticks) {
        transferCooldownTicks = Math.max(1, ticks);
    }

    public boolean tickDehydration(int intervalTicks) {
        dehydrationTicks++;
        if (dehydrationTicks < Math.max(1, intervalTicks)) {
            return false;
        }
        dehydrationTicks = 0;
        return true;
    }

    public void clearDehydration() {
        dehydrationTicks = 0;
    }

    public void captureSupplyTargets(Set<UUID> ids) {
        supplyTargetIds = ids == null ? Set.of() : Set.copyOf(ids);
    }

    public OceanResourceSnapshot snapshot() {
        return new OceanResourceSnapshot(
                water, waveActive, dehydrationTicks, transferCooldownTicks, supplyTargetIds
        );
    }

    public void restore(OceanResourceSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        water = snapshot.water();
        waveActive = snapshot.waveActive();
        dehydrationTicks = snapshot.dehydrationTicks();
        transferCooldownTicks = snapshot.transferCooldownTicks();
        supplyTargetIds = snapshot.supplyTargetIds();
    }
}
