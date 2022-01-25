package cool.avocado.mc.forge.refill.compats.backpacks;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DummyBackpacksInterop implements BackpacksInterop {
    @Override
    public boolean isBackpack(Item item) {
        return false;
    }

    @Override
    public Optional<ItemStack> findItemAndAccumulateBackpacks(Item item, ItemStack backpack, ConcurrentLinkedQueue<ItemStack> backpacks) {
        return Optional.empty();
    }
}