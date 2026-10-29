# Conversion status

Source of truth: `infuse_new/` from the provided archive for Minecraft 1.21.11.

This first commit establishes the Java 21 / Paper API Maven project, GitHub Actions build workflow, YAML configuration split, and a base command/state layer. Full parity still requires porting every effect handler, heartbeat behavior, rituals, crafting recipes, item metadata/custom model data, spark charge/cooldown logic, WorldGuard region behavior, and the recipe GUI. Do not treat this initial conversion as production complete.
