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
public class SpeakerAudioPacket implements CustomPacketPayload {

    public enum AudioFormat {
        PCM_S16LE,
        OGG_VORBIS,
        MP3,
        AUDIO_FILE,     // client decodes this directly
        MP3_STREAM,     // streamed audio via Icecast/Shoutcast
        HLS_STREAM,     // streamed audio via HLS
        TS_STREAM       // streamed audio via MPEG-TS
    }

    public static final int MAX_BYTES = 8 * 1024 * 1024;
    public static final int MAX_URL_CHARS = 512;
    public static final int MAX_SYNC_GROUP = 64;

    public final UUID        source;
    public final AudioFormat format;
    public final float       volume;
    public final float       x, y, z;           
    public final int         blockX, blockY, blockZ; 
    public final byte[]      data;
    public final String      streamUrl;         
    public final long        startTick;         
    public final UUID        syncGroupId;       
    public final int         syncGroupSize;     

    public static final Type<SpeakerAudioPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FseticketMod.MODID, "speaker_audio"));

    public SpeakerAudioPacket(UUID source, AudioFormat format, float volume, float x, float y, float z, int blockX, int blockY, int blockZ, byte[] data, String streamUrl, long startTick, UUID syncGroupId, int syncGroupSize) {
        this.source = source;
        this.format = format != null ? format : AudioFormat.AUDIO_FILE;
        this.volume = Float.isFinite(volume) ? Math.max(0.0f, Math.min(3.0f, volume)) : 1.0f;
        this.x = Float.isFinite(x) ? x : 0.0f; this.y = Float.isFinite(y) ? y : 0.0f; this.z = Float.isFinite(z) ? z : 0.0f;
        this.blockX = blockX; this.blockY = blockY; this.blockZ = blockZ;
        if (data == null) data = new byte[0];
        this.data = data.length <= MAX_BYTES ? data : new byte[0];
        this.streamUrl = streamUrl != null && streamUrl.length() <= MAX_URL_CHARS ? streamUrl : "";
        this.startTick = Math.max(0L, startTick);
        this.syncGroupId = syncGroupId;
        this.syncGroupSize = syncGroupId == null ? 0 : Math.max(1, Math.min(MAX_SYNC_GROUP, syncGroupSize));
    }

    public boolean isStreamingFormat() {
        return format == AudioFormat.MP3_STREAM ||
               format == AudioFormat.HLS_STREAM ||
               format == AudioFormat.TS_STREAM;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SpeakerAudioPacket> STREAM_CODEC = StreamCodec.of(
        (RegistryFriendlyByteBuf buf, SpeakerAudioPacket pkt) -> {
            buf.writeUUID(pkt.source);
            buf.writeEnum(pkt.format);
            buf.writeFloat(pkt.volume);
            buf.writeFloat(pkt.x);
            buf.writeFloat(pkt.y);
            buf.writeFloat(pkt.z);
            buf.writeInt(pkt.blockX);
            buf.writeInt(pkt.blockY);
            buf.writeInt(pkt.blockZ);
            buf.writeVarLong(pkt.startTick);
            buf.writeBoolean(pkt.syncGroupId != null);
            if (pkt.syncGroupId != null) {
                buf.writeUUID(pkt.syncGroupId);
                buf.writeVarInt(pkt.syncGroupSize);
            }
            if (pkt.isStreamingFormat()) {
                buf.writeBoolean(true); 
                String safeUrl = pkt.streamUrl != null && pkt.streamUrl.length() <= MAX_URL_CHARS ? pkt.streamUrl : "";
                buf.writeUtf(safeUrl, MAX_URL_CHARS);
            } else {
                buf.writeBoolean(false); 
                int len = pkt.data == null ? 0 : Math.min(pkt.data.length, MAX_BYTES);
                buf.writeVarInt(len);
                if (len > 0) {
                    buf.writeBytes(pkt.data, 0, len);
                }
            }
        },
        (RegistryFriendlyByteBuf buf) -> {
            UUID        source = buf.readUUID();
            AudioFormat format = buf.readEnum(AudioFormat.class);
            float       volume = buf.readFloat();
            float       x      = buf.readFloat();
            float       y      = buf.readFloat();
            float       z      = buf.readFloat();
            int         blockX = buf.readInt();
            int         blockY = buf.readInt();
            int         blockZ = buf.readInt();
            long        startTick = buf.readVarLong();
            UUID        syncGroupId = buf.readBoolean() ? buf.readUUID() : null;
            int         syncGroupSize = syncGroupId != null ? buf.readVarInt() : 0;
            boolean isStreaming = buf.readBoolean();

            if (isStreaming) {
                String streamUrl = buf.readUtf(MAX_URL_CHARS);
                return new SpeakerAudioPacket(source, format, volume, x, y, z, blockX, blockY, blockZ, new byte[0], streamUrl, startTick, syncGroupId, syncGroupSize);
            } else {
                int len = buf.readVarInt();
                byte[] data = new byte[len];
                if (len > 0) buf.readBytes(data);
                return new SpeakerAudioPacket(source, format, volume, x, y, z, blockX, blockY, blockZ, data, "", startTick, syncGroupId, syncGroupSize);
            }
        }
    );

    public static void handle(SpeakerAudioPacket pkt, IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> {
                net.fsefmgftc.fseticket.client.audio.SpeakerClientHandler.receive(pkt);
            });
        }
    }

    @SubscribeEvent
    public static void registerMessage(RegisterPayloadHandlersEvent event) {
        event.registrar(FseticketMod.MODID).playBidirectional(TYPE, STREAM_CODEC, SpeakerAudioPacket::handle);
    }
}

