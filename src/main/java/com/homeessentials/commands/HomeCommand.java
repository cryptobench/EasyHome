package com.homeessentials.commands;

import com.homeessentials.HomeEssentials;
import com.homeessentials.data.Home;
import com.homeessentials.data.PlayerHomes;
import com.homeessentials.util.Messages;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * /home [name] - Teleport to a saved home.
 */
public class HomeCommand extends AbstractPlayerCommand {
    private static final String DEFAULT_HOME_NAME = "home";

    private final HomeEssentials plugin;
    private final OptionalArg<String> nameArg;

    public HomeCommand(HomeEssentials plugin) {
        super("home", "Teleport to one of your saved homes");
        this.plugin = plugin;

        this.nameArg = withOptionalArg("name", "Name of the home to teleport to (default: home)", ArgTypes.STRING);

        requirePermission("homes.use");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {

        String homeName = nameArg.get(ctx);
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

        // Always use warmup - bypass disabled for now
        plugin.getWarmupManager().startWarmup(playerData, playerRef, store, world, home, false);
    }
}
