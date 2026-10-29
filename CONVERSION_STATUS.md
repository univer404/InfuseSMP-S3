# Conversion status — active work

Source of truth: `infuse_new/` from the supplied archive. This repository branch is an in-progress Java port, not a feature-parity release.

Implemented foundation: Java 21 / Paper API, YAML configuration and messages, UUID-keyed saved effect slots, history, trust lists, disabled state, display/bind preferences, command handlers, a GitHub Actions artifact workflow, common passive effects, and baseline spark activation/cooldown behavior for the main effect set.

Still needing direct parity work: custom-model/resource-pack icons and charge bars, complete per-effect spark routines and hit counters, full recipe and ritual system including crafting limits, item/potion commands, all listener edge cases, WorldGuard region integration, exact damage/projectile/block behaviors, GUI recipe book and item interaction protections. The current Java mechanics are an approximation for the implemented abilities and must not be advertised as identical to the Skript until these items are ported and tested.
