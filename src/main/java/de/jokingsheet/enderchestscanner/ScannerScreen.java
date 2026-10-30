package de.jokingsheet.enderchestscanner;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;
import java.util.List;
import java.util.Locale;
public final class ScannerScreen extends Screen {
    private EditBox search; private int scroll; private ScannerData.Entry selected;
    public ScannerScreen() { super(Component.literal("ENDER CHEST SCANNER")); }
    @Override protected void init() {
        search = new EditBox(font, width/2-115, 42, 230, 20, Component.literal("Search"));
        search.setHint(Component.literal("Search items...")); addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal("Refresh"), b -> EnderChestScannerClient.refresh()).bounds(width/2+122,42,70,20).build());
    }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        // Avoid vanilla's per-frame blur: other client mods may already apply it.
        g.fill(0, 0, width, height, 0xC0101018);
        int l=width/2-160, t=22;
        g.fill(l,t,l+320,height-22,0xF0181A20); g.fill(l,t,l+320,t+3,0xFFFFC94A);
        g.drawCenteredString(font,"ENDER CHEST SCANNER",width/2,28,0xFFFFD36A);
        g.drawString(font,"Types: "+ScannerData.itemTypeCount()+"   Shulkers: "+ScannerData.shulkerCount(),l+12,68,0xFF72E08B);
        super.render(g,mx,my,delta);
        List<ScannerData.Entry> list=filtered(); int y=94;
        for(int i=scroll;i<Math.min(list.size(),scroll+8);i++) {
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
    }
    private List<ScannerData.Entry> filtered() {
        String q=search==null?"":search.getValue().toLowerCase(Locale.ROOT);
        return ScannerData.entries().stream().filter(e -> e.displayStack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(q)
            || net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.displayStack().getItem()).toString().contains(q)).toList();
    }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double x = event.x(), y = event.y();
        int l=width/2-160;
        if(x>=l+10&&x<=l+310&&y>=90&&y<height-100) {
            int i=scroll+(int)((y-90)/27); List<ScannerData.Entry> list=filtered();
            if(i>=0&&i<list.size()) selected=list.get(i);
        }
        return super.mouseClicked(event, doubled);
    }
    @Override public boolean isPauseScreen() { return false; }
}
