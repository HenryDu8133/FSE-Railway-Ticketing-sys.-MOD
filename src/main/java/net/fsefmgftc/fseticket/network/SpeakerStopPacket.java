package net.fsefmgftc.fseticket.network;

import net.fsefmgftc.fseticket.FseticketMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.UUID;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = FseticketMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class SpeakerStopPacket implements CustomPacketPayload {

    public final UUID source;

    public static final Type<SpeakerStopPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FseticketMod.MODID, "speaker_stop"));

    public SpeakerStopPacket(UUID source) {
        this.source = source;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SpeakerStopPacket> STREAM_CODEC = StreamCodec.of(
        (RegistryFriendlyByteBuf buf, SpeakerStopPacket pkt) -> {
            buf.writeUUID(pkt.source);
        },
        (RegistryFriendlyByteBuf buf) -> {
            return new SpeakerStopPacket(buf.readUUID());
        }
    );

    public static void handle(SpeakerStopPacket pkt, IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> {
                net.fsefmgftc.fseticket.client.audio.SpeakerClientHandler.stop(pkt.source);
            });
        }
    }

    @SubscribeEvent
    public static void registerMessage(RegisterPayloadHandlersEvent event) {
        event.registrar(FseticketMod.MODID).playBidirectional(TYPE, STREAM_CODEC, SpeakerStopPacket::handle);
    }
}
