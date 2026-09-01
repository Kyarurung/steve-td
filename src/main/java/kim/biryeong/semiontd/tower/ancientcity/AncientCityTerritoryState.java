package kim.biryeong.semiontd.tower.ancientcity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import kim.biryeong.semiontd.game.GridPosition;
import net.minecraft.core.BlockPos;

final class AncientCityTerritoryState {
    private final Set<BlockPos> territory = new HashSet<>();
    private final Set<BlockPos> finalDefenseTerritory = new HashSet<>();
    private final Set<BlockPos> territoryView = Collections.unmodifiableSet(territory);
    private final Set<BlockPos> finalDefenseTerritoryView = Collections.unmodifiableSet(finalDefenseTerritory);
    private final Set<Long> occupiedColumns = new HashSet<>();
    private BlockPos seedOrigin;
    private boolean seeded;
    private boolean finalDefenseSeeded;
    private int activeRound;
    private int waveSpreadRound = -1;
    private int deathSpreadsThisRound;

    Set<BlockPos> territory() {
        return territoryView;
    }

    Set<BlockPos> finalDefenseTerritory() {
        return finalDefenseTerritoryView;
    }

    boolean add(BlockPos position, boolean mainTerritory) {
        if (position == null) {
            return false;
        }
        BlockPos immutable = position.immutable();
        Set<BlockPos> positions = mainTerritory ? territory : finalDefenseTerritory;
        if (!positions.add(immutable)) {
            return false;
        }
        occupiedColumns.add(columnKey(immutable.getX(), immutable.getZ()));
        return true;
    }

    boolean containsMain(BlockPos position) {
        return territory.contains(position);
    }

    boolean contains(GridPosition position) {
        return position != null && occupiedColumns.contains(columnKey(position.x(), position.z()));
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

    private static long columnKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }
}
