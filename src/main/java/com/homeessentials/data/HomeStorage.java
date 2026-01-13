package com.homeessentials.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages persistent storage of player homes using JSON files.
 */
public class HomeStorage {
    private final Path homesDirectory;
    private final Gson gson;
    private final Map<UUID, PlayerHomes> cache;

    public HomeStorage(Path dataDirectory) {
        this.homesDirectory = dataDirectory.resolve("homes");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.cache = new ConcurrentHashMap<>();

        try {
            Files.createDirectories(homesDirectory);
        } catch (IOException e) {
            e.printStackTrace();
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
                if (data != null && data.homes != null) {
                    for (Map.Entry<String, HomeJson> entry : data.homes.entrySet()) {
                        HomeJson h = entry.getValue();
                        Home home = new Home(entry.getKey(), h.world, h.x, h.y, h.z, h.yaw, h.pitch);
                        homes.setHome(home);
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

    private static class HomeData {
        Map<String, HomeJson> homes;
    }

    private static class HomeJson {
        String world;
        double x, y, z;
        float yaw, pitch;
    }
}
