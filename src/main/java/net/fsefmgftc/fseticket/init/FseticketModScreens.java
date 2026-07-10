package net.fsefmgftc.fseticket.init;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.fsefmgftc.fseticket.client.gui.LocalTicketGUIScreen;
import net.fsefmgftc.fseticket.client.gui.ICGUIScreen;
import net.fsefmgftc.fseticket.client.gui.FSEPassGUIScreen;
import net.fsefmgftc.fseticket.client.gui.ExpTicketGUIScreen;
import net.fsefmgftc.fseticket.FseticketMod;

@EventBusSubscriber(modid = FseticketMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FseticketModScreens {
	private FseticketModScreens() {
	}

	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(FseticketModMenus.LOCAL_TICKET_GUI.get(), LocalTicketGUIScreen::new);
		event.register(FseticketModMenus.EXP_TICKET_GUI.get(), ExpTicketGUIScreen::new);
		event.register(FseticketModMenus.ICGUI.get(), ICGUIScreen::new);
		event.register(FseticketModMenus.FSE_PASS_GUI.get(), FSEPassGUIScreen::new);
	}

	public interface ScreenAccessor {
		void updateMenuState(int elementType, String name, Object elementState);
	}
}
