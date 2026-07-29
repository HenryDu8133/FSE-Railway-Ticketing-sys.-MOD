package net.fsefmgftc.fseticket.world.inventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.FriendlyByteBuf;
import net.fsefmgftc.fseticket.init.FseticketModMenus;

public class LocalTicketGUIMenu extends AbstractTicketMenu {
	public LocalTicketGUIMenu(int id, Inventory inv, FriendlyByteBuf buf) {
		super(FseticketModMenus.LOCAL_TICKET_GUI.get(), id, inv, buf);
	}
}
