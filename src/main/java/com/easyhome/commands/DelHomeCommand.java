package com.easyhome.commands;

import com.easyhome.EasyHome;
import com.easyhome.data.PlayerHomes;
import com.easyhome.util.Messages;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * /delhome <name> - Delete a saved home.
 */
public class DelHomeCommand extends AbstractPlayerCommand {
    private static final Color RED = new Color(255, 85, 85);

    private final EasyHome plugin;

    public DelHomeCommand(EasyHome plugin) {
        super("delhome", "Delete one of your saved homes");
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
            playerData.sendMessage(Message.raw("Usage: /delhome <name>").color(RED));
            return;
        }

        PlayerHomes homes = plugin.getStorage().getHomes(playerData.getUuid());

        if (!homes.removeHome(homeName)) {
            playerData.sendMessage(Messages.homeNotFound(homeName));
            return;
        }

        plugin.getStorage().saveHomes(playerData.getUuid());
        playerData.sendMessage(Messages.homeDeleted(homeName));
    }
}
