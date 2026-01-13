package com.homeessentials.commands;

import com.homeessentials.HomeEssentials;
import com.homeessentials.data.Home;
import com.homeessentials.data.PlayerHomes;
import com.homeessentials.util.Messages;
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

    private final HomeEssentials plugin;

    public HomesCommand(HomeEssentials plugin) {
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

        Player player = store.getComponent(playerRef, Player.getComponentType());

        PlayerHomes playerHomes = plugin.getStorage().getHomes(playerData.getUuid());
        Collection<Home> homes = playerHomes.getAllHomes();

        if (homes.isEmpty()) {
            playerData.sendMessage(Messages.noHomes());
            return;
        }

        int limit = plugin.getHomeLimit(player);

        playerData.sendMessage(Messages.homesList(homes.size(), limit));

        for (Home home : homes) {
            playerData.sendMessage(Messages.homeEntry(home.getName(), home.getFormattedLocation()));
        }
    }
}
