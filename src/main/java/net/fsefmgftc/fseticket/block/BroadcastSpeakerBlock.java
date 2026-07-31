package net.fsefmgftc.fseticket.block;

import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.fsefmgftc.fseticket.block.entity.BroadcastSpeakerBlockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

public class BroadcastSpeakerBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BroadcastSpeakerBlock() {
        super(BlockBehaviour.Properties.of().strength(1f, 10f));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BroadcastSpeakerBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BroadcastSpeakerBlockEntity speakerEntity) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (tag != null) {
                List<UUID> uuids = new ArrayList<>();
                List<String> names = new ArrayList<>();
                
                if (tag.contains("BoundHosts", Tag.TAG_LIST)) {
                    ListTag uuidsList = tag.getList("BoundHosts", Tag.TAG_STRING);
                    for (int i = 0; i < uuidsList.size(); i++) {
                        try {
                            uuids.add(UUID.fromString(uuidsList.getString(i)));
                        } catch (Exception e) {}
                    }
                }
                
                if (tag.contains("BoundHostNames", Tag.TAG_LIST)) {
                    ListTag namesList = tag.getList("BoundHostNames", Tag.TAG_STRING);
                    for (int i = 0; i < namesList.size(); i++) {
                        names.add(namesList.getString(i));
                    }
                }
                
                speakerEntity.setBoundHosts(uuids, names);
            }
        }
    }
}