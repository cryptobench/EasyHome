package com.easyhome.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Manages persistent storage of player homes using JSON files.
 */
public class HomeStorage {
    private final Path homesDirectory;
    private final Gson gson;
    private final Map<UUID, PlayerHomes> cache;
    private final Map<UUID, String> usernameCache;  // Tracks usernames for each UUID

    public HomeStorage(Path dataDirectory) {
        this.homesDirectory = dataDirectory.resolve("homes");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.cache = new ConcurrentHashMap<>();
        this.usernameCache = new ConcurrentHashMap<>();

        try {
            Files.createDirectories(homesDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the cached username for a player.
     * Called when a player uses any home command.
     */
    public void updateUsername(UUID playerId, String username) {
        if (playerId != null && username != null && !username.isEmpty()) {
            usernameCache.put(playerId, username);
        }
    }

    public PlayerHomes getHomes(UUID playerId) {
        return cache.computeIfAbsent(playerId, this::loadHomes);
    }

    private PlayerHomes loadHomes(UUID playerId) {
        Path file = homesDirectory.resolve(playerId.toString() + ".json");

        if (Files.exists(file)) {
            try {
                String json = Files.readString(file);
                HomeData data = gson.fromJson(json, HomeData.class);

                PlayerHomes homes = new PlayerHomes();
                if (data != null) {
                    // Load username if present (for backwards compatibility with cache)
                    if (data.username != null && !data.username.isEmpty()) {
                        usernameCache.put(playerId, data.username);
                    }

                    if (data.homes != null) {
                        for (Map.Entry<String, HomeJson> entry : data.homes.entrySet()) {
                            HomeJson h = entry.getValue();
                            Home home = new Home(entry.getKey(), h.world, h.x, h.y, h.z, h.yaw, h.pitch);
                            homes.setHome(home);
                        }
                    }
                }
                return homes;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new PlayerHomes();
    }

    public void saveHomes(UUID playerId) {
        PlayerHomes homes = cache.get(playerId);
        if (homes == null) return;

        Path file = homesDirectory.resolve(playerId.toString() + ".json");

        HomeData data = new HomeData();
        data.username = usernameCache.get(playerId);  // Include username for offline lookups
        data.homes = new HashMap<>();

        for (Home home : homes.getAllHomes()) {
            HomeJson h = new HomeJson();
            h.world = home.getWorld();
            h.x = home.getX();
            h.y = home.getY();
            h.z = home.getZ();
            h.yaw = home.getYaw();
            h.pitch = home.getPitch();
            data.homes.put(home.getName(), h);
        }

        try {
            Files.writeString(file, gson.toJson(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAll() {
        for (UUID playerId : cache.keySet()) {
            saveHomes(playerId);
        }
    }

    /**
     * Scan all homes files and return username mappings.
     * Used to populate PlayerCache on startup for backwards compatibility.
     */
    public Map<UUID, String> scanForUsernames() {
        Map<UUID, String> mappings = new HashMap<>();

        try (Stream<Path> files = Files.list(homesDirectory)) {
            files.filter(p -> p.toString().endsWith(".json"))
                 .forEach(file -> {
                     try {
                         String filename = file.getFileName().toString();
                         String uuidStr = filename.replace(".json", "");
                         UUID uuid = UUID.fromString(uuidStr);

                         String json = Files.readString(file);
                         HomeData data = gson.fromJson(json, HomeData.class);

                         if (data != null && data.username != null && !data.username.isEmpty()) {
                             mappings.put(uuid, data.username);
                         }
                     } catch (Exception ignored) {
                         // Skip invalid files
                     }
                 });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mappings;
    }

    private static class HomeData {
        String username;  // Player's username for offline lookups
        Map<String, HomeJson> homes;
    }

    private static class HomeJson {
        String world;
        double x, y, z;
        float yaw, pitch;
    }
}
