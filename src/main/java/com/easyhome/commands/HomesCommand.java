package com.easyhome.commands;

import com.easyhome.EasyHome;
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
import java.util.Collection;

/**
 * /homes - List all your saved homes.
 */
public class HomesCommand extends AbstractPlayerCommand {

    private final EasyHome plugin;

    public HomesCommand(EasyHome plugin) {
        super("homes", "List all your saved homes");
        this.plugin = plugin;
        requirePermission("homes.use");
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

        Player player = store.getComponent(playerRef, Player.getComponentType());

        PlayerHomes playerHomes = plugin.getStorage().getHomes(playerData.getUuid());
        Collection<Home> homes = playerHomes.getAllHomes();

        if (homes.isEmpty()) {
            playerData.sendMessage(Messages.noHomes());
            return;
        }

        int limit = plugin.getHomeLimit(player, playerData.getUuid());

        playerData.sendMessage(Messages.homesList(homes.size(), limit));

        for (Home home : homes) {
            playerData.sendMessage(Messages.homeEntry(home.getName(), home.getFormattedLocation()));
        }
    }
}
