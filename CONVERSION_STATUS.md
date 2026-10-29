# Conversion status

Source of truth: the supplied archive's `infuse_new/` tree for Minecraft 1.21.11. This is an active port and is not yet a verified feature-parity release.

## Implemented in the Java branch
- Java 21 / Paper API Maven project and pull-request GitHub Actions build.
- YAML configuration, non-italic MiniMessage output, small-cap styling for most plugin messages.
- UUID-based player effect slots, history, trusted players, disabled state, binds and display preference saved in `players.yml`.
- `/infuse add|remove|get|history|toggle`, `/activate`, `/bind`, `/swap`, `/trust`, `/item`, `/recipes`, `/resetcooldown`, `/resetcraft`, `/conf`, and drain aliases.
- Custom-model-data potion items for the source effect IDs, drink-to-equip flow, primary/secondary selection, previous-item return, and equipped-item drops on death.
- Protected recipe browser GUI with the original ingredient arrangements and protected brewing-stand ritual crafting GUI.
- Craft limits and configurable augmented ritual duration with brewing-stand failure detection.
- Base passive effects, spark cooldowns, fire/frost/thunder hit triggers, feather fall protection, and trust-aware target filtering.

## Not yet complete / must be verified
- The original source has many effect-specific behaviors in `heartbeat.sk`, `listeners/damage.sk`, `listeners/projectiles.sk`, `listeners/per-effect.sk`, `events/spark.sk`, and `functions/per-effect.sk`. Several of these are still approximations or unported, including full ender curse/teleport behavior, full heart health transfer, ocean pull mechanics, feather wind-charge systems, emerald XP/consumable/enchant behavior, haste enchant/attack-speed behavior, frost boots/lava/block behavior, fire block/water interactions, and full projectile interactions.
- Exact original item lore, all potion metadata, resource-pack icon/bar rendering, roll animations, region/WorldGuard integration, all ritual visual details, and edge cases still need parity work.
- The GitHub Actions compile run exposed duplicate method visibility and an oversized `Map.of`; these were corrected. A fresh successful build has not yet been confirmed after the fixes. Do not use as a production release until CI passes and in-game testing confirms parity.
