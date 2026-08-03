package net.fsefmgftc.fseticket.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.fsefmgftc.fseticket.init.FseticketModBlockEntities;
import net.fsefmgftc.fseticket.cc.BroadcastHostPeripheral;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BroadcastHostBlockEntity extends BlockEntity {
    private UUID hostId = UUID.randomUUID();
    private String hostName = "未命名广播主机"; // Wtf is this
    private BroadcastHostPeripheral peripheral;

    public BroadcastHostBlockEntity(BlockPos pos, BlockState state) {
        super(FseticketModBlockEntities.BROADCAST_HOST.get(), pos, state);
    }

    public BroadcastHostPeripheral getPeripheral() {
        if (this.peripheral == null) {
            this.peripheral = new BroadcastHostPeripheral(this);
        }
        return this.peripheral;
    }

    public UUID getHostId() {
        return hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putUUID("HostId", this.hostId);
        tag.putString("HostName", this.hostName);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.hasUUID("HostId")) {
            this.hostId = tag.getUUID("HostId");
        }
        if (tag.contains("HostName")) {
            this.hostName = tag.getString("HostName");
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        this.saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
