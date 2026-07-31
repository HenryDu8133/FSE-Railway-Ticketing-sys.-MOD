package net.fsefmgftc.fseticket.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.fsefmgftc.fseticket.block.entity.BroadcastHostBlockEntity;
import net.fsefmgftc.fseticket.FseticketMod;

@EventBusSubscriber
public record BroadcastHostRenameMessage(BlockPos pos, String newName) implements CustomPacketPayload {
    public static final Type<BroadcastHostRenameMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FseticketMod.MODID, "broadcast_host_rename"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BroadcastHostRenameMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, BroadcastHostRenameMessage message) -> {
        buffer.writeBlockPos(message.pos);
        buffer.writeUtf(message.newName);
    }, (RegistryFriendlyByteBuf buffer) -> new BroadcastHostRenameMessage(buffer.readBlockPos(), buffer.readUtf()));

    @Override
    public Type<BroadcastHostRenameMessage> type() {
        return TYPE;
    }

    public static void handleData(final BroadcastHostRenameMessage message, final IPayloadContext context) {
        if (context.flow() == PacketFlow.SERVERBOUND) {
            context.enqueueWork(() -> handleAction(context.player(), message.pos, message.newName)).exceptionally(e -> {
                context.connection().disconnect(Component.literal(e.getMessage()));
                return null;
            });
        }
    }

    public static void handleAction(Player entity, BlockPos pos, String newName) {
        Level world = entity.level();
        if (!world.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())))
            return;
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof BroadcastHostBlockEntity host) {
            host.setHostName(newName);
        }
    }

    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        FseticketMod.addNetworkMessage(BroadcastHostRenameMessage.TYPE, BroadcastHostRenameMessage.STREAM_CODEC, BroadcastHostRenameMessage::handleData);
    }
}

