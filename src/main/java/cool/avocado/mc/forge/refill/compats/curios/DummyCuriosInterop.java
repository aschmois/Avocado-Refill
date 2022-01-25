package cool.avocado.mc.forge.refill.compats.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DummyCuriosInterop implements CuriosInterop {
    @Override
    public void addBackpacksFromCuriosSlots(Player player, ConcurrentLinkedQueue<ItemStack> backpacks) {
        // no op
    }
}