# InfuseSMP-S3

[![Build](https://github.com/univer404/InfuseSMP-S3/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/univer404/InfuseSMP-S3/actions/workflows/build.yml)
[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Open Source](https://img.shields.io/badge/Open%20Source-Yes-brightgreen.svg)](LICENSE)

**InfuseSMP-S3 is free and open-source software, released under the MIT License.** You may use, study, modify, and redistribute this project, including in commercial projects, provided you retain the copyright and license notice. See [LICENSE](LICENSE) for the complete terms.

InfuseSMP-S3 is an open-source Paper/Purpur plugin port of the Infuse S3 Skript. It brings custom Infuse items, equipable effects, two-slot activation, recipe browsing, ritual crafting, and player trust controls to a Java plugin.

> **Status:** Active development. Some mechanics still need parity work and in-game verification. See [the development roadmap](CONVERSION_STATUS.md) before using this as a production replacement.

## Compatibility

- Minecraft: 1.21.11
- Server: Paper or compatible Purpur
- Java: 21
- Build: Maven
- Optional integrations: PlaceholderAPI and WorldGuard

## Features

- Manage Infuse effects through the `/infuse` command and inventory GUI.
- Equip primary and secondary effect slots, bind controls, activate, swap, and drain effects.
- Create and manage custom Infuse items with plugin data identifiers.
- Browse recipes and inspect augmented item previews.
- Use ritual crafting with persistent progress.
- Manage trusted players and view effect history.
- Configure plugin behavior through YAML files.
- Optionally integrate with PlaceholderAPI and WorldGuard.

## Commands

| Command | Description |
|---|---|
| `/infuse` | Open or manage Infuse effects. Includes management actions such as add, remove, get, history, and toggle. |
| `/bind` | Set an activation binding for an Infuse slot. |
| `/activate` | Activate a bound Infuse slot. |
| `/swap` | Swap primary and secondary effects. |
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

Aliases:

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

## Permissions

| Permission | Default | Purpose |
|---|---|---|
| `infuse.command` | Everyone | Use the main Infuse command. |
| `infuse.admin` | Operators | Use administrative commands, including resets, region management, and item giving. |

Use each command's in-game help or usage output for supported arguments and subcommands.

## Configuration and data

Configuration files are generated in `plugins/InfuseSMP-S3/`.

| File | Purpose |
|---|---|
| `config.yml` | General settings, effect tuning, cooldowns, craft limits, and disabled worlds. |
| `messages.yml` | Player-facing messages and formatting. |
| `gui.yml` | Recipe and ritual GUI titles, slots, materials, names, and enable switches. |
| `recipes.yml` | Recipe definitions. |
| `playerdata.yml` | UUID-based player state, equipped effects, bindings, trust, display settings, and history. |
| `rituals.yml` | Persistent ritual crafting tallies. |
| `regions.yml` | WorldGuard region mappings where Infuse effects are disabled. |

Infuse item names, colors, and lore are currently defined by the plugin to preserve the intended item presentation. GUI settings are configured separately in `gui.yml`.

## Installation

1. Download a JAR from [GitHub Releases](https://github.com/univer404/InfuseSMP-S3/releases) or a successful [GitHub Actions build](https://github.com/univer404/InfuseSMP-S3/actions).
2. Put the JAR in your server's `plugins` directory.
3. Start the server once to generate the configuration files.
4. Stop the server and review the generated YAML files.
5. Install PlaceholderAPI and/or WorldGuard if you want those optional integrations.
6. Start the server and check the console for errors.

Back up your plugin data before updating.

## Build from source

You need JDK 21 and Maven.

```bash
git clone https://github.com/univer404/InfuseSMP-S3.git
cd InfuseSMP-S3
mvn --batch-mode clean package
```

The plugin JAR is generated in `target/`. GitHub Actions also builds the plugin and the resource-pack artifacts on pushes to `main` and `develop`, on pull requests, and when manually dispatched.

## Contributing

Contributions, bug reports, and suggestions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request. For bug reports, include your server software and version, Java version, plugin version, relevant configuration, and a complete error log.

## License

<ins>This project is licensed under the MIT License.</ins> The MIT License allows use, modification, distribution, sublicensing, and sale of copies, subject to retaining the copyright and permission notice. It is provided without warranty. Read the full [MIT License](LICENSE) before using or redistributing the project.

## Acknowledgements

This project is a Java port of the Infuse S3 Skript. The goal is to preserve the original behavior while making the implementation maintainable as a Paper/Purpur plugin.
