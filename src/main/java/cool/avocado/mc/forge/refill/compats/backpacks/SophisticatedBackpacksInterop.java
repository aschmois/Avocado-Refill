package cool.avocado.mc.forge.refill.compats.backpacks;

import cool.avocado.mc.forge.refill.utils.InventoryUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.api.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SophisticatedBackpacksInterop implements BackpacksInterop {
    @Override
    public boolean isBackpack(Item item) {
        return item instanceof BackpackItem;
    }

    @Override
    public Optional<ItemStack> findItemAndAccumulateBackpacks(Item item, ItemStack backpack, ConcurrentLinkedQueue<ItemStack> backpacks) {
        return backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance())
                .map(IBackpackWrapper::getInventoryHandler)
                .map(backpackInventoryHandler -> {
                    for (int slot = 0; slot < backpackInventoryHandler.getSlots(); slot++) {
                        ItemStack stack = backpackInventoryHandler.getStackInSlot(slot);
                        Item stackItem = stack.getItem();
                        if (item.equals(stackItem) && InventoryUtils.itemIsNotDamaged(stack)) {
                            return backpackInventoryHandler.extractItem(slot, Math.min(stack.getCount(), new ItemStack(stack.getItem()).getMaxStackSize()), false);
                        }
                        if (stackItem instanceof BackpackItem) backpacks.add(stack);
                    }
                    return null;
                });
    }
}

