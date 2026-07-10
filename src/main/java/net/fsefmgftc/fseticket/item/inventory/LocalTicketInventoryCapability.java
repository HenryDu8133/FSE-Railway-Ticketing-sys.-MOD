package net.fsefmgftc.fseticket.item.inventory;

import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;

import net.fsefmgftc.fseticket.world.inventory.LocalTicketGUIMenu;
import net.fsefmgftc.fseticket.init.FseticketModItems;

import javax.annotation.Nonnull;

@EventBusSubscriber
public class LocalTicketInventoryCapability extends ComponentItemHandler {
	@SubscribeEvent
	public static void onItemDropped(ItemTossEvent event) {
		if (event.getEntity().getItem().getItem() == FseticketModItems.LOCAL_TICKET.get()) {
			Player player = event.getPlayer();
			if (player.containerMenu instanceof LocalTicketGUIMenu)
				player.closeContainer();
		}
	}

	public LocalTicketInventoryCapability(MutableDataComponentHolder parent) {
		super(parent, DataComponents.CONTAINER, 9);
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		return stack.getItem() != FseticketModItems.LOCAL_TICKET.get();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return super.getStackInSlot(slot).copy();
	}
}