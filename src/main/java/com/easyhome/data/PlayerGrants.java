package com.easyhome.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores grant data for a single player.
 * Grants are additive bonuses that stack with permissions.
 */
public class PlayerGrants {
    private int bonusHomes;
    private boolean instantTeleport;
    private List<GrantHistoryEntry> grantHistory;

    public PlayerGrants() {
        this.bonusHomes = 0;
        this.instantTeleport = false;
        this.grantHistory = new ArrayList<>();
    }

    public int getBonusHomes() {
        return bonusHomes;
    }

    public void addBonusHomes(int amount) {
        this.bonusHomes = Math.max(0, this.bonusHomes + amount);
        addHistoryEntry("homes", amount, true);
    }

    public void removeBonusHomes(int amount) {
        this.bonusHomes = Math.max(0, this.bonusHomes - amount);
        addHistoryEntry("homes", -amount, true);
    }

    public boolean hasInstantTeleport() {
        return instantTeleport;
    }

    public void setInstantTeleport(boolean instant) {
        this.instantTeleport = instant;
        addHistoryEntry("instanttp", instant ? 1 : 0, instant);
    }

    public List<GrantHistoryEntry> getGrantHistory() {
        if (grantHistory == null) {
            grantHistory = new ArrayList<>();
        }
        return grantHistory;
    }

    private void addHistoryEntry(String type, int amount, boolean granted) {
        if (grantHistory == null) {
            grantHistory = new ArrayList<>();
        }
        grantHistory.add(new GrantHistoryEntry(type, amount, granted, System.currentTimeMillis()));
    }

    /**
     * Represents a single grant/revoke action in history.
     */
    public static class GrantHistoryEntry {
        private String type;
        private int amount;
        private boolean granted;
        private long timestamp;

        public GrantHistoryEntry() {
            // For Gson
        }

        public GrantHistoryEntry(String type, int amount, boolean granted, long timestamp) {
            this.type = type;
            this.amount = amount;
            this.granted = granted;
            this.timestamp = timestamp;
        }

        public String getType() {
            return type;
        }

        public int getAmount() {
            return amount;
        }

        public boolean isGranted() {
            return granted;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
