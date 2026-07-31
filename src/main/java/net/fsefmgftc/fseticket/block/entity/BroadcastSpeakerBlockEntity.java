package net.fsefmgftc.fseticket.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.fsefmgftc.fseticket.init.FseticketModBlockEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BroadcastSpeakerBlockEntity extends BlockEntity {
    private List<UUID> boundHosts = new ArrayList<>();
    private List<String> boundHostNames = new ArrayList<>();

    public BroadcastSpeakerBlockEntity(BlockPos pos, BlockState state) {
        super(FseticketModBlockEntities.BROADCAST_SPEAKER.get(), pos, state);
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
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
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        
        this.boundHosts.clear();
        if (tag.contains("BoundHosts", Tag.TAG_LIST)) {
            ListTag uuidsList = tag.getList("BoundHosts", Tag.TAG_STRING);
            for (int i = 0; i < uuidsList.size(); i++) {
                try {
                    this.boundHosts.add(UUID.fromString(uuidsList.getString(i)));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid UUIDs
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
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        this.saveAdditional(tag, provider);
        return tag;
    }
}
