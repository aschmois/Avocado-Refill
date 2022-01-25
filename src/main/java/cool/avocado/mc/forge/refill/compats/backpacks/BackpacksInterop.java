package cool.avocado.mc.forge.refill.compats.backpacks;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface BackpacksInterop {
    boolean isBackpack(Item item);

    Optional<ItemStack> findItemAndAccumulateBackpacks(Item item, ItemStack backpack, ConcurrentLinkedQueue<ItemStack> backpacks);
}