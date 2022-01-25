package cool.avocado.mc.forge.refill.events;

import cool.avocado.mc.forge.refill.logger.Log;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import static cool.avocado.mc.forge.refill.Constants.ITEM_SAVED_TAG;

@EventBusSubscriber
public class RepairEvent {
    @SubscribeEvent
    public void onRepairEvent(AnvilUpdateEvent e) {
        ItemStack left = e.getLeft();
        ItemStack right = e.getRight();
        left.removeTagKey(ITEM_SAVED_TAG);
        Log.debug("Cleared any possible " + ITEM_SAVED_TAG + " tag from " + left);
        right.removeTagKey(ITEM_SAVED_TAG);
        Log.debug("Cleared any possible " + ITEM_SAVED_TAG + " tag from " + right);
    }
}