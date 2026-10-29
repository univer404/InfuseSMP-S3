# InfuseSMP-S3

Java Paper/Purpur conversion of the Infuse S3 Skript for Minecraft 1.21.11.

## Build
Requires JDK 21 and Maven. Run `mvn clean package`. GitHub Actions builds the plugin and generates separate downloadable plugin JAR and Java 26.3 resource-pack ZIP artifacts when both steps succeed.

## Configuration and data
- `config.yml`: general settings, effect tuning, cooldowns, craft limits, effect tuning and disabled worlds. Infuse item colors, names, and lore are fixed in code to preserve the intended design..
- `messages.yml`: MiniMessage messages rendered without italics.
- `gui.yml`: recipe and ritual GUI titles, slots, materials, names and enable switches.
- `players.yml`: UUID-keyed player state and per-slot history.
- `rituals.yml`: persistent ritual craft tallies.
- `regions.yml`: WorldGuard-mapped regions where Infuse effects are disabled.

## Optional integrations
- PlaceholderAPI: source-compatible `%infuse_...%` placeholders.
- WorldGuard: `/mapregion add|delete|list|clear` region mapping; the plugin can run without WorldGuard installed.

## Source and release status
The supplied archive contains `infuse_old` and `infuse_new`; this port targets `infuse_new/`. The resource pack targets Java Edition 26.3 (resource pack format 97.1). The plugin port is still in progress and must not be treated as production-ready until CI succeeds and remaining effect behavior is tested in-game. See [CONVERSION_STATUS.md](CONVERSION_STATUS.md).
