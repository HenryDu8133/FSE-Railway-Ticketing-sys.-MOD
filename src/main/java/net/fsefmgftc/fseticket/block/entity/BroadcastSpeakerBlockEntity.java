package net.fsefmgftc.fseticket.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.fsefmgftc.fseticket.init.FseticketModBlockEntities;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class BroadcastSpeakerBlockEntity extends BlockEntity {
    public static final Set<BroadcastSpeakerBlockEntity> ALL_SPEAKERS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final List<UUID> boundHosts = new ArrayList<>();
    private final List<String> boundHostNames = new ArrayList<>();

    public BroadcastSpeakerBlockEntity(BlockPos pos, BlockState state) {
        super(FseticketModBlockEntities.BROADCAST_SPEAKER.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide()) {
            ALL_SPEAKERS.add(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level != null && !this.level.isClientSide()) {
            ALL_SPEAKERS.remove(this);
        }
    }

    public List<UUID> getBoundHosts() {
        return boundHosts;
    }

    public List<String> getBoundHostNames() {
        return boundHostNames;
    }

    public void setBoundHosts(List<UUID> uuids, List<String> names) {
        this.boundHosts.clear();
        this.boundHosts.addAll(uuids);
        this.boundHostNames.clear();
        this.boundHostNames.addAll(names);
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
        
        ListTag uuidsList = new ListTag();
        for (UUID uuid : this.boundHosts) {
            uuidsList.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put("BoundHosts", uuidsList);

        ListTag namesList = new ListTag();
        for (String name : this.boundHostNames) {
            namesList.add(StringTag.valueOf(name));
        }
        tag.put("BoundHostNames", namesList);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        
        this.boundHosts.clear();
        if (tag.contains("BoundHosts", Tag.TAG_LIST)) {
            ListTag uuidsList = tag.getList("BoundHosts", Tag.TAG_STRING);
            for (int i = 0; i < uuidsList.size(); i++) {
                try {
                    this.boundHosts.add(UUID.fromString(uuidsList.getString(i)));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        this.boundHostNames.clear();
        if (tag.contains("BoundHostNames", Tag.TAG_LIST)) {
            ListTag namesList = tag.getList("BoundHostNames", Tag.TAG_STRING);
            for (int i = 0; i < namesList.size(); i++) {
                this.boundHostNames.add(namesList.getString(i));
            }
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
