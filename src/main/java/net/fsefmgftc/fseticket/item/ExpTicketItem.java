package net.fsefmgftc.fseticket.item;

import net.fsefmgftc.fseticket.world.inventory.ExpTicketGUIMenu;
import net.fsefmgftc.fseticket.util.TicketDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;

public class ExpTicketItem extends AbstractTicketItem {
	public ExpTicketItem() {
		super(new Item.Properties().stacksTo(64).fireResistant(), TicketDataUtil.TYPE_LIMITED_EXPRESS);
	}

	@Override
	public int getEnchantmentValue() {
		return 0;
	}

	@Override
	protected String getMenuTitle() {
		return "Exp Ticket";
	}

	@Override
	protected AbstractContainerMenu createTicketMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
		return new ExpTicketGUIMenu(id, inventory, buffer);
	}
}
