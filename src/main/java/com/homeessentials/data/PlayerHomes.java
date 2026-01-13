package com.homeessentials.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores all homes for a single player.
 */
public class PlayerHomes {
    private final Map<String, Home> homes;

    public PlayerHomes() {
        this.homes = new HashMap<>();
    }

    public Home getHome(String name) {
        return homes.get(name.toLowerCase());
    }

    public void setHome(Home home) {
        homes.put(home.getName().toLowerCase(), home);
    }

    public boolean removeHome(String name) {
        return homes.remove(name.toLowerCase()) != null;
    }

    public boolean hasHome(String name) {
        return homes.containsKey(name.toLowerCase());
    }

    public int getHomeCount() {
        return homes.size();
    }

    public Collection<Home> getAllHomes() {
        return homes.values();
    }

    public Map<String, Home> getHomesMap() {
        return homes;
    }
}
