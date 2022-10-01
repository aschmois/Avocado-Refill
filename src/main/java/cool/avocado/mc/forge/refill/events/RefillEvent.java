package cool.avocado.mc.forge.refill.events;

import cool.avocado.mc.forge.refill.logger.Log;
import cool.avocado.mc.forge.refill.utils.InventoryUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

@EventBusSubscriber
public class RefillEvent {

    public static final ConcurrentLinkedQueue<Interaction> queue = new ConcurrentLinkedQueue<>();

    public static class Interaction {

        @Nonnull
        private final Player player;
        @Nonnull
        private final ItemStack itemStack;
        @Nullable
        private final InteractionHand interactionHand;
        @Nullable
        private final EquipmentSlot equipmentSlot;

        public Interaction(@Nonnull Player player, @Nonnull ItemStack itemStack, @Nullable InteractionHand interactionHand) {
            this.player = player;
            this.itemStack = itemStack;
            this.interactionHand = interactionHand;
            this.equipmentSlot = null;
        }

        public Interaction(@Nonnull Player player, @Nonnull ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot) {
            this.player = player;
            this.itemStack = itemStack;
            this.interactionHand = null;
            this.equipmentSlot = equipmentSlot;
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent e) {
        if (getLevelOnServer(e.level) == null || e.phase.equals(TickEvent.Phase.START))
            return;

        Interaction curr = queue.poll();
        if (curr != null && !curr.itemStack.isEmpty()) {
            Log.debug("WorldTick: Refilling");
            if (curr.interactionHand != null) {
                if (curr.player.getItemInHand(curr.interactionHand).isEmpty()) {
                    Log.debug("WorldTick: Replaced " + curr.interactionHand + " with " + curr.itemStack);
                    curr.player.setItemInHand(curr.interactionHand, curr.itemStack);
                } else {
                    Log.debug("WorldTick: " + curr.interactionHand + " was not free. Adding " + curr.itemStack + " to inventory.");
                    InventoryUtils.giveOrDropItemStack(curr.player, curr.itemStack);
                }
            } else if (curr.equipmentSlot != null) {
                if (curr.player.getItemBySlot(curr.equipmentSlot).isEmpty()) {
                    Log.debug("WorldTick: Replaced " + curr.equipmentSlot + " with " + curr.itemStack);
                    curr.player.setItemSlot(curr.equipmentSlot, curr.itemStack);
                } else {
                    Log.debug("WorldTick: " + curr.equipmentSlot + " was not free. Adding " + curr.itemStack + " to inventory.");
                    InventoryUtils.giveOrDropItemStack(curr.player, curr.itemStack);
                }
            }
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void onEntityPlace(BlockEvent.EntityPlaceEvent e) {
        final Player player;
        if (getLevelOnServer(e.getLevel()) == null || (player = getSurvivalPlayer(e.getEntity())) == null)
            return;

        final Inventory inv = player.getInventory();

        final Item eventItem = e.getPlacedBlock().getBlock().asItem();
        ItemStack handStack = player.getMainHandItem();
        Item handItem = handStack.getItem();
        Log.debug("onEntityPlace: Placed " + eventItem);
        final InteractionHand hand;
        if (handItem.equals(eventItem)) {
            hand = InteractionHand.MAIN_HAND;
        } else {
            hand = InteractionHand.OFF_HAND;
            handStack = player.getOffhandItem();
            handItem = handStack.getItem();
            if (!handItem.equals(eventItem)) {
                return;
            }
        }
        Log.debug("onEntityPlace: " + eventItem + " was placed with " + hand + ". " + handStack.getCount() + " placed hand before placing.");

        if (handStack.getCount() > 1) // We can only replace this item if it will be removed in the next tick (or somehow has already been removed, though this should be impossible)
            return;

        final Optional<ItemStack> foundItemOpt = InventoryUtils.findItemInInventory(inv.selected, player, eventItem);
        if (foundItemOpt.isPresent()) {
            ItemStack foundItem = foundItemOpt.get();
            Log.debug("onEntityPlace: found stack of " + foundItem);
            if (foundItem.getCount() < 2) {
                Log.debug("onEntityPlace: delaying the refill of " + foundItem + " into " + hand + " slot.");
                queue.add(new Interaction(player, foundItem.copy(), hand));
                foundItem.setCount(0);
            } else {
                Log.debug("onEntityPlace: instantly refilled " + foundItem);
                player.setItemInHand(hand, foundItem.copy());
                foundItem.setCount(0);
            }
        } else {
            Log.debug("onEntityPlace: did not find any " + eventItem + " in player inventory.");
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void onItemUsed(LivingEntityUseItemEvent.Finish e) {
        final Player player;
        if ((player = getSurvivalPlayer(e.getEntity())) == null || getLevelOnServer(player.getCommandSenderWorld()) == null)
            return;

        final Inventory inv = player.getInventory();

        final InteractionHand hand = player.getUsedItemHand();
        final ItemStack resultStack = e.getResultStack();
        final ItemStack eventItemStack = e.getItem();
        Log.debug("onItemUsed: " + eventItemStack + " was used using the " + hand + " slot, resulting in " + resultStack + ".");

        // We should only replace this item if the result is 0 (like for food items) or the used item has max stack size of 1 (like for milk or potions)
        if (resultStack.getCount() != 0 && eventItemStack.getMaxStackSize() != 1)
            return;

        final ItemStack resultStackCopy = resultStack.copy();
        final Item eventItem = eventItemStack.getItem();
        final Optional<ItemStack> foundItemOpt = InventoryUtils.findItemInInventory(inv.selected, player, eventItem);
        if (foundItemOpt.isPresent()) {
            ItemStack foundItem = foundItemOpt.get();
            Log.debug("onItemUsed: found stack of " + foundItem);
            if (!resultStackCopy.isEmpty()) {
                Log.debug("onItemUsed: placing " + resultStack + " back into the inventory from the " + hand + " slot.");
                InventoryUtils.giveOrDropItemStack(player, resultStackCopy);
                e.setResultStack(ItemStack.EMPTY);
            }
            if (foundItem.getCount() < 2) {
                Log.debug("onItemUsed: delaying the refill of " + foundItem + " into " + hand + " slot.");
                queue.add(new Interaction(player, foundItem.copy(), hand));
                foundItem.setCount(0);
            } else {
                Log.debug("onItemUsed: instantly refilled " + foundItem + " with the help of forge's result stack.");
                e.setResultStack(foundItem.copy());
                foundItem.setCount(0);
            }
        } else {
            Log.debug("onItemUsed: did not find any " + eventItem + " in player inventory.");
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void onItemToss(ItemTossEvent e) {
        final Player player;
        if ((player = getSurvivalPlayer(e.getPlayer())) == null || getLevelOnServer(player.getCommandSenderWorld()) == null)
            return;

        final Inventory inv = player.getInventory();

        final ItemStack eventItemStack = e.getEntity().getItem();
        final Item eventItem = eventItemStack.getItem();
        final ItemStack handStack = player.getMainHandItem();
        final InteractionHand hand = InteractionHand.MAIN_HAND; // You can't toss items from the offhand
        Log.debug("onItemToss: " + eventItemStack + " was tossed with " + hand + ". " + handStack.getCount() + " on placed hand before toss.");
        final AbstractContainerMenu menu = player.containerMenu;
        if (handStack.getCount() > 0 || !menu.getCarried().isEmpty()) {
            return;
        }

        final Optional<ItemStack> foundItemOpt = InventoryUtils.findItemInInventory(inv.selected, player, eventItem);
        if (foundItemOpt.isPresent()) {
            ItemStack foundItem = foundItemOpt.get();
            Log.debug("onItemToss: found stack of " + foundItem);
            if (foundItem.getCount() < 2) {
                Log.debug("onItemToss: delaying the refill of " + foundItem + " into " + hand + " slot.");
                queue.add(new Interaction(player, foundItem.copy(), hand));
                foundItem.setCount(0);
            } else {
                Log.debug("onItemToss: instantly refilled " + foundItem + " into slot #" + inv.selected);
                player.setItemInHand(hand, foundItem.copy());
                foundItem.setCount(0);
            }
        } else {
            Log.debug("onItemToss: did not find any " + eventItem + " in player inventory.");
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem e) {
        final Player player;
        if ((player = getSurvivalPlayer(e.getEntity())) == null || getLevelOnServer(e.getLevel()) == null)
            return;

        final Inventory inv = player.getInventory();

        final ItemStack eventItemStack = e.getItemStack();
        final Item eventItem = eventItemStack.getItem();
        final InteractionHand eventHand = e.getHand();

        Log.debug("onRightClickItem: Right clicked " + eventItemStack + " with the " + eventHand + " slot");

        if (eventItemStack.getCount() > 1) {
            return;
        }

        if (!(eventItem instanceof EggItem)
                && !(eventItem instanceof EnderEyeItem)
                && !(eventItem instanceof EnderpearlItem)
                && !(eventItem instanceof FireChargeItem)
                && !(eventItem instanceof FireworkRocketItem)
                && !(eventItem instanceof BoneMealItem)
                && !(eventItem instanceof ThrowablePotionItem)) {
            return;
        }

        final Optional<ItemStack> foundItemOpt = InventoryUtils.findItemInInventory(inv.selected, player, eventItem);
        if (foundItemOpt.isPresent()) {
            ItemStack foundItem = foundItemOpt.get();
            Log.debug("onRightClickItem: found stack of " + foundItem);
            Log.debug("onRightClickItem: delaying the refill of " + foundItem + " into " + eventHand + " slot.");
            queue.add(new Interaction(player, foundItem.copy(), eventHand));
            foundItem.setCount(0);
        } else {
            Log.debug("onRightClickItem: did not find any " + eventItem + " in player inventory.");
        }
    }

    @Nullable
    public static Level getLevelOnServer(LevelAccessor world) {
        if (world.isClientSide()) {
            return null;
        }
        if (world instanceof Level level) {
            return level;
        }
        return null;
    }

    @Nullable
    public static Player getSurvivalPlayer(Entity entity) {
        if (!(entity instanceof Player player)) {
            return null;
        }
        if (player.isCreative()) {
            return null;
        }
        return player;
    }

}