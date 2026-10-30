package de.jokingsheet.enderchestscanner;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.ChestMenu;
import org.lwjgl.glfw.GLFW;
public final class EnderChestScannerClient implements ClientModInitializer {
    private static KeyMapping openKey;
    private static boolean wasEnderChestOpen;
    @Override public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.enderchestscanner.open", GLFW.GLFW_KEY_RIGHT_SHIFT, "category.enderchestscanner"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) if (client.player != null) client.setScreen(new ScannerScreen());
            boolean open = isEnderChest(client);
            if (open && !wasEnderChestOpen && client.player != null) ScannerData.scan(client.player.containerMenu);
            wasEnderChestOpen = open;
        });
    }
    private static boolean isEnderChest(Minecraft client) {
        if (!(client.screen instanceof AbstractContainerScreen<?> screen)) return false;
        if (!(client.player.containerMenu instanceof ChestMenu)) return false;
        String title = screen.getTitle().getString().toLowerCase(java.util.Locale.ROOT);
        return title.contains("ender chest") || title.contains("endertruhe");
    }
    public static void refresh() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) ScannerData.scan(client.player.containerMenu);
    }
}
