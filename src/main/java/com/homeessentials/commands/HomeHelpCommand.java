package com.homeessentials.commands;

import com.homeessentials.HomeEssentials;
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
 * /homehelp - Show help for home commands.
 */
public class HomeHelpCommand extends AbstractPlayerCommand {

    private static final Color GOLD = new Color(255, 170, 0);
    private static final Color WHITE = new Color(255, 255, 255);
    private static final Color GRAY = new Color(170, 170, 170);
    private static final Color GREEN = new Color(85, 255, 85);
    private static final Color AQUA = new Color(85, 255, 255);

    public HomeHelpCommand(HomeEssentials plugin) {
        super("homehelp", "Show help for home commands");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {

        send(playerData, "");
        send(playerData, "---------- HomeEssentials ----------", GOLD);
        send(playerData, "");

        // Commands
        send(playerData, "COMMANDS:", GREEN);
        send(playerData, "  /sethome        Save location as 'home'", WHITE);
        send(playerData, "  /sethome base   Save location as 'base'", WHITE);
        send(playerData, "  /home           Teleport to 'home'", WHITE);
        send(playerData, "  /home base      Teleport to 'base'", WHITE);
        send(playerData, "  /homes          List all your homes", WHITE);
        send(playerData, "  /delhome base   Delete home 'base'", WHITE);
        send(playerData, "");

        // Info
        send(playerData, "INFO:", GREEN);
        send(playerData, "  - Teleports have 3 second warmup", GRAY);
        send(playerData, "  - Moving cancels teleport", GRAY);
        send(playerData, "  - Names: letters, numbers, _ only", GRAY);
        send(playerData, "");

        // Permissions
        send(playerData, "PERMISSIONS (for admins):", GREEN);
        send(playerData, "  homes.use            Basic access", AQUA);
        send(playerData, "  homes.limit.1        1 home allowed", AQUA);
        send(playerData, "  homes.limit.3        3 homes allowed", AQUA);
        send(playerData, "  homes.limit.5        5 homes allowed", AQUA);
        send(playerData, "  homes.limit.unlimited   No limit", AQUA);
        send(playerData, "  homes.bypass.warmup  Skip warmup", AQUA);
        send(playerData, "");

        // Setup
        send(playerData, "SETUP (run in console):", GREEN);
        send(playerData, "  perm group add default homes.use", GRAY);
        send(playerData, "  perm group add default homes.limit.1", GRAY);
        send(playerData, "");
        send(playerData, "------------------------------------", GOLD);
    }

    private void send(PlayerRef player, String text) {
        player.sendMessage(Message.raw(text));
    }

    private void send(PlayerRef player, String text, Color color) {
        player.sendMessage(Message.raw(text).color(color));
    }
}
