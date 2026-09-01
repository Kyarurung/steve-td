package kim.biryeong.semiontd.tower.ancientcity;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;

final class AncientCityTerritoryState {
    private final Set<BlockPos> territory = new HashSet<>();
    private final Set<BlockPos> finalDefenseTerritory = new HashSet<>();
    private BlockPos seedOrigin;
    private boolean seeded;
    private boolean finalDefenseSeeded;
    private int activeRound;
    private int waveSpreadRound = -1;
    private int deathSpreadsThisRound;

    Set<BlockPos> territory() {
        return territory;
    }

    Set<BlockPos> finalDefenseTerritory() {
        return finalDefenseTerritory;
    }

    BlockPos seedOrigin() {
        return seedOrigin;
    }

    void seedAt(BlockPos origin) {
        seeded = true;
        seedOrigin = origin;
    }

    boolean seeded() {
        return seeded;
    }

    boolean finalDefenseSeeded() {
        return finalDefenseSeeded;
    }

    void seedFinalDefense() {
        finalDefenseSeeded = true;
    }

    int waveSpreadRound() {
        return waveSpreadRound;
    }

    void recordWaveSpread(int round) {
        beginRound(round);
        waveSpreadRound = round;
    }

    int deathSpreadsThisRound() {
        return deathSpreadsThisRound;
    }

    void recordDeathSpread() {
        deathSpreadsThisRound++;
    }

    void beginRound(int round) {
        if (activeRound == round) {
            return;
        }
        activeRound = round;
        deathSpreadsThisRound = 0;
    }
}
