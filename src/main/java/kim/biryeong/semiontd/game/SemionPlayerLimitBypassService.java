package kim.biryeong.semiontd.game;

import com.mojang.authlib.GameProfile;

public final class SemionPlayerLimitBypassService {
    private static volatile SemionGameManager gameManager;

    private SemionPlayerLimitBypassService() {
    }

    public static void configure(SemionGameManager manager) {
        gameManager = manager;
    }

    public static boolean canBypassPlayerLimit(GameProfile profile) {
        SemionGameManager manager = gameManager;
        return manager != null
                && profile != null
                && profile.getId() != null
                && manager.canBypassPlayerLimit(profile.getId());
    }
}
