package net.fsefmgftc.fseticket.world.inventory;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.IItemHandler;
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
public class ExpTicketGUIMenu extends AbstractContainerMenu implements FseticketModMenus.MenuAccessor {
	public final Map<String,Object> menuState=new HashMap<>();
	public final Level world;public final Player entity;public int x,y,z;
	public String startNameEn="",terminalNameEn="",startStation="",terminalStation="",fromNameCnU="",toNameCnU="",ticketId="",orderDatetime="";
	public int rides=0;public double cost=0;
	private final IItemHandler internal=new ItemStackHandler(0);
	private final Map<Integer,Slot> customSlots=new HashMap<>();
	public ExpTicketGUIMenu(int id,Inventory inv,FriendlyByteBuf buf){
		super(FseticketModMenus.EXP_TICKET_GUI.get(),id);
		entity=inv.player;world=inv.player.level();
		if(buf!=null){BlockPos p=buf.readBlockPos();x=p.getX();y=p.getY();z=p.getZ();buf.readByte();
		startNameEn=buf.readUtf();terminalNameEn=buf.readUtf();startStation=buf.readUtf();terminalStation=buf.readUtf();
		fromNameCnU=buf.readUtf();toNameCnU=buf.readUtf();rides=buf.readInt();ticketId=buf.readUtf();
		cost=buf.readDouble();orderDatetime=buf.readUtf();}
	}
	@Override public boolean stillValid(Player p){return true;}
	@Override public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}
	@Override public Map<Integer,Slot> getSlots(){return Collections.unmodifiableMap(customSlots);}
	@Override public Map<String,Object> getMenuState(){return menuState;}
}