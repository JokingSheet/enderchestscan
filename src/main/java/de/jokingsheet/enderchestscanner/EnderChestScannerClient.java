package de.jokingsheet.enderchestscanner;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public final class EnderChestScannerClient implements ClientModInitializer {
    private static KeyMapping openKey;
    private static AbstractContainerMenu enderChestMenu;

    @Override
    public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.enderchestscanner.open",
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("enderchestscanner", "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                if (client.player == null || client.screen == null) continue;

                // Only accept the actual Ender Chest screen. Never scan player inventory slots.
                if (!isEnderChestScreen(client)) continue;

                enderChestMenu = client.player.containerMenu;
                ScannerData.scan(enderChestMenu);
                client.setScreen(new ScannerScreen());
            }
        });
    }

    private static boolean isEnderChestScreen(Minecraft client) {
        if (!(client.screen instanceof AbstractContainerScreen<?> screen)) return false;
        if (!(client.player.containerMenu instanceof ChestMenu)) return false;

        String title = screen.getTitle().getString().toLowerCase(Locale.ROOT);
        return title.contains("ender chest")
            || title.contains("endertruhe")
            || title.contains("ender chest");
    }

    public static void refresh() {
        Minecraft client = Minecraft.getInstance();
        // Keep using the menu captured while the Ender Chest was open.
        // If the server/menu changed, do not fall back to the player's inventory.
        if (client.player == null || enderChestMenu == null || client.player.containerMenu != enderChestMenu) {
            ScannerData.clear();
            enderChestMenu = null;
            return;
        }
        ScannerData.scan(enderChestMenu);
    }
}
