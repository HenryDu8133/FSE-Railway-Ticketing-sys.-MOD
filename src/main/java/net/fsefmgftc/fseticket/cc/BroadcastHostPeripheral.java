package net.fsefmgftc.fseticket.cc;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.fsefmgftc.fseticket.FseticketMod;
import net.fsefmgftc.fseticket.block.entity.BroadcastHostBlockEntity;
import net.fsefmgftc.fseticket.block.entity.BroadcastSpeakerBlockEntity;
import net.fsefmgftc.fseticket.network.IcyMetaPacket;
import net.fsefmgftc.fseticket.network.SpeakerAudioPacket;
import net.fsefmgftc.fseticket.network.SpeakerStopPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BroadcastHostPeripheral implements IPeripheral {

    private static final ConcurrentHashMap<UUID, BroadcastHostPeripheral> HOST_REGISTRY = new ConcurrentHashMap<>();

    private final BroadcastHostBlockEntity host;
    private float volume = 1.0f;

    private String icyTitle = "";
    private final String icyArtist = "";
    private final String icySong = "";
    private String icyStationName = "";
    private String icyGenre = "";
    private String icyDescription = "";

    public BroadcastHostPeripheral(BroadcastHostBlockEntity host) {
        this.host = host;
        HOST_REGISTRY.put(host.getHostId(), this);
    }

    public static void onIcyMetaReceived(IcyMetaPacket pkt) {
        BroadcastHostPeripheral p = HOST_REGISTRY.get(pkt.source);
        if (p != null) {
            p.icyTitle = pkt.rawTitle;
            p.icyStationName = pkt.stationName;
            p.icyGenre = pkt.genre;
            p.icyDescription = pkt.description;
        }
    }

    @Nonnull
    @Override
    public String getType() {
        return "broadcast_host";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return this == other;
    }

    private List<BroadcastSpeakerBlockEntity> getBoundSpeakers() {
        List<BroadcastSpeakerBlockEntity> speakers = new ArrayList<>();
        if (!(host.getLevel() instanceof ServerLevel serverLevel)) return speakers;

        UUID myId = host.getHostId();

        for (BroadcastSpeakerBlockEntity speaker : BroadcastSpeakerBlockEntity.ALL_SPEAKERS) {
            FseticketMod.LOGGER.info("Checking speaker at {}, bound hosts: {} against myId: {}", speaker.getBlockPos(), speaker.getBoundHosts(), myId);
            if (speaker.getLevel() == serverLevel && speaker.getBoundHosts().contains(myId)) {
                speakers.add(speaker);
            }
        }
        return speakers;
    }

    private void sendToPlayersNear(SpeakerAudioPacket pkt, BlockPos pos, ServerLevel level) {
        FseticketMod.LOGGER.info("Sending SpeakerAudioPacket format: {} data size: {} to nearby players", pkt.format, pkt.data.length);
        for (ServerPlayer player : level.players()) {
            double dx = player.getX() - pos.getX();
            double dy = player.getY() - pos.getY();
            double dz = player.getZ() - pos.getZ();
            if (dx * dx + dy * dy + dz * dz <= 64 * 64) {
                PacketDistributor.sendToPlayer(player, pkt);
            }
        }
    }

    private void sendStopToPlayersNear(SpeakerStopPacket pkt, BlockPos pos, ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            double dx = player.getX() - pos.getX();
            double dy = player.getY() - pos.getY();
            double dz = player.getZ() - pos.getZ();
            if (dx * dx + dy * dy + dz * dz <= 32 * 32) {
                PacketDistributor.sendToPlayer(player, pkt);
            }
        }
    }

    @LuaFunction
    public final void setVolume(double v) {
        this.volume = (float) Math.clamp(v, 0.0, 3.0);
    }

    @LuaFunction
    public final double getVolume() {
        return this.volume;
    }

    @LuaFunction
    public final void stop() {
        if (!(host.getLevel() instanceof ServerLevel serverLevel)) return;
        List<BroadcastSpeakerBlockEntity> speakers = getBoundSpeakers();
        for (BroadcastSpeakerBlockEntity speaker : speakers) {
            UUID source = host.getHostId();
            SpeakerStopPacket pkt = new SpeakerStopPacket(source);
            sendStopToPlayersNear(pkt, speaker.getBlockPos(), serverLevel);
        }
    }

    @LuaFunction
    public final boolean playStream(String url) {
        if (!(host.getLevel() instanceof ServerLevel serverLevel)) return false;
        List<BroadcastSpeakerBlockEntity> speakers = getBoundSpeakers();
        if (speakers.isEmpty()) return false;

        long startTick = serverLevel.getServer().getTickCount() + 5;
        UUID syncGroupId = UUID.randomUUID();

        for (BroadcastSpeakerBlockEntity speaker : speakers) {
            BlockPos p = speaker.getBlockPos();
            SpeakerAudioPacket pkt = new SpeakerAudioPacket(
                    host.getHostId(),
                    SpeakerAudioPacket.AudioFormat.MP3_STREAM,
                    this.volume,
                    p.getX() + 0.5f, p.getY() + 0.5f, p.getZ() + 0.5f,
                    p.getX(), p.getY(), p.getZ(),
                    new byte[0],
                    url,
                    startTick,
                    syncGroupId,
                    speakers.size()
            );
            sendToPlayersNear(pkt, p, serverLevel);
        }
        return true;
    }

    @LuaFunction
    public final boolean playUrl(String url) {
        return playStream(url);
    }

    @LuaFunction
    public final boolean playLocal(IArguments args) throws LuaException {
        if (!(host.getLevel() instanceof ServerLevel serverLevel)) return false;

        java.nio.ByteBuffer dataBuf = args.getBytes(0);
        byte[] data = new byte[dataBuf.remaining()];
        dataBuf.duplicate().get(data);

        if (data.length == 0) throw new LuaException("Audio data is empty");
        if (data.length > 8 * 1024 * 1024) throw new LuaException("Audio file too large (max 8MB)");

        List<BroadcastSpeakerBlockEntity> speakers = getBoundSpeakers();
        FseticketMod.LOGGER.info("playLocal invoked. Bound speakers found: {}", speakers.size());
        if (speakers.isEmpty()) return false;

        long startTick = serverLevel.getServer().getTickCount() + 5;
        UUID syncGroupId = UUID.randomUUID();

        for (BroadcastSpeakerBlockEntity speaker : speakers) {
            BlockPos p = speaker.getBlockPos();
            SpeakerAudioPacket pkt = new SpeakerAudioPacket(
                    host.getHostId(),
                    SpeakerAudioPacket.AudioFormat.AUDIO_FILE,
                    this.volume,
                    p.getX() + 0.5f, p.getY() + 0.5f, p.getZ() + 0.5f,
                    p.getX(), p.getY(), p.getZ(),
                    data,
                    "",
                    startTick,
                    syncGroupId,
                    speakers.size()
            );
            sendToPlayersNear(pkt, p, serverLevel);
        }
        return true;
    }

    @LuaFunction
    public final Map<String, Object> getStreamMeta() {
        return Map.of(
                "title", icyTitle,
                "station", icyStationName,
                "genre", icyGenre,
                "description", icyDescription
        );
    }
}
