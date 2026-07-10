package net.fsefmgftc.fseticket.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.bus.api.IEventBus;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.Minecraft;

import net.fsefmgftc.fseticket.world.inventory.LocalTicketGUIMenu;
import net.fsefmgftc.fseticket.world.inventory.ICGUIMenu;
import net.fsefmgftc.fseticket.world.inventory.FSEPassGUIMenu;
import net.fsefmgftc.fseticket.world.inventory.ExpTicketGUIMenu;
import net.fsefmgftc.fseticket.network.MenuStateUpdateMessage;
import net.fsefmgftc.fseticket.FseticketMod;

import java.util.Map;

public final class FseticketModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, FseticketMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<LocalTicketGUIMenu>> LOCAL_TICKET_GUI = REGISTRY.register("local_ticket_gui", () -> IMenuTypeExtension.create(LocalTicketGUIMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<ExpTicketGUIMenu>> EXP_TICKET_GUI = REGISTRY.register("exp_ticket_gui", () -> IMenuTypeExtension.create(ExpTicketGUIMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<ICGUIMenu>> ICGUI = REGISTRY.register("icgui", () -> IMenuTypeExtension.create(ICGUIMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<FSEPassGUIMenu>> FSE_PASS_GUI = REGISTRY.register("fse_pass_gui", () -> IMenuTypeExtension.create(FSEPassGUIMenu::new));

	private FseticketModMenus() {
	}

	public static void register(IEventBus eventBus) {
		REGISTRY.register(eventBus);
	}

	public interface MenuAccessor {
		Map<String, Object> getMenuState();

		Map<Integer, Slot> getSlots();

		default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
			getMenuState().put(elementType + ":" + name, elementState);
			if (player instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, new MenuStateUpdateMessage(elementType, name, elementState));
			} else if (player.level().isClientSide) {
				if (Minecraft.getInstance().screen instanceof FseticketModScreens.ScreenAccessor accessor && needClientUpdate)
					accessor.updateMenuState(elementType, name, elementState);
				PacketDistributor.sendToServer(new MenuStateUpdateMessage(elementType, name, elementState));
			}
		}

		default <T> T getMenuState(int elementType, String name, T defaultValue) {
			try {
				return (T) getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
			} catch (ClassCastException e) {
				return defaultValue;
			}
		}
	}
}
