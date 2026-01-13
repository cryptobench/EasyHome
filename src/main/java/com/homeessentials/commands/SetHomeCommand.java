package com.homeessentials.commands;

import com.homeessentials.HomeEssentials;
import com.homeessentials.data.Home;
import com.homeessentials.data.PlayerHomes;
import com.homeessentials.util.Messages;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

/**
 * /sethome [name] - Save current location as a home.
 */
public class SetHomeCommand extends AbstractPlayerCommand {
    private static final String DEFAULT_HOME_NAME = "home";
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final int MAX_NAME_LENGTH = 32;

    private final HomeEssentials plugin;
    private final OptionalArg<String> nameArg;

    public SetHomeCommand(HomeEssentials plugin) {
        super("sethome", "Save your current location as a home");
        this.plugin = plugin;

        this.nameArg = withOptionalArg("name", "Name for this home (default: home)", ArgTypes.STRING);

        requirePermission("homes.use");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {

        Player player = store.getComponent(playerRef, Player.getComponentType());

        String homeName = nameArg.get(ctx);
        if (homeName == null || homeName.isEmpty()) {
            homeName = DEFAULT_HOME_NAME;
        }

        if (!VALID_NAME_PATTERN.matcher(homeName).matches()) {
            playerData.sendMessage(Messages.invalidHomeName());
            return;
        }

        if (homeName.length() > MAX_NAME_LENGTH) {
            playerData.sendMessage(Messages.homeNameTooLong());
            return;
        }

        PlayerHomes homes = plugin.getStorage().getHomes(playerData.getUuid());

        if (!homes.hasHome(homeName)) {
            int limit = plugin.getHomeLimit(player);
            if (homes.getHomeCount() >= limit) {
                playerData.sendMessage(Messages.homeLimitReached(limit));
                return;
            }
        }

        TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());

        Vector3d position = transform.getPosition();
        Vector3f rotation = transform.getRotation();
        String worldName = world.getName();

        Home home = new Home(
                homeName,
                worldName,
                position.getX(),
                position.getY(),
                position.getZ(),
                rotation.getYaw(),
                rotation.getPitch()
        );

        homes.setHome(home);
        plugin.getStorage().saveHomes(playerData.getUuid());

        playerData.sendMessage(Messages.homeSet(homeName));
    }
}
