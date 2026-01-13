# Hytale Plugin Development Guide

## Overview
This document contains learnings from developing the HomeEssentials plugin for Hytale servers.

## Project Structure

```
plugins/PluginName/
├── src/main/java/com/yourplugin/
│   ├── YourPlugin.java           # Main plugin class
│   ├── commands/                  # Command classes
│   └── data/                      # Data classes
├── src/main/resources/
│   └── manifest.json              # Plugin manifest (REQUIRED)
├── pom.xml                        # Maven build file
└── target/
    └── PluginName-1.0.0.jar       # Built JAR goes in mods/ folder
```

## manifest.json (CRITICAL)

The manifest MUST use **PascalCase** field names:

```json
{
    "Group": "YourGroup",
    "Name": "PluginName",
    "Version": "1.0.0",
    "Main": "com.yourplugin.YourPlugin",
    "Description": "Plugin description",
    "Authors": [],
    "Website": "",
    "Dependencies": {},
    "OptionalDependencies": {}
}
```

**Important:** Using lowercase field names (e.g., `name` instead of `Name`) will cause validation errors!

## Main Plugin Class

Extend `JavaPlugin` (NOT `PluginBase`) for external plugins:

```java
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class YourPlugin extends JavaPlugin {

    public YourPlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    public void setup() {
        // Register commands, initialize storage
        getCommandRegistry().registerCommand(new YourCommand(this));
    }

    @Override
    public void start() {
        // Called after setup
    }

    @Override
    public void shutdown() {
        // Cleanup, save data
    }
}
```

## Commands

Extend `AbstractPlayerCommand` for player-only commands:

```java
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;

public class YourCommand extends AbstractPlayerCommand {

    private final OptionalArg<String> optionalArg;
    private final RequiredArg<String> requiredArg;

    public YourCommand(YourPlugin plugin) {
        super("commandname", "Command description");

        // Optional argument
        this.optionalArg = withOptionalArg("argname", "Arg description", ArgTypes.STRING);

        // Required argument
        this.requiredArg = withRequiredArg("argname", "Arg description", ArgTypes.STRING);

        // Require permission
        requirePermission("yourplugin.use");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {

        // Get argument values
        String optional = optionalArg.get(ctx);
        String required = requiredArg.get(ctx);

        // Get Player component for permissions
        Player player = store.getComponent(playerRef, Player.getComponentType());

        // Check permissions
        if (player.hasPermission("some.permission")) {
            // ...
        }

        // Send messages
        playerData.sendMessage(Message.raw("Hello!").color(new Color(85, 255, 85)));
    }
}
```

## Messages & Colors

Hytale uses a fluent API for messages - NOT Minecraft color codes (§):

```java
import com.hypixel.hytale.server.core.Message;
import java.awt.Color;

// Define colors
Color GREEN = new Color(85, 255, 85);
Color RED = new Color(255, 85, 85);
Color YELLOW = new Color(255, 255, 85);
Color GOLD = new Color(255, 170, 0);
Color GRAY = new Color(170, 170, 170);
Color AQUA = new Color(85, 255, 255);

// Send colored message
playerData.sendMessage(Message.raw("Success!").color(GREEN));

// DON'T use Minecraft codes - they show as literal text:
// playerData.sendMessage(Message.raw("§aSuccess!")); // WRONG - shows "§aSuccess!"
```

## Teleportation

Use the Teleport component:

```java
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;

// Must execute on world thread
world.execute(() -> {
    Vector3d position = new Vector3d(x, y, z);
    Vector3f rotation = new Vector3f(yaw, pitch, 0);

    Teleport teleport = new Teleport(world, position, rotation);
    store.addComponent(playerRef, Teleport.getComponentType(), teleport);
});
```

## Player Position

Get player position via TransformComponent:

```java
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;

TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
Vector3d position = transform.getPosition();
Vector3f rotation = transform.getRotation();

double x = position.getX();
double y = position.getY();
double z = position.getZ();
float yaw = rotation.getYaw();
float pitch = rotation.getPitch();
```

## Permissions

```java
// Check permission
Player player = store.getComponent(playerRef, Player.getComponentType());
if (player.hasPermission("myplugin.admin")) {
    // Has permission
}

// Console commands for permission management:
// perm user add <player> <permission>
// perm group add Adventure <permission>
// perm group remove Adventure <permission>
```

## Data Storage

Use `getDataDirectory()` for plugin data folder:

```java
Path dataDir = getDataDirectory();  // plugins/YourPlugin/
Path homesDir = dataDir.resolve("homes");
Files.createDirectories(homesDir);

// Use Gson for JSON (provided by Hytale)
Gson gson = new GsonBuilder().setPrettyPrinting().create();
```

## Scheduling / Delayed Tasks

Use Java's ScheduledExecutorService, but execute game actions on world thread:

```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

// Schedule task
scheduler.schedule(() -> {
    // Run on world thread for game actions
    world.execute(() -> {
        // Teleport, modify entities, etc.
    });
}, 3, TimeUnit.SECONDS);

// Don't forget to shutdown in plugin shutdown()
scheduler.shutdown();
```

## Building

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.yourplugin</groupId>
    <artifactId>YourPlugin</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>25</maven.compiler.source>
        <maven.compiler.target>25</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Hytale Server API -->
        <dependency>
            <groupId>com.hypixel.hytale</groupId>
            <artifactId>HytaleServer</artifactId>
            <version>1.0.0</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/../../HytaleServer.jar</systemPath>
        </dependency>

        <!-- Gson (provided by Hytale) -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
            <scope>provided</scope>
        </dependency>

        <!-- Annotations -->
        <dependency>
            <groupId>com.google.code.findbugs</groupId>
            <artifactId>jsr305</artifactId>
            <version>3.0.2</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>true</filtering>
            </resource>
        </resources>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.3.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

### Build Command

```bash
mvn clean package
```

### Installation

1. Copy JAR to `Server/mods/` folder (NOT plugins/)
2. Start server with: `java -jar HytaleServer.jar --assets Assets.zip`
3. Plugin loads automatically on server start

## Server Commands

```bash
# Start server
cd Server
java -jar HytaleServer.jar --assets Assets.zip

# In-game/console commands
plugin list                          # List loaded plugins
plugin load Group:PluginName         # Load a plugin
plugin reload Group:PluginName       # Reload a plugin

# Permissions
perm user add <player> <permission>
perm group add Adventure <permission>
op add <player>                      # Make player operator
```

## Common Issues

1. **"Name can't be null" error**: Use PascalCase in manifest.json (`Name` not `name`)
2. **Plugin not loading**: Put JAR in `mods/` folder, not `plugins/`
3. **Color codes showing as text**: Use `Message.raw().color(Color)`, not `§` codes
4. **Teleport not working**: Must run on world thread via `world.execute()`
5. **Class not found**: Ensure you extend `JavaPlugin`, not `PluginBase`
6. **Build fails with Java version**: Hytale requires Java 25

## Useful Imports

```java
// Plugin
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

// Commands
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;

// Entity/Player
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;

// Transform/Position
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;

// Teleport
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;

// Messages
import com.hypixel.hytale.server.core.Message;
```
