package net.fsefmgftc.fseticket.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.fsefmgftc.fseticket.world.inventory.LocalTicketGUIMenu;
import io.netty.buffer.Unpooled;
import java.util.List;

public class LocalTicketItem extends Item {
	public LocalTicketItem(){super(new Item.Properties().stacksTo(1).fireResistant().component(DataComponents.CUSTOM_DATA,def()));}
	private static CustomData def(){CompoundTag t=new CompoundTag();t.putString("start_name_en","");t.putString("terminal_name_en","");t.putString("start_station","");t.putString("terminal_station","");t.putString("fromNameCnU","");t.putString("toNameCnU","");t.putString("type","local");t.putInt("rides",1);t.putBoolean("entered",false);t.putBoolean("exited",false);t.putString("ticketId","");t.putLong("timestamp",0);t.putDouble("cost",0);t.putString("order_datetime","");return CustomData.of(t);}
	@Override public int getEnchantmentValue(){return 1;}

	@Override public void appendHoverText(ItemStack s,TooltipContext ctx,List<Component> list,TooltipFlag f){
		super.appendHoverText(s,ctx,list,f);
		CompoundTag t=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
		String from=t.getString("start_name_en");from=from.isEmpty()||from.equals("\"\"")?"---":from;
		String to=t.getString("terminal_name_en");to=to.isEmpty()||to.equals("\"\"")?"---":to;
		int rides=t.getInt("rides");boolean en=t.getBoolean("entered");boolean ex=t.getBoolean("exited");
		list.add(Component.literal("§7区间：§f"+from+"§7→§f"+to));
		list.add(Component.literal("§7票价：§6¤"+String.format("%.2f",t.getDouble("cost"))));
		list.add(Component.literal("§7乘次：§f"+rides));
		list.add(Component.literal("§7票号：§f"+t.getString("ticketId")));
		list.add(Component.literal(en?"§a已进站":ex?"§e已出站":"§7未进站"));
	}

	@Override public InteractionResultHolder<ItemStack> use(Level w,Player e,InteractionHand h){
		if(e instanceof ServerPlayer sp){ItemStack s=e.getItemInHand(h);CompoundTag t=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
		sp.openMenu(new MenuProvider(){@Override public Component getDisplayName(){return Component.literal("Ticket");}
		@Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){FriendlyByteBuf b=new FriendlyByteBuf(Unpooled.buffer());b.writeBlockPos(e.blockPosition());b.writeByte(h==InteractionHand.MAIN_HAND?0:1);b.writeUtf(t.getString("start_name_en"));b.writeUtf(t.getString("terminal_name_en"));b.writeUtf(t.getString("start_station"));b.writeUtf(t.getString("terminal_station"));b.writeUtf(t.getString("fromNameCnU"));b.writeUtf(t.getString("toNameCnU"));b.writeInt(t.getInt("rides"));b.writeUtf(t.getString("ticketId"));b.writeDouble(t.getDouble("cost"));b.writeUtf(t.getString("order_datetime"));return new LocalTicketGUIMenu(id,inv,b);}},buf->{buf.writeBlockPos(e.blockPosition());buf.writeByte(h==InteractionHand.MAIN_HAND?0:1);buf.writeUtf(t.getString("start_name_en"));buf.writeUtf(t.getString("terminal_name_en"));buf.writeUtf(t.getString("start_station"));buf.writeUtf(t.getString("terminal_station"));buf.writeUtf(t.getString("fromNameCnU"));buf.writeUtf(t.getString("toNameCnU"));buf.writeInt(t.getInt("rides"));buf.writeUtf(t.getString("ticketId"));buf.writeDouble(t.getDouble("cost"));buf.writeUtf(t.getString("order_datetime"));});}
		return super.use(w,e,h);
	}
}
