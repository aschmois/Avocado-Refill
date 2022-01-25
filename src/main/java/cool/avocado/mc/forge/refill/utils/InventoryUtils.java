package cool.avocado.mc.forge.refill.utils;

import cool.avocado.mc.forge.refill.Constants;
import cool.avocado.mc.forge.refill.compats.backpacks.BackpacksInterop;
import cool.avocado.mc.forge.refill.compats.backpacks.DummyBackpacksInterop;
import cool.avocado.mc.forge.refill.compats.curios.CuriosInterop;
import cool.avocado.mc.forge.refill.compats.curios.DummyCuriosInterop;
import cool.avocado.mc.forge.refill.config.Handler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static net.minecraft.world.entity.player.Inventory.INVENTORY_SIZE;

public class InventoryUtils {

    public static BackpacksInterop backpacksInterop = new DummyBackpacksInterop();
    public static CuriosInterop curiosInterop = new DummyCuriosInterop();

    /**
     * A helper to find items in an inventory much easier
     *
     * @param skipSlot the index of the inventory slot to skip, any number outside the range of the container is ignored. i.e. -1
     * @param player   the player
     * @param item     the item to find
     * @return returns the first item stack found. Could be found inside a backpack.
     */
    public static Optional<ItemStack> findItemInInventory(final int skipSlot, final Player player, final Item item) {
        final Inventory inv = player.getInventory();
        final ConcurrentLinkedQueue<ItemStack> backpacks = new ConcurrentLinkedQueue<>();
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            if (slot == skipSlot) continue;
            ItemStack stack = inv.getItem(slot);
            Item stackItem = stack.getItem();
            if (item.equals(stackItem) && itemIsNotDamaged(stack)) {
                return Optional.of(stack);
            }
            if (Handler.GENERAL.lookInBackpacks.get() && backpacksInterop.isBackpack(stackItem)) backpacks.add(stack);
        }
        if (Handler.GENERAL.lookInBackpacks.get()) {
            curiosInterop.addBackpacksFromCuriosSlots(player, backpacks);
            // If we haven't found a matching item, let's look in backpacks
            return getItemFromAnyBackpack(item, backpacks);
        }
        return Optional.empty();
    }

    public static Optional<ItemStack> getItemFromAnyBackpack(final Item item, final ConcurrentLinkedQueue<ItemStack> backpacks) {
        final ItemStack backpack = backpacks.poll();
        if (backpack == null) return Optional.empty(); // no more backpacks (or incepted backpacks) left to search
        Optional<ItemStack> foundItem = backpacksInterop.findItemAndAccumulateBackpacks(item, backpack, backpacks);
        if (foundItem.isPresent()) {
            return foundItem;
        }
        return getItemFromAnyBackpack(item, backpacks);
    }

    public static boolean itemIsNotDamaged(ItemStack itemStack) {
        return itemStack.getTagElement(Constants.ITEM_SAVED_TAG) == null;
    }

    public static void giveOrDropItemStack(Player player, ItemStack itemStack) {
        if (!player.getInventory().add(itemStack)) {
            player.drop(itemStack, false);
        }
    }

    public static boolean isArmor(Item item) {
        return item instanceof ArmorItem armorItem && armorItem.getSlot().getType() == EquipmentSlot.Type.ARMOR;
    }
}
