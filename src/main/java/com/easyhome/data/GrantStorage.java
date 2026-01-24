package com.easyhome.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages persistent storage of player grants using JSON files.
 * Grants are stored in mods/cryptobench_EasyHome/grants/<uuid>.json
 */
public class GrantStorage {
    private final Path grantsDirectory;
    private final Gson gson;
    private final Map<UUID, PlayerGrants> cache;

    public GrantStorage(Path dataDirectory) {
        this.grantsDirectory = dataDirectory.resolve("grants");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.cache = new ConcurrentHashMap<>();

        try {
            Files.createDirectories(grantsDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get grants for a player, loading from disk if not cached.
     */
    public PlayerGrants getGrants(UUID playerId) {
        return cache.computeIfAbsent(playerId, this::loadGrants);
    }

    /**
     * Load grants from disk for a player.
     */
    private PlayerGrants loadGrants(UUID playerId) {
        Path file = grantsDirectory.resolve(playerId.toString() + ".json");

        if (Files.exists(file)) {
            try {
                String json = Files.readString(file);
                PlayerGrants grants = gson.fromJson(json, PlayerGrants.class);
                if (grants != null) {
                    return grants;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new PlayerGrants();
    }

    /**
     * Save grants for a specific player.
     */
    public void saveGrants(UUID playerId) {
        PlayerGrants grants = cache.get(playerId);
        if (grants == null) return;

        Path file = grantsDirectory.resolve(playerId.toString() + ".json");

        try {
            Files.writeString(file, gson.toJson(grants));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save all cached grants to disk.
     */
    public void saveAll() {
        for (UUID playerId : cache.keySet()) {
            saveGrants(playerId);
        }
    }

    /**
     * Grant bonus homes to a player.
     * @param playerId The player's UUID
     * @param amount Number of bonus homes to add
     */
    public void grantHomes(UUID playerId, int amount) {
        PlayerGrants grants = getGrants(playerId);
        grants.addBonusHomes(amount);
        saveGrants(playerId);
    }

    /**
     * Revoke bonus homes from a player.
     * @param playerId The player's UUID
     * @param amount Number of bonus homes to remove
     */
    public void revokeHomes(UUID playerId, int amount) {
        PlayerGrants grants = getGrants(playerId);
        grants.removeBonusHomes(amount);
        saveGrants(playerId);
    }

    /**
     * Grant instant teleport to a player.
     * @param playerId The player's UUID
     */
    public void grantInstantTeleport(UUID playerId) {
        PlayerGrants grants = getGrants(playerId);
        grants.setInstantTeleport(true);
        saveGrants(playerId);
    }

    /**
     * Revoke instant teleport from a player.
     * @param playerId The player's UUID
     */
    public void revokeInstantTeleport(UUID playerId) {
        PlayerGrants grants = getGrants(playerId);
        grants.setInstantTeleport(false);
        saveGrants(playerId);
    }

    /**
     * Check if a player has instant teleport granted.
     */
    public boolean hasInstantTeleport(UUID playerId) {
        return getGrants(playerId).hasInstantTeleport();
    }

    /**
     * Get the number of bonus homes for a player.
     */
    public int getBonusHomes(UUID playerId) {
        return getGrants(playerId).getBonusHomes();
    }
}
