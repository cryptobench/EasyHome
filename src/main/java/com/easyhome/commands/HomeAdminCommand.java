package com.easyhome.commands;

import com.easyhome.EasyHome;
import com.easyhome.config.HomeConfig;
import com.easyhome.data.PlayerGrants;
import com.easyhome.data.PlayerHomes;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.NameMatching;

import java.awt.Color;
import java.util.UUID;

/**
 * /easyhome admin - Admin configuration commands for EasyHome.
 * Supports both console and player execution.
 *
 * Subcommands:
 *   /easyhome admin config              - Show current settings
 *   /easyhome admin set <key> <value>   - Change a setting
 *   /easyhome admin reload              - Reload config from file
 *   /easyhome admin grant homes <player> <amount>
 *   /easyhome admin revoke homes <player> <amount>
 *   /easyhome admin grant instanttp <player>
 *   /easyhome admin revoke instanttp <player>
 *   /easyhome admin status <player>
 */
public class HomeAdminCommand extends CommandBase {
    private final EasyHome plugin;

    private static final Color GOLD = new Color(255, 170, 0);
    private static final Color GREEN = new Color(85, 255, 85);
    private static final Color RED = new Color(255, 85, 85);
    private static final Color GRAY = new Color(170, 170, 170);
    private static final Color AQUA = new Color(85, 255, 255);
    private static final Color YELLOW = new Color(255, 255, 85);

    public HomeAdminCommand(EasyHome plugin) {
        super("easyhome", "EasyHome admin commands");
        this.plugin = plugin;
        setAllowsExtraArguments(true);
    }

    private String[] parseArgs(CommandContext ctx) {
        String input = ctx.getInputString().trim();
        if (input.isEmpty()) {
            return new String[0];
        }
        String[] allArgs = input.split("\\s+");
        if (allArgs.length > 0 && allArgs[0].equalsIgnoreCase("easyhome")) {
            String[] args = new String[allArgs.length - 1];
            System.arraycopy(allArgs, 1, args, 0, allArgs.length - 1);
            return args;
        }
        return allArgs;
    }

    @Override
    protected void executeSync(CommandContext ctx) {
        String[] args = parseArgs(ctx);

        if (args.length == 0) {
            showUsage(ctx);
            return;
        }

        String subcommand = args[0];

        if (subcommand.equalsIgnoreCase("admin")) {
            handleAdmin(ctx, args);
        } else {
            showUsage(ctx);
        }
    }

    private void showUsage(CommandContext ctx) {
        ctx.sendMessage(Message.raw("=== EasyHome ===").color(GOLD));
        ctx.sendMessage(Message.raw("/easyhome admin - Admin settings").color(GRAY));
    }

    private void handleAdmin(CommandContext ctx, String[] args) {
        if (!hasAdminPermission(ctx)) {
            return;
        }

        // args[0] is "admin", so admin subcommand is args[1]
        if (args.length < 2) {
            showAdminHelp(ctx);
            return;
        }

        String adminCmd = args[1];
        String arg1 = args.length > 2 ? args[2] : null;
        String arg2 = args.length > 3 ? args[3] : null;
        String arg3 = args.length > 4 ? args[4] : null;

        switch (adminCmd.toLowerCase()) {
            case "config":
                showConfig(ctx);
                break;
            case "set":
                handleSet(ctx, arg1, arg2);
                break;
            case "reload":
                handleReload(ctx);
                break;
            case "grant":
                handleGrant(ctx, arg1, arg2, arg3);
                break;
            case "revoke":
                handleRevoke(ctx, arg1, arg2, arg3);
                break;
            case "status":
                handleStatus(ctx, arg1);
                break;
            default:
                ctx.sendMessage(Message.raw("Unknown command: " + adminCmd).color(RED));
                showAdminHelp(ctx);
        }
    }

    private boolean hasAdminPermission(CommandContext ctx) {
        // Console always has permission
        if (!ctx.isPlayer()) {
            return true;
        }

        // Check player permission using sender's UUID
        UUID senderUuid = ctx.sender().getUuid();
        if (!PermissionsModule.get().hasPermission(senderUuid, "homes.admin")) {
            ctx.sendMessage(Message.raw("You don't have permission for this command.").color(RED));
            return false;
        }
        return true;
    }

    private void showAdminHelp(CommandContext ctx) {
        ctx.sendMessage(Message.raw("=== EasyHome Admin ===").color(GOLD));
        ctx.sendMessage(Message.raw("").color(GRAY));
        ctx.sendMessage(Message.raw("Configuration:").color(YELLOW));
        ctx.sendMessage(Message.raw("  /easyhome admin config - Show current settings").color(GRAY));
        ctx.sendMessage(Message.raw("  /easyhome admin set <key> <value> - Change a setting").color(GRAY));
        ctx.sendMessage(Message.raw("  /easyhome admin reload - Reload settings").color(GRAY));
        ctx.sendMessage(Message.raw("").color(GRAY));
        ctx.sendMessage(Message.raw("Player Grants:").color(YELLOW));
        ctx.sendMessage(Message.raw("  /easyhome admin grant homes <player> <amount>").color(GRAY));
        ctx.sendMessage(Message.raw("  /easyhome admin revoke homes <player> <amount>").color(GRAY));
        ctx.sendMessage(Message.raw("  /easyhome admin grant instanttp <player>").color(GRAY));
        ctx.sendMessage(Message.raw("  /easyhome admin revoke instanttp <player>").color(GRAY));
        ctx.sendMessage(Message.raw("  /easyhome admin status <player>").color(GRAY));
    }

    private void showConfig(CommandContext ctx) {
        HomeConfig config = plugin.getConfig();
        ctx.sendMessage(Message.raw("=== EasyHome Settings ===").color(GOLD));
        ctx.sendMessage(Message.raw("").color(GRAY));
        ctx.sendMessage(Message.raw("Default homes: " + config.getDefaultHomeLimit()).color(AQUA));
        ctx.sendMessage(Message.raw("Max homes: " + config.getMaxHomeLimit()).color(AQUA));
        ctx.sendMessage(Message.raw("Teleport delay: " + config.getWarmupSeconds() + " seconds").color(AQUA));
        ctx.sendMessage(Message.raw("Permission mode: " + (config.isPermissionOverridesEnabled() ? "on" : "off")).color(AQUA));
        ctx.sendMessage(Message.raw("").color(GRAY));
        ctx.sendMessage(Message.raw("Change settings with:").color(GRAY));
        ctx.sendMessage(Message.raw("  /easyhome admin set default 5").color(YELLOW));
        ctx.sendMessage(Message.raw("  /easyhome admin set max 25").color(YELLOW));
        ctx.sendMessage(Message.raw("  /easyhome admin set warmup 0").color(YELLOW));
        ctx.sendMessage(Message.raw("  /easyhome admin set permissions on").color(YELLOW));
    }

    private void handleSet(CommandContext ctx, String key, String valueStr) {
        if (key == null || valueStr == null) {
            ctx.sendMessage(Message.raw("How to change settings:").color(GOLD));
            ctx.sendMessage(Message.raw("  /easyhome admin set default 3").color(YELLOW));
            ctx.sendMessage(Message.raw("    Give everyone 3 homes").color(GRAY));
            ctx.sendMessage(Message.raw("  /easyhome admin set max 25").color(YELLOW));
            ctx.sendMessage(Message.raw("    Max homes anyone can have").color(GRAY));
            ctx.sendMessage(Message.raw("  /easyhome admin set warmup 0").color(YELLOW));
            ctx.sendMessage(Message.raw("    Teleport delay (0 = instant)").color(GRAY));
            ctx.sendMessage(Message.raw("  /easyhome admin set permissions on").color(YELLOW));
            ctx.sendMessage(Message.raw("    Let permissions override default").color(GRAY));
            return;
        }

        HomeConfig config = plugin.getConfig();

        switch (key.toLowerCase()) {
            case "default":
            case "defaultlimit":
                try {
                    int value = Integer.parseInt(valueStr);
                    config.setDefaultHomeLimit(value);
                    ctx.sendMessage(Message.raw("Default home limit set to " + value + "!").color(GREEN));
                } catch (NumberFormatException e) {
                    ctx.sendMessage(Message.raw("Please enter a number!").color(RED));
                }
                break;

            case "max":
            case "maxlimit":
                try {
                    int value = Integer.parseInt(valueStr);
                    config.setMaxHomeLimit(value);
                    ctx.sendMessage(Message.raw("Maximum home limit set to " + value + "!").color(GREEN));
                } catch (NumberFormatException e) {
                    ctx.sendMessage(Message.raw("Please enter a number!").color(RED));
                }
                break;

            case "warmup":
                try {
                    int value = Integer.parseInt(valueStr);
                    config.setWarmupSeconds(value);
                    if (value == 0) {
                        ctx.sendMessage(Message.raw("Teleport warmup disabled (instant teleport)!").color(GREEN));
                    } else {
                        ctx.sendMessage(Message.raw("Teleport warmup set to " + value + " seconds!").color(GREEN));
                    }
                } catch (NumberFormatException e) {
                    ctx.sendMessage(Message.raw("Please enter a number!").color(RED));
                }
                break;

            case "permissions":
            case "perms":
                boolean enabled = valueStr.equalsIgnoreCase("on") ||
                                  valueStr.equalsIgnoreCase("true") ||
                                  valueStr.equalsIgnoreCase("yes") ||
                                  valueStr.equalsIgnoreCase("enabled");
                config.setPermissionOverridesEnabled(enabled);
                if (enabled) {
                    ctx.sendMessage(Message.raw("Permission overrides enabled!").color(GREEN));
                    ctx.sendMessage(Message.raw("Players with homes.limit.X permissions can exceed default.").color(GRAY));
                } else {
                    ctx.sendMessage(Message.raw("Permission overrides disabled!").color(GREEN));
                    ctx.sendMessage(Message.raw("All players now get " + config.getDefaultHomeLimit() + " homes.").color(GRAY));
                }
                break;

            default:
                ctx.sendMessage(Message.raw("Unknown setting! Try: default, max, warmup, permissions").color(RED));
        }
    }

    private void handleReload(CommandContext ctx) {
        plugin.getConfig().reload();
        ctx.sendMessage(Message.raw("Configuration reloaded!").color(GREEN));
        showConfig(ctx);
    }

    /**
     * Resolve a player identifier to a UUID.
     * Supports both username and UUID format.
     */
    private UUID resolvePlayer(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return null;
        }

        // Check if it's a UUID format (contains dashes)
        if (identifier.contains("-")) {
            try {
                return UUID.fromString(identifier);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        // Try to find online player by name
        PlayerRef playerRef = Universe.get().getPlayerByUsername(identifier, NameMatching.EXACT_IGNORE_CASE);
        if (playerRef != null) {
            return playerRef.getUuid();
        }

        return null;
    }

    private void handleGrant(CommandContext ctx, String type, String playerIdentifier, String amountStr) {
        if (type == null) {
            ctx.sendMessage(Message.raw("Usage:").color(GOLD));
            ctx.sendMessage(Message.raw("  /easyhome admin grant homes <player> <amount>").color(GRAY));
            ctx.sendMessage(Message.raw("  /easyhome admin grant instanttp <player>").color(GRAY));
            return;
        }

        switch (type.toLowerCase()) {
            case "homes":
                grantHomes(ctx, playerIdentifier, amountStr);
                break;
            case "instanttp":
                grantInstantTeleport(ctx, playerIdentifier);
                break;
            default:
                ctx.sendMessage(Message.raw("Unknown grant type: " + type).color(RED));
                ctx.sendMessage(Message.raw("Valid types: homes, instanttp").color(GRAY));
        }
    }

    private void grantHomes(CommandContext ctx, String playerIdentifier, String amountStr) {
        if (playerIdentifier == null || amountStr == null) {
            ctx.sendMessage(Message.raw("Usage: /easyhome admin grant homes <player|uuid> <amount>").color(YELLOW));
            return;
        }

        UUID targetUuid = resolvePlayer(playerIdentifier);
        if (targetUuid == null) {
            ctx.sendMessage(Message.raw("Player not found: " + playerIdentifier).color(RED));
            ctx.sendMessage(Message.raw("Note: For offline players, use their UUID.").color(GRAY));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) {
                ctx.sendMessage(Message.raw("Amount must be positive!").color(RED));
                return;
            }
        } catch (NumberFormatException e) {
            ctx.sendMessage(Message.raw("Invalid amount: " + amountStr).color(RED));
            return;
        }

        plugin.getGrantStorage().grantHomes(targetUuid, amount);

        int newTotal = plugin.getGrantStorage().getBonusHomes(targetUuid);
        ctx.sendMessage(Message.raw("Granted +" + amount + " home slots to " + playerIdentifier).color(GREEN));
        ctx.sendMessage(Message.raw("They now have +" + newTotal + " bonus homes.").color(GRAY));
    }

    private void grantInstantTeleport(CommandContext ctx, String playerIdentifier) {
        if (playerIdentifier == null) {
            ctx.sendMessage(Message.raw("Usage: /easyhome admin grant instanttp <player|uuid>").color(YELLOW));
            return;
        }

        UUID targetUuid = resolvePlayer(playerIdentifier);
        if (targetUuid == null) {
            ctx.sendMessage(Message.raw("Player not found: " + playerIdentifier).color(RED));
            ctx.sendMessage(Message.raw("Note: For offline players, use their UUID.").color(GRAY));
            return;
        }

        plugin.getGrantStorage().grantInstantTeleport(targetUuid);
        ctx.sendMessage(Message.raw("Granted instant teleport to " + playerIdentifier).color(GREEN));
    }

    private void handleRevoke(CommandContext ctx, String type, String playerIdentifier, String amountStr) {
        if (type == null) {
            ctx.sendMessage(Message.raw("Usage:").color(GOLD));
            ctx.sendMessage(Message.raw("  /easyhome admin revoke homes <player> <amount>").color(GRAY));
            ctx.sendMessage(Message.raw("  /easyhome admin revoke instanttp <player>").color(GRAY));
            return;
        }

        switch (type.toLowerCase()) {
            case "homes":
                revokeHomes(ctx, playerIdentifier, amountStr);
                break;
            case "instanttp":
                revokeInstantTeleport(ctx, playerIdentifier);
                break;
            default:
                ctx.sendMessage(Message.raw("Unknown revoke type: " + type).color(RED));
                ctx.sendMessage(Message.raw("Valid types: homes, instanttp").color(GRAY));
        }
    }

    private void revokeHomes(CommandContext ctx, String playerIdentifier, String amountStr) {
        if (playerIdentifier == null || amountStr == null) {
            ctx.sendMessage(Message.raw("Usage: /easyhome admin revoke homes <player|uuid> <amount>").color(YELLOW));
            return;
        }

        UUID targetUuid = resolvePlayer(playerIdentifier);
        if (targetUuid == null) {
            ctx.sendMessage(Message.raw("Player not found: " + playerIdentifier).color(RED));
            ctx.sendMessage(Message.raw("Note: For offline players, use their UUID.").color(GRAY));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) {
                ctx.sendMessage(Message.raw("Amount must be positive!").color(RED));
                return;
            }
        } catch (NumberFormatException e) {
            ctx.sendMessage(Message.raw("Invalid amount: " + amountStr).color(RED));
            return;
        }

        plugin.getGrantStorage().revokeHomes(targetUuid, amount);

        int newTotal = plugin.getGrantStorage().getBonusHomes(targetUuid);
        ctx.sendMessage(Message.raw("Revoked " + amount + " home slots from " + playerIdentifier).color(GREEN));
        ctx.sendMessage(Message.raw("They now have +" + newTotal + " bonus homes.").color(GRAY));
    }

    private void revokeInstantTeleport(CommandContext ctx, String playerIdentifier) {
        if (playerIdentifier == null) {
            ctx.sendMessage(Message.raw("Usage: /easyhome admin revoke instanttp <player|uuid>").color(YELLOW));
            return;
        }

        UUID targetUuid = resolvePlayer(playerIdentifier);
        if (targetUuid == null) {
            ctx.sendMessage(Message.raw("Player not found: " + playerIdentifier).color(RED));
            ctx.sendMessage(Message.raw("Note: For offline players, use their UUID.").color(GRAY));
            return;
        }

        plugin.getGrantStorage().revokeInstantTeleport(targetUuid);
        ctx.sendMessage(Message.raw("Revoked instant teleport from " + playerIdentifier).color(GREEN));
    }

    private void handleStatus(CommandContext ctx, String playerIdentifier) {
        if (playerIdentifier == null) {
            ctx.sendMessage(Message.raw("Usage: /easyhome admin status <player|uuid>").color(YELLOW));
            return;
        }

        UUID targetUuid = resolvePlayer(playerIdentifier);
        if (targetUuid == null) {
            ctx.sendMessage(Message.raw("Player not found: " + playerIdentifier).color(RED));
            ctx.sendMessage(Message.raw("Note: For offline players, use their UUID.").color(GRAY));
            return;
        }

        PlayerGrants grants = plugin.getGrantStorage().getGrants(targetUuid);
        PlayerHomes homes = plugin.getStorage().getHomes(targetUuid);
        HomeConfig config = plugin.getConfig();

        int baseLimit = config.getDefaultHomeLimit();
        int bonusHomes = grants.getBonusHomes();
        int totalLimit = plugin.getHomeLimitByUuid(targetUuid);
        int currentHomes = homes.getHomeCount();
        boolean hasInstantTp = grants.hasInstantTeleport();

        ctx.sendMessage(Message.raw("=== Player Status: " + playerIdentifier + " ===").color(GOLD));
        ctx.sendMessage(Message.raw("UUID: " + targetUuid).color(GRAY));
        ctx.sendMessage(Message.raw("").color(GRAY));
        ctx.sendMessage(Message.raw("Home Limits:").color(YELLOW));
        ctx.sendMessage(Message.raw("  Base limit: " + baseLimit).color(AQUA));
        ctx.sendMessage(Message.raw("  Bonus homes: +" + bonusHomes).color(AQUA));
        ctx.sendMessage(Message.raw("  Total limit: " + totalLimit).color(GREEN));
        ctx.sendMessage(Message.raw("  Current homes: " + currentHomes + "/" + totalLimit).color(AQUA));
        ctx.sendMessage(Message.raw("").color(GRAY));
        ctx.sendMessage(Message.raw("Perks:").color(YELLOW));
        ctx.sendMessage(Message.raw("  Instant teleport: " + (hasInstantTp ? "Yes" : "No")).color(hasInstantTp ? GREEN : GRAY));
    }
}
