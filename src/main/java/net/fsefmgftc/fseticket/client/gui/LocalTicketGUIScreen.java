package net.fsefmgftc.fseticket.client.gui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.fsefmgftc.fseticket.world.inventory.LocalTicketGUIMenu;

public class LocalTicketGUIScreen extends AbstractTicketScreen<LocalTicketGUIMenu> {
	private static final TicketScreenLayout LAYOUT = new TicketScreenLayout(-49, -76, -14);

	public LocalTicketGUIScreen(LocalTicketGUIMenu c, Inventory inv, Component text) {
		super(c, inv, text);
	}

	@Override
	protected String getTranslationPrefix() {
		return "gui.fseticket.local_ticket_gui.";
	}

	@Override
	protected TicketScreenLayout getLayout() {
		return LAYOUT;
	}
}
