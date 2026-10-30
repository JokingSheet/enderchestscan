package de.enderscanner.client;

import de.enderscanner.gui.ScannerScreen;
import de.enderscanner.scanner.EnderChestScanner;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class EnderChestScannerClient implements ClientModInitializer {
    private static final EnderChestScanner SCANNER = new EnderChestScanner();
    private static KeyMapping openKey;
    private static boolean wasEnderChestOpen;
    private static AbstractContainerMenu activeEnderChestMenu;
    private static int refreshTicker;

    @Override public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.enderchest_scanner.open", GLFW.GLFW_KEY_RIGHT_SHIFT,
                Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) { wasEnderChestOpen = false; return; }
            boolean open = client.screen instanceof ContainerScreen<?> containerScreen
                    && containerScreen.getTitle().equals(Component.translatable("container.enderchest"));
            if (open) {
                activeEnderChestMenu = ((ContainerScreen<?>) client.screen).getMenu();
                if (!wasEnderChestOpen) scanOpenEnderChest(client);
            }
            if ((open || client.screen instanceof ScannerScreen) && activeEnderChestMenu != null) {
                if (++refreshTicker >= 10) { scanStoredMenu(); refreshTicker = 0; }
            }
            wasEnderChestOpen = open;
            while (openKey.consumeClick()) {
                if (open) activeEnderChestMenu = ((ContainerScreen<?>) client.screen).getMenu();
                client.setScreen(new ScannerScreen(SCANNER.result(), () -> scanStoredMenu()));
            }
        });
    }

    private static void scanOpenEnderChest(Minecraft client) {
        if (client.screen instanceof ContainerScreen<?> screen
                && screen.getTitle().equals(Component.translatable("container.enderchest"))) {
            activeEnderChestMenu = screen.getMenu();
        }
        scanStoredMenu();
    }

    private static void scanStoredMenu() {
        if (activeEnderChestMenu == null) return;
        List<ItemStack> snapshot = new ArrayList<>(27);
        for (int i = 0; i < 27; i++) {
            if (i < activeEnderChestMenu.slots.size()) snapshot.add(activeEnderChestMenu.slots.get(i).getItem().copy());
            else snapshot.add(ItemStack.EMPTY);
        }
        SCANNER.scan(snapshot);
    }
}
