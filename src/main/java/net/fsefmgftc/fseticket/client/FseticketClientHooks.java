package net.fsefmgftc.fseticket.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.fsefmgftc.fseticket.FseticketMod;
import net.fsefmgftc.fseticket.init.FseticketModMenus;
import net.fsefmgftc.fseticket.network.MenuStateUpdateMessage;

@EventBusSubscriber(modid = FseticketMod.MODID, value = Dist.CLIENT)
public final class FseticketClientHooks {
	private FseticketClientHooks() {
	}

	public static void updateMenuScreen(int elementType, String name, Object elementState, boolean needClientUpdate) {
		if (needClientUpdate && Minecraft.getInstance().screen instanceof FseticketModMenus.ScreenAccessor accessor) {
			accessor.updateMenuState(elementType, name, elementState);
		}
	}

	public static void handleClientboundMenuState(MenuStateUpdateMessage message) {
		if (Minecraft.getInstance().screen instanceof FseticketModMenus.ScreenAccessor accessor) {
			accessor.updateMenuState(message.elementType(), message.name(), message.elementState());
		}
	}
}
