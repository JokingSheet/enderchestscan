package de.jokingsheet.enderchestscanner;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.lwjgl.glfw.GLFW;

public final class EnderChestScannerClient implements ClientModInitializer {
    private static KeyMapping openKey;
    private static AbstractContainerMenu scannedMenu;

    @Override
    public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.enderchestscanner.open",
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("enderchestscanner", "main"))
        ));

        // This command always opens the last captured list. It does not require
        // the container to still be open (chat can replace the visible screen).
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("ecscan").executes(context -> {
                Minecraft client = Minecraft.getInstance();
                if (client.player == null) return 0;
                if (isContainerScreen(client)) {
                    captureOpenContainer(client);
                }
                client.setScreen(new ScannerScreen());
                return 1;
            }))
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Capture container contents automatically while a container GUI
            // is open. This also supports server menus that are not vanilla ChestMenu.
            if (isContainerScreen(client)) {
                captureOpenContainer(client);
            }

            while (openKey.consumeClick()) {
                if (isContainerScreen(client)) {
                    captureOpenContainer(client);
                }
                if (scannedMenu == null) {
                    client.player.displayClientMessage(
                        Component.literal("Noch keine Ender-Chest-Daten erfasst. Öffne zuerst deine Ender Chest."),
                        true
                    );
                    continue;
                }
                client.setScreen(new ScannerScreen());
            }
        });
    }

    private static boolean isContainerScreen(Minecraft client) {
        return client.screen instanceof AbstractContainerScreen<?>
            && client.player != null
            && client.player.containerMenu != client.player.inventoryMenu
            && client.player.containerMenu.slots.size() >= 63;
    }

    private static void captureOpenContainer(Minecraft client) {
        if (client.player == null) return;
        AbstractContainerMenu menu = client.player.containerMenu;
        if (menu == client.player.inventoryMenu || menu.slots.size() < 63) return;
        scannedMenu = menu;
        ScannerData.scan(menu);
    }

    public static void refresh() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || scannedMenu == null) {
            ScannerData.clear();
            return;
        }
        // If the captured menu is still active, refresh it. Otherwise retain
        // the last automatic snapshot instead of wiping the GUI.
        if (client.player.containerMenu == scannedMenu) {
            ScannerData.scan(scannedMenu);
        }
    }
}
