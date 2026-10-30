package de.enderscanner.scanner;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class ScanResult {
    public record Source(int enderSlot, int amount, boolean available) {}
    public static final class Entry {
        private final Item item;
        private int total;
        private final List<Source> sources = new ArrayList<>();
        Entry(Item item) { this.item = item; }
        public Item item() { return item; }
        public int total() { return total; }
        public List<Source> sources() { return Collections.unmodifiableList(sources); }
        void add(int slot, int amount) {
            total += amount;
            for (int i = 0; i < sources.size(); i++) {
                Source old = sources.get(i);
                if (old.enderSlot() == slot) {
                    sources.set(i, new Source(slot, old.amount() + amount, true));
                    return;
                }
            }
            sources.add(new Source(slot, amount, true));
        }
    }
    private final Map<Item, Entry> entries = new LinkedHashMap<>();
    private int shulkerCount;
    private final List<Integer> unavailableShulkers = new ArrayList<>();

    public void clear() { entries.clear(); shulkerCount = 0; unavailableShulkers.clear(); }
    public void add(ItemStack stack, int sourceSlot) {
        if (stack.isEmpty()) return;
        Entry entry = entries.computeIfAbsent(stack.getItem(), Entry::new);
        entry.add(sourceSlot, stack.getCount());
    }
    public void shulkerFound(int slot, boolean dataAvailable) {
        shulkerCount++;
        if (!dataAvailable) unavailableShulkers.add(slot);
    }
    public Collection<Entry> entries() { return Collections.unmodifiableCollection(entries.values()); }
    public int itemTypeCount() { return entries.size(); }
    public int shulkerCount() { return shulkerCount; }
    public List<Integer> unavailableShulkers() { return Collections.unmodifiableList(unavailableShulkers); }
}
