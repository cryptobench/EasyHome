package com.homeessentials;

import com.homeessentials.commands.DelHomeCommand;
import com.homeessentials.commands.HomeCommand;
import com.homeessentials.commands.HomeHelpCommand;
import com.homeessentials.commands.HomesCommand;
import com.homeessentials.commands.SetHomeCommand;
import com.homeessentials.data.HomeStorage;
import com.homeessentials.util.WarmupManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

/**
 * HomeEssentials - A user-friendly home management plugin for Hytale.
 */
public class HomeEssentials extends JavaPlugin {

    private HomeStorage storage;
    private WarmupManager warmupManager;

    public HomeEssentials(JavaPluginInit init) {
        super(init);
    }

    @Override
    public void setup() {
        // Initialize storage
        storage = new HomeStorage(getDataDirectory());

        // Initialize warmup manager
        warmupManager = new WarmupManager();

        // Register commands
        getCommandRegistry().registerCommand(new SetHomeCommand(this));
        getCommandRegistry().registerCommand(new HomeCommand(this));
        getCommandRegistry().registerCommand(new DelHomeCommand(this));
        getCommandRegistry().registerCommand(new HomesCommand(this));
        getCommandRegistry().registerCommand(new HomeHelpCommand(this));
    }

    @Override
    public void start() {
        // Plugin started
    }

    @Override
    public void shutdown() {
        // Save all data
        if (storage != null) {
            storage.saveAll();
        }

        // Shutdown warmup manager
        if (warmupManager != null) {
            warmupManager.shutdown();
        }
    }

    public HomeStorage getStorage() {
        return storage;
    }

    public WarmupManager getWarmupManager() {
        return warmupManager;
    }

    public int getHomeLimit(Player player) {
        if (player.hasPermission("homes.limit.unlimited")) {
            return Integer.MAX_VALUE;
        }
        if (player.hasPermission("homes.limit.5")) {
            return 5;
        }
        if (player.hasPermission("homes.limit.3")) {
            return 3;
        }
        if (player.hasPermission("homes.limit.1")) {
            return 1;
        }
        return 1;
    }
}
