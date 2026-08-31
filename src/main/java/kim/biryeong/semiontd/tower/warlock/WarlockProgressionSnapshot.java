package kim.biryeong.semiontd.tower.warlock;

import java.util.UUID;

record WarlockProgressionSnapshot(
        int totalSacrificeCount,
        int roundSacrificeCount,
        WarlockAwakeningStates.Snapshot awakening
) {
    WarlockProgressionSnapshot {
        totalSacrificeCount = Math.max(0, totalSacrificeCount);
        roundSacrificeCount = Math.max(0, roundSacrificeCount);
    }

    static WarlockProgressionSnapshot from(WarlockProgressionState state, UUID ownerPlayer) {
        return new WarlockProgressionSnapshot(
                state.totalSacrificeCount(),
                state.roundSacrificeCount(),
                WarlockAwakeningStates.snapshot(ownerPlayer)
        );
    }

    int lifeStealSacrificeCount(WarlockPath path) {
        return path == WarlockPath.MELEE ? roundSacrificeCount : totalSacrificeCount;
    }

    int defenseSacrificeCount(WarlockPath path) {
        return path == WarlockPath.RANGED ? roundSacrificeCount : totalSacrificeCount;
    }

    int splashSacrificeCount(WarlockPath path) {
        return path == WarlockPath.RANGED ? totalSacrificeCount : roundSacrificeCount;
    }
}
