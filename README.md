# InfuseSMP-S3

Java Paper/Purpur conversion of the Infuse S3 Skript for Minecraft 1.21.11.

## Build
Requires JDK 21 and Maven. Run `mvn clean package`. GitHub Actions builds the plugin and generates separate downloadable plugin JAR and Java 26.3 resource-pack ZIP artifacts when both steps succeed.

## Configuration and data
- `config.yml`: general settings, effect tuning, cooldowns, craft limits, effect tuning and disabled worlds. Infuse item colors, names, and lore are fixed in code to preserve the intended design..
- `messages.yml`: MiniMessage messages rendered without italics.
- `gui.yml`: recipe and ritual GUI titles, slots, materials, names and enable switches.
- `playerdata.yml`: UUID-keyed player state, equipped effects, binds, trusted players, display settings and action history. Existing `players.yml` data is migrated automatically if `playerdata.yml` does not exist.
- `rituals.yml`: persistent ritual craft tallies.
- `regions.yml`: WorldGuard-mapped regions where Infuse effects are disabled.

## Optional integrations
- PlaceholderAPI: source-compatible `%infuse_...%` placeholders.
- WorldGuard: `/mapregion add|delete|list|clear` region mapping; the plugin can run without WorldGuard installed.

## Source and release status
The supplied archive contains `infuse_old` and `infuse_new`; this port targets `infuse_new/`. The resource pack targets Java Edition 26.3 (resource pack format 97.1). The plugin port is still in progress and must not be treated as production-ready until CI succeeds and remaining effect behavior is tested in-game. See [CONVERSION_STATUS.md](CONVERSION_STATUS.md).


## Paid release / BuiltByBit licensing

This plugin includes a BuiltByBit V2 Embed API usage-rights check. Before uploading a paid release:

1. Create a BuiltByBit API token with **only** the `resources.embed.latest` scope.
2. Put that shareable token into `license.api-token` in `config.yml` before building the buyer JAR. Never embed a token with broader permissions.
3. Keep the `%%__NONCE__%%` placeholder unchanged in the compiled plugin so BuiltByBit can replace it during distribution.
4. In the BuiltByBit listing, disclose the BuiltByBit API/DRM check and link to the official BuiltByBit licensing documentation in the Dependencies section. Select the DRM/licensing filter accurately.
5. Test the exact uploaded JAR by downloading it through the listing and starting it on a clean Paper/Purpur 1.21.11 server. A recent successful verification is cached for up to 24 hours if the API is temporarily unavailable.

The license check fails closed when no configured token exists or when neither the API nor a recent successful cache can verify usage rights. Do not publish a JAR that still contains `PUT_SHAREABLE_TOKEN_HERE`.

See BuiltByBit's official [license check guide](https://builtbybit.com/help/developers/v2-embed-api/license-checks-via-builtbybit/) and [resource dependency rules](https://builtbybit.com/help/resource-creators/your-listing/dependencies).

## Pre-publication checklist

- [ ] Build succeeds on JDK 21 and the GitHub Actions workflow completes.
- [ ] Test all commands, GUIs, crafting, augmented items, persistence and permissions on Paper/Purpur 1.21.11.
- [ ] Test licensing using a real shareable BuiltByBit token and a JAR downloaded through BuiltByBit.
- [ ] Confirm the shipped JAR contains the configured token and the BuiltByBit nonce placeholder is preserved/replaced as expected.
- [ ] Include the plugin JAR, compatible resource pack(s), setup instructions, dependency/DRM disclosure, supported versions, permissions, commands, configuration guide and changelog in the product package.
- [ ] Confirm you own or have redistribution rights for every source asset and the included resource pack.
- [ ] Set the BuiltByBit EULA, price, version filters and dependency fields accurately.
