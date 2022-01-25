package cool.avocado.mc.forge.refill.compats.curios;

import cool.avocado.mc.forge.refill.utils.InventoryUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RealCuriosInterop implements CuriosInterop {
    @Override
    public void addBackpacksFromCuriosSlots(Player player, ConcurrentLinkedQueue<ItemStack> backpacks) {
        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            ICurioStacksHandler stacksHandler = handler.getCurios().get(SlotTypePreset.BACK.getIdentifier());
            if (stacksHandler != null) {
                IDynamicStackHandler stacks = stacksHandler.getStacks();

                for (int slot = 0; slot < stacks.getSlots(); slot++) {
                    ItemStack stack = stacks.getStackInSlot(slot);
                    if (InventoryUtils.backpacksInterop.isBackpack(stack.getItem())) backpacks.add(stack);
                }
            }
        });
    }
}
