package com.homeessentials.util;

import com.hypixel.hytale.server.core.Message;

import java.awt.Color;

/**
 * Centralized message formatting for HomeEssentials.
 */
public class Messages {
    // Colors
    private static final Color GREEN = new Color(85, 255, 85);
    private static final Color RED = new Color(255, 85, 85);
    private static final Color YELLOW = new Color(255, 255, 85);
    private static final Color GOLD = new Color(255, 170, 0);
    private static final Color GRAY = new Color(170, 170, 170);
    private static final Color WHITE = new Color(255, 255, 255);

    public static Message homeSet(String name) {
        return Message.raw("Home '" + name + "' has been set!").color(GREEN);
    }

    public static Message homeDeleted(String name) {
        return Message.raw("Home '" + name + "' has been deleted!").color(GREEN);
    }

    public static Message homeNotFound(String name) {
        return Message.raw("Home '" + name + "' not found!").color(RED);
    }

    public static Message noHomes() {
        return Message.raw("You don't have any homes set. Use /sethome to create one!").color(YELLOW);
    }

    public static Message homesList(int count, int limit) {
        String limitStr = limit == Integer.MAX_VALUE ? "unlimited" : String.valueOf(limit);
        return Message.raw("Your homes (" + count + "/" + limitStr + "):").color(GOLD);
    }

    public static Message homeEntry(String name, String location) {
        return Message.raw("- " + name + " (" + location + ")").color(GRAY);
    }

    public static Message homeLimitReached(int limit) {
        return Message.raw("You've reached your home limit (" + limit + "). Delete a home first!").color(RED);
    }

    public static Message invalidHomeName() {
        return Message.raw("Invalid home name! Use only letters, numbers, and underscores.").color(RED);
    }

    public static Message homeNameTooLong() {
        return Message.raw("Home name is too long! Maximum 32 characters.").color(RED);
    }

    public static Message warmupStarted(String name, int seconds) {
        return Message.raw("Teleporting to '" + name + "' in " + seconds + " seconds... Don't move!").color(YELLOW);
    }

    public static Message teleportCancelled() {
        return Message.raw("Teleport cancelled - you moved!").color(RED);
    }

    public static Message teleportedTo(String name) {
        return Message.raw("Teleported to '" + name + "'!").color(GREEN);
    }

    public static Message worldNotFound(String world) {
        return Message.raw("Cannot teleport - world '" + world + "' not found!").color(RED);
    }

    public static Message useHomesHint() {
        return Message.raw("Use /homes to see your saved homes.").color(GRAY);
    }

    public static Message specifyHomeName() {
        return Message.raw("Please specify a home name to delete!").color(RED);
    }
}
