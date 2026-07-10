package net.fsefmgftc.fseticket.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;

import net.fsefmgftc.fseticket.FseticketMod;

@EventBusSubscriber
public record OpenUrlMessage(String url) implements CustomPacketPayload {
	public static final Type<OpenUrlMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FseticketMod.MODID, "open_url"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenUrlMessage> STREAM_CODEC = StreamCodec.of(OpenUrlMessage::write, OpenUrlMessage::read);

	public static void write(FriendlyByteBuf buffer, OpenUrlMessage message) {
		buffer.writeUtf(message.url, 2048);
	}

	public static OpenUrlMessage read(FriendlyByteBuf buffer) {
		return new OpenUrlMessage(buffer.readUtf(2048));
	}

	@Override
	public Type<OpenUrlMessage> type() {
		return TYPE;
	}

	public static void handleOpenUrl(final OpenUrlMessage message, final IPayloadContext context) {
		if (context.flow() != PacketFlow.CLIENTBOUND || message.url == null || message.url.length() > 2048) {
			return;
		}
		context.enqueueWork(() -> Util.getPlatform().openUri(message.url)).exceptionally(e -> {
			context.connection().disconnect(Component.literal(e.getMessage()));
			return null;
		});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		FseticketMod.addNetworkMessage(OpenUrlMessage.TYPE, OpenUrlMessage.STREAM_CODEC, OpenUrlMessage::handleOpenUrl);
	}
}

