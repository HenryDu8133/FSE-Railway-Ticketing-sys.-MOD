package net.fsefmgftc.fseticket.world.inventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.FriendlyByteBuf;
import net.fsefmgftc.fseticket.init.FseticketModMenus;

public class ExpTicketGUIMenu extends AbstractTicketMenu {
	public ExpTicketGUIMenu(int id, Inventory inv, FriendlyByteBuf buf) {
		super(FseticketModMenus.EXP_TICKET_GUI.get(), id, inv, buf);
	}
}
