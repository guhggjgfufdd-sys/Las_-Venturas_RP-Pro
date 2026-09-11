# Las Venturas RP — SA-MP Mobile Launcher

Private single-server Android launcher for Las Venturas RP.

## Server

- Address: 142.132.203.47:21299
- Discord/news: https://discord.gg/eZFKQ83ke
- Cache path: /storage/emulated/0/GTA/

The launcher is locked to Las Venturas RP and does not download or expose a public server directory.

## Native support

This branch is based on SA-MP 2.10 and keeps native client support for ARMv7 (armeabi-v7a) and ARM64 (arm64-v8a). It also retains the native MonetLoader library used by compatible scripts and extensions.

## Build

1. Open the GTA-2.10-based Android project in Android Studio.
2. Use JDK 17 and NDK 26.2.11394342.
3. Build the release variant from the las-venturas-rp branch.
4. Place the GTA cache under /storage/emulated/0/GTA/ before launching.

The release build uses the standard Gradle debug signing fallback in this repository. Configure a private release keystore in CI before publishing to a store.

## Notes

The ARM64 and ARMv7 native binaries are part of this branch; changing Gradle ABI filters alone cannot add a new architecture without a matching native client binary.

This project is not affiliated with Rockstar Games, Take-Two Interactive, or the original SA-MP team.
