package kim.biryeong.semiontd.tower.warlock;

import java.util.UUID;

record WarlockProgressionSnapshot(
        int totalSacrificeCount,
        int roundSacrificeCount,
        WarlockAwakeningProgress.Snapshot awakening
) {
    WarlockProgressionSnapshot {
        totalSacrificeCount = Math.max(0, totalSacrificeCount);
        roundSacrificeCount = Math.max(0, roundSacrificeCount);
    }

    static WarlockProgressionSnapshot from(WarlockState state, UUID ownerPlayer) {
        return new WarlockProgressionSnapshot(
                state.totalSacrificeCount(),
                state.roundSacrificeCount(),
                WarlockAwakeningProgress.snapshot(ownerPlayer)
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
