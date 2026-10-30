package de.enderscanner.scanner;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;

import java.util.List;

public final class EnderChestScanner {
    private final ScanResult result = new ScanResult();
    public ScanResult result() { return result; }

    /** Scans a snapshot of the 27 synchronized ender-chest slots; never mutates any stack. */
    public void scan(List<ItemStack> slots) {
        result.clear();
        int limit = Math.min(27, slots.size());
        for (int slot = 0; slot < limit; slot++) {
            ItemStack stack = slots.get(slot);
            if (stack == null || stack.isEmpty()) continue;
            Item item = stack.getItem();
            if (Block.byItem(item) instanceof ShulkerBoxBlock) {
                ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
                boolean available = contents != null;
                result.shulkerFound(slot + 1, available);
                if (contents != null) {
                    for (ItemStack inner : contents.nonEmptyItems()) {
                        if (inner != null && !inner.isEmpty()) result.add(inner, slot + 1);
                    }
                }
            } else {
                result.add(stack, 0);
            }
        }
    }
}
