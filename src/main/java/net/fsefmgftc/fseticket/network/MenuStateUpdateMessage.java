package net.fsefmgftc.fseticket.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;

import net.fsefmgftc.fseticket.init.FseticketModMenus;
import net.fsefmgftc.fseticket.FseticketMod;

@EventBusSubscriber
public record MenuStateUpdateMessage(int elementType, String name, Object elementState) implements CustomPacketPayload {
	public static final Type<MenuStateUpdateMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FseticketMod.MODID, "guistate_update"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MenuStateUpdateMessage> STREAM_CODEC = StreamCodec.of(MenuStateUpdateMessage::write, MenuStateUpdateMessage::read);

	public static void write(FriendlyByteBuf buffer, MenuStateUpdateMessage message) {
		buffer.writeInt(message.elementType);
		buffer.writeUtf(message.name);
		if (message.elementType == 0) {
			buffer.writeUtf((String) message.elementState);
		} else if (message.elementType == 1) {
			buffer.writeBoolean((boolean) message.elementState);
		} else if (message.elementType == 2 && message.elementState instanceof Number n) {
			buffer.writeDouble(n.doubleValue());
		}
	}

	public static MenuStateUpdateMessage read(FriendlyByteBuf buffer) {
		int elementType = buffer.readInt();
		String name = buffer.readUtf();
		Object elementState = null;
		if (elementType == 0) {
			elementState = buffer.readUtf();
		} else if (elementType == 1) {
			elementState = buffer.readBoolean();
		} else if (elementType == 2) {
			elementState = buffer.readDouble();
		}
		return new MenuStateUpdateMessage(elementType, name, elementState);
	}

	@Override
	public Type<MenuStateUpdateMessage> type() {
		return TYPE;
	}

	public static void handleMenuState(final MenuStateUpdateMessage message, final IPayloadContext context) {
		if (message.name.length() > 256 || message.elementState instanceof String string && string.length() > 8192)
			return;
		context.enqueueWork(() -> {
			if (context.player().containerMenu instanceof FseticketModMenus.MenuAccessor menu) {
				menu.getMenuState().put(message.elementType + ":" + message.name, message.elementState);
				if (context.flow() == PacketFlow.CLIENTBOUND) {
					ClientMenuUpdater.updateClient(message);
				}
			}
		}).exceptionally(e -> {
			context.connection().disconnect(Component.literal(e.getMessage()));
			return null;
		});
	}

	private static class ClientMenuUpdater {
		public static void updateClient(MenuStateUpdateMessage message) {
			if (Minecraft.getInstance().screen instanceof FseticketModMenus.ScreenAccessor accessor) {
				accessor.updateMenuState(message.elementType, message.name, message.elementState);
			}
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		FseticketMod.addNetworkMessage(MenuStateUpdateMessage.TYPE, MenuStateUpdateMessage.STREAM_CODEC, MenuStateUpdateMessage::handleMenuState);
	}
}