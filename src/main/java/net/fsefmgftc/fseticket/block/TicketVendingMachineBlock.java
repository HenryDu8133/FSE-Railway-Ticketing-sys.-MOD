package net.fsefmgftc.fseticket.block;

import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.fsefmgftc.fseticket.block.entity.TicketVendingMachineBlockEntity;

public class TicketVendingMachineBlock extends Block implements net.minecraft.world.level.block.EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public TicketVendingMachineBlock(){super(BlockBehaviour.Properties.of().strength(1f,10f));registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
	@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(FACING);}
	@Override public BlockState getStateForPlacement(BlockPlaceContext c){return super.getStateForPlacement(c).setValue(FACING,c.getHorizontalDirection().getOpposite());}
	public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
	public BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
	@Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new TicketVendingMachineBlockEntity(p,s);}
}
