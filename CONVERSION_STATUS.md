# Conversion status

Source of truth: the supplied archive's `infuse_new/` tree for Minecraft 1.21.11. This remains an active conversion branch, not a verified production release.

## Implemented in the Java branch
- Java 21 / Paper API Maven project and GitHub Actions build workflow, including a separate generated resource-pack ZIP artifact for Java Edition 26.3.
- Non-italic MiniMessage rendering, small-cap default text, and YAML-configurable item names/colors/model data/lore.
- UUID-based player effect slots, disabled state, per-slot history, trust list, bind preferences, display preference and player state persistence.
- Commands for `/infuse`, `/item`, `/activate`, `/bind`, `/swap`, `/trust`, `/toggledisplay`, drain aliases, `/recipes`, `/resetcooldown`, `/resetcraft`, `/conf`, and optional WorldGuard `/mapregion`.
- Optional PlaceholderAPI expansion matching source identifiers for effect names, icons, augmented state, timers and tick counters.
- Protected recipe browser and brewing-stand ritual crafting GUIs whose titles, slots, filler, control items and enabled flags are read from `gui.yml`.
- Craft limits, augmented ritual duration, brewing-stand failure handling, and persistent craft tallies.
- Custom-model-data potion items, drink-to-equip flow, previous-item return, death drops, configurable effect lore, and overflow item drops.
- A generated Java Edition 26.3 resource pack with pack format 97.1, modern `assets/minecraft/items/potion.json` dispatch, and 13 effect icons with augmented variants.
- Base passive and spark effects, cooldown timers, fire/frost/thunder hit triggers, feather fall protection and launch, ocean pull, heart health/absorption, emerald XP/consumable behavior, and haste/emerald tool enchantments.
- Optional WorldGuard region mapping via reflection; WorldGuard is not a hard dependency.

## Still required for full parity and release
- Exact parity for every branch in `heartbeat.sk`, `listeners/damage.sk`, `listeners/projectiles.sk`, `listeners/per-effect.sk`, `events/spark.sk`, `functions/per-effect.sk`, and the ritual/resource-pack scripts.
- Remaining or approximate effect mechanics include complete ender curse/teleport behavior, full heart damage transfer, emerald enchant/XP edge cases, all feather wind-charge interactions, all frost block/boots/lava effects, fire water/block interactions, projectile behavior and region edge cases.
- Exact original resource-pack font animation/bar rendering, original texture artwork, all item metadata, all sounds/particles, roll animation parity and crossplay-specific visual alternatives. The new 26.3 pack is a compatible replacement for the potion item models, not an exact recreation of the original pack.
- A fresh successful CI build must be confirmed, followed by in-game testing on Paper/Purpur 1.21.11. A previous CI run found compilation issues that were fixed; do not merge or publish as production until a fresh build passes.
