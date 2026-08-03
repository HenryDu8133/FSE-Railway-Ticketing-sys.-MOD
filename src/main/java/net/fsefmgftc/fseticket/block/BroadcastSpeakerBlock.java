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
import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

public class BroadcastSpeakerBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BroadcastSpeakerBlock() {
        super(BlockBehaviour.Properties.of().strength(1f, 10f));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BroadcastSpeakerBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BroadcastSpeakerBlockEntity speakerEntity) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            List<UUID> uuids = new ArrayList<>();
            List<String> names = new ArrayList<>();

            if (tag.contains("BoundHosts", Tag.TAG_LIST)) {
                ListTag uuidsList = tag.getList("BoundHosts", Tag.TAG_STRING);
                for (int i = 0; i < uuidsList.size(); i++) {
                    try {
                        uuids.add(UUID.fromString(uuidsList.getString(i)));
                    } catch (Exception ignored) {}
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

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BroadcastSpeakerBlockEntity speakerEntity) {
                List<String> namesList = speakerEntity.getBoundHostNames();
//                player.sendSystemMessage(Component.literal("当前扬声器绑定的广播主机："));
                player.sendSystemMessage(Component.translatable("message.fseticket.broadcast_speaker.currently_bound"));
                if (namesList != null && !namesList.isEmpty()) {
                    for (String name : namesList) {
                        player.sendSystemMessage(Component.literal("- " + name));
                    }
                } else {
//                    player.sendSystemMessage(Component.literal("暂无绑定"));
                    player.sendSystemMessage(Component.translatable("message.fseticket.broadcast_speaker.not_bound"));
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}