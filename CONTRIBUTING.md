# Contributing

Thanks for considering a contribution to InfuseSMP-S3.

## Before you start

- Check existing issues and pull requests before starting substantial work.
- For larger changes, open an issue first so the approach can be discussed.
- Keep changes focused and avoid unrelated formatting or refactoring.

## Development setup

- JDK 21
- Maven
- Paper/Purpur 1.21.11 for in-game testing

Build locally with:

```bash
mvn --batch-mode clean package
```

## Pull requests

- Target the `develop` branch for normal development changes.
- Keep the project compiling and include tests or reproducible test steps where practical.
- Preserve existing Skript behavior unless the change intentionally fixes or changes it.
- Keep player-facing text and settings consistent with the existing configuration approach.
- Document new commands, permissions, configuration keys, and optional dependencies.
- Do not commit server worlds, local IDE files, build output, secrets, or personal data.
- Include a clear summary and the Minecraft/server versions you tested.

## Bug reports

Include:

- Plugin version and build source
- Minecraft version and Paper/Purpur build
- Java version
- Steps to reproduce
- Expected and actual behavior
- Relevant configuration and complete error logs (remove tokens or private information)
