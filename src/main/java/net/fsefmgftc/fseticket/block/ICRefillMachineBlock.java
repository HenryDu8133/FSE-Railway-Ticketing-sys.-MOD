package net.fsefmgftc.fseticket.block;

import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.fsefmgftc.fseticket.block.entity.ICRefillMachineBlockEntity;

public class ICRefillMachineBlock extends Block implements net.minecraft.world.level.block.EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty HAS_CARD = BooleanProperty.create("has_card");

	public ICRefillMachineBlock(){super(BlockBehaviour.Properties.of().strength(1f,10f));registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(HAS_CARD,false));}
	@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(FACING,HAS_CARD);}
	@Override public BlockState getStateForPlacement(BlockPlaceContext c){return super.getStateForPlacement(c).setValue(FACING,c.getHorizontalDirection().getOpposite()).setValue(HAS_CARD,false);}
	public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
	public BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
	@Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ICRefillMachineBlockEntity(p,s);}
	@Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
		BlockEntity be=level.getBlockEntity(pos);
		if(be instanceof ICRefillMachineBlockEntity rm)return rm.onUse(state,level,pos,player,hand,hit);
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}
}
