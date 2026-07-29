package net.fsefmgftc.fseticket.item;

import net.fsefmgftc.fseticket.world.inventory.LocalTicketGUIMenu;
import net.fsefmgftc.fseticket.util.TicketDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;

public class LocalTicketItem extends AbstractTicketItem {
	public LocalTicketItem() {
		super(new Item.Properties().stacksTo(1).fireResistant(), TicketDataUtil.TYPE_LOCAL);
	}

	@Override
	public int getEnchantmentValue() {
		return 1;
	}

	@Override
	protected String getMenuTitle() {
		return "Ticket";
	}

	@Override
	protected AbstractContainerMenu createTicketMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
		return new LocalTicketGUIMenu(id, inventory, buffer);
	}
}
