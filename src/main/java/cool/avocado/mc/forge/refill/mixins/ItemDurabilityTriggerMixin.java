package cool.avocado.mc.forge.refill.mixins;

import cool.avocado.mc.forge.refill.config.Handler;
import cool.avocado.mc.forge.refill.events.RefillEvent;
import cool.avocado.mc.forge.refill.logger.Log;
import cool.avocado.mc.forge.refill.utils.InventoryUtils;
import net.minecraft.advancements.critereon.ItemDurabilityTrigger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static cool.avocado.mc.forge.refill.Constants.ITEM_SAVED_TAG;

@Mixin(ItemDurabilityTrigger.class)
public class ItemDurabilityTriggerMixin {

    @Inject(method = "trigger", at = @At(value = "HEAD"), cancellable = true)
    public void triggerItemDurabilityEventBefore(ServerPlayer player, ItemStack itemStack, int damageDealt, CallbackInfo ci) {
        int dealing = damageDealt - itemStack.getDamageValue();
        Item item = itemStack.getItem();
        if ((InventoryUtils.isArmor(item) && Handler.GENERAL.protectArmor.get())
                || (!InventoryUtils.isArmor(item) && Handler.GENERAL.protectTools.get())) {
            Log.debug("triggerItemDurabilityEventBefore: Dealing " + dealing + " damage to " + itemStack + " with player: " + player + " item_saved tag: " + itemStack.getTagElement(ITEM_SAVED_TAG));
            if (damageDealt >= itemStack.getMaxDamage()) {
                CompoundTag tag = itemStack.getTagElement(ITEM_SAVED_TAG);
                if (tag == null) {
                    // TODO: Curios support
                    itemStack.setDamageValue(damageDealt - dealing);
                    itemStack.getOrCreateTagElement(ITEM_SAVED_TAG);
                    ItemStack itemStackCopy = itemStack.copy();
                    InventoryUtils.giveOrDropItemStack(player, itemStack);
                    triggerItemDurabilityEventAfter(player, itemStackCopy, damageDealt, ci);
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "trigger", at = @At(value = "RETURN"))
    public void triggerItemDurabilityEventAfter(final ServerPlayer player, final ItemStack itemStack, final int damageDealt, CallbackInfo ci) {
        int dealt = damageDealt - itemStack.getDamageValue();
        Log.debug("triggerItemDurabilityEventAfter: Dealt " + dealt + " damage to " + itemStack + " with player: " + player + " item_saved tag: " + itemStack.getTagElement(ITEM_SAVED_TAG));
        final Item item = itemStack.getItem();
        final Inventory inv = player.getInventory();

        if (itemStack.getMaxStackSize() != 1) {
            return;
        }

        if (damageDealt >= itemStack.getMaxDamage()) {
            Log.debug("triggerItemDurabilityEventAfter: " + itemStack + " was destroyed.");
            final Optional<ItemStack> foundItemOpt = InventoryUtils.findItemInInventory(InventoryUtils.isArmor(item) ? -1 : inv.selected, player, item);
            if (foundItemOpt.isPresent()) {
                ItemStack foundItem = foundItemOpt.get();
                Log.debug("triggerItemDurabilityEventAfter: found stack of " + foundItem);
                // TODO: Curios support
                if (InventoryUtils.isArmor(item)) {
                    assert item instanceof ArmorItem;
                    ArmorItem armorItem = (ArmorItem) item;
                    Log.debug("triggerItemDurabilityEventAfter: delaying the refill of " + foundItem + " into " + armorItem.getSlot() + " slot.");
                    RefillEvent.queue.add(new RefillEvent.Interaction(player, foundItem.copy(), armorItem.getSlot()));
                } else {
                    Log.debug("triggerItemDurabilityEventAfter: delaying the refill of " + foundItem + " into " + InteractionHand.MAIN_HAND + " slot.");
                    RefillEvent.queue.add(new RefillEvent.Interaction(player, foundItem.copy(), InteractionHand.MAIN_HAND));
                }
                foundItem.setCount(0);
            } else {
                Log.debug("triggerItemDurabilityEventAfter: did not find any " + item + " in player inventory.");
            }
        }
    }
}
