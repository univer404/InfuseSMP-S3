# InfuseSMP-S3

Java Paper/Purpur conversion of the Infuse S3 Skript targeting Minecraft 1.21.11.

## Build
Requires JDK 21 and Maven. Run `mvn clean package`. GitHub Actions uploads the resulting JAR as a workflow artifact.

## Configuration
- `config.yml`: general settings.
- `messages.yml`: MiniMessage messages; italics are disabled by the Java message renderer.
- `gui.yml`: recipe GUI presentation settings.
- `players.yml`: UUID-keyed player state.

## Source and parity
The supplied archive includes `infuse_old` and `infuse_new`; `infuse_new` is the target for modern Skript/SkBee and Minecraft 1.21.11. The full source includes custom-model items, rituals, recipes, per-effect listeners and a tick heartbeat. This initial branch is a foundation and is not yet a feature-parity or production-ready release; see CONVERSION_STATUS.md.
