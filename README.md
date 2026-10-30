# EnderChest Scanner
Client-only Fabric mod for Minecraft 1.21.11 (Java 21).

GitHub Actions builds the remapped mod JAR on each push to main. Download it from Actions → Build Fabric mod → Artifacts.

Install Fabric Loader for Minecraft 1.21.11 and Fabric API, then place the downloaded JAR in the client's mods folder.

Safety: only the 27 locally visible container slots are inspected. No custom packets are sent and no items are moved or modified. Shulker data is used only when present in the received item component; unavailable contents are not inferred.
