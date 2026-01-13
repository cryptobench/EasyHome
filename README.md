# HomeEssentials

A user-friendly home management plugin for Hytale servers. Allows players to save, teleport to, and manage multiple named home locations.

## Features

- **Multiple named homes** - Save locations with custom names
- **Permission-based limits** - Configure how many homes each player/group can have
- **Teleport warmup** - 3-second warmup that cancels if player moves
- **Clean colored messages** - Easy to read feedback
- **Simple commands** - Intuitive command syntax

## Commands

| Command | Description |
|---------|-------------|
| `/sethome` | Save current location as 'home' |
| `/sethome <name>` | Save current location with custom name |
| `/home` | Teleport to 'home' |
| `/home <name>` | Teleport to named home |
| `/homes` | List all your saved homes |
| `/delhome <name>` | Delete a saved home |
| `/homehelp` | Show help and setup guide |

## Permissions

| Permission | Description |
|------------|-------------|
| `homes.use` | Basic access to home commands |
| `homes.limit.1` | Allow 1 home (default if no limit set) |
| `homes.limit.3` | Allow 3 homes |
| `homes.limit.5` | Allow 5 homes |
| `homes.limit.unlimited` | Unlimited homes |
| `homes.bypass.warmup` | Skip the 3-second teleport delay |

## Installation

1. Build the plugin:
   ```bash
   mvn clean package
   ```

2. Copy `target/HomeEssentials-1.0.0.jar` to your server's `mods/` folder

3. Start/restart the server

## Quick Setup

Run these commands in the server console to set up basic permissions:

```
perm group add default homes.use
perm group add default homes.limit.1
```

For VIP players with more homes:
```
perm group add vip homes.use
perm group add vip homes.limit.3
```

For admins with unlimited homes:
```
perm group add admin homes.use
perm group add admin homes.limit.unlimited
```

## Usage Examples

**Save your first home:**
```
/sethome
```

**Save a home called "base":**
```
/sethome base
```

**Teleport home:**
```
/home
```

**Teleport to "base":**
```
/home base
```

**See all your homes:**
```
/homes
```

**Delete a home:**
```
/delhome base
```

## Data Storage

Player homes are stored as JSON files in `mods/Community_HomeEssentials/homes/`:
```
homes/
  ├── <player-uuid-1>.json
  ├── <player-uuid-2>.json
  └── ...
```

## Requirements

- Hytale Server
- Java 25

## License

MIT License - Feel free to modify and distribute.
