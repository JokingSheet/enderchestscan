package de.jokingsheet.enderchestscanner;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ScannerScreen extends Screen {
    private EditBox search;
    private int scroll;
    private int sortMode;
    private ScannerData.Entry selected;

    public ScannerScreen() { super(Component.literal("ENDER CHEST SCANNER")); }

    @Override protected void init() {
        search = new EditBox(font, width/2-148, 42, 145, 20, Component.literal("Search"));
        search.setHint(Component.literal("Search items..."));
        search.setResponder(value -> scroll = 0);
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(sortLabel(), b -> {
            sortMode = (sortMode + 1) % 4;
            scroll = 0;
            b.setMessage(sortLabel());
        }).bounds(width/2+2, 42, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Refresh"), b -> EnderChestScannerClient.refresh())
            .bounds(width/2+97, 42, 65, 20).build());
    }

    private Component sortLabel() {
        return Component.literal(switch (sortMode) {
            case 0 -> "Count: high";
            case 1 -> "Count: low";
            case 2 -> "Name: A-Z";
            default -> "Name: Z-A";
        });
    }

    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0xC0101018);
        int l=width/2-160, t=22;
        g.fill(l,t,l+320,height-22,0xF0181A20);
        g.fill(l,t,l+320,t+3,0xFFFFC94A);
        g.drawCenteredString(font,"ENDER CHEST SCANNER",width/2,28,0xFFFFD36A);
        List<ScannerData.Entry> list=filtered();
        int totalItems=list.stream().mapToInt(ScannerData.Entry::total).sum();
        g.drawString(font,"Types: "+ScannerData.itemTypeCount()+"  Items: "+totalItems+"  Shulkers: "+ScannerData.shulkerCount(),l+12,68,0xFF72E08B);
        g.drawString(font,"Showing "+(list.isEmpty()?0:scroll+1)+"-"+Math.min(list.size(),scroll+7)+" / "+list.size()+"  (Mouse wheel to scroll)",l+12,83,0xFFB8BEC9);
        if (ScannerData.unreadableShulkerCount() > 0)
            g.drawString(font,"Unreadable shulkers: "+ScannerData.unreadableShulkerCount(),l+12,94,0xFFFF9A70);
        super.render(g,mx,my,delta);

        int y=119;
        for(int i=scroll;i<Math.min(list.size(),scroll+7);i++) {
            var e=list.get(i);
            int rowTop=y-4;
            int color=e==selected?0xFF51452D:0xFF292D36;
            g.fill(l+10,rowTop,l+310,y+20,color);
            g.renderItem(e.displayStack(),l+16,y-2);
            g.drawString(font,e.displayStack().getHoverName(),l+40,y+2,0xFFE8E8E8);
            g.drawString(font,"x"+e.total(),l+270,y+2,0xFF72E08B);
            y+=27;
        }
        if(selected!=null) {
            g.fill(l+12,height-91,l+308,height-32,0xFF34313A);
            g.drawString(font,selected.displayStack().getHoverName(),l+22,height-82,0xFFFFD36A);
            g.drawString(font,"Total: "+selected.total()+"  Outside shulkers: "+selected.outside(),l+22,height-67,0xFF72E08B);
            String locations=selected.shulkers().entrySet().stream()
                .map(e -> "Slot "+(e.getKey()+1)+": "+e.getValue())
                .reduce((a,b) -> a+" | "+b).orElse("No shulker contents");
            g.drawString(font,font.plainSubstrByWidth(locations,280),l+22,height-49,0xFFE8E8E8);
        }
        if (list.isEmpty() && ScannerData.shulkerCount() == 0)
            g.drawString(font,"No items captured yet. Open your Ender Chest first.",l+12,130,0xFFFF9A70);
        else if (list.isEmpty() && ScannerData.unreadableShulkerCount() > 0)
            g.drawString(font,"The server did not send readable Shulker contents.",l+12,130,0xFFFF9A70);
        else if (list.isEmpty())
            g.drawString(font,"No items match your search.",l+12,130,0xFFB8BEC9);
    }

    private List<ScannerData.Entry> filtered() {
        String q=search==null?"":search.getValue().toLowerCase(Locale.ROOT);
        Comparator<ScannerData.Entry> comparator = switch (sortMode) {
            case 0 -> Comparator.comparingInt(ScannerData.Entry::total).reversed()
                .thenComparing(e -> e.displayStack().getHoverName().getString(), String.CASE_INSENSITIVE_ORDER);
            case 1 -> Comparator.comparingInt(ScannerData.Entry::total)
                .thenComparing(e -> e.displayStack().getHoverName().getString(), String.CASE_INSENSITIVE_ORDER);
            case 2 -> Comparator.comparing(e -> e.displayStack().getHoverName().getString(), String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing((ScannerData.Entry e) -> e.displayStack().getHoverName().getString(), String.CASE_INSENSITIVE_ORDER).reversed();
        };
        return ScannerData.entries().stream()
            .filter(e -> e.displayStack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(q)
                || net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.displayStack().getItem()).toString().contains(q))
            .sorted(comparator).toList();
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll=Math.max(0,filtered().size()-7);
        if(verticalAmount<0) scroll=Math.min(maxScroll,scroll+1);
        else if(verticalAmount>0) scroll=Math.max(0,scroll-1);
        return true;
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double x = event.x(), y = event.y();
        int l=width/2-160;
        if(x>=l+10&&x<=l+310&&y>=115&&y<height-100) {
            int i=scroll+(int)((y-115)/27);
            List<ScannerData.Entry> list=filtered();
            if(i>=0&&i<list.size()&&i<scroll+7) selected=list.get(i);
        }
        return super.mouseClicked(event, doubled);
    }

    @Override public boolean isPauseScreen() { return false; }
}