package net.fsefmgftc.fseticket.client.gui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.fsefmgftc.fseticket.world.inventory.ExpTicketGUIMenu;

public class ExpTicketGUIScreen extends AbstractTicketScreen<ExpTicketGUIMenu> {
	private static final TicketScreenLayout LAYOUT = new TicketScreenLayout(-22, -75, -27);

	public ExpTicketGUIScreen(ExpTicketGUIMenu c, Inventory inv, Component text) {
		super(c, inv, text);
	}

	@Override
	protected String getTranslationPrefix() {
		return "gui.fseticket.exp_ticket_gui.";
	}

	@Override
	protected TicketScreenLayout getLayout() {
		return LAYOUT;
	}
}
