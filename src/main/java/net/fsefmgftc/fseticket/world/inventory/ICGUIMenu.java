package net.fsefmgftc.fseticket.world.inventory;

import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.fsefmgftc.fseticket.init.FseticketModMenus;
import java.util.*;

public class ICGUIMenu extends AbstractContainerMenu implements FseticketModMenus.MenuAccessor {
	public final Map<String,Object> menuState=new HashMap<>();
	public final Level world;public final Player entity;public int x,y,z;
	public String cardId="",ownerName="";public double balance=0;

	public ICGUIMenu(int id,Inventory inv,FriendlyByteBuf buf){
		super(FseticketModMenus.ICGUI.get(),id);
		entity=inv.player;world=inv.player.level();
		if(buf!=null){BlockPos p=buf.readBlockPos();x=p.getX();y=p.getY();z=p.getZ();buf.readByte();
		cardId=buf.readUtf();ownerName=buf.readUtf();balance=buf.readDouble();}
	}
	@Override public boolean stillValid(Player p){return true;}
	@Override public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}
	@Override public Map<Integer,Slot> getSlots(){return Collections.emptyMap();}
	@Override public Map<String,Object> getMenuState(){return menuState;}
}
