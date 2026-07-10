package net.fsefmgftc.fseticket.block.entity;

import dan200.computercraft.api.peripheral.*;
import dan200.computercraft.api.lua.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.item.ItemEntity;
import net.fsefmgftc.fseticket.init.FseticketModItems;
import net.fsefmgftc.fseticket.init.FseticketModBlockEntities;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TicketVendingMachineBlockEntity extends BlockEntity {
	private final VendingPeripheral peripheral = new VendingPeripheral();
	public TicketVendingMachineBlockEntity(BlockPos pos, BlockState state) { super(FseticketModBlockEntities.TICKET_VENDING_MACHINE.get(), pos, state); }
	public IPeripheral getPeripheral() { return peripheral; }

	private class VendingPeripheral implements IPeripheral, IDynamicPeripheral {
		private final Set<IComputerAccess> computers = new HashSet<>();
		@Override public String getType() { return "ticket_vending_machine"; }
		@Override public void attach(IComputerAccess c) { computers.add(c); }
		@Override public void detach(IComputerAccess c) { computers.remove(c); }
		@Override public boolean equals(IPeripheral o) { return this == o; }
		@Override public String[] getMethodNames() { return new String[]{"issueTicket", "issueICCard", "issueFSEPass"}; }

		@Override public MethodResult callMethod(IComputerAccess comp, ILuaContext ctx, int m, IArguments args) throws LuaException {
			if (m == 0) { // issueTicket
				String sn=args.count()>0?args.getString(0):"???"; String tn=args.count()>1?args.getString(1):"???";
				String type=args.count()>2?args.getString(2):"local"; int rides=args.count()>3?Math.max(1,args.getInt(3)):1;
				double cost=args.count()>4?args.getDouble(4):0; String ss=args.count()>5?args.getString(5):"";
				String ts=args.count()>6?args.getString(6):""; String fc=args.count()>7?args.getString(7):"";
				String tc=args.count()>8?args.getString(8):"";
				CompoundTag d=new CompoundTag();
				d.putString("start_name_en",sn!=null?sn:"???"); d.putString("terminal_name_en",tn!=null?tn:"???");
				d.putString("start_station",ss!=null?ss:""); d.putString("terminal_station",ts!=null?ts:"");
				d.putString("fromNameCnU",fc!=null?fc:""); d.putString("toNameCnU",tc!=null?tc:"");
				d.putString("type",type); d.putInt("rides",rides); d.putBoolean("entered",false); d.putBoolean("exited",false);
				String tid=String.format("%c%c-%08d",(char)(Math.random()*26+65),(char)(Math.random()*26+65),(int)(Math.random()*100000000));
				d.putString("ticketId",tid); d.putLong("timestamp",System.currentTimeMillis()); d.putDouble("cost",cost);
				d.putString("order_datetime",LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
				Item ti="limited_express".equals(type)?FseticketModItems.EXP_TICKET.get():"single".equals(type)?FseticketModItems.SINGLETRIP_TICKET.get():FseticketModItems.LOCAL_TICKET.get();
				ItemStack t=new ItemStack(ti); t.set(DataComponents.CUSTOM_DATA,CustomData.of(d));
				spawnItem(t); return MethodResult.of(true,tid);
			}
			if (m == 1) { // issueICCard
				CompoundTag d=new CompoundTag();
				d.putString("cardId","IC-"+UUID.randomUUID().toString().substring(0,8));
				String on=args.count()>0?args.getString(0):""; d.putString("ownerName",on!=null?on:""); double bal=args.count()>1?args.getDouble(1):0; d.putDouble("balance",bal);
				ItemStack card=new ItemStack(FseticketModItems.IC_CARD.get());
				card.set(DataComponents.CUSTOM_DATA,CustomData.of(d));
				spawnItem(card); return MethodResult.of(true,d.getString("cardId"));
			}
			return MethodResult.of();
		}

		private void spawnItem(ItemStack item) {
			BlockState state=getBlockState();
			Direction f=state.hasProperty(HorizontalDirectionalBlock.FACING)?state.getValue(HorizontalDirectionalBlock.FACING):Direction.NORTH;
			level.addFreshEntity(new ItemEntity(level,worldPosition.getX()+0.5+f.getStepX()*0.7,worldPosition.getY()+0.8,worldPosition.getZ()+0.5+f.getStepZ()*0.7,item));
		}
	}
}
