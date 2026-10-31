# InfuseSMP-S3

**InfuseSMP-S3** is a Paper/Purpur plugin port of the Infuse S3 Skript. It adds custom Infuse items, equipable effects, two-slot activation controls, recipe browsing, ritual crafting, and player-to-player trust settings.

> **Minecraft version:** 1.21.11  
> **Java:** 21  
> **Build system:** Maven

## Features

### Infuse effects and slots
- Equip and manage Infuse effects through the `/infuse` command and its GUI.
- Use primary and secondary effect slots.
- Bind slot activation to supported controls, then activate effects in-game.
- Swap the primary and secondary effects.
- Drain the primary or secondary effect when needed.
- Configure effect behavior and tuning through the plugin configuration.
- Toggle the Infuse item display style.

### Custom Infuse items
- Includes custom Infuse items used by the plugin's effect and crafting systems.
- Custom item identity is tracked with plugin data, rather than relying only on display names.
- Admins can give supported Infuse items with the item command.

### Recipes and ritual crafting
- Browse available Infuse recipes in an inventory menu.
- Open recipe details and inspect the augmented item preview.
- Use the configured recipe and ritual crafting systems.
- Ritual craft tallies are saved so progress persists across restarts.
- Administrators can reset ritual crafting tallies.

### Player controls and trust
- Configure activation bindings.
- Toggle the Infuse display style.
- Manage trusted players using the trust command.
- View Infuse history and current effect information through the management command.

### Region support
- Optional WorldGuard integration.
- Map WorldGuard regions where Infuse effects are disabled.
- The plugin can run without WorldGuard; region integration is only available when WorldGuard is installed.

### PlaceholderAPI support
- Optional PlaceholderAPI integration.
- Provides the plugin's `%infuse_...%` placeholders when PlaceholderAPI is installed.

### Persistent data
The plugin stores player state and plugin data in YAML files, including:
- Player effects, equipped slots, bindings, trusted players, display settings, and action history.
- Ritual crafting tallies.
- WorldGuard region mappings.
- Existing `players.yml` data is migrated to `playerdata.yml` when the new file does not already exist.

## Commands

| Command | Description |
|---|---|
| `/infuse` | Open or manage Infuse effects. Supports the GUI and management actions such as add, remove, get, history, and toggle. |
| `/bind` | Set an activation binding for an Infuse slot. |
| `/activate` | Activate a bound Infuse slot. |
| `/swap` | Swap the primary and secondary effects. |
| `/toggledisplay` | Toggle the Infuse item display style. |
| `/trust` | Manage trusted players. |
| `/ldrain` | Drain the primary effect. |
| `/rdrain` | Drain the secondary effect. |
| `/conf` | Read or change supported configuration options. |
| `/recipes` | Open the recipe browser. |
| `/resetcooldown` | Reset Infuse spark cooldowns. |
| `/resetcraft` | Reset ritual crafting tallies. |
| `/mapregion` | Manage regions where Infuse effects are disabled. |
| `/item` | Give supported Infuse custom items. |

Some commands have aliases:
- `/bind`: `/controls`, `/controlset`, `/control`
- `/toggledisplay`: `/display`, `/toggleicon`, `/simpleicon`
- `/activate`: `/u`, `/use`, `/a`
- `/swap`: `/swapeff`, `/swapeffects`, `/swapslots`
- `/trust`: `/t`
- `/ldrain`: `/pdrain`
- `/rdrain`: `/sdrain`
- `/resetcooldown`: `/rscd`, `/rcd`, `/rscooldown`
- `/resetcraft`: `/rscraft`
- `/item`: `/giveitem`, `/i`, `/potion`, `/pot`

### Permissions

| Permission | Default | Purpose |
|---|---|---|
| `infuse.command` | Everyone | Use the main Infuse command. |
| `infuse.admin` | Operators | Use administrative commands such as cooldown/craft resets, region management, and item giving. |

Check the command's in-game usage for its supported arguments and subcommands.

## Configuration

The plugin creates its YAML files in `plugins/InfuseSMP-S3/`.

| File | Purpose |
|---|---|
| `config.yml` | General settings, effect tuning, cooldowns, craft limits, and disabled worlds. |
| `messages.yml` | Player-facing messages and formatting. |
| `gui.yml` | Recipe and ritual GUI titles, slots, materials, names, and enable switches. |
| `recipes.yml` | Recipe definitions used by the recipe and crafting systems. |
| `playerdata.yml` | UUID-based player data, equipped effects, bindings, trust, display settings, and action history. |
| `rituals.yml` | Persistent ritual crafting tallies. |
| `regions.yml` | WorldGuard region mappings where Infuse effects are disabled. |

Infuse item names, colors, and lore are currently defined by the plugin to retain the intended item presentation. GUI settings are provided separately in `gui.yml`.

## Requirements and optional integrations

- Paper or a compatible Purpur server running Minecraft 1.21.11.
- Java 21 runtime.
- PlaceholderAPI is optional and enables Infuse placeholders.
- WorldGuard is optional and enables the region-mapping integration.

## Installation

1. Download the plugin JAR from the project's GitHub Actions artifact or official release.
2. Place the JAR in your server's `plugins` folder.
3. Start the server once to generate the plugin's configuration files.
4. Stop the server and review `config.yml`, `messages.yml`, `gui.yml`, and `recipes.yml`.
5. Install PlaceholderAPI and/or WorldGuard if you want their optional integrations.
6. Start the server and check the console for plugin startup messages.

Back up your YAML data files before updating the plugin.

## Building from source

Requires JDK 21 and Maven:

```bash
mvn clean package
```

GitHub Actions is configured to build the project and publish downloadable workflow artifacts when the workflow completes successfully. Check the [Actions page](https://github.com/univer404/InfuseSMP-S3/actions) for the latest build.

## Project status

This repository is a Skript-to-Java port and should be considered under active development until the current GitHub Actions build succeeds and the features have been tested on a live Paper/Purpur 1.21.11 server. A successful compile alone does not replace in-game testing of every effect, recipe, GUI interaction, persistence path, and optional integration.

## Publishing

This project is intended for distribution through Modrinth. It does not include external license verification, DRM checks, or API-token requirements.

## Source

[GitHub repository](https://github.com/univer404/InfuseSMP-S3/tree/conversion/paper-plugin-1.0.0)
