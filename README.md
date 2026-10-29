# InfuseSMP-S3

Java Paper/Purpur conversion of the Infuse S3 Skript targeting Minecraft 1.21.11.

## Build
Requires JDK 21 and Maven. Run `mvn clean package`. The GitHub Actions workflow builds pull requests and uploads the JAR as a workflow artifact when the build succeeds.

## Configuration and data
- `config.yml`: general settings, effect durations, cooldowns, craft limits, disabled worlds and ritual duration.
- `messages.yml`: MiniMessage messages rendered without italics.
- `gui.yml`: recipe GUI defaults.
- `players.yml`: UUID-keyed player state saved by the plugin.
- `rituals.yml`: persistent craft tallies.

## Source of truth
The supplied archive includes `infuse_old` and `infuse_new`. This port targets `infuse_new/` for Minecraft 1.21.11.

## Status
This is an active conversion branch. Several original effect-specific mechanics and resource-pack visuals still need porting and the latest CI build must pass before production use. See [CONVERSION_STATUS.md](CONVERSION_STATUS.md).
