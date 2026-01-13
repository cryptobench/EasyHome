package com.homeessentials.commands;

import com.homeessentials.HomeEssentials;
import com.homeessentials.data.PlayerHomes;
import com.homeessentials.util.Messages;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * /delhome <name> - Delete a saved home.
 */
public class DelHomeCommand extends AbstractPlayerCommand {

    private final HomeEssentials plugin;
    private final RequiredArg<String> nameArg;

    public DelHomeCommand(HomeEssentials plugin) {
        super("delhome", "Delete one of your saved homes");
        this.plugin = plugin;

        this.nameArg = withRequiredArg("name", "Name of the home to delete", ArgTypes.STRING);

        requirePermission("homes.use");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {

        String homeName = nameArg.get(ctx);

        PlayerHomes homes = plugin.getStorage().getHomes(playerData.getUuid());

        if (!homes.removeHome(homeName)) {
            playerData.sendMessage(Messages.homeNotFound(homeName));
            return;
        }

        plugin.getStorage().saveHomes(playerData.getUuid());
        playerData.sendMessage(Messages.homeDeleted(homeName));
    }
}
