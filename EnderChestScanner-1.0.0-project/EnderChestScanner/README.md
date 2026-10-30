# EnderChest Scanner

Client-only Fabric mod for Minecraft Java 1.21.11 / Java 21.

## Features
- Right Shift opens the scanner (rebindable in Options → Controls → Miscellaneous).
- Scans the 27 client-visible ender chest slots when the Ender Chest screen opens.
- Reads `minecraft:container` item component data from shulker stacks only when present on the client.
- Does not send custom packets, access server storage, or mutate/move item stacks.
- Search, totals, shulker slot attribution, and refresh button.

## Build
Run `./gradlew build` (Linux/macOS) or `gradlew.bat build` (Windows), with JDK 21 installed. Output: `build/libs/enderchest-scanner-1.0.0.jar`.

## Install
Install Fabric Loader for Minecraft 1.21.11 and Fabric API for 1.21.11. Copy the JAR into `.minecraft/mods` and launch the Fabric profile. The server does not need the mod.

## Important limitations
The client can only index shulker contents that are actually synchronized as item component data. Missing data is not guessed or fetched. The scanner deliberately does not inspect other players' inventories or unopened containers.
