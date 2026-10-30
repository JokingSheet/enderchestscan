package de.enderscanner.gui;

import de.enderscanner.scanner.ScanResult;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.Locale;

public final class ScannerScreen extends Screen {
    private final ScanResult result;
    private final Runnable refresh;
    private EditBox search;
    private int scroll;
    private ScanResult.Entry selected;
    private List<ScanResult.Entry> filtered = List.of();
    private static final int GOLD = 0xFFFFC94A;
    private static final int PANEL = 0xF0181A20;
    private static final int ROW = 0xFF242832;

    public ScannerScreen(ScanResult result, Runnable refresh) {
        super(Component.literal("ENDER CHEST SCANNER"));
        this.result = result;
        this.refresh = refresh;
    }

    @Override protected void init() {
        int left = (width - 360) / 2;
        search = new EditBox(font, left + 14, 48, 230, 20, Component.literal("Items suchen..."));
        search.setHint(Component.literal("Item suchen...").withStyle(ChatFormatting.GRAY));
        search.setResponder(s -> { scroll = 0; rebuildFiltered(); });
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal("AKTUALISIEREN"), b -> { refresh.run(); selected = null; rebuildFiltered(); }).bounds(left + 250, 48, 96, 20).build());
        rebuildFiltered();
    }

    private void rebuildFiltered() {
        String q = search == null ? "" : search.getValue().toLowerCase(Locale.ROOT);
        filtered = result.entries().stream().filter(e -> q.isBlank() || e.item().getName(ItemStack.EMPTY).getString().toLowerCase(Locale.ROOT).contains(q)).toList();
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        renderBackground(g, mouseX, mouseY, delta);
        int x = (width - 360) / 2, y = (height - 270) / 2;
        g.fill(x, y, x + 360, y + 270, PANEL);
        g.renderOutline(x, y, 360, 270, 0xFF80622D);
        g.drawCenteredString(font, "ENDER CHEST SCANNER", width / 2, y + 12, GOLD);
        g.drawString(font, "Itemtypen: " + result.itemTypeCount(), x + 14, y + 34, 0xFFE5E7EB);
        g.drawString(font, "Shulker: " + result.shulkerCount(), x + 170, y + 34, 0xFFE5E7EB);
        int listTop = y + 78, listBottom = y + 252;
        g.fill(x + 12, listTop, x + 226, listBottom, 0xFF111319);
        int rowY = listTop + 4;
        for (int i = scroll; i < Math.min(filtered.size(), scroll + 8); i++) {
            ScanResult.Entry entry = filtered.get(i);
            g.fill(x + 16, rowY, x + 222, rowY + 20, entry == selected ? 0xFF514126 : ROW);
            g.renderItem(new ItemStack(entry.item()), x + 19, rowY + 2);
            g.drawString(font, entry.item().getName(ItemStack.EMPTY), x + 42, rowY + 6, 0xFFE8E8E8);
            String amount = "x" + entry.total();
            g.drawString(font, amount, x + 218 - font.width(amount), rowY + 6, 0xFF63D889);
            rowY += 21;
        }
        g.fill(x + 236, listTop, x + 348, listBottom, 0xFF111319);
        if (selected != null) {
            g.drawString(font, selected.item().getName(ItemStack.EMPTY), x + 244, listTop + 8, GOLD);
            g.drawString(font, "Gesamt: " + selected.total(), x + 244, listTop + 24, 0xFF63D889);
            int sy = listTop + 46;
            for (ScanResult.Source source : selected.sources()) {
                String line = source.enderSlot() == 0 ? "Außerhalb Shulker: " + source.amount() : "Shulker Slot " + source.enderSlot() + ": " + source.amount();
                g.drawString(font, line, x + 244, sy, 0xFFE5E7EB);
                sy += 13;
            }
        } else {
            g.drawString(font, "Item auswählen", x + 244, listTop + 10, 0xFFB7B9C0);
        }
        if (!result.unavailableShulkers().isEmpty()) {
            g.drawString(font, "Inhalt nicht verfügbar: " + result.unavailableShulkers().size(), x + 14, y + 257, 0xFFFF8A65);
        }
        super.render(g, mouseX, mouseY, delta);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        int x = (width - 360) / 2, y = (height - 270) / 2;
        int top = y + 82;
        if (mouseX >= x + 16 && mouseX <= x + 222 && mouseY >= top && mouseY < y + 252) {
            int index = scroll + (int)((mouseY - top) / 21);
            if (index >= 0 && index < filtered.size()) selected = filtered.get(index);
            return true;
        }
        return false;
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = Math.max(0, Math.min(Math.max(0, filtered.size() - 8), scroll - (int)Math.signum(verticalAmount)));
        return true;
    }
    @Override public boolean isPauseScreen() { return false; }
}
