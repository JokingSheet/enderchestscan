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
    private Button sortButton;
    private int scroll;
    private int sortMode;
    private ScannerData.Entry selected;

    public ScannerScreen() { super(Component.literal("ENDER CHEST SCANNER")); }

    @Override protected void init() {
        search = new EditBox(font, width/2-148, 42, 145, 20, Component.literal("Search"));
        search.setHint(Component.literal("Search items..."));
        addRenderableWidget(search);
        sortButton = Button.builder(sortLabel(), b -> {
            sortMode = (sortMode + 1) % 4;
            scroll = 0;
            b.setMessage(sortLabel());
        }).bounds(width/2+2, 42, 90, 20).build();
        addRenderableWidget(sortButton);
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
        g.fill(l,t,l+320,height-22,0xF0181A20); g.fill(l,t,l+320,t+3,0xFFFFC94A);
        g.drawCenteredString(font,"ENDER CHEST SCANNER",width/2,28,0xFFFFD36A);
        g.drawString(font,"Types: "+ScannerData.itemTypeCount()+"   Shulkers: "+ScannerData.shulkerCount(),l+12,68,0xFF72E08B);
        if (ScannerData.unreadableShulkerCount() > 0)
            g.drawString(font,"Shulkers without readable data: "+ScannerData.unreadableShulkerCount(),l+12,81,0xFFFF9A70);
        super.render(g,mx,my,delta);
        List<ScannerData.Entry> list=filtered(); int y=101;
        for(int i=scroll;i<Math.min(list.size(),scroll+7);i++) {
            var e=list.get(i); g.fill(l+10,y-4,l+310,y+20,0xFF292D36);
            g.renderItem(e.displayStack(),l+16,y-2);
            g.drawString(font,e.displayStack().getHoverName(),l+40,y+2,0xFFE8E8E8);
            g.drawString(font,"x"+e.total(),l+270,y+2,0xFF72E08B); y+=27;
        }
        if(selected!=null) {
            g.fill(l+12,height-92,l+308,height-32,0xFF34313A);
            g.drawString(font,selected.displayStack().getHoverName(),l+22,height-82,0xFFFFD36A);
            g.drawString(font,"Total: "+selected.total(),l+22,height-66,0xFF72E08B);
            int yy=height-50;
            for(var e:selected.shulkers().entrySet()) { g.drawString(font,"Shulker slot "+(e.getKey()+1)+": "+e.getValue(),l+22,yy,0xFFE8E8E8); yy+=12; }
        }
        if (list.isEmpty() && ScannerData.shulkerCount() == 0)
            g.drawString(font,"No Ender Chest items captured. Close and reopen the Ender Chest, then press Right Shift.",l+12,112,0xFFFF9A70);
        else if (list.isEmpty() && ScannerData.unreadableShulkerCount() > 0)
            g.drawString(font,"The server has not provided readable Shulker contents to the client.",l+12,112,0xFFFF9A70);
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

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double x = event.x(), y = event.y();
        int l=width/2-160;
        if(x>=l+10&&x<=l+310&&y>=97&&y<height-100) {
            int i=scroll+(int)((y-97)/27); List<ScannerData.Entry> list=filtered();
            if(i>=0&&i<list.size()) selected=list.get(i);
        }
        return super.mouseClicked(event, doubled);
    }

    @Override public boolean isPauseScreen() { return false; }
}