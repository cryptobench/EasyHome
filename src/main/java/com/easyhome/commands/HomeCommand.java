package com.easyhome.commands;

import com.easyhome.EasyHome;
import com.easyhome.config.HomeConfig;
import com.easyhome.data.Home;
import com.easyhome.data.PlayerHomes;
import com.easyhome.util.Messages;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * /home [name] - Teleport to a saved home.
 */
public class HomeCommand extends AbstractPlayerCommand {
    private static final String DEFAULT_HOME_NAME = "home";

    private final EasyHome plugin;

    public HomeCommand(EasyHome plugin) {
        super("home", "Teleport to one of your saved homes");
        this.plugin = plugin;
        setAllowsExtraArguments(true);  // Allow positional arguments
        requirePermission("homes.use");
    }

    private String parseHomeName(CommandContext ctx) {
        String input = ctx.getInputString().trim();
        if (input.isEmpty()) {
            return null;
        }
        String[] args = input.split("\\s+");
        if (args.length > 1) {
            return args[1];
        }
        return null;
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {

        // Update player cache for offline lookups
        plugin.getPlayerCache().updatePlayer(playerData.getUuid(), playerData.getUsername());
        plugin.getStorage().updateUsername(playerData.getUuid(), playerData.getUsername());

        String homeName = parseHomeName(ctx);
        if (homeName == null || homeName.isEmpty()) {
            homeName = DEFAULT_HOME_NAME;
        }

        PlayerHomes homes = plugin.getStorage().getHomes(playerData.getUuid());

        Home home = homes.getHome(homeName);
        if (home == null) {
            playerData.sendMessage(Messages.homeNotFound(homeName));

            if (homes.getHomeCount() > 0) {
                playerData.sendMessage(Messages.useHomesHint());
            }
            return;
        }

        // Get config values
        HomeConfig config = plugin.getConfig();
        int warmupSeconds = config.getWarmupSeconds();
        double movementThreshold = config.getMovementThreshold();

        // Check for warmup bypass - either via permission or via grant
        Player player = store.getComponent(playerRef, Player.getComponentType());
        boolean hasPermissionBypass = player.hasPermission("homes.bypass.warmup");
        boolean hasGrantBypass = plugin.getGrantStorage().hasInstantTeleport(playerData.getUuid());
        boolean bypassWarmup = hasPermissionBypass || hasGrantBypass;

        plugin.getWarmupManager().startWarmup(
                playerData, playerRef, store, world, home,
                warmupSeconds, movementThreshold, bypassWarmup
        );
    }
}
