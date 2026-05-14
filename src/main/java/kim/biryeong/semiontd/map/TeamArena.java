package kim.biryeong.semiontd.map;

import kim.biryeong.semiontd.game.TeamId;
import net.minecraft.server.level.ServerLevel;
import xyz.nucleoid.map_templates.BlockBounds;

public final class TeamArena {
    private final TeamId teamId;
    private final Runnable unloadAction;
    private final ServerLevel world;
    private final ArenaLayout layout;
    private final BlockBounds preloadBounds;

    public TeamArena(TeamId teamId, Runnable unloadAction, ServerLevel world, ArenaLayout layout) {
        this(teamId, unloadAction, world, layout, null);
    }

    public TeamArena(
            TeamId teamId,
            Runnable unloadAction,
            ServerLevel world,
            ArenaLayout layout,
            BlockBounds preloadBounds
    ) {
        this.teamId = teamId;
        this.unloadAction = unloadAction;
        this.world = world;
        this.layout = layout;
        this.preloadBounds = preloadBounds;
    }

    public TeamId teamId() {
        return teamId;
    }

    public ServerLevel world() {
        return world;
    }

    public ArenaLayout layout() {
        return layout;
    }

    public void unload() {
        unloadAction.run();
    }

    public void preloadForTeleport() {
        if (preloadBounds != null) {
            RuntimeWorldWarmup.loadChunks(world, preloadBounds);
        }
    }
}
