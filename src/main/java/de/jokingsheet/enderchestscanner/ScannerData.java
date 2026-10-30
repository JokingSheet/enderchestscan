package de.jokingsheet.enderchestscanner;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.lang.reflect.Method;
import java.util.*;
public final class ScannerData {
    public record Entry(ItemStack displayStack, int total, Map<Integer,Integer> shulkers, int outside) {}
    private static final Map<String, MutableEntry> ITEMS = new LinkedHashMap<>();
    private static int shulkerCount;
    private static int unreadableShulkerCount;
    private static final class MutableEntry {
        ItemStack stack; int total; int outside; final Map<Integer,Integer> shulkers = new LinkedHashMap<>();
        MutableEntry(ItemStack stack) { this.stack = stack.copyWithCount(1); }
    }
    private ScannerData() {}
    public static void scan(AbstractContainerMenu menu) {
        ITEMS.clear(); shulkerCount = 0; unreadableShulkerCount = 0;
        if (menu == null || menu.slots.size() < 27) return;
        for (int slot = 0; slot < 27; slot++) {
            ItemStack stack = menu.slots.get(slot).getItem();
            if (stack.isEmpty()) continue;
            if (isShulker(stack)) {
                shulkerCount++;
                Object component = stack.get(DataComponents.CONTAINER);
                List<ItemStack> contents = readContents(component);
                if (contents == null) unreadableShulkerCount++;
                else for (ItemStack item : contents) add(item, slot, true);
            } else add(stack, -1, false);
        }
    }
    private static boolean isShulker(ItemStack s) {
        return s.is(Items.SHULKER_BOX) || s.is(Items.WHITE_SHULKER_BOX) || s.is(Items.ORANGE_SHULKER_BOX)
            || s.is(Items.MAGENTA_SHULKER_BOX) || s.is(Items.LIGHT_BLUE_SHULKER_BOX) || s.is(Items.YELLOW_SHULKER_BOX)
            || s.is(Items.LIME_SHULKER_BOX) || s.is(Items.PINK_SHULKER_BOX) || s.is(Items.GRAY_SHULKER_BOX)
            || s.is(Items.LIGHT_GRAY_SHULKER_BOX) || s.is(Items.CYAN_SHULKER_BOX) || s.is(Items.PURPLE_SHULKER_BOX)
            || s.is(Items.BLUE_SHULKER_BOX) || s.is(Items.BROWN_SHULKER_BOX) || s.is(Items.GREEN_SHULKER_BOX)
            || s.is(Items.RED_SHULKER_BOX) || s.is(Items.BLACK_SHULKER_BOX);
    }
    private static List<ItemStack> readContents(Object component) {
        if (component == null) return null;
        try {
            Method m = component.getClass().getMethod("nonEmptyItems");
            // The implementation class may not be public even though the method is.
            // Make the method accessible before invoking it.
            if (!m.canAccess(component) && !m.trySetAccessible()) return null;
            Object result = m.invoke(component);
            if (result instanceof java.util.stream.Stream<?> stream)
                return stream.filter(ItemStack.class::isInstance).map(ItemStack.class::cast).toList();
            if (result instanceof Iterable<?> values) {
                List<ItemStack> out = new ArrayList<>();
                for (Object v : values) if (v instanceof ItemStack item) out.add(item);
                return out;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {}
        return null;
    }
    private static void add(ItemStack stack, int slot, boolean inside) {
        if (stack.isEmpty()) return;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        MutableEntry e = ITEMS.computeIfAbsent(id, k -> new MutableEntry(stack));
        e.total += stack.getCount();
        if (inside) e.shulkers.merge(slot, stack.getCount(), Integer::sum); else e.outside += stack.getCount();
    }
    public static List<Entry> entries() {
        return ITEMS.values().stream().map(e -> new Entry(e.stack, e.total, Map.copyOf(e.shulkers), e.outside)).toList();
    }
    public static int itemTypeCount() { return ITEMS.size(); }
    public static int shulkerCount() { return shulkerCount; }
    public static int unreadableShulkerCount() { return unreadableShulkerCount; }
}
