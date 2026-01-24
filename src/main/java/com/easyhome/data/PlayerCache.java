package com.easyhome.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Caches player username to UUID mappings for offline player lookups.
 * Updated whenever a player uses any home command.
 */
public class PlayerCache {
    private final Path cacheFile;
    private final Gson gson;
    private final Map<String, UUID> usernameToUuid;
    private final Map<UUID, String> uuidToUsername;

    public PlayerCache(Path dataDirectory) {
        this.cacheFile = dataDirectory.resolve("player_cache.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.usernameToUuid = new ConcurrentHashMap<>();
        this.uuidToUsername = new ConcurrentHashMap<>();
        load();
    }

    /**
     * Sync cache with existing data from HomeStorage.
     * Call this on startup after HomeStorage is initialized.
     * This ensures backwards compatibility by populating the cache
     * from usernames stored in homes files.
     */
    public void syncFromHomeStorage(HomeStorage homeStorage) {
        Map<UUID, String> existingMappings = homeStorage.scanForUsernames();
        int synced = 0;

        for (Map.Entry<UUID, String> entry : existingMappings.entrySet()) {
            UUID uuid = entry.getKey();
            String username = entry.getValue();

            // Only add if not already in cache (don't overwrite newer data)
            if (!uuidToUsername.containsKey(uuid)) {
                usernameToUuid.put(username.toLowerCase(), uuid);
                uuidToUsername.put(uuid, username);
                synced++;
            }
        }

        if (synced > 0) {
            save();
        }
    }

    /**
     * Sync cache from universe path (universe/players/).
     */
    public void syncFromUniversePath(Path universePath) {
        if (universePath == null) {
            return;
        }
        syncFromServerPlayerData(universePath.resolve("players"));
    }

    /**
     * Sync cache from a players directory containing player JSON files.
     */
    public void syncFromServerPlayerData(Path playersDir) {
        if (playersDir == null) {
            return;
        }

        try {

            if (!Files.exists(playersDir) || !Files.isDirectory(playersDir)) {
                return;
            }

            int synced = 0;

            // Get list of files first, then process
            java.io.File[] jsonFiles = playersDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
            if (jsonFiles == null) {
                return;
            }

            for (java.io.File file : jsonFiles) {
                try {
                    String filename = file.getName();
                    String uuidStr = filename.replace(".json", "");
                    UUID uuid = UUID.fromString(uuidStr);

                    // Skip if already in cache
                    if (uuidToUsername.containsKey(uuid)) {
                        continue;
                    }

                    String json = Files.readString(file.toPath());
                    JsonObject root = JsonParser.parseString(json).getAsJsonObject();

                    // Try to get username from Nameplate.Text
                    String username = null;
                    if (root.has("Components")) {
                        JsonObject components = root.getAsJsonObject("Components");
                        if (components.has("Nameplate")) {
                            JsonObject nameplate = components.getAsJsonObject("Nameplate");
                            if (nameplate.has("Text")) {
                                username = nameplate.get("Text").getAsString();
                            }
                        }
                    }

                    if (username != null && !username.isEmpty()) {
                        usernameToUuid.put(username.toLowerCase(), uuid);
                        uuidToUsername.put(uuid, username);
                        synced++;
                    }
                } catch (Exception ignored) {
                    // Skip invalid files
                }
            }

            if (synced > 0) {
                save();
            }
        } catch (Exception e) {
            // Don't let this crash the plugin - just log and continue
            e.printStackTrace();
        }
    }

    /**
     * Load the cache from disk.
     */
    private void load() {
        if (Files.exists(cacheFile)) {
            try {
                String json = Files.readString(cacheFile);
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> data = gson.fromJson(json, type);
                if (data != null) {
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        try {
                            UUID uuid = UUID.fromString(entry.getValue());
                            String username = entry.getKey();
                            usernameToUuid.put(username.toLowerCase(), uuid);
                            uuidToUsername.put(uuid, username);
                        } catch (IllegalArgumentException ignored) {
                            // Skip invalid UUIDs
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save the cache to disk.
     */
    public void save() {
        try {
            // Build map with original case usernames
            Map<String, String> data = new ConcurrentHashMap<>();
            for (Map.Entry<UUID, String> entry : uuidToUsername.entrySet()) {
                data.put(entry.getValue(), entry.getKey().toString());
            }
            Files.writeString(cacheFile, gson.toJson(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the cache with a player's current username.
     * Call this when a player uses any command.
     */
    public void updatePlayer(UUID uuid, String username) {
        if (uuid == null || username == null || username.isEmpty()) {
            return;
        }

        // Check if username changed
        String oldUsername = uuidToUsername.get(uuid);
        if (oldUsername != null && !oldUsername.equalsIgnoreCase(username)) {
            // Remove old username mapping
            usernameToUuid.remove(oldUsername.toLowerCase());
        }

        usernameToUuid.put(username.toLowerCase(), uuid);
        uuidToUsername.put(uuid, username);
        save();
    }

    /**
     * Get UUID for a username (case-insensitive).
     * Returns null if player has never been seen.
     */
    public UUID getUuid(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        return usernameToUuid.get(username.toLowerCase());
    }

    /**
     * Get username for a UUID.
     * Returns null if player has never been seen.
     */
    public String getUsername(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return uuidToUsername.get(uuid);
    }

    /**
     * Check if a player exists in the cache.
     */
    public boolean hasPlayer(String username) {
        return username != null && usernameToUuid.containsKey(username.toLowerCase());
    }
}
