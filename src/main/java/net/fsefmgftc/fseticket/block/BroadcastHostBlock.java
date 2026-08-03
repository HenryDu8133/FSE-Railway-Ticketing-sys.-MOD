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
import net.fsefmgftc.fseticket.block.entity.BroadcastHostBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.fsefmgftc.fseticket.init.FseticketModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

public class BroadcastHostBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BroadcastHostBlock() {
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
        return new BroadcastHostBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BroadcastHostBlockEntity hostEntity) {
                ClientHandler.openRenameScreen(pos, hostEntity.getHostName());
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static class ClientHandler {
        public static void openRenameScreen(BlockPos pos, String currentName) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new net.fsefmgftc.fseticket.client.gui.BroadcastHostRenameScreen(pos, currentName));
        }
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() == FseticketModItems.BROADCAST_SPEAKER.get()) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof BroadcastHostBlockEntity hostEntity) {
                    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    ListTag uuidsList = tag.contains("BoundHosts", Tag.TAG_LIST) ? tag.getList("BoundHosts", Tag.TAG_STRING) : new ListTag();
                    ListTag namesList = tag.contains("BoundHostNames", Tag.TAG_LIST) ? tag.getList("BoundHostNames", Tag.TAG_STRING) : new ListTag();
                    
                    String hostIdStr = hostEntity.getHostId().toString();
                    String hostName = hostEntity.getHostName();
                    
                    boolean alreadyBound = false;
                    for (int i = 0; i < uuidsList.size(); i++) {
                        if (uuidsList.getString(i).equals(hostIdStr)) {
                            alreadyBound = true;
                            break;
                        }
                    }
                    
                    if (!alreadyBound) {
                        uuidsList.add(StringTag.valueOf(hostIdStr));
                        namesList.add(StringTag.valueOf(hostName));
                        tag.put("BoundHosts", uuidsList);
                        tag.put("BoundHostNames", namesList);
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                        player.sendSystemMessage(Component.literal("扬声器绑定成功！广播主机：" + hostName));
                    } else {
                        player.sendSystemMessage(Component.literal("该扬声器已绑定过此广播主机：" + hostName));
                    }
                }
            }
            return net.minecraft.world.ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
