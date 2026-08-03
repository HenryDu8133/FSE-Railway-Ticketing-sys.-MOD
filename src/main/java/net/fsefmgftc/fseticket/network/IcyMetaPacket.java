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
public class IcyMetaPacket implements CustomPacketPayload {

    public static final int MAX_FIELD_LEN = 256;

    public final UUID   source;      
    public final String rawTitle;    
    public final String stationName; 
    public final String genre;       
    public final String description; 

    public static final Type<IcyMetaPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FseticketMod.MODID, "icy_meta"));

    public IcyMetaPacket(UUID source, String rawTitle, String stationName, String genre, String description) {
        this.source      = source;
        this.rawTitle    = cap(rawTitle);
        this.stationName = cap(stationName);
        this.genre       = cap(genre);
        this.description = cap(description);
    }

    private static String cap(String s) {
        if (s == null) return "";
        return s.length() > MAX_FIELD_LEN ? s.substring(0, MAX_FIELD_LEN) : s;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, IcyMetaPacket> STREAM_CODEC = StreamCodec.of(
        (RegistryFriendlyByteBuf buf, IcyMetaPacket pkt) -> {
            buf.writeUUID(pkt.source);
            buf.writeUtf(pkt.rawTitle,    MAX_FIELD_LEN);
            buf.writeUtf(pkt.stationName, MAX_FIELD_LEN);
            buf.writeUtf(pkt.genre,       MAX_FIELD_LEN);
            buf.writeUtf(pkt.description, MAX_FIELD_LEN);
        },
        (RegistryFriendlyByteBuf buf) -> {
            UUID   source      = buf.readUUID();
            String rawTitle    = buf.readUtf(MAX_FIELD_LEN);
            String stationName = buf.readUtf(MAX_FIELD_LEN);
            String genre       = buf.readUtf(MAX_FIELD_LEN);
            String description = buf.readUtf(MAX_FIELD_LEN);
            return new IcyMetaPacket(source, rawTitle, stationName, genre, description);
        }
    );

    public static void handle(IcyMetaPacket pkt, IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.SERVERBOUND) {
            ctx.enqueueWork(() -> {
                net.fsefmgftc.fseticket.cc.BroadcastHostPeripheral.onIcyMetaReceived(pkt);
            });
        }
    }

    @SubscribeEvent
    public static void registerMessage(RegisterPayloadHandlersEvent event) {
        event.registrar(FseticketMod.MODID).playBidirectional(TYPE, STREAM_CODEC, IcyMetaPacket::handle);
    }
}
