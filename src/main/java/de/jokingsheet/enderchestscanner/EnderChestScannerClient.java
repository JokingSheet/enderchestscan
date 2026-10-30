package de.jokingsheet.enderchestscanner;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import org.lwjgl.glfw.GLFW;

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

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("ecscan").executes(context -> {
                Minecraft client = Minecraft.getInstance();
                if (client.player == null || !(client.player.containerMenu instanceof ChestMenu chestMenu)
                    || chestMenu.getRowCount() != 3) {
                    if (client.player != null) {
                        client.player.displayClientMessage(
                            Component.literal("Open your Ender Chest first, then run /ecscan in chat."),
                            false
                        );
                    }
                    return 0;
                }

                enderChestMenu = client.player.containerMenu;
                ScannerData.scan(enderChestMenu);
                client.setScreen(new ScannerScreen());
                return 1;
            }))
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                if (client.player == null) continue;

                if (!isEnderChestScreen(client)) {
                    client.player.displayClientMessage(
                        Component.literal("Open your Ender Chest first, then press Right Shift."),
                        true
                    );
                    continue;
                }

                enderChestMenu = client.player.containerMenu;
                ScannerData.scan(enderChestMenu);
                client.setScreen(new ScannerScreen());
            }
        });
    }

    private static boolean isEnderChestScreen(Minecraft client) {
        if (client.player == null || !(client.screen instanceof AbstractContainerScreen<?>)) return false;
        if (!(client.player.containerMenu instanceof ChestMenu chestMenu)) return false;

        // Ender chests use the vanilla 3-row chest menu. Do not rely on the
        // visible title: servers often replace/localize it with custom text.
        return chestMenu.getRowCount() == 3;
    }

    public static void refresh() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || enderChestMenu == null || client.player.containerMenu != enderChestMenu) {
            ScannerData.clear();
            enderChestMenu = null;
            return;
        }
        ScannerData.scan(enderChestMenu);
    }
}
